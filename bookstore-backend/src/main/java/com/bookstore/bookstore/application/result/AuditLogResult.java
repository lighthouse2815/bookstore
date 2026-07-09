package com.bookstore.bookstore.application.result;

import java.time.Instant;
import java.util.UUID;

public record AuditLogResult(
        UUID id,
        UUID actorId,
        String actorUsername,
        String actorRole,
        String action,
        String targetType,
        String targetId,
        String description,
        String beforeValue,
        String afterValue,
        String ipAddress,
        String userAgent,
        Instant createdAt
) {
}
