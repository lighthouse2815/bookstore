package com.bookstore.bookstore.presentation.response;

import java.time.Instant;
import java.util.UUID;

public record NotificationResponse(
        UUID notificationId,
        UUID userId,
        String title,
        String content,
        boolean read,
        Instant createdAt,
        Instant updatedAt,
        Instant readAt
) {
}
