package com.bookstore.bookstore.application.result;

import com.bookstore.bookstore.domain.enums.OrderStatus;
import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

public record RecentOrderResult(
        UUID orderId,
        String orderCode,
        String customerName,
        BigDecimal finalAmount,
        OrderStatus status,
        Instant createdAt
) {
    public RecentOrderResult {
        finalAmount = finalAmount == null ? BigDecimal.ZERO : finalAmount;
    }
}
