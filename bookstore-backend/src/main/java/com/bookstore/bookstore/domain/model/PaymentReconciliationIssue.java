package com.bookstore.bookstore.domain.model;

import com.bookstore.bookstore.domain.enums.PaymentReconciliationIssueType;
import com.bookstore.bookstore.domain.enums.PaymentReconciliationStatus;
import com.bookstore.bookstore.domain.exception.DomainErrorCode;
import com.bookstore.bookstore.domain.rule.PaymentReconciliationIssueRule;
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
        this.id = Guard.notNull(id, DomainErrorCode.INVALID_PAYMENT_RECONCILIATION_ID, "id");
        this.paymentId = Guard.notNull(
                paymentId,
                DomainErrorCode.INVALID_PAYMENT_RECONCILIATION_PAYMENT_ID,
                "paymentId"
        );
        this.orderId = Guard.notNull(
                orderId,
                DomainErrorCode.INVALID_PAYMENT_RECONCILIATION_ORDER_ID,
                "orderId"
        );
        this.issueType = Guard.notNull(
                issueType,
                DomainErrorCode.INVALID_PAYMENT_RECONCILIATION_ISSUE_TYPE,
                "issueType"
        );
        this.expectedAmount = Guard.notNull(
                expectedAmount,
                DomainErrorCode.INVALID_PAYMENT_RECONCILIATION_AMOUNT,
                "expectedAmount"
        );
        this.receivedAmount = Guard.notNull(
                receivedAmount,
                DomainErrorCode.INVALID_PAYMENT_RECONCILIATION_AMOUNT,
                "receivedAmount"
        );
        PaymentReconciliationIssueRule.requireNonNegativeAmount(this.expectedAmount, "expectedAmount");
        PaymentReconciliationIssueRule.requireNonNegativeAmount(this.receivedAmount, "receivedAmount");
        this.externalTransactionId = Guard.notBlankOrNull(
                externalTransactionId,
                DomainErrorCode.INVALID_PAYMENT_RECONCILIATION_EXTERNAL_TRANSACTION_ID,
                "externalTransactionId"
        );
        this.deduplicationKey = Guard.notBlank(
                deduplicationKey,
                DomainErrorCode.INVALID_PAYMENT_RECONCILIATION_DEDUPLICATION_KEY,
                "deduplicationKey"
        );
        this.details = Guard.notBlankOrNull(
                details,
                DomainErrorCode.INVALID_PAYMENT_RECONCILIATION_DETAILS,
                "details"
        );
        this.status = Guard.notNull(
                status,
                DomainErrorCode.INVALID_PAYMENT_RECONCILIATION_STATUS,
                "status"
        );
        this.detectedAt = Guard.notInFuture(
                detectedAt,
                DomainErrorCode.INVALID_PAYMENT_RECONCILIATION_DETECTED_AT,
                "detectedAt"
        );
        this.resolvedAt = Guard.notInFutureOrNull(
                resolvedAt,
                DomainErrorCode.INVALID_PAYMENT_RECONCILIATION_RESOLVED_AT,
                "resolvedAt"
        );
        this.resolvedBy = resolvedBy;
        this.resolutionNote = Guard.notBlankOrNull(
                resolutionNote,
                DomainErrorCode.INVALID_PAYMENT_RECONCILIATION_RESOLUTION_NOTE,
                "resolutionNote"
        );
        this.createdAt = Guard.notInFuture(
                createdAt,
                DomainErrorCode.INVALID_PAYMENT_RECONCILIATION_CREATED_AT,
                "createdAt"
        );
        this.updatedAt = Guard.notInFuture(
                updatedAt,
                DomainErrorCode.INVALID_PAYMENT_RECONCILIATION_UPDATED_AT,
                "updatedAt"
        );
        Guard.notBefore(
                this.resolvedAt,
                this.detectedAt,
                DomainErrorCode.INVALID_PAYMENT_RECONCILIATION_AUDIT_ORDER,
                "resolvedAt",
                "detectedAt"
        );
        Guard.notBefore(
                this.updatedAt,
                this.createdAt,
                DomainErrorCode.INVALID_PAYMENT_RECONCILIATION_AUDIT_ORDER,
                "updatedAt",
                "createdAt"
        );
    }

    public void resolve(UUID resolverId, String note, Instant now) {
        PaymentReconciliationIssueRule.requireOpen(status);
        UUID validResolverId = Guard.notNull(
                resolverId,
                DomainErrorCode.INVALID_PAYMENT_RECONCILIATION_RESOLVED_BY,
                "resolverId"
        );
        String validNote = Guard.notBlank(
                note,
                DomainErrorCode.INVALID_PAYMENT_RECONCILIATION_RESOLUTION_NOTE,
                "resolutionNote"
        );
        Instant resolvedAt = Guard.notInFuture(
                now,
                DomainErrorCode.INVALID_PAYMENT_RECONCILIATION_RESOLVED_AT,
                "resolvedAt"
        );
        Guard.notBefore(
                resolvedAt,
                detectedAt,
                DomainErrorCode.INVALID_PAYMENT_RECONCILIATION_AUDIT_ORDER,
                "resolvedAt",
                "detectedAt"
        );
        this.status = PaymentReconciliationStatus.RESOLVED;
        this.resolvedBy = validResolverId;
        this.resolutionNote = validNote;
        this.resolvedAt = resolvedAt;
        this.updatedAt = resolvedAt;
    }
}
