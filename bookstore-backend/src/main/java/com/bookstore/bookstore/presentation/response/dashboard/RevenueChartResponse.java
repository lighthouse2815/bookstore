package com.bookstore.bookstore.presentation.response.dashboard;

import java.math.BigDecimal;

public record RevenueChartResponse(
        String label,
        BigDecimal revenue,
        long orders
) {
}
