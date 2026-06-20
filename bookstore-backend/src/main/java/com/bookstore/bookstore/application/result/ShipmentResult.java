package com.bookstore.bookstore.application.result;

import com.bookstore.bookstore.domain.enums.OrderStatus;
import com.bookstore.bookstore.domain.enums.PaymentMethod;
import com.bookstore.bookstore.domain.enums.PaymentStatus;
import com.bookstore.bookstore.domain.enums.ShipmentStatus;
import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

public record ShipmentResult(
        UUID shipmentId,
        UUID orderId,
        String orderCode,
        UUID shipperId,
        PaymentMethod paymentMethod,
        PaymentStatus paymentStatus,
        OrderStatus orderStatus,
        ShipmentStatus shipmentStatus,
        BigDecimal totalAmount,
        BigDecimal finalAmount,
        String receiverName,
        String receiverPhone,
        String receiverAddress,
        String failureReason,
        Instant assignedAt,
        Instant updatedAt,
        Instant pickedUpAt,
        Instant deliveringAt,
        Instant deliveredAt,
        Instant failedAt
) {
}
