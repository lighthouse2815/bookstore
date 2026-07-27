package com.bookstore.bookstore.domain.model;

import com.bookstore.bookstore.domain.exception.DomainErrorCode;
import com.bookstore.bookstore.domain.validation.Guard;
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
        this.id = Guard.notNull(id, DomainErrorCode.INVALID_AUDIT_LOG_ID, "id");
        this.actorId = actorId;
        this.actorUsername = Guard.notBlankOrNull(
                actorUsername,
                DomainErrorCode.INVALID_AUDIT_LOG_ACTOR_USERNAME,
                "actorUsername"
        );
        this.actorRole = Guard.notBlankOrNull(
                actorRole,
                DomainErrorCode.INVALID_AUDIT_LOG_ACTOR_ROLE,
                "actorRole"
        );
        this.action = Guard.notBlank(action, DomainErrorCode.INVALID_AUDIT_LOG_ACTION, "action");
        this.targetType = Guard.notBlank(
                targetType,
                DomainErrorCode.INVALID_AUDIT_LOG_TARGET_TYPE,
                "targetType"
        );
        this.targetId = Guard.notBlankOrNull(
                targetId,
                DomainErrorCode.INVALID_AUDIT_LOG_TARGET_ID,
                "targetId"
        );
        this.description = Guard.notBlankOrNull(
                description,
                DomainErrorCode.INVALID_AUDIT_LOG_DESCRIPTION,
                "description"
        );
        this.beforeValue = Guard.notBlankOrNull(
                beforeValue,
                DomainErrorCode.INVALID_AUDIT_LOG_BEFORE_VALUE,
                "beforeValue"
        );
        this.afterValue = Guard.notBlankOrNull(
                afterValue,
                DomainErrorCode.INVALID_AUDIT_LOG_AFTER_VALUE,
                "afterValue"
        );
        this.ipAddress = Guard.notBlankOrNull(
                ipAddress,
                DomainErrorCode.INVALID_AUDIT_LOG_IP_ADDRESS,
                "ipAddress"
        );
        this.userAgent = Guard.notBlankOrNull(
                userAgent,
                DomainErrorCode.INVALID_AUDIT_LOG_USER_AGENT,
                "userAgent"
        );
        this.createdAt = Guard.notInFuture(
                createdAt,
                DomainErrorCode.INVALID_AUDIT_LOG_CREATED_AT,
                "createdAt"
        );
    }
}
