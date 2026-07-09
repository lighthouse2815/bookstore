package com.bookstore.bookstore.presentation.response;

import java.time.Instant;
import java.util.UUID;

public record AuditLogResponse(
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
