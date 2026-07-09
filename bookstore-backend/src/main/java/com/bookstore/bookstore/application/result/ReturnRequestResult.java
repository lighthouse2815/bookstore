package com.bookstore.bookstore.application.result;

import com.bookstore.bookstore.domain.enums.OrderStatus;
import com.bookstore.bookstore.domain.enums.PaymentMethod;
import com.bookstore.bookstore.domain.enums.PaymentStatus;
import com.bookstore.bookstore.domain.enums.ReturnRequestStatus;
import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

public record ReturnRequestResult(
        UUID id,
        UUID orderId,
        String orderCode,
        UUID userId,
        String username,
        String userEmail,
        String receiverName,
        String reason,
        ReturnRequestStatus status,
        BigDecimal requestedRefundAmount,
        BigDecimal approvedRefundAmount,
        String adminNote,
        UUID processedBy,
        String processedByName,
        Instant processedAt,
        OrderStatus orderStatus,
        PaymentMethod paymentMethod,
        PaymentStatus paymentStatus,
        BigDecimal orderFinalAmount,
        Instant orderCreatedAt,
        Instant createdAt,
        Instant updatedAt
) {
}
