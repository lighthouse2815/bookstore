package com.bookstore.bookstore.domain.model;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import com.bookstore.bookstore.domain.enums.CouponDiscountType;
import com.bookstore.bookstore.domain.exception.DomainErrorCode;
import com.bookstore.bookstore.domain.exception.DomainException;
import java.math.BigDecimal;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.UUID;
import org.junit.jupiter.api.Test;

class CouponTest {

    @Test
    void applyToOrder_percentageCouponCapsByMaxDiscount() {
        Coupon coupon = percentageCoupon();

        BigDecimal discountAmount = coupon.applyToOrder(new BigDecimal("300.00"), Instant.now());

        assertEquals(new BigDecimal("30.00"), discountAmount);
        assertEquals(1, coupon.getUsedCount());
    }

    @Test
    void applyToOrder_whenBelowMinOrderAmount_rejects() {
        Coupon coupon = percentageCoupon();

        DomainException exception = assertThrows(
                DomainException.class,
                () -> coupon.applyToOrder(new BigDecimal("50.00"), Instant.now())
        );

        assertEquals(DomainErrorCode.COUPON_MIN_ORDER_AMOUNT_NOT_REACHED, exception.getErrorCode());
    }

    @Test
    void rollbackUsage_decreasesUsedCount() {
        Coupon coupon = couponWithUsedCount(1);

        coupon.rollbackUsage(Instant.now());

        assertEquals(0, coupon.getUsedCount());
    }

    private static Coupon percentageCoupon() {
        return couponWithUsedCount(0);
    }

    private static Coupon couponWithUsedCount(int usedCount) {
        Instant now = Instant.now().truncatedTo(ChronoUnit.SECONDS);
        return new Coupon(
                UUID.randomUUID(),
                "SALE10",
                "Sale 10%",
                CouponDiscountType.PERCENTAGE,
                new BigDecimal("10"),
                new BigDecimal("100.00"),
                new BigDecimal("50.00"),
                100,
                usedCount,
                now.minus(1, ChronoUnit.DAYS),
                now.plus(10, ChronoUnit.DAYS),
                true,
                Instant.EPOCH,
                Instant.EPOCH,
                null
        );
    }
}
