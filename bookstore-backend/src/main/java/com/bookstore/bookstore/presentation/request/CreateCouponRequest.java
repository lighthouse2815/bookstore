package com.bookstore.bookstore.presentation.request;

import com.bookstore.bookstore.domain.enums.CouponDiscountType;
import com.bookstore.bookstore.domain.enums.CouponType;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Positive;
import java.math.BigDecimal;
import java.time.Instant;

public record CreateCouponRequest(
        @NotBlank(message = "code không được để trống")
        String code,
        @Pattern(regexp = "^(?!\\s*$).+", message = "description không được để trống")
        String description,
        @NotNull(message = "couponType không được null")
        CouponType couponType,
        @NotNull(message = "discountType không được null")
        CouponDiscountType discountType,
        @NotNull(message = "discountValue không được null")
        @Positive(message = "discountValue phải lớn hơn 0")
        BigDecimal discountValue,
        @NotNull(message = "minOrderAmount không được null")
        @DecimalMin(value = "0.0", message = "minOrderAmount không được âm")
        BigDecimal minOrderAmount,
        @DecimalMin(value = "0.0", message = "maxDiscountAmount không được âm")
        BigDecimal maxDiscountAmount,
        @Positive(message = "maxUsageCount phải lớn hơn 0")
        Integer maxUsageCount,
        @NotNull(message = "startsAt không được null")
        Instant startsAt,
        @NotNull(message = "expiresAt không được null")
        Instant expiresAt,
        boolean active
) {
}

