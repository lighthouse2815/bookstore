package com.bookstore.bookstore.presentation.dto.response.dashboard;

import java.math.BigDecimal;

public record RevenueChartResponse(
        String label,
        BigDecimal revenue,
        long orders
) {
}
