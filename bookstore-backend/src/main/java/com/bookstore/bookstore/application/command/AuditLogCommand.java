package com.bookstore.bookstore.application.command;

import com.bookstore.bookstore.domain.enums.AuditAction;
import com.bookstore.bookstore.domain.enums.AuditTargetType;
import java.time.Instant;
import java.util.UUID;

public record AuditLogCommand(
        UUID actorId,
        String actorUsername,
        String actorRole,
        AuditAction action,
        AuditTargetType targetType,
        String targetId,
        String description,
        Object beforeValue,
        Object afterValue,
        String ipAddress,
        String userAgent,
        Instant createdAt
) {
}
