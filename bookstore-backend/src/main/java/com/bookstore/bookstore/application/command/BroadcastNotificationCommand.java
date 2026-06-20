package com.bookstore.bookstore.application.command;

import java.util.UUID;

public record BroadcastNotificationCommand(
        String title,
        String content,
        String type,
        String targetType,
        UUID targetId,
        String link
) {
}
