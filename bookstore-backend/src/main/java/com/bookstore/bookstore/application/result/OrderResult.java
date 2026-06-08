package com.bookstore.bookstore.application.result;

import com.bookstore.bookstore.domain.enums.OrderStatus;
import com.bookstore.bookstore.domain.enums.PaymentMethod;
import com.bookstore.bookstore.domain.enums.PaymentStatus;
import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;
import java.util.UUID;

public record OrderResult(
        UUID orderId,
        UUID userId,
        List<OrderItemResult> items,
        BigDecimal totalAmount,
        BigDecimal discountAmount,
        BigDecimal shippingFee,
        BigDecimal finalAmount,
        UUID couponId,
        String couponCode,
        PaymentMethod paymentMethod,
        PaymentStatus paymentStatus,
        OrderStatus status,
        String receiverName,
        String receiverPhone,
        String receiverAddress,
        Instant createdAt,
        Instant updatedAt,
        Instant cancelledAt
) {
    public OrderResult {
        items = items == null ? List.of() : List.copyOf(items);
        totalAmount = totalAmount == null ? BigDecimal.ZERO : totalAmount;
        discountAmount = discountAmount == null ? BigDecimal.ZERO : discountAmount;
        shippingFee = shippingFee == null ? BigDecimal.ZERO : shippingFee;
        finalAmount = finalAmount == null ? BigDecimal.ZERO : finalAmount;
    }
}
