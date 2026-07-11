package com.bookstore.bookstore.integration;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import org.flywaydb.core.Flyway;
import org.flywaydb.core.api.configuration.FluentConfiguration;
import org.junit.jupiter.api.Test;
import org.testcontainers.containers.MySQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

/** Verifies that V14 upgrades an already populated V13 MySQL schema without changing existing data. */
@Testcontainers(disabledWithoutDocker = true)
class FlywayMigrationMySqlIT {

    @Container
    static final MySQLContainer<?> MYSQL = new MySQLContainer<>("mysql:8.0.36")
            .withDatabaseName("bookstore_flyway_upgrade")
            .withUsername("bookstore")
            .withPassword("bookstore");

    @Test
    void v14MigratesExistingV13DataAndCreatesRefundAndOutboxTables() throws SQLException {
        migrateTo("13");

        try (Connection connection = connection(); PreparedStatement statement = connection.prepareStatement(
                "INSERT INTO categories (id, name, description, created_at, updated_at) VALUES (UNHEX(?), ?, ?, NOW(6), NOW(6))"
        )) {
            statement.setString(1, "11111111111111111111111111111111");
            statement.setString(2, "Flyway regression category");
            statement.setString(3, "existing data must survive V14");
            statement.executeUpdate();
        }

        migrateTo(null);

        assertEquals(1, count("SELECT COUNT(*) FROM categories WHERE name = 'Flyway regression category'"));
        assertEquals(1, count("SELECT COUNT(*) FROM information_schema.tables WHERE table_schema = DATABASE() AND table_name = 'refunds'"));
        assertEquals(1, count("SELECT COUNT(*) FROM information_schema.tables WHERE table_schema = DATABASE() AND table_name = 'outbox_events'"));
        assertEquals(1, count("SELECT COUNT(*) FROM information_schema.tables WHERE table_schema = DATABASE() AND table_name = 'outbox_deliveries'"));
        assertEquals(1, count("SELECT COUNT(*) FROM flyway_schema_history WHERE version = '14' AND success = 1"));
    }

    private void migrateTo(String target) {
        FluentConfiguration configuration = Flyway.configure()
                .dataSource(MYSQL.getJdbcUrl(), MYSQL.getUsername(), MYSQL.getPassword())
                .locations("classpath:db/migration");
        if (target != null) {
            configuration.target(target);
        }
        configuration.load().migrate();
    }

    private Connection connection() throws SQLException {
        return DriverManager.getConnection(MYSQL.getJdbcUrl(), MYSQL.getUsername(), MYSQL.getPassword());
    }

    private int count(String sql) throws SQLException {
        try (Connection connection = connection(); PreparedStatement statement = connection.prepareStatement(sql); ResultSet result = statement.executeQuery()) {
            result.next();
            return result.getInt(1);
        }
    }
}
