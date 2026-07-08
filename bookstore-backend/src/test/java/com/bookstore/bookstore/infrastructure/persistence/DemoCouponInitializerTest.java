package com.bookstore.bookstore.infrastructure.persistence;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertFalse;

import java.math.BigDecimal;
import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;
import java.sql.Timestamp;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Map;
import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.DefaultApplicationArguments;
import org.springframework.boot.test.autoconfigure.jdbc.JdbcTest;
import org.springframework.jdbc.core.JdbcTemplate;

@JdbcTest(properties = {
        "spring.flyway.enabled=false"
})
public class DemoCouponInitializerTest {

    @Autowired
    private JdbcTemplate jdbcTemplate;

    private DemoCouponInitializer demoCouponInitializer;

    @BeforeEach
    void setUp() {
        demoCouponInitializer = new DemoCouponInitializer(jdbcTemplate);
        jdbcTemplate.execute("DROP TABLE IF EXISTS coupon_targets");
        jdbcTemplate.execute("DROP TABLE IF EXISTS coupons");
        jdbcTemplate.execute(
                "CREATE ALIAS IF NOT EXISTS UUID_TO_BIN FOR \"com.bookstore.bookstore.infrastructure.persistence.DemoCouponInitializerTest.uuidToBin\""
        );
        jdbcTemplate.execute(
                "CREATE ALIAS IF NOT EXISTS BIN_TO_UUID FOR \"com.bookstore.bookstore.infrastructure.persistence.DemoCouponInitializerTest.binToUuid\""
        );
        jdbcTemplate.execute("""
                CREATE TABLE coupons (
                    id BINARY(16) NOT NULL PRIMARY KEY,
                    active BOOLEAN NOT NULL,
                    code VARCHAR(100) NOT NULL UNIQUE,
                    created_at TIMESTAMP(6) NOT NULL,
                    deleted_at TIMESTAMP(6) NULL,
                    description TEXT NULL,
                    discount_type VARCHAR(32) NOT NULL,
                    discount_value DECIMAL(19,2) NOT NULL,
                    expires_at TIMESTAMP(6) NOT NULL,
                    max_discount_amount DECIMAL(19,2) NULL,
                    max_usage_count INT NULL,
                    min_order_amount DECIMAL(19,2) NOT NULL,
                    starts_at TIMESTAMP(6) NOT NULL,
                    updated_at TIMESTAMP(6) NOT NULL,
                    used_count INT NOT NULL,
                    coupon_type VARCHAR(32) NOT NULL
                )
                """);
        jdbcTemplate.execute("""
                CREATE TABLE coupon_targets (
                    id BINARY(16) NOT NULL PRIMARY KEY,
                    created_at TIMESTAMP(6) NOT NULL,
                    target_id BINARY(16) NULL,
                    target_type VARCHAR(32) NOT NULL,
                    updated_at TIMESTAMP(6) NOT NULL,
                    coupon_id BINARY(16) NOT NULL,
                    CONSTRAINT fk_coupon_targets_coupon
                        FOREIGN KEY (coupon_id) REFERENCES coupons(id)
                )
                """);
    }

    @Test
    void run_whenSeededCouponExists_refreshesCouponAndResetsAllOrderTarget() throws Exception {
        UUID couponId = deterministicId("coupon", 3);
        UUID legacyTargetId = UUID.randomUUID();
        Instant createdAt = Instant.now().truncatedTo(ChronoUnit.SECONDS).minus(2, ChronoUnit.HOURS);

        jdbcTemplate.update("""
                        INSERT INTO coupons (
                            id, active, code, created_at, deleted_at, description, discount_type,
                            discount_value, expires_at, max_discount_amount, max_usage_count,
                            min_order_amount, starts_at, updated_at, used_count, coupon_type
                        ) VALUES (
                            UUID_TO_BIN(?), ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?
                        )
                        """,
                couponId.toString(),
                false,
                "DOCHEM03",
                Timestamp.from(createdAt),
                Timestamp.from(createdAt),
                "stale",
                "FIXED_AMOUNT",
                new BigDecimal("5000.00"),
                Timestamp.from(createdAt.plusSeconds(60)),
                new BigDecimal("5000.00"),
                2,
                new BigDecimal("10000.00"),
                Timestamp.from(createdAt.minusSeconds(60)),
                Timestamp.from(createdAt),
                7,
                "SHIPPING"
        );
        jdbcTemplate.update("""
                        INSERT INTO coupon_targets (id, created_at, target_id, target_type, updated_at, coupon_id)
                        VALUES (UUID_TO_BIN(?), ?, UUID_TO_BIN(?), ?, ?, UUID_TO_BIN(?))
                        """,
                legacyTargetId.toString(),
                Timestamp.from(createdAt),
                UUID.randomUUID().toString(),
                "BOOK",
                Timestamp.from(createdAt),
                couponId.toString()
        );

        demoCouponInitializer.run(new DefaultApplicationArguments(new String[0]));

        Map<String, Object> couponRow = jdbcTemplate.queryForMap("""
                        SELECT active, code, description, discount_type, discount_value, coupon_type,
                               min_order_amount, max_discount_amount, max_usage_count, used_count,
                               created_at, starts_at, updated_at, deleted_at
                        FROM coupons
                        WHERE id = UUID_TO_BIN(?)
                        """,
                couponId.toString()
        );

        assertEquals(Boolean.TRUE, couponRow.get("active"));
        assertEquals("DOCHEM03", couponRow.get("code"));
        assertEquals("BOOK", couponRow.get("coupon_type"));
        assertEquals("PERCENTAGE", couponRow.get("discount_type"));
        assertEquals(new BigDecimal("10.00"), couponRow.get("discount_value"));
        assertEquals(new BigDecimal("50000.00"), couponRow.get("min_order_amount"));
        assertEquals(new BigDecimal("25000.00"), couponRow.get("max_discount_amount"));
        assertEquals(107, couponRow.get("max_usage_count"));
        assertEquals(7, couponRow.get("used_count"));
        assertEquals(null, couponRow.get("deleted_at"));
        assertFalse(((Timestamp) couponRow.get("updated_at"))
                .before((Timestamp) couponRow.get("created_at")));
        assertFalse(((Timestamp) couponRow.get("updated_at"))
                .before((Timestamp) couponRow.get("starts_at")));

        assertEquals(1, jdbcTemplate.queryForObject(
                "SELECT COUNT(*) FROM coupon_targets WHERE coupon_id = UUID_TO_BIN(?) AND target_type = 'ALL_ORDER'",
                Integer.class,
                couponId.toString()
        ));
    }

    @Test
    void run_whenCanonicalCodeBelongsToNonSeedCoupon_keepsForeignCouponAndUsesNextCandidate() throws Exception {
        UUID foreignCouponId = UUID.randomUUID();
        Instant createdAt = Instant.parse("2026-01-01T08:00:00Z");

        jdbcTemplate.update("""
                        INSERT INTO coupons (
                            id, active, code, created_at, deleted_at, description, discount_type,
                            discount_value, expires_at, max_discount_amount, max_usage_count,
                            min_order_amount, starts_at, updated_at, used_count, coupon_type
                        ) VALUES (
                            UUID_TO_BIN(?), ?, ?, ?, NULL, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?
                        )
                        """,
                foreignCouponId.toString(),
                true,
                "DOCHEM03",
                Timestamp.from(createdAt),
                "foreign coupon",
                "FIXED_AMOUNT",
                new BigDecimal("5000.00"),
                Timestamp.from(createdAt.plusSeconds(60)),
                new BigDecimal("5000.00"),
                5,
                new BigDecimal("10000.00"),
                Timestamp.from(createdAt.minusSeconds(60)),
                Timestamp.from(createdAt),
                0,
                "BOOK"
        );

        demoCouponInitializer.run(new DefaultApplicationArguments(new String[0]));

        assertEquals(1, jdbcTemplate.queryForObject(
                "SELECT COUNT(*) FROM coupons WHERE code = 'DOCHEM03' AND id = UUID_TO_BIN(?)",
                Integer.class,
                foreignCouponId.toString()
        ));

        UUID seededCouponId = deterministicId("coupon", 9);
        Map<String, Object> couponRow = jdbcTemplate.queryForMap("""
                        SELECT code, coupon_type, discount_type, discount_value, min_order_amount, max_discount_amount
                        FROM coupons
                        WHERE id = UUID_TO_BIN(?)
                        """,
                seededCouponId.toString()
        );

        assertNotNull(couponRow);
        assertEquals("DOCHEM09", couponRow.get("code"));
        assertEquals("BOOK", couponRow.get("coupon_type"));
        assertEquals("PERCENTAGE", couponRow.get("discount_type"));
        assertEquals(new BigDecimal("10.00"), couponRow.get("discount_value"));
        assertEquals(new BigDecimal("50000.00"), couponRow.get("min_order_amount"));
        assertEquals(new BigDecimal("25000.00"), couponRow.get("max_discount_amount"));
    }

    public static byte[] uuidToBin(String uuidText) {
        UUID uuid = UUID.fromString(uuidText);
        ByteBuffer buffer = ByteBuffer.allocate(16);
        buffer.putLong(uuid.getMostSignificantBits());
        buffer.putLong(uuid.getLeastSignificantBits());
        return buffer.array();
    }

    public static String binToUuid(byte[] value) {
        ByteBuffer buffer = ByteBuffer.wrap(value);
        return new UUID(buffer.getLong(), buffer.getLong()).toString();
    }

    private static UUID deterministicId(String namespace, int index) {
        return UUID.nameUUIDFromBytes(("bookstore-seed:" + namespace + ":" + index)
                .getBytes(StandardCharsets.UTF_8));
    }
}
