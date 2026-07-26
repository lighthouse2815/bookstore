package com.bookstore.bookstore.domain.model;

import com.bookstore.bookstore.domain.enums.RefundMethod;
import com.bookstore.bookstore.domain.enums.RefundStatus;
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
        this.id = require(id, "id");
        this.orderId = require(orderId, "orderId");
        this.paymentId = require(paymentId, "paymentId");
        this.returnRequestId = returnRequestId;
        this.amount = require(amount, "amount");
        if (amount.signum() <= 0) {
            throw new IllegalArgumentException("amount must be positive");
        }
        this.currency = requireText(currency, "currency");
        this.reason = requireText(reason, "reason");
        this.method = require(method, "method");
        this.status = require(status, "status");
        this.externalReference = trim(externalReference);
        this.evidenceUrl = trim(evidenceUrl);
        this.evidenceMetadata = trim(evidenceMetadata);
        this.idempotencyKey = requireText(idempotencyKey, "idempotencyKey");
        this.requestedBy = require(requestedBy, "requestedBy");
        this.approvedBy = approvedBy;
        this.processedBy = processedBy;
        this.requestedAt = require(requestedAt, "requestedAt");
        this.approvedAt = approvedAt;
        this.processedAt = processedAt;
        this.failureReason = trim(failureReason);
        this.createdAt = require(createdAt, "createdAt");
        this.updatedAt = require(updatedAt, "updatedAt");
        this.version = version;
    }

    public void approve(UUID actorId, Instant now) {
        transitionTo(RefundStatus.APPROVED, now);
        approvedBy = require(actorId, "approvedBy");
        approvedAt = now;
    }

    public void startProcessing(UUID actorId, Instant now) {
        transitionTo(RefundStatus.PROCESSING, now);
        processedBy = require(actorId, "processedBy");
        failureReason = null;
    }

    public void succeed(UUID actorId, String reference, String url, String metadata, Instant now) {
        UUID actor = require(actorId, "processedBy");
        String normalizedReference = requireText(reference, "externalReference");
        String normalizedEvidenceUrl = trim(url);
        String normalizedEvidenceMetadata = trim(metadata);
        if (normalizedEvidenceUrl == null && normalizedEvidenceMetadata == null) {
            throw new IllegalArgumentException("evidence is required");
        }
        transitionTo(RefundStatus.SUCCEEDED, now);
        processedBy = actor;
        externalReference = normalizedReference;
        evidenceUrl = normalizedEvidenceUrl;
        evidenceMetadata = normalizedEvidenceMetadata;
        processedAt = now;
        failureReason = null;
    }

    public void fail(UUID actorId, String reason, Instant now) {
        transitionTo(RefundStatus.FAILED, now);
        processedBy = require(actorId, "processedBy");
        failureReason = requireText(reason, "failureReason");
        processedAt = now;
    }

    public void cancel(UUID actorId, String reason, Instant now) {
        transitionTo(RefundStatus.CANCELLED, now);
        processedBy = require(actorId, "processedBy");
        failureReason = trim(reason);
        processedAt = now;
    }

    private void transitionTo(RefundStatus target, Instant now) {
        if (!status.canTransitionTo(target)) {
            throw new IllegalStateException("invalid refund transition: " + status + " -> " + target);
        }
        status = target;
        updatedAt = require(now, "updatedAt");
    }

    private static <T> T require(T value, String field) {
        if (value == null) {
            throw new IllegalArgumentException(field + " is required");
        }
        return value;
    }

    private static String requireText(String value, String field) {
        String normalized = trim(value);
        if (normalized == null) {
            throw new IllegalArgumentException(field + " is required");
        }
        return normalized;
    }

    private static String trim(String value) {
        if (value == null) {
            return null;
        }
        String normalized = value.trim();
        return normalized.isEmpty() ? null : normalized;
    }
}
