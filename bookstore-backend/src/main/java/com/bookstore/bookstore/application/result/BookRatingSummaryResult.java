package com.bookstore.bookstore.application.result;

import java.math.BigDecimal;
import java.util.Map;

public record BookRatingSummaryResult(
        BigDecimal averageRating,
        long reviewCount,
        Map<Integer, Long> starBreakdown
) {
}
