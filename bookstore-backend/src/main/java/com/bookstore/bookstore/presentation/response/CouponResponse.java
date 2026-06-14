package com.bookstore.bookstore.presentation.response;

import com.bookstore.bookstore.domain.enums.CouponDiscountType;
import com.bookstore.bookstore.domain.enums.CouponType;
import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

public record CouponResponse(
        UUID id,
        String code,
        String description,
        CouponType couponType,
        CouponDiscountType discountType,
        BigDecimal discountValue,
        BigDecimal minOrderAmount,
        BigDecimal maxDiscountAmount,
        Integer maxUsageCount,
        Integer usedCount,
        Instant startsAt,
        Instant expiresAt,
        boolean active,
        Instant createdAt,
        Instant updatedAt
) {
}
