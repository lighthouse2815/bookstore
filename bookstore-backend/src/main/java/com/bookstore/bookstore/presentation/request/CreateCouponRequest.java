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
        @NotBlank(message = "code khong duoc de trong")
        String code,
        @Pattern(regexp = "^(?!\\s*$).+", message = "description khong duoc de trong")
        String description,
        @NotNull(message = "couponType khong duoc null")
        CouponType couponType,
        @NotNull(message = "discountType khong duoc null")
        CouponDiscountType discountType,
        @NotNull(message = "discountValue khong duoc null")
        @Positive(message = "discountValue phai lon hon 0")
        BigDecimal discountValue,
        @NotNull(message = "minOrderAmount khong duoc null")
        @DecimalMin(value = "0.0", message = "minOrderAmount khong duoc am")
        BigDecimal minOrderAmount,
        @DecimalMin(value = "0.0", message = "maxDiscountAmount khong duoc am")
        BigDecimal maxDiscountAmount,
        @Positive(message = "maxUsageCount phai lon hon 0")
        Integer maxUsageCount,
        @NotNull(message = "startsAt khong duoc null")
        Instant startsAt,
        @NotNull(message = "expiresAt khong duoc null")
        Instant expiresAt,
        boolean active
) {
}
