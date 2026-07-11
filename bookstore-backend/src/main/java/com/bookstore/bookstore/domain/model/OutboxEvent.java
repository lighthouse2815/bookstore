package com.bookstore.bookstore.domain.model;

import com.bookstore.bookstore.domain.enums.OutboxStatus;
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
        this.id = require(id, "id");
        this.aggregateType = requireText(aggregateType, "aggregateType");
        this.aggregateId = require(aggregateId, "aggregateId");
        this.eventType = requireText(eventType, "eventType");
        this.payload = requireText(payload, "payload");
        this.deduplicationKey = requireText(deduplicationKey, "deduplicationKey");
        this.status = require(status, "status");
        this.attemptCount = attemptCount;
        this.nextAttemptAt = require(nextAttemptAt, "nextAttemptAt");
        this.lockedAt = lockedAt;
        this.lockedBy = trim(lockedBy);
        this.lastError = trim(lastError);
        this.createdAt = require(createdAt, "createdAt");
        this.processedAt = processedAt;
        this.updatedAt = require(updatedAt, "updatedAt");
        this.version = version;
    }

    public void claim(String workerId, Instant now) {
        if ((status != OutboxStatus.PENDING && status != OutboxStatus.FAILED) || nextAttemptAt.isAfter(now)) {
            throw new IllegalStateException("outbox event is not claimable");
        }
        status = OutboxStatus.PROCESSING;
        lockedAt = now;
        lockedBy = requireText(workerId, "workerId");
        updatedAt = now;
    }

    public void reclaim(Instant now) {
        if (status != OutboxStatus.PROCESSING) {
            return;
        }
        status = OutboxStatus.PENDING;
        lockedAt = null;
        lockedBy = null;
        nextAttemptAt = now;
        updatedAt = now;
    }

    public void succeed(Instant now) {
        if (status != OutboxStatus.PROCESSING) {
            throw new IllegalStateException("outbox event is not processing");
        }
        status = OutboxStatus.SUCCEEDED;
        processedAt = now;
        lockedAt = null;
        lockedBy = null;
        lastError = null;
        updatedAt = now;
    }

    public void fail(String error, int maxAttempts, Instant now) {
        if (status != OutboxStatus.PROCESSING) {
            throw new IllegalStateException("outbox event is not processing");
        }
        attemptCount++;
        lastError = trim(error);
        lockedAt = null;
        lockedBy = null;
        if (attemptCount >= maxAttempts) {
            status = OutboxStatus.DEAD;
            processedAt = now;
            nextAttemptAt = now;
        } else {
            status = OutboxStatus.FAILED;
            nextAttemptAt = now.plus(backoff(attemptCount));
        }
        updatedAt = now;
    }

    public void retry(Instant now) {
        if (status != OutboxStatus.DEAD && status != OutboxStatus.FAILED) {
            throw new IllegalStateException("outbox event cannot be retried");
        }
        status = OutboxStatus.PENDING;
        nextAttemptAt = now;
        lockedAt = null;
        lockedBy = null;
        processedAt = null;
        updatedAt = now;
    }

    private static Duration backoff(int attempt) {
        long seconds = Math.min(3600L, 5L * (1L << Math.min(attempt - 1, 9)));
        return Duration.ofSeconds(seconds);
    }

    private static <T> T require(T value, String field) {
        if (value == null) throw new IllegalArgumentException(field + " is required");
        return value;
    }
    private static String requireText(String value, String field) {
        String normalized = trim(value);
        if (normalized == null) throw new IllegalArgumentException(field + " is required");
        return normalized;
    }
    private static String trim(String value) {
        if (value == null) return null;
        String normalized = value.trim();
        return normalized.isEmpty() ? null : normalized;
    }
}
