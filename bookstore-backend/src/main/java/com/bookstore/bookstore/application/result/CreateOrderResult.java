package com.bookstore.bookstore.application.result;

import com.bookstore.bookstore.domain.enums.PaymentMethod;
import com.bookstore.bookstore.domain.enums.PaymentStatus;
import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

public record CreateOrderResult(
        UUID orderId,
        String orderCode,
        PaymentMethod paymentMethod,
        PaymentStatus paymentStatus,
        BigDecimal totalAmount,
        String transferContent,
        Instant paymentExpiresAt
) {
    public CreateOrderResult(
            UUID orderId,
            String orderCode,
            PaymentMethod paymentMethod,
            PaymentStatus paymentStatus,
            BigDecimal totalAmount,
            String transferContent
    ) {
        this(orderId, orderCode, paymentMethod, paymentStatus, totalAmount, transferContent, null);
    }
    public CreateOrderResult {
        totalAmount = totalAmount == null ? BigDecimal.ZERO : totalAmount;
    }
}
