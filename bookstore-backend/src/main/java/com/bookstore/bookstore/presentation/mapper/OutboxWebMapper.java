package com.bookstore.bookstore.presentation.mapper;

import com.bookstore.bookstore.application.result.OutboxEventResult;
import com.bookstore.bookstore.presentation.response.OutboxEventResponse;
import org.springframework.stereotype.Component;

@Component
public class OutboxWebMapper {
    public OutboxEventResponse toResponse(OutboxEventResult result) {
        return new OutboxEventResponse(result.id(), result.aggregateType(), result.aggregateId(), result.eventType(), result.status(),
                result.attemptCount(), result.nextAttemptAt(), result.lockedAt(), result.lockedBy(), result.lastError(), result.createdAt(), result.processedAt());
    }
}
