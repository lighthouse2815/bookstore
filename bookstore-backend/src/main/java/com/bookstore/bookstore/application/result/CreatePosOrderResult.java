package com.bookstore.bookstore.application.result;

import com.bookstore.bookstore.domain.enums.OrderStatus;
import com.bookstore.bookstore.domain.enums.PaymentMethod;
import com.bookstore.bookstore.domain.enums.PaymentStatus;
import java.math.BigDecimal;
import java.util.UUID;

public record CreatePosOrderResult(
        UUID orderId,
        String orderCode,
        BigDecimal totalAmount,
        BigDecimal discountAmount,
        BigDecimal finalAmount,
        PaymentMethod paymentMethod,
        PaymentStatus paymentStatus,
        OrderStatus orderStatus
) {
}
