package com.bookstore.bookstore.domain.model;

import java.time.Instant;
import java.util.Objects;
import java.util.UUID;
import lombok.Getter;

@Getter
public class OrderTimelineEvent {

    private final UUID id;
    private final UUID orderId;
    private final UUID actorId;
    private final String actorName;
    private final String actorRole;
    private final String eventType;
    private final String oldStatus;
    private final String newStatus;
    private final String title;
    private final String description;
    private final String metadata;
    private final Instant createdAt;

    public OrderTimelineEvent(
            UUID id,
            UUID orderId,
            UUID actorId,
            String actorName,
            String actorRole,
            String eventType,
            String oldStatus,
            String newStatus,
            String title,
            String description,
            String metadata,
            Instant createdAt
    ) {
        this.id = Objects.requireNonNull(id, "id must not be null");
        this.orderId = Objects.requireNonNull(orderId, "orderId must not be null");
        this.actorId = actorId;
        this.actorName = normalize(actorName);
        this.actorRole = normalize(actorRole);
        this.eventType = requireText(eventType, "eventType");
        this.oldStatus = normalize(oldStatus);
        this.newStatus = normalize(newStatus);
        this.title = requireText(title, "title");
        this.description = normalize(description);
        this.metadata = normalize(metadata);
        this.createdAt = Objects.requireNonNull(createdAt, "createdAt must not be null");
    }

    private static String requireText(String value, String fieldName) {
        String normalized = normalize(value);
        if (normalized == null) {
            throw new IllegalArgumentException(fieldName + " must not be blank");
        }
        return normalized;
    }

    private static String normalize(String value) {
        if (value == null) {
            return null;
        }
        String trimmed = value.trim();
        return trimmed.isEmpty() ? null : trimmed;
    }
}
