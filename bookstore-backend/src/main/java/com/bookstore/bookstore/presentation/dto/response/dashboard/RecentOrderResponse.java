package com.bookstore.bookstore.presentation.dto.response.dashboard;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

public record RecentOrderResponse(
        UUID orderId,
        String orderCode,
        String customerName,
        BigDecimal finalAmount,
        String status,
        Instant createdAt
) {
}
