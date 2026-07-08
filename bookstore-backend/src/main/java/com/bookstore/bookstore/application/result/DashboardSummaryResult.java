package com.bookstore.bookstore.application.result;

import java.math.BigDecimal;

public record DashboardSummaryResult(
        BigDecimal totalRevenue,
        BigDecimal todayRevenue,
        BigDecimal monthRevenue,
        long totalOrders,
        long todayOrders,
        long pendingOrders,
        long deliveredOrders,
        long cancelledOrders,
        long totalUsers,
        long totalBooks,
        long lowStockBooks,
        long newCustomers,
        long newReviews,
        long activeCoupons
) {
    public DashboardSummaryResult {
        totalRevenue = totalRevenue == null ? BigDecimal.ZERO : totalRevenue;
        todayRevenue = todayRevenue == null ? BigDecimal.ZERO : todayRevenue;
        monthRevenue = monthRevenue == null ? BigDecimal.ZERO : monthRevenue;
    }
}
