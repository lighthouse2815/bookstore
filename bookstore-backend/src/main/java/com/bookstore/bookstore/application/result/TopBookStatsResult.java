package com.bookstore.bookstore.application.result;

import java.math.BigDecimal;
import java.util.UUID;

public record TopBookStatsResult(
        UUID bookId,
        String title,
        long soldQuantity,
        BigDecimal revenue
) {
    public TopBookStatsResult {
        revenue = revenue == null ? BigDecimal.ZERO : revenue;
    }
}
