package com.bookstore.bookstore.application.result;

import java.time.Instant;
import java.util.UUID;

public record NotificationResult(
        UUID notificationId,
        UUID userId,
        String title,
        String content,
        boolean read,
        Instant createdAt,
        Instant updatedAt,
        Instant readAt,
        String type,
        String targetType,
        UUID targetId,
        String link
) {
}
