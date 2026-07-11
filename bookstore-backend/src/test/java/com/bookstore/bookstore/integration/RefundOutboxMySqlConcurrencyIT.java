package com.bookstore.bookstore.integration;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.time.Instant;
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

/** MySQL lock, idempotency and reclaim regressions used by the refund ledger and transactional outbox. */
@Testcontainers(disabledWithoutDocker = true)
class RefundOutboxMySqlConcurrencyIT {
    @Container
    static final MySQLContainer<?> MYSQL = new MySQLContainer<>("mysql:8.0.36")
            .withDatabaseName("bookstore_refund_outbox_concurrency").withUsername("bookstore").withPassword("bookstore");

    @BeforeEach
    void setUp() throws SQLException {
        try (Connection c = connection(); Statement s = c.createStatement()) {
            s.execute("DROP TABLE IF EXISTS outbox_events_test");
            s.execute("DROP TABLE IF EXISTS refund_claims_test");
            s.execute("DROP TABLE IF EXISTS payment_gate_test");
            s.execute("CREATE TABLE payment_gate_test (id INT PRIMARY KEY, paid_amount DECIMAL(19,2) NOT NULL)");
            s.execute("INSERT INTO payment_gate_test VALUES (1, 100.00)");
            s.execute("CREATE TABLE refund_claims_test (id VARCHAR(32) PRIMARY KEY, payment_id INT NOT NULL, amount DECIMAL(19,2) NOT NULL, status VARCHAR(16) NOT NULL, idempotency_key VARCHAR(64) NOT NULL, UNIQUE KEY uk_refund_idempotency(payment_id, idempotency_key))");
            s.execute("CREATE TABLE outbox_events_test (id VARCHAR(32) PRIMARY KEY, dedupe VARCHAR(64) NOT NULL UNIQUE, status VARCHAR(16) NOT NULL, attempt_count INT NOT NULL, next_attempt_at BIGINT NOT NULL, locked_at BIGINT NULL, locked_by VARCHAR(64) NULL)");
        }
    }

    @Test
    void concurrentRefundReservations_neverExceedPaidAmount() throws Exception {
        assertEquals(1L, concurrently(List.of(() -> reserveRefund("r1", "k1", new BigDecimal("70.00")), () -> reserveRefund("r2", "k2", new BigDecimal("60.00")))).stream().filter(Boolean::booleanValue).count());
        assertTrue(decimalValue("SELECT COALESCE(SUM(amount), 0) FROM refund_claims_test WHERE status IN ('REQUESTED','APPROVED','PROCESSING','SUCCEEDED')")
                .compareTo(new BigDecimal("100.00")) <= 0);
    }

    @Test
    void partialRefunds_respectRemainingAmount() throws Exception {
        assertTrue(reserveRefund("r1", "k1", new BigDecimal("30.00")));
        assertTrue(reserveRefund("r2", "k2", new BigDecimal("40.00")));
        assertEquals(false, reserveRefund("r3", "k3", new BigDecimal("40.01")));
    }

    @Test
    void duplicateRefundCommand_hasOneLedgerRecord() throws Exception {
        assertEquals(1L, concurrently(2, () -> reserveRefund("same", "same-key", new BigDecimal("20.00"))).stream().filter(Boolean::booleanValue).count());
        assertEquals(1, intValue("SELECT COUNT(*) FROM refund_claims_test"));
    }

    @Test
    void twoOutboxWorkers_claimOneEventOnce() throws Exception {
        insertOutbox("event-1", "dedupe-1", "PENDING", 0, 0, null);
        assertEquals(1L, concurrently(2, () -> claimOutbox("worker")).stream().filter(Boolean::booleanValue).count());
        assertEquals("PROCESSING", stringValue("SELECT status FROM outbox_events_test WHERE id = 'event-1'"));
    }

    @Test
    void failedOutboxEvent_retriesThenDeadLetters() throws Exception {
        insertOutbox("event-1", "dedupe-1", "PROCESSING", 0, 0, 0L);
        failOutbox("event-1", 2, 1000L);
        assertEquals("FAILED", stringValue("SELECT status FROM outbox_events_test WHERE id = 'event-1'"));
        failOutbox("event-1", 2, 2000L);
        assertEquals("DEAD", stringValue("SELECT status FROM outbox_events_test WHERE id = 'event-1'"));
    }

    @Test
    void staleProcessingEvent_isReclaimed() throws Exception {
        insertOutbox("event-1", "dedupe-1", "PROCESSING", 0, 0, 1L);
        try (Connection c = connection(); PreparedStatement update = c.prepareStatement("UPDATE outbox_events_test SET status = 'PENDING', locked_at = NULL, locked_by = NULL WHERE status = 'PROCESSING' AND locked_at <= ?")) {
            update.setLong(1, 10L); assertEquals(1, update.executeUpdate());
        }
        assertEquals("PENDING", stringValue("SELECT status FROM outbox_events_test WHERE id = 'event-1'"));
    }

    @Test
    void rolledBackBusinessTransaction_hasNoOutboxEvent() throws Exception {
        try (Connection c = connection(); PreparedStatement insert = c.prepareStatement("INSERT INTO outbox_events_test VALUES ('event-1','dedupe-1','PENDING',0,0,NULL,NULL)")) {
            c.setAutoCommit(false); insert.executeUpdate(); c.rollback();
        }
        assertEquals(0, intValue("SELECT COUNT(*) FROM outbox_events_test"));
    }

    private boolean reserveRefund(String id, String key, BigDecimal amount) throws SQLException {
        try (Connection c = connection()) {
            c.setAutoCommit(false);
            try (PreparedStatement lock = c.prepareStatement("SELECT paid_amount FROM payment_gate_test WHERE id = 1 FOR UPDATE")) {
                ResultSet rows = lock.executeQuery(); rows.next(); BigDecimal paid = rows.getBigDecimal(1);
                try (PreparedStatement sum = c.prepareStatement("SELECT COALESCE(SUM(amount), 0) FROM refund_claims_test WHERE payment_id = 1 AND status IN ('REQUESTED','APPROVED','PROCESSING','SUCCEEDED')")) {
                    ResultSet totals = sum.executeQuery(); totals.next(); if (totals.getBigDecimal(1).add(amount).compareTo(paid) > 0) { c.commit(); return false; }
                }
                try (PreparedStatement insert = c.prepareStatement("INSERT INTO refund_claims_test VALUES (?, 1, ?, 'REQUESTED', ?)")) { insert.setString(1, id); insert.setBigDecimal(2, amount); insert.setString(3, key); insert.executeUpdate(); }
                c.commit(); return true;
            } catch (SQLException duplicate) { c.rollback(); return false; }
        }
    }

    private boolean claimOutbox(String worker) throws SQLException {
        try (Connection c = connection()) {
            c.setAutoCommit(false);
            try (PreparedStatement lock = c.prepareStatement("SELECT id FROM outbox_events_test WHERE status IN ('PENDING','FAILED') AND next_attempt_at <= 100 FOR UPDATE")) {
                ResultSet rows = lock.executeQuery(); if (!rows.next()) { c.commit(); return false; }
                try (PreparedStatement update = c.prepareStatement("UPDATE outbox_events_test SET status = 'PROCESSING', locked_at = 100, locked_by = ? WHERE id = ?")) { update.setString(1, worker); update.setString(2, rows.getString(1)); update.executeUpdate(); }
                c.commit(); return true;
            }
        }
    }

    private void failOutbox(String id, int maxAttempts, long now) throws SQLException {
        try (Connection c = connection()) {
            c.setAutoCommit(false);
            try (PreparedStatement lock = c.prepareStatement("SELECT attempt_count FROM outbox_events_test WHERE id = ? FOR UPDATE")) {
                lock.setString(1, id); ResultSet rows = lock.executeQuery(); rows.next(); int attempts = rows.getInt(1) + 1;
                try (PreparedStatement update = c.prepareStatement("UPDATE outbox_events_test SET attempt_count = ?, status = ?, next_attempt_at = ?, locked_at = NULL WHERE id = ?")) {
                    update.setInt(1, attempts); update.setString(2, attempts >= maxAttempts ? "DEAD" : "FAILED"); update.setLong(3, now); update.setString(4, id); update.executeUpdate();
                }
                c.commit();
            }
        }
    }
    private void insertOutbox(String id, String dedupe, String status, int attempts, long nextAttempt, Long lockedAt) throws SQLException { try (Connection c = connection(); PreparedStatement s = c.prepareStatement("INSERT INTO outbox_events_test VALUES (?, ?, ?, ?, ?, ?, NULL)")) { s.setString(1,id); s.setString(2,dedupe); s.setString(3,status); s.setInt(4,attempts); s.setLong(5,nextAttempt); if(lockedAt == null) s.setNull(6, java.sql.Types.BIGINT); else s.setLong(6,lockedAt); s.executeUpdate(); } }
    private List<Boolean> concurrently(int workers, Work work) throws Exception { List<Work> jobs = new ArrayList<>(); for (int i=0;i<workers;i++) jobs.add(work); return concurrently(jobs); }
    private List<Boolean> concurrently(List<Work> jobs) throws Exception { CountDownLatch ready = new CountDownLatch(jobs.size()); CountDownLatch start = new CountDownLatch(1); ExecutorService pool = Executors.newFixedThreadPool(jobs.size()); try { List<Future<Boolean>> futures = new ArrayList<>(); for (Work job: jobs) futures.add(pool.submit(() -> { ready.countDown(); start.await(); return job.run(); })); ready.await(); start.countDown(); List<Boolean> values = new ArrayList<>(); for(Future<Boolean> future:futures) values.add(future.get()); return values; } finally { pool.shutdownNow(); } }
    private int intValue(String sql) throws SQLException { try (Connection c=connection(); Statement s=c.createStatement(); ResultSet r=s.executeQuery(sql)) { r.next(); return r.getInt(1); } }
    private String stringValue(String sql) throws SQLException { try (Connection c=connection(); Statement s=c.createStatement(); ResultSet r=s.executeQuery(sql)) { r.next(); return r.getString(1); } }
    private BigDecimal decimalValue(String sql) throws SQLException { try (Connection c=connection(); Statement s=c.createStatement(); ResultSet r=s.executeQuery(sql)) { r.next(); return r.getBigDecimal(1); } }
    private Connection connection() throws SQLException { return DriverManager.getConnection(MYSQL.getJdbcUrl(), MYSQL.getUsername(), MYSQL.getPassword()); }
    @FunctionalInterface private interface Work { boolean run() throws Exception; }
}
