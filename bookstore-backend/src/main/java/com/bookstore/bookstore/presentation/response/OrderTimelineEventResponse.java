package com.bookstore.bookstore.presentation.response;

import java.time.Instant;
import java.util.UUID;

public record OrderTimelineEventResponse(
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
