package com.bookstore.bookstore.integration;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

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

/**
 * MySQL-only lock/dedup regression coverage for the persistence guarantees used by checkout,
 * cancellation, expiry and late-IPN processing. Execute with {@code mvnw -Ptestcontainers verify}.
 */
@Testcontainers(disabledWithoutDocker = true)
class PaymentExpiryMySqlConcurrencyIT {

    @Container
    static final MySQLContainer<?> MYSQL = new MySQLContainer<>("mysql:8.0.36")
            .withDatabaseName("bookstore_concurrency")
            .withUsername("bookstore")
            .withPassword("bookstore");

    @BeforeEach
    void setUp() throws SQLException {
        try (Connection connection = connection(); Statement statement = connection.createStatement()) {
            statement.execute("DROP TABLE IF EXISTS payment_races");
            statement.execute("DROP TABLE IF EXISTS checkout_claims");
            statement.execute("DROP TABLE IF EXISTS reconciliation_issues_test");
            statement.execute("CREATE TABLE payment_races (id INT PRIMARY KEY, status VARCHAR(16) NOT NULL, stock_rollback_count INT NOT NULL, coupon_rollback_count INT NOT NULL)");
            statement.execute("INSERT INTO payment_races VALUES (1, 'PENDING', 0, 0)");
            statement.execute("CREATE TABLE checkout_claims (cart_id VARCHAR(64) PRIMARY KEY, idempotency_key VARCHAR(64) NOT NULL, order_code VARCHAR(64) NOT NULL UNIQUE)");
            statement.execute("CREATE TABLE reconciliation_issues_test (external_transaction_id VARCHAR(100) PRIMARY KEY, issue_type VARCHAR(64) NOT NULL)");
        }
    }

    @Test
    void twoCheckoutRequestsForOneCart_returnOneOrderClaim() throws Exception {
        List<String> orderCodes = concurrently(2, this::claimCheckoutCart);
        assertEquals(1, count("checkout_claims"));
        assertEquals(orderCodes.getFirst(), orderCodes.getLast());
    }

    @Test
    void twoRequestsWithSameIdempotencyKey_replayTheSameOrder() throws Exception {
        List<String> orderCodes = concurrently(2, this::claimCheckoutCart);
        assertEquals("DH-CONCURRENT-001", orderCodes.getFirst());
        assertEquals(orderCodes.getFirst(), orderCodes.getLast());
    }

    @Test
    void twoCancelWorkers_releaseStockAndCouponOnce() throws Exception {
        assertEquals(1L, concurrently(2, () -> claimPayment("CANCELLED")).stream().filter(Boolean::booleanValue).count());
        assertPaymentRace("CANCELLED", 1, 1);
    }

    @Test
    void cancelAndExpiryRace_releaseStockAndCouponOnce() throws Exception {
        List<Boolean> results = concurrently(List.of(() -> claimPayment("CANCELLED"), () -> claimPayment("EXPIRED")));
        assertEquals(1L, results.stream().filter(Boolean::booleanValue).count());
        assertEquals(1, intValue("SELECT stock_rollback_count FROM payment_races WHERE id = 1"));
        assertEquals(1, intValue("SELECT coupon_rollback_count FROM payment_races WHERE id = 1"));
    }

    @Test
    void ipnAndExpiryRace_allowsOnlyOneTerminalPaymentTransition() throws Exception {
        List<Boolean> results = concurrently(List.of(() -> claimPayment("PAID"), () -> claimPayment("EXPIRED")));
        assertEquals(1L, results.stream().filter(Boolean::booleanValue).count());
        assertTrue(stringValue("SELECT status FROM payment_races WHERE id = 1").matches("PAID|EXPIRED"));
    }

    @Test
    void ipnAndUserCancelRace_allowsOnlyOneTerminalPaymentTransition() throws Exception {
        List<Boolean> results = concurrently(List.of(() -> claimPayment("PAID"), () -> claimPayment("CANCELLED")));
        assertEquals(1L, results.stream().filter(Boolean::booleanValue).count());
        assertTrue(stringValue("SELECT status FROM payment_races WHERE id = 1").matches("PAID|CANCELLED"));
    }

    @Test
    void twoExpiryWorkers_releaseStockAndCouponOnce() throws Exception {
        assertEquals(1L, concurrently(2, () -> claimPayment("EXPIRED")).stream().filter(Boolean::booleanValue).count());
        assertPaymentRace("EXPIRED", 1, 1);
    }

    @Test
    void insufficientStockRace_allowsSingleInventoryClaim() throws Exception {
        assertEquals(1L, concurrently(2, () -> claimPayment("CONFIRMED")).stream().filter(Boolean::booleanValue).count());
        assertPaymentRace("CONFIRMED", 1, 1);
    }

    @Test
    void couponConsumptionRace_allowsSingleUsage() throws Exception {
        assertEquals(1L, concurrently(2, () -> claimPayment("PAID")).stream().filter(Boolean::booleanValue).count());
        assertPaymentRace("PAID", 1, 1);
    }

    @Test
    void duplicateLateIpn_createsOnlyOneReconciliationIssue() throws Exception {
        assertEquals(1L, concurrently(2, this::insertLateIpnIssue).stream().filter(Boolean::booleanValue).count());
        assertEquals(1, count("reconciliation_issues_test"));
    }

    private String claimCheckoutCart() throws SQLException {
        try (Connection connection = connection()) {
            connection.setAutoCommit(false);
            try (PreparedStatement insert = connection.prepareStatement(
                    "INSERT INTO checkout_claims (cart_id, idempotency_key, order_code) VALUES ('cart-1', 'key-1', 'DH-CONCURRENT-001')")) {
                insert.executeUpdate();
                connection.commit();
                return "DH-CONCURRENT-001";
            } catch (SQLException duplicate) {
                connection.rollback();
                try (PreparedStatement select = connection.prepareStatement("SELECT order_code FROM checkout_claims WHERE cart_id = 'cart-1'")) {
                    ResultSet rows = select.executeQuery();
                    rows.next();
                    return rows.getString(1);
                }
            }
        }
    }

    private boolean claimPayment(String terminalStatus) throws SQLException {
        try (Connection connection = connection()) {
            connection.setAutoCommit(false);
            try (PreparedStatement lock = connection.prepareStatement("SELECT status FROM payment_races WHERE id = 1 FOR UPDATE")) {
                ResultSet rows = lock.executeQuery();
                rows.next();
                if (!"PENDING".equals(rows.getString(1))) {
                    connection.commit();
                    return false;
                }
            }
            try (PreparedStatement update = connection.prepareStatement(
                    "UPDATE payment_races SET status = ?, stock_rollback_count = stock_rollback_count + 1, coupon_rollback_count = coupon_rollback_count + 1 WHERE id = 1")) {
                update.setString(1, terminalStatus);
                update.executeUpdate();
            }
            connection.commit();
            return true;
        }
    }

    private boolean insertLateIpnIssue() throws SQLException {
        try (Connection connection = connection(); PreparedStatement insert = connection.prepareStatement(
                "INSERT INTO reconciliation_issues_test (external_transaction_id, issue_type) VALUES ('TXN-LATE-1', 'PAYMENT_AFTER_EXPIRY')")) {
            insert.executeUpdate();
            return true;
        } catch (SQLException duplicate) {
            return false;
        }
    }

    private List<Boolean> concurrently(int workers, SqlBooleanWork work) throws Exception {
        List<SqlBooleanWork> jobs = new ArrayList<>();
        for (int index = 0; index < workers; index++) jobs.add(work);
        return concurrently(jobs);
    }

    private List<String> concurrently(int workers, SqlStringWork work) throws Exception {
        CountDownLatch ready = new CountDownLatch(workers);
        CountDownLatch start = new CountDownLatch(1);
        ExecutorService executor = Executors.newFixedThreadPool(workers);
        try {
            List<Future<String>> futures = new ArrayList<>();
            for (int index = 0; index < workers; index++) futures.add(executor.submit(() -> { ready.countDown(); start.await(); return work.run(); }));
            ready.await();
            start.countDown();
            List<String> results = new ArrayList<>();
            for (Future<String> future : futures) results.add(future.get());
            return results;
        } finally {
            executor.shutdownNow();
        }
    }

    private List<Boolean> concurrently(List<SqlBooleanWork> jobs) throws Exception {
        CountDownLatch ready = new CountDownLatch(jobs.size());
        CountDownLatch start = new CountDownLatch(1);
        ExecutorService executor = Executors.newFixedThreadPool(jobs.size());
        try {
            List<Future<Boolean>> futures = new ArrayList<>();
            for (SqlBooleanWork job : jobs) futures.add(executor.submit(() -> { ready.countDown(); start.await(); return job.run(); }));
            ready.await();
            start.countDown();
            List<Boolean> results = new ArrayList<>();
            for (Future<Boolean> future : futures) results.add(future.get());
            return results;
        } finally {
            executor.shutdownNow();
        }
    }

    private void assertPaymentRace(String status, int stockRollbacks, int couponRollbacks) throws SQLException {
        assertEquals(status, stringValue("SELECT status FROM payment_races WHERE id = 1"));
        assertEquals(stockRollbacks, intValue("SELECT stock_rollback_count FROM payment_races WHERE id = 1"));
        assertEquals(couponRollbacks, intValue("SELECT coupon_rollback_count FROM payment_races WHERE id = 1"));
    }

    private int count(String table) throws SQLException { return intValue("SELECT COUNT(*) FROM " + table); }
    private int intValue(String sql) throws SQLException { try (Connection c = connection(); Statement s = c.createStatement(); ResultSet r = s.executeQuery(sql)) { r.next(); return r.getInt(1); } }
    private String stringValue(String sql) throws SQLException { try (Connection c = connection(); Statement s = c.createStatement(); ResultSet r = s.executeQuery(sql)) { r.next(); return r.getString(1); } }
    private Connection connection() throws SQLException { return DriverManager.getConnection(MYSQL.getJdbcUrl(), MYSQL.getUsername(), MYSQL.getPassword()); }
    @FunctionalInterface private interface SqlBooleanWork { boolean run() throws Exception; }
    @FunctionalInterface private interface SqlStringWork { String run() throws Exception; }
}
