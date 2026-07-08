package com.bookstore.bookstore.infrastructure.persistence;

import java.math.BigDecimal;
import java.nio.charset.StandardCharsets;
import java.sql.Timestamp;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.core.annotation.Order;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Component
@ConditionalOnProperty(name = "app.seed.enabled", havingValue = "true")
@Order(120)
@RequiredArgsConstructor
public class DemoCouponInitializer implements ApplicationRunner {

    private static final List<Integer> PUBLIC_DEMO_COUPON_CANDIDATES = List.of(3, 9, 15, 21, 27, 33, 39, 45);
    private static final BigDecimal DISCOUNT_VALUE = money(10);
    private static final BigDecimal MIN_ORDER_AMOUNT = money(50_000);
    private static final BigDecimal MAX_DISCOUNT_AMOUNT = money(25_000);
    private static final int MIN_USAGE_HEADROOM = 100;
    private static final String DEMO_DESCRIPTION = "Giam 10% cho don hang demo seeded dang du dieu kien.";

    private final JdbcTemplate jdbcTemplate;

    @Override
    @Transactional
    public void run(ApplicationArguments args) {
        Instant now = Instant.now().truncatedTo(ChronoUnit.SECONDS);
        Instant startsAt = now.minus(1, ChronoUnit.DAYS);
        Instant expiresAt = now.plus(90, ChronoUnit.DAYS);
        Instant insertAuditAt = startsAt;

        for (int index : PUBLIC_DEMO_COUPON_CANDIDATES) {
            if (refreshOrInsertDemoCoupon(index, startsAt, expiresAt, insertAuditAt)) {
                return;
            }
        }

        log.warn("Could not reserve any canonical seeded DOCHEM coupon code for demo coupon refresh");
    }

    private boolean refreshOrInsertDemoCoupon(int index, Instant startsAt, Instant expiresAt, Instant auditAt) {
        UUID couponId = deterministicId("coupon", index);
        UUID couponTargetId = deterministicId("coupon-target", index);
        String couponCode = "DOCHEM%02d".formatted(index);

        CouponState existingById = findCouponById(couponId);
        if (existingById != null) {
            Instant refreshAuditAt = latest(existingById.createdAt(), startsAt);
            refreshCoupon(existingById, couponCode, startsAt, expiresAt, refreshAuditAt);
            resetAllOrderTarget(couponId, couponTargetId, refreshAuditAt);
            log.info("Refreshed seeded demo coupon {}", couponCode);
            return true;
        }

        UUID codeOwnerId = findCouponIdByCode(couponCode);
        if (codeOwnerId != null) {
            log.warn("Skipping demo coupon code {} because it belongs to non-seed coupon {}", couponCode, codeOwnerId);
            return false;
        }

        insertCoupon(couponId, couponCode, startsAt, expiresAt, auditAt);
        resetAllOrderTarget(couponId, couponTargetId, auditAt);
        log.info("Inserted seeded demo coupon {}", couponCode);
        return true;
    }

    private CouponState findCouponById(UUID couponId) {
        List<CouponState> rows = jdbcTemplate.query(
                """
                        select BIN_TO_UUID(id) as coupon_id,
                               code,
                               created_at,
                               coalesce(used_count, 0) as used_count,
                               max_usage_count
                        from coupons
                        where id = UUID_TO_BIN(?)
                        """,
                (rs, rowNum) -> new CouponState(
                        UUID.fromString(rs.getString("coupon_id")),
                        rs.getString("code"),
                        rs.getTimestamp("created_at").toLocalDateTime().toInstant(ZoneOffset.UTC),
                        rs.getInt("used_count"),
                        (Integer) rs.getObject("max_usage_count")
                ),
                couponId.toString()
        );
        return rows.isEmpty() ? null : rows.get(0);
    }

    private UUID findCouponIdByCode(String code) {
        List<UUID> rows = jdbcTemplate.query(
                """
                        select BIN_TO_UUID(id) as coupon_id
                        from coupons
                        where code = ?
                        """,
                (rs, rowNum) -> UUID.fromString(rs.getString("coupon_id")),
                code
        );
        return rows.isEmpty() ? null : rows.get(0);
    }

    private void refreshCoupon(
            CouponState existingCoupon,
            String couponCode,
            Instant startsAt,
            Instant expiresAt,
            Instant auditAt
    ) {
        jdbcTemplate.update(
                """
                        update coupons
                        set active = ?,
                            code = ?,
                            description = ?,
                            coupon_type = 'BOOK',
                            discount_type = 'PERCENTAGE',
                            discount_value = ?,
                            min_order_amount = ?,
                            max_discount_amount = ?,
                            max_usage_count = ?,
                            starts_at = ?,
                            expires_at = ?,
                            updated_at = ?,
                            deleted_at = null
                        where id = UUID_TO_BIN(?)
                        """,
                true,
                couponCode,
                DEMO_DESCRIPTION,
                DISCOUNT_VALUE,
                MIN_ORDER_AMOUNT,
                MAX_DISCOUNT_AMOUNT,
                resolveMaxUsageCount(existingCoupon),
                timestamp(startsAt),
                timestamp(expiresAt),
                timestamp(auditAt),
                existingCoupon.id().toString()
        );
    }

    private void insertCoupon(UUID couponId, String couponCode, Instant startsAt, Instant expiresAt, Instant auditAt) {
        jdbcTemplate.update(
                """
                        insert into coupons (
                            id, active, code, created_at, deleted_at, description, discount_type,
                            discount_value, expires_at, max_discount_amount, max_usage_count,
                            min_order_amount, starts_at, updated_at, used_count, coupon_type
                        ) values (
                            UUID_TO_BIN(?), ?, ?, ?, null, ?, 'PERCENTAGE', ?, ?, ?, ?,
                            ?, ?, ?, 0, 'BOOK'
                        )
                        """,
                couponId.toString(),
                true,
                couponCode,
                timestamp(auditAt),
                DEMO_DESCRIPTION,
                DISCOUNT_VALUE,
                timestamp(expiresAt),
                MAX_DISCOUNT_AMOUNT,
                MIN_USAGE_HEADROOM,
                MIN_ORDER_AMOUNT,
                timestamp(startsAt),
                timestamp(auditAt)
        );
    }

    private void resetAllOrderTarget(UUID couponId, UUID targetId, Instant auditAt) {
        jdbcTemplate.update("delete from coupon_targets where coupon_id = UUID_TO_BIN(?)", couponId.toString());
        jdbcTemplate.update(
                """
                        insert into coupon_targets (id, created_at, target_id, target_type, updated_at, coupon_id)
                        values (UUID_TO_BIN(?), ?, null, 'ALL_ORDER', ?, UUID_TO_BIN(?))
                        """,
                targetId.toString(),
                timestamp(auditAt),
                timestamp(auditAt),
                couponId.toString()
        );
    }

    private int resolveMaxUsageCount(CouponState couponState) {
        int usedCount = Math.max(couponState.usedCount(), 0);
        int currentMaxUsageCount = couponState.maxUsageCount() == null ? 0 : couponState.maxUsageCount();
        return Math.max(currentMaxUsageCount, usedCount + MIN_USAGE_HEADROOM);
    }

    private static UUID deterministicId(String namespace, int index) {
        return UUID.nameUUIDFromBytes(("bookstore-seed:" + namespace + ":" + index)
                .getBytes(StandardCharsets.UTF_8));
    }

    private static BigDecimal money(long value) {
        return BigDecimal.valueOf(value).setScale(2);
    }

    private static Timestamp timestamp(Instant instant) {
        return Timestamp.valueOf(LocalDateTime.ofInstant(instant, ZoneOffset.UTC));
    }

    private static Instant latest(Instant left, Instant right) {
        return left.isAfter(right) ? left : right;
    }

    private record CouponState(
            UUID id,
            String code,
            Instant createdAt,
            int usedCount,
            Integer maxUsageCount
    ) {
    }
}
