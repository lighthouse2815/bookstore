package com.bookstore.bookstore.domain.model;

import com.bookstore.bookstore.domain.enums.RefundMethod;
import com.bookstore.bookstore.domain.enums.RefundStatus;
import com.bookstore.bookstore.domain.exception.DomainErrorCode;
import com.bookstore.bookstore.domain.rule.RefundRule;
import com.bookstore.bookstore.domain.validation.Guard;
import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;
import lombok.Getter;

@Getter
public class Refund {

    private final UUID id;
    private final UUID orderId;
    private final UUID paymentId;
    private final UUID returnRequestId;
    private final BigDecimal amount;
    private final String currency;
    private final String reason;
    private final RefundMethod method;
    private final String idempotencyKey;
    private final UUID requestedBy;
    private RefundStatus status;
    private String externalReference;
    private String evidenceUrl;
    private String evidenceMetadata;
    private UUID approvedBy;
    private UUID processedBy;
    private Instant requestedAt;
    private Instant approvedAt;
    private Instant processedAt;
    private String failureReason;
    private final Instant createdAt;
    private Instant updatedAt;
    private long version;

    public Refund(
            UUID id,
            UUID orderId,
            UUID paymentId,
            UUID returnRequestId,
            BigDecimal amount,
            String currency,
            String reason,
            RefundMethod method,
            RefundStatus status,
            String externalReference,
            String evidenceUrl,
            String evidenceMetadata,
            String idempotencyKey,
            UUID requestedBy,
            UUID approvedBy,
            UUID processedBy,
            Instant requestedAt,
            Instant approvedAt,
            Instant processedAt,
            String failureReason,
            Instant createdAt,
            Instant updatedAt,
            long version
    ) {
        this.id = Guard.notNull(id, DomainErrorCode.INVALID_REFUND_ID, "id");
        this.orderId = Guard.notNull(orderId, DomainErrorCode.INVALID_REFUND_ORDER_ID, "orderId");
        this.paymentId = Guard.notNull(paymentId, DomainErrorCode.INVALID_REFUND_PAYMENT_ID, "paymentId");
        this.returnRequestId = returnRequestId;
        this.amount = Guard.notNull(amount, DomainErrorCode.INVALID_REFUND_AMOUNT, "amount");
        RefundRule.requirePositiveAmount(this.amount);
        this.currency = Guard.notBlank(currency, DomainErrorCode.INVALID_REFUND_CURRENCY, "currency");
        this.reason = Guard.notBlank(reason, DomainErrorCode.INVALID_REFUND_REASON, "reason");
        this.method = Guard.notNull(method, DomainErrorCode.INVALID_REFUND_METHOD, "method");
        this.status = Guard.notNull(status, DomainErrorCode.INVALID_REFUND_STATUS, "status");
        this.externalReference = trim(externalReference);
        this.evidenceUrl = trim(evidenceUrl);
        this.evidenceMetadata = trim(evidenceMetadata);
        this.idempotencyKey = Guard.notBlank(
                idempotencyKey,
                DomainErrorCode.INVALID_REFUND_IDEMPOTENCY_KEY,
                "idempotencyKey"
        );
        this.requestedBy = Guard.notNull(
                requestedBy,
                DomainErrorCode.INVALID_REFUND_REQUESTED_BY,
                "requestedBy"
        );
        this.approvedBy = approvedBy;
        this.processedBy = processedBy;
        this.requestedAt = Guard.notNull(
                requestedAt,
                DomainErrorCode.INVALID_REFUND_REQUESTED_AT,
                "requestedAt"
        );
        this.approvedAt = approvedAt;
        this.processedAt = processedAt;
        this.failureReason = trim(failureReason);
        this.createdAt = Guard.notNull(createdAt, DomainErrorCode.INVALID_REFUND_CREATED_AT, "createdAt");
        this.updatedAt = Guard.notNull(updatedAt, DomainErrorCode.INVALID_REFUND_UPDATED_AT, "updatedAt");
        this.version = version;
    }

    public void approve(UUID actorId, Instant now) {
        UUID actor = Guard.notNull(actorId, DomainErrorCode.INVALID_REFUND_APPROVED_BY, "approvedBy");
        transitionTo(RefundStatus.APPROVED, now);
        approvedBy = actor;
        approvedAt = now;
    }

    public void startProcessing(UUID actorId, Instant now) {
        UUID actor = Guard.notNull(actorId, DomainErrorCode.INVALID_REFUND_PROCESSED_BY, "processedBy");
        transitionTo(RefundStatus.PROCESSING, now);
        processedBy = actor;
        failureReason = null;
    }

    public void succeed(UUID actorId, String reference, String url, String metadata, Instant now) {
        UUID actor = Guard.notNull(actorId, DomainErrorCode.INVALID_REFUND_PROCESSED_BY, "processedBy");
        String normalizedReference = Guard.notBlank(
                reference,
                DomainErrorCode.INVALID_REFUND_EXTERNAL_REFERENCE,
                "externalReference"
        );
        String normalizedEvidenceUrl = trim(url);
        String normalizedEvidenceMetadata = trim(metadata);
        RefundRule.requireEvidence(normalizedEvidenceUrl, normalizedEvidenceMetadata);
        transitionTo(RefundStatus.SUCCEEDED, now);
        processedBy = actor;
        externalReference = normalizedReference;
        evidenceUrl = normalizedEvidenceUrl;
        evidenceMetadata = normalizedEvidenceMetadata;
        processedAt = now;
        failureReason = null;
    }

    public void fail(UUID actorId, String reason, Instant now) {
        UUID actor = Guard.notNull(actorId, DomainErrorCode.INVALID_REFUND_PROCESSED_BY, "processedBy");
        String normalizedReason = Guard.notBlank(
                reason,
                DomainErrorCode.INVALID_REFUND_FAILURE_REASON,
                "failureReason"
        );
        transitionTo(RefundStatus.FAILED, now);
        processedBy = actor;
        failureReason = normalizedReason;
        processedAt = now;
    }

    public void cancel(UUID actorId, String reason, Instant now) {
        UUID actor = Guard.notNull(actorId, DomainErrorCode.INVALID_REFUND_PROCESSED_BY, "processedBy");
        transitionTo(RefundStatus.CANCELLED, now);
        processedBy = actor;
        failureReason = trim(reason);
        processedAt = now;
    }

    private void transitionTo(RefundStatus target, Instant now) {
        Instant validUpdatedAt = Guard.notNull(
                now,
                DomainErrorCode.INVALID_REFUND_UPDATED_AT,
                "updatedAt"
        );
        RefundRule.requireCanTransition(status, target);
        status = target;
        updatedAt = validUpdatedAt;
    }

    private static String trim(String value) {
        if (value == null) {
            return null;
        }
        String normalized = value.trim();
        return normalized.isEmpty() ? null : normalized;
    }
}
