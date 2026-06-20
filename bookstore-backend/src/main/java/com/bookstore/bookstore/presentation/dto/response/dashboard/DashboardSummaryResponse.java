package com.bookstore.bookstore.presentation.dto.response.dashboard;

import java.math.BigDecimal;

public record DashboardSummaryResponse(
        BigDecimal todayRevenue,
        BigDecimal monthRevenue,
        long todayOrders,
        long pendingOrders,
        long lowStockBooks,
        long newCustomers,
        long newReviews,
        long activeCoupons
) {
}
