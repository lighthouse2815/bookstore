package com.bookstore.bookstore.presentation.response;

import com.bookstore.bookstore.domain.enums.OutboxStatus;
import java.time.Instant;
import java.util.UUID;

public record OutboxEventResponse(
        UUID id, String aggregateType, UUID aggregateId, String eventType, OutboxStatus status, int attemptCount,
        Instant nextAttemptAt, Instant lockedAt, String lockedBy, String lastError, Instant createdAt, Instant processedAt
) { }
