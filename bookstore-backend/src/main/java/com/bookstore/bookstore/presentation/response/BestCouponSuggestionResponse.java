package com.bookstore.bookstore.presentation.response;

import com.bookstore.bookstore.domain.enums.CouponType;
import java.math.BigDecimal;

public record BestCouponSuggestionResponse(
        boolean available,
        String couponCode,
        CouponType couponType,
        BigDecimal discountAmount,
        BigDecimal finalAmountEstimate,
        String label,
        String reason
) {
}
