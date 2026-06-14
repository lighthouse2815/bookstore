package com.bookstore.bookstore.application.result;

import com.bookstore.bookstore.domain.enums.PaymentMethod;
import com.bookstore.bookstore.domain.enums.PaymentStatus;
import java.math.BigDecimal;
import java.util.UUID;

public record CreateOrderResult(
        UUID orderId,
        String orderCode,
        PaymentMethod paymentMethod,
        PaymentStatus paymentStatus,
        BigDecimal totalAmount,
        String transferContent
) {
    public CreateOrderResult {
        totalAmount = totalAmount == null ? BigDecimal.ZERO : totalAmount;
    }
}
