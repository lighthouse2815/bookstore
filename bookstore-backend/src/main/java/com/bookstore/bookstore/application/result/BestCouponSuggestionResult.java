package com.bookstore.bookstore.application.result;

import com.bookstore.bookstore.domain.enums.CouponType;
import java.math.BigDecimal;

public record BestCouponSuggestionResult(
        boolean available,
        String couponCode,
        CouponType couponType,
        BigDecimal discountAmount,
        BigDecimal finalAmountEstimate,
        String label,
        String reason
) {
    public BestCouponSuggestionResult {
        discountAmount = discountAmount == null ? BigDecimal.ZERO : discountAmount;
        finalAmountEstimate = finalAmountEstimate == null ? BigDecimal.ZERO : finalAmountEstimate;
    }

    public static BestCouponSuggestionResult unavailable(String reason) {
        return new BestCouponSuggestionResult(
                false,
                null,
                null,
                BigDecimal.ZERO,
                BigDecimal.ZERO,
                null,
                reason
        );
    }
}
