package com.bookstore.bookstore.presentation.response;

import com.bookstore.bookstore.domain.enums.AuditAction;
import java.time.Instant;
import java.util.UUID;

public record AuditLogResponse(
        UUID id,
        UUID actorId,
        String actorUsername,
        String actorRole,
        AuditAction action,
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
