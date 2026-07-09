package com.bookstore.bookstore.application.command;

import java.time.Instant;
import java.util.UUID;

public record AuditLogCommand(
        UUID actorId,
        String actorUsername,
        String actorRole,
        String action,
        String targetType,
        String targetId,
        String description,
        Object beforeValue,
        Object afterValue,
        String ipAddress,
        String userAgent,
        Instant createdAt
) {
}
