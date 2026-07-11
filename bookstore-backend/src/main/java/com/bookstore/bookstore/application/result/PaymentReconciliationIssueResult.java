package com.bookstore.bookstore.application.result;

import com.bookstore.bookstore.domain.enums.PaymentReconciliationIssueType;
import com.bookstore.bookstore.domain.enums.PaymentReconciliationStatus;
import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

public record PaymentReconciliationIssueResult(
        UUID id, UUID paymentId, UUID orderId, PaymentReconciliationIssueType issueType,
        BigDecimal expectedAmount, BigDecimal receivedAmount, String externalTransactionId,
        String details, PaymentReconciliationStatus status, Instant detectedAt, Instant resolvedAt,
        UUID resolvedBy, String resolutionNote, Instant createdAt, Instant updatedAt
) {
}
