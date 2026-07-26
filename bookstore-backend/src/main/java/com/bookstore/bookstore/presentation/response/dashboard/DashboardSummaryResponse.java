package com.bookstore.bookstore.presentation.response.dashboard;

import java.math.BigDecimal;

public record DashboardSummaryResponse(
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
}
