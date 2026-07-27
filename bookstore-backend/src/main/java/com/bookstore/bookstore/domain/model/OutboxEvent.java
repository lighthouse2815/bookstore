package com.bookstore.bookstore.domain.model;

import com.bookstore.bookstore.domain.enums.OutboxStatus;
import com.bookstore.bookstore.domain.exception.DomainErrorCode;
import com.bookstore.bookstore.domain.rule.OutboxEventRule;
import com.bookstore.bookstore.domain.validation.Guard;
import java.time.Duration;
import java.time.Instant;
import java.util.UUID;
import lombok.Getter;

@Getter
public class OutboxEvent {

    private final UUID id;
    private final String aggregateType;
    private final UUID aggregateId;
    private final String eventType;
    private final String payload;
    private final String deduplicationKey;
    private OutboxStatus status;
    private int attemptCount;
    private Instant nextAttemptAt;
    private Instant lockedAt;
    private String lockedBy;
    private String lastError;
    private final Instant createdAt;
    private Instant processedAt;
    private Instant updatedAt;
    private long version;

    public OutboxEvent(
            UUID id, String aggregateType, UUID aggregateId, String eventType, String payload,
            String deduplicationKey, OutboxStatus status, int attemptCount, Instant nextAttemptAt,
            Instant lockedAt, String lockedBy, String lastError, Instant createdAt, Instant processedAt,
            Instant updatedAt, long version
    ) {
        this.id = Guard.notNull(id, DomainErrorCode.INVALID_OUTBOX_EVENT_ID, "id");
        this.aggregateType = Guard.notBlank(
                aggregateType,
                DomainErrorCode.INVALID_OUTBOX_AGGREGATE_TYPE,
                "aggregateType"
        );
        this.aggregateId = Guard.notNull(
                aggregateId,
                DomainErrorCode.INVALID_OUTBOX_AGGREGATE_ID,
                "aggregateId"
        );
        this.eventType = Guard.notBlank(eventType, DomainErrorCode.INVALID_OUTBOX_EVENT_TYPE, "eventType");
        this.payload = Guard.notBlank(payload, DomainErrorCode.INVALID_OUTBOX_PAYLOAD, "payload");
        this.deduplicationKey = Guard.notBlank(
                deduplicationKey,
                DomainErrorCode.INVALID_OUTBOX_DEDUPLICATION_KEY,
                "deduplicationKey"
        );
        this.status = Guard.notNull(status, DomainErrorCode.INVALID_OUTBOX_STATUS, "status");
        OutboxEventRule.requireNonNegativeAttemptCount(attemptCount);
        this.attemptCount = attemptCount;
        this.nextAttemptAt = Guard.notNull(
                nextAttemptAt,
                DomainErrorCode.INVALID_OUTBOX_NEXT_ATTEMPT_AT,
                "nextAttemptAt"
        );
        this.lockedAt = lockedAt;
        this.lockedBy = trim(lockedBy);
        this.lastError = trim(lastError);
        this.createdAt = Guard.notNull(createdAt, DomainErrorCode.INVALID_OUTBOX_CREATED_AT, "createdAt");
        this.processedAt = processedAt;
        this.updatedAt = Guard.notNull(updatedAt, DomainErrorCode.INVALID_OUTBOX_UPDATED_AT, "updatedAt");
        this.version = version;
    }

    public void claim(String workerId, Instant now) {
        String normalizedWorkerId = Guard.notBlank(
                workerId,
                DomainErrorCode.INVALID_OUTBOX_WORKER_ID,
                "workerId"
        );
        Instant claimedAt = Guard.notNull(now, DomainErrorCode.INVALID_OUTBOX_UPDATED_AT, "now");
        OutboxEventRule.requireClaimable(status, nextAttemptAt, claimedAt);
        status = OutboxStatus.PROCESSING;
        lockedAt = claimedAt;
        lockedBy = normalizedWorkerId;
        updatedAt = claimedAt;
    }

    public void reclaim(Instant now) {
        if (status != OutboxStatus.PROCESSING) {
            return;
        }
        Instant reclaimedAt = Guard.notNull(now, DomainErrorCode.INVALID_OUTBOX_UPDATED_AT, "now");
        status = OutboxStatus.PENDING;
        lockedAt = null;
        lockedBy = null;
        nextAttemptAt = reclaimedAt;
        updatedAt = reclaimedAt;
    }

    public void succeed(Instant now) {
        OutboxEventRule.requireProcessing(status);
        Instant succeededAt = Guard.notNull(now, DomainErrorCode.INVALID_OUTBOX_UPDATED_AT, "now");
        status = OutboxStatus.SUCCEEDED;
        processedAt = succeededAt;
        lockedAt = null;
        lockedBy = null;
        lastError = null;
        updatedAt = succeededAt;
    }

    public void fail(String error, int maxAttempts, Instant now) {
        OutboxEventRule.requireProcessing(status);
        OutboxEventRule.requirePositiveMaxAttempts(maxAttempts);
        Instant failedAt = Guard.notNull(now, DomainErrorCode.INVALID_OUTBOX_UPDATED_AT, "now");
        attemptCount++;
        lastError = trim(error);
        lockedAt = null;
        lockedBy = null;
        if (attemptCount >= maxAttempts) {
            status = OutboxStatus.DEAD;
            processedAt = failedAt;
            nextAttemptAt = failedAt;
        } else {
            status = OutboxStatus.FAILED;
            nextAttemptAt = failedAt.plus(backoff(attemptCount));
        }
        updatedAt = failedAt;
    }

    public void retry(Instant now) {
        OutboxEventRule.requireRetryable(status);
        Instant retriedAt = Guard.notNull(now, DomainErrorCode.INVALID_OUTBOX_UPDATED_AT, "now");
        status = OutboxStatus.PENDING;
        nextAttemptAt = retriedAt;
        lockedAt = null;
        lockedBy = null;
        processedAt = null;
        updatedAt = retriedAt;
    }

    private static Duration backoff(int attempt) {
        long seconds = Math.min(3600L, 5L * (1L << Math.min(attempt - 1, 9)));
        return Duration.ofSeconds(seconds);
    }

    private static String trim(String value) {
        if (value == null) return null;
        String normalized = value.trim();
        return normalized.isEmpty() ? null : normalized;
    }
}
