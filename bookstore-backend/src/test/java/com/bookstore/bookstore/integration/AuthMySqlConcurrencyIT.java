package com.bookstore.bookstore.integration;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.testcontainers.containers.MySQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

/** MySQL transaction regressions for the lock/conditional-update patterns used by auth services. */
@Testcontainers(disabledWithoutDocker = true)
class AuthMySqlConcurrencyIT {

    @Container
    static final MySQLContainer<?> MYSQL = new MySQLContainer<>("mysql:8.0.36")
            .withDatabaseName("bookstore_auth_concurrency")
            .withUsername("bookstore")
            .withPassword("bookstore");

    @BeforeEach
    void setUp() throws SQLException {
        try (Connection c = connection(); Statement s = c.createStatement()) {
            s.execute("DROP TABLE IF EXISTS auth_tokens_test");
            s.execute("DROP TABLE IF EXISTS auth_reset_test");
            s.execute("DROP TABLE IF EXISTS auth_otp_test");
            s.execute("DROP TABLE IF EXISTS auth_attempt_test");
            s.execute("CREATE TABLE auth_tokens_test (id VARCHAR(20) PRIMARY KEY, family_id VARCHAR(20) NOT NULL, revoked BOOLEAN NOT NULL, replaced_by VARCHAR(20) NULL)");
            s.execute("INSERT INTO auth_tokens_test VALUES ('old', 'family-1', false, NULL), ('other', 'family-1', false, NULL)");
            s.execute("CREATE TABLE auth_reset_test (id INT PRIMARY KEY, used BOOLEAN NOT NULL)");
            s.execute("INSERT INTO auth_reset_test VALUES (1, false)");
            s.execute("CREATE TABLE auth_otp_test (id INT PRIMARY KEY, verified BOOLEAN NOT NULL, attempts INT NOT NULL)");
            s.execute("INSERT INTO auth_otp_test VALUES (1, false, 0)");
            s.execute("CREATE TABLE auth_attempt_test (subject_hash VARCHAR(64) PRIMARY KEY, failures INT NOT NULL)");
        }
    }

    @Test
    void twoRefreshesForOneToken_allowExactlyOneRotation() throws Exception {
        assertEquals(1L, concurrently(2, this::rotateOld).stream().filter(Boolean::booleanValue).count());
        assertEquals(1, intValue("SELECT COUNT(*) FROM auth_tokens_test WHERE id = 'old' AND revoked = true"));
        assertEquals(1, intValue("SELECT COUNT(*) FROM auth_tokens_test WHERE id LIKE 'child-%' AND revoked = false"));
    }

    @Test
    void reuseAndChildRefresh_leaveFamilyRevoked() throws Exception {
        rotateOld();
        concurrently(List.of(this::reuseOld, this::rotateChild));
        assertEquals(0, intValue("SELECT COUNT(*) FROM auth_tokens_test WHERE family_id = 'family-1' AND revoked = false"));
    }

    @Test
    void twoPasswordResetConfirms_allowExactlyOneConsume() throws Exception {
        assertEquals(1L, concurrently(2, this::consumeReset).stream().filter(Boolean::booleanValue).count());
        assertEquals(1, intValue("SELECT used FROM auth_reset_test WHERE id = 1"));
    }

    @Test
    void twoOtpVerifies_allowExactlyOneConsume() throws Exception {
        assertEquals(1L, concurrently(2, this::verifyOtp).stream().filter(Boolean::booleanValue).count());
        assertEquals(1, intValue("SELECT verified FROM auth_otp_test WHERE id = 1"));
    }

    @Test
    void twoLoginFailureUpdates_preserveBothFailures() throws Exception {
        concurrently(2, this::incrementLoginFailure);
        assertEquals(2, intValue("SELECT failures FROM auth_attempt_test WHERE subject_hash = 'same-account'"));
    }

    @Test
    void logoutAllAndRefresh_leaveNoActiveToken() throws Exception {
        concurrently(List.of(this::logoutAll, this::rotateOld));
        assertEquals(0, intValue("SELECT COUNT(*) FROM auth_tokens_test WHERE revoked = false"));
    }

    @Test
    void revokeSessionAndRefresh_leaveFamilyRevoked() throws Exception {
        concurrently(List.of(this::revokeOldFamily, this::rotateOld));
        assertEquals(0, intValue("SELECT COUNT(*) FROM auth_tokens_test WHERE family_id = 'family-1' AND revoked = false"));
    }

    private boolean rotateOld() throws Exception { return rotate("old", "child-a"); }
    private boolean rotateChild() throws Exception { return rotate("child-a", "child-b"); }

    private boolean rotate(String id, String child) throws Exception {
        try (Connection c = connection()) {
            c.setAutoCommit(false);
            try (PreparedStatement lock = c.prepareStatement("SELECT family_id, revoked FROM auth_tokens_test WHERE id = ? FOR UPDATE")) {
                lock.setString(1, id);
                ResultSet r = lock.executeQuery();
                if (!r.next() || r.getBoolean("revoked")) { c.commit(); return false; }
                String family = r.getString("family_id");
                try (PreparedStatement revoke = c.prepareStatement("UPDATE auth_tokens_test SET revoked = true, replaced_by = ? WHERE id = ?");
                     PreparedStatement insert = c.prepareStatement("INSERT IGNORE INTO auth_tokens_test VALUES (?, ?, false, NULL)")) {
                    revoke.setString(1, child); revoke.setString(2, id); revoke.executeUpdate();
                    insert.setString(1, child); insert.setString(2, family); insert.executeUpdate();
                }
                c.commit(); return true;
            }
        }
    }

    private boolean reuseOld() throws Exception { return revokeFamilyFor("old"); }
    private boolean revokeOldFamily() throws Exception { return revokeFamilyFor("old"); }

    private boolean revokeFamilyFor(String id) throws Exception {
        try (Connection c = connection()) {
            c.setAutoCommit(false);
            try (PreparedStatement lock = c.prepareStatement("SELECT family_id FROM auth_tokens_test WHERE id = ? FOR UPDATE")) {
                lock.setString(1, id); ResultSet r = lock.executeQuery();
                if (!r.next()) { c.commit(); return false; }
                try (PreparedStatement revoke = c.prepareStatement("UPDATE auth_tokens_test SET revoked = true WHERE family_id = ?")) {
                    revoke.setString(1, r.getString(1)); revoke.executeUpdate();
                }
                c.commit(); return true;
            }
        }
    }

    private boolean consumeReset() throws Exception { return claimBoolean("SELECT used FROM auth_reset_test WHERE id = 1 FOR UPDATE", "UPDATE auth_reset_test SET used = true WHERE id = 1"); }
    private boolean verifyOtp() throws Exception { return claimBoolean("SELECT verified FROM auth_otp_test WHERE id = 1 FOR UPDATE", "UPDATE auth_otp_test SET verified = true WHERE id = 1"); }

    private boolean claimBoolean(String lockSql, String updateSql) throws Exception {
        try (Connection c = connection(); Statement lock = c.createStatement()) {
            c.setAutoCommit(false); ResultSet r = lock.executeQuery(lockSql);
            r.next(); if (r.getBoolean(1)) { c.commit(); return false; }
            try (Statement update = c.createStatement()) { update.executeUpdate(updateSql); }
            c.commit(); return true;
        }
    }

    private boolean incrementLoginFailure() throws Exception {
        try (Connection c = connection(); PreparedStatement statement = c.prepareStatement(
                "INSERT INTO auth_attempt_test VALUES ('same-account', 1) ON DUPLICATE KEY UPDATE failures = failures + 1")) {
            statement.executeUpdate(); return true;
        }
    }

    private boolean logoutAll() throws Exception { return revokeFamilyFor("old"); }

    private List<Boolean> concurrently(int count, Work work) throws Exception {
        List<Work> jobs = new ArrayList<>(); for (int i = 0; i < count; i++) jobs.add(work); return concurrently(jobs);
    }

    private List<Boolean> concurrently(List<Work> jobs) throws Exception {
        CountDownLatch ready = new CountDownLatch(jobs.size()); CountDownLatch start = new CountDownLatch(1);
        ExecutorService executor = Executors.newFixedThreadPool(jobs.size());
        try {
            List<Future<Boolean>> futures = new ArrayList<>();
            for (Work job : jobs) futures.add(executor.submit(() -> { ready.countDown(); start.await(); return job.run(); }));
            ready.await(); start.countDown(); List<Boolean> values = new ArrayList<>();
            for (Future<Boolean> future : futures) values.add(future.get()); return values;
        } finally { executor.shutdownNow(); }
    }

    private int intValue(String sql) throws SQLException { try (Connection c = connection(); Statement s = c.createStatement(); ResultSet r = s.executeQuery(sql)) { r.next(); return r.getInt(1); } }
    private Connection connection() throws SQLException { return DriverManager.getConnection(MYSQL.getJdbcUrl(), MYSQL.getUsername(), MYSQL.getPassword()); }
    @FunctionalInterface private interface Work { boolean run() throws Exception; }
}
