package com.bookstore.bookstore.application.result;

import com.bookstore.bookstore.domain.enums.PaymentProvider;
import com.bookstore.bookstore.domain.enums.PaymentStatus;
import com.bookstore.bookstore.domain.enums.RefundMethod;
import com.bookstore.bookstore.domain.enums.RefundStatus;
import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

public record RefundResult(
        UUID id, UUID orderId, String orderCode, UUID paymentId, PaymentProvider paymentProvider, PaymentStatus paymentStatus,
        BigDecimal paidAmount, UUID returnRequestId, BigDecimal amount, String currency, String reason, RefundMethod method,
        RefundStatus status, String externalReference, String evidenceUrl, String evidenceMetadata, UUID requestedBy, UUID approvedBy,
        UUID processedBy, Instant requestedAt, Instant approvedAt, Instant processedAt, String failureReason,
        Instant createdAt, Instant updatedAt
) {
}
