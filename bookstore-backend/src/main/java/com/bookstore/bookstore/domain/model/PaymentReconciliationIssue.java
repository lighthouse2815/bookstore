package com.bookstore.bookstore.domain.model;

import com.bookstore.bookstore.domain.enums.PaymentReconciliationIssueType;
import com.bookstore.bookstore.domain.enums.PaymentReconciliationStatus;
import com.bookstore.bookstore.domain.exception.DomainErrorCode;
import com.bookstore.bookstore.domain.validation.Guard;
import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;
import lombok.Getter;

@Getter
public class PaymentReconciliationIssue {

    private final UUID id;
    private final UUID paymentId;
    private final UUID orderId;
    private final PaymentReconciliationIssueType issueType;
    private final BigDecimal expectedAmount;
    private final BigDecimal receivedAmount;
    private final String externalTransactionId;
    private final String deduplicationKey;
    private final String details;
    private PaymentReconciliationStatus status;
    private Instant detectedAt;
    private Instant resolvedAt;
    private UUID resolvedBy;
    private String resolutionNote;
    private Instant createdAt;
    private Instant updatedAt;

    public PaymentReconciliationIssue(
            UUID id, UUID paymentId, UUID orderId, PaymentReconciliationIssueType issueType,
            BigDecimal expectedAmount, BigDecimal receivedAmount, String externalTransactionId,
            String deduplicationKey, String details, PaymentReconciliationStatus status,
            Instant detectedAt, Instant resolvedAt, UUID resolvedBy, String resolutionNote,
            Instant createdAt, Instant updatedAt
    ) {
        this.id = Guard.notNull(id, DomainErrorCode.INVALID_PAYMENT_ID, "id");
        this.paymentId = Guard.notNull(paymentId, DomainErrorCode.INVALID_PAYMENT_ID, "paymentId");
        this.orderId = Guard.notNull(orderId, DomainErrorCode.INVALID_PAYMENT_ORDER_ID, "orderId");
        this.issueType = Guard.notNull(issueType, DomainErrorCode.INVALID_PAYMENT_STATUS, "issueType");
        this.expectedAmount = Guard.notNull(expectedAmount, DomainErrorCode.INVALID_PAYMENT_AMOUNT, "expectedAmount");
        this.receivedAmount = Guard.notNull(receivedAmount, DomainErrorCode.INVALID_PAYMENT_AMOUNT, "receivedAmount");
        this.externalTransactionId = Guard.notBlankOrNull(externalTransactionId, DomainErrorCode.INVALID_PAYMENT_TRANSACTION_ID, "externalTransactionId");
        this.deduplicationKey = Guard.notBlank(deduplicationKey, DomainErrorCode.INVALID_PAYMENT_REFERENCE_CODE, "deduplicationKey");
        this.details = Guard.notBlankOrNull(details, DomainErrorCode.INVALID_PAYMENT_GATEWAY, "details");
        this.status = Guard.notNull(status, DomainErrorCode.INVALID_PAYMENT_STATUS, "status");
        this.detectedAt = Guard.notInFuture(detectedAt, DomainErrorCode.INVALID_PAYMENT_CREATED_AT, "detectedAt");
        this.resolvedAt = Guard.notInFutureOrNull(resolvedAt, DomainErrorCode.INVALID_PAYMENT_UPDATED_AT, "resolvedAt");
        this.resolvedBy = resolvedBy;
        this.resolutionNote = Guard.notBlankOrNull(resolutionNote, DomainErrorCode.INVALID_PAYMENT_GATEWAY, "resolutionNote");
        this.createdAt = Guard.notInFuture(createdAt, DomainErrorCode.INVALID_PAYMENT_CREATED_AT, "createdAt");
        this.updatedAt = Guard.notInFuture(updatedAt, DomainErrorCode.INVALID_PAYMENT_UPDATED_AT, "updatedAt");
    }

    public void resolve(UUID resolverId, String note, Instant now) {
        this.status = PaymentReconciliationStatus.RESOLVED;
        this.resolvedBy = Guard.notNull(resolverId, DomainErrorCode.INVALID_USER_ID, "resolverId");
        this.resolutionNote = Guard.notBlank(note, DomainErrorCode.INVALID_PAYMENT_GATEWAY, "resolutionNote");
        this.resolvedAt = now;
        this.updatedAt = now;
    }
}
