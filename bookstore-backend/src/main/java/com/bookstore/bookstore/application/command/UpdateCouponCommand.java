package com.bookstore.bookstore.application.command;

import com.bookstore.bookstore.domain.enums.CouponDiscountType;
import com.bookstore.bookstore.domain.enums.CouponType;
import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

public record UpdateCouponCommand(
        UUID couponId,
        String code,
        String description,
        CouponType couponType,
        CouponDiscountType discountType,
        BigDecimal discountValue,
        BigDecimal minOrderAmount,
        BigDecimal maxDiscountAmount,
        Integer maxUsageCount,
        Instant startsAt,
        Instant expiresAt,
        boolean active
) {
    public UpdateCouponCommand(
            UUID couponId,
            String code,
            String description,
            CouponDiscountType discountType,
            BigDecimal discountValue,
            BigDecimal minOrderAmount,
            BigDecimal maxDiscountAmount,
            Integer maxUsageCount,
            Instant startsAt,
            Instant expiresAt,
            boolean active
    ) {
        this(
                couponId,
                code,
                description,
                CouponType.BOOK,
                discountType,
                discountValue,
                minOrderAmount,
                maxDiscountAmount,
                maxUsageCount,
                startsAt,
                expiresAt,
                active
        );
    }
}
