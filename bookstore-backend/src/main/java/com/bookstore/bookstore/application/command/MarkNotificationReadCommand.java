package com.bookstore.bookstore.application.command;

import java.util.UUID;

public record MarkNotificationReadCommand(
        UUID notificationId,
        UUID userId
) {
}
