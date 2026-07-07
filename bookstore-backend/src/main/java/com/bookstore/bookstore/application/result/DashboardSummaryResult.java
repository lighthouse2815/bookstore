package com.bookstore.bookstore.application.result;

import java.math.BigDecimal;

public record DashboardSummaryResult(
        BigDecimal todayRevenue,
        BigDecimal monthRevenue,
        long todayOrders,
        long pendingOrders,
        long lowStockBooks,
        long newCustomers,
        long newReviews,
        long activeCoupons
) {
    public DashboardSummaryResult {
        todayRevenue = todayRevenue == null ? BigDecimal.ZERO : todayRevenue;
        monthRevenue = monthRevenue == null ? BigDecimal.ZERO : monthRevenue;
    }
}
