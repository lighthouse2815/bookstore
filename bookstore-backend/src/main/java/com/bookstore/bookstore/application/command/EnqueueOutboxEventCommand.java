package com.bookstore.bookstore.application.command;

import java.util.UUID;

public record EnqueueOutboxEventCommand(
        String aggregateType,
        UUID aggregateId,
        String eventType,
        Object payload,
        String deduplicationSeed
) {
}
