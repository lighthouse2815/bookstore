package com.bookstore.bookstore.application.result;

import java.math.BigDecimal;
import java.time.LocalDate;

public record RevenueReportRowResult(
        LocalDate date,
        long totalOrders,
        BigDecimal revenue,
        long cancelledOrders
) {
    public RevenueReportRowResult {
        revenue = revenue == null ? BigDecimal.ZERO : revenue;
    }
}
