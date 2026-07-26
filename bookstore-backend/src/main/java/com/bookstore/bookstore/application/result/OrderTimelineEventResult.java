package com.bookstore.bookstore.application.result;

import java.time.Instant;
import java.util.UUID;

public record OrderTimelineEventResult(
        UUID id,
        UUID orderId,
        String eventType,
        String title,
        String description,
        String oldStatus,
        String newStatus,
        String actorName,
        String actorRole,
        Instant createdAt,
        String metadata
) {
}
