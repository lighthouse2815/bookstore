package com.bookstore.bookstore.application.result;

import com.bookstore.bookstore.domain.enums.OrderStatus;
import com.bookstore.bookstore.domain.enums.PaymentStatus;
import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

public record OrderReportRowResult(
        UUID orderId,
        String orderCode,
        String customerName,
        OrderStatus status,
        PaymentStatus paymentStatus,
        BigDecimal finalAmount,
        Instant createdAt
) {
    public OrderReportRowResult {
        finalAmount = finalAmount == null ? BigDecimal.ZERO : finalAmount;
    }
}
