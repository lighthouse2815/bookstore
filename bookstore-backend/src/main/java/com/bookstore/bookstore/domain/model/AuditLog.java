package com.bookstore.bookstore.domain.model;

import java.time.Instant;
import java.util.UUID;
import lombok.Getter;

@Getter
public class AuditLog {

    private final UUID id;
    private final UUID actorId;
    private final String actorUsername;
    private final String actorRole;
    private final String action;
    private final String targetType;
    private final String targetId;
    private final String description;
    private final String beforeValue;
    private final String afterValue;
    private final String ipAddress;
    private final String userAgent;
    private final Instant createdAt;

    public AuditLog(
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
        this.id = id;
        this.actorId = actorId;
        this.actorUsername = normalize(actorUsername);
        this.actorRole = normalize(actorRole);
        this.action = normalize(action);
        this.targetType = normalize(targetType);
        this.targetId = normalize(targetId);
        this.description = normalize(description);
        this.beforeValue = normalize(beforeValue);
        this.afterValue = normalize(afterValue);
        this.ipAddress = normalize(ipAddress);
        this.userAgent = normalize(userAgent);
        this.createdAt = createdAt;
    }

    private String normalize(String value) {
        if (value == null) {
            return null;
        }

        String normalized = value.trim();
        return normalized.isEmpty() ? null : normalized;
    }
}
