package com.bookstore.bookstore.application.command;

import java.util.UUID;

/** Payload is intentionally limited to notification presentation data; it never contains credentials or payment secrets. */
public record OutboxNotificationPayload(
        UUID userId,
        String title,
        String content,
        String type,
        String targetType,
        UUID targetId,
        String link
) {
}
