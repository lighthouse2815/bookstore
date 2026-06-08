package com.bookstore.bookstore.application.command;

import com.bookstore.bookstore.domain.enums.CouponDiscountType;
import java.math.BigDecimal;
import java.time.Instant;

public record CreateCouponCommand(
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
}
