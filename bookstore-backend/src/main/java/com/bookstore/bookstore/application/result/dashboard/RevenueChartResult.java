package com.bookstore.bookstore.application.result.dashboard;

import java.math.BigDecimal;

public record RevenueChartResult(
        String label,
        BigDecimal revenue,
        long orders
) {
    public RevenueChartResult {
        revenue = revenue == null ? BigDecimal.ZERO : revenue;
    }
}
