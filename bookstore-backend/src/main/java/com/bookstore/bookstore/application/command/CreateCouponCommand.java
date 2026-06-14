package com.bookstore.bookstore.application.command;

import com.bookstore.bookstore.domain.enums.CouponDiscountType;
import com.bookstore.bookstore.domain.enums.CouponType;
import java.math.BigDecimal;
import java.time.Instant;

public record CreateCouponCommand(
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
    public CreateCouponCommand(
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
