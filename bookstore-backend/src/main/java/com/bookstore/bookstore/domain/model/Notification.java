package com.bookstore.bookstore.domain.model;

import com.bookstore.bookstore.domain.exception.DomainErrorCode;
import com.bookstore.bookstore.domain.rule.NotificationRule;
import com.bookstore.bookstore.domain.validation.Guard;
import java.time.Instant;
import java.util.UUID;
import lombok.Getter;

@Getter
public class Notification {

    private UUID id;
    private UUID userId;
    private String title;
    private String content;
    private String type;
    private String targetType;
    private UUID targetId;
    private String link;
    private boolean read;
    private Instant createdAt;
    private Instant updatedAt;
    private Instant readAt;
    private Instant deletedAt;

    public Notification(
            UUID id,
            UUID userId,
            String title,
            String content,
            boolean read,
            Instant createdAt,
            Instant updatedAt,
            Instant readAt,
            Instant deletedAt
    ) {
        this(
                id,
                userId,
                title,
                content,
                null,
                null,
                null,
                null,
                read,
                createdAt,
                updatedAt,
                readAt,
                deletedAt
        );
    }

    public Notification(
            UUID id,
            UUID userId,
            String title,
            String content,
            String type,
            String targetType,
            UUID targetId,
            String link,
            boolean read,
            Instant createdAt,
            Instant updatedAt,
            Instant readAt,
            Instant deletedAt
    ) {
        this.id = Guard.notNull(id, DomainErrorCode.INVALID_NOTIFICATION_ID, "id");
        setUserId(userId);
        setTitle(title);
        setContent(content);
        setType(type);
        setTargetType(targetType);
        setTargetId(targetId);
        setLink(link);
        setRead(read);
        setCreatedAt(createdAt);
        setUpdatedAt(updatedAt);
        setReadAt(readAt);
        setDeletedAt(deletedAt);
        NotificationRule.requireReadStateConsistent(this.read, this.readAt);
        NotificationRule.requireAuditTimeline(this.createdAt, this.updatedAt, this.readAt, this.deletedAt);
    }

    public void markRead() {
        NotificationRule.requireCanMarkRead(deletedAt, read);
        Instant now = Instant.now();
        setRead(true);
        setUpdatedAt(now);
        setReadAt(now);
    }

    public void softDelete() {
        NotificationRule.requireCanSoftDelete(deletedAt);
        Instant now = Instant.now();
        setUpdatedAt(now);
        setDeletedAt(now);
    }

    private void setUserId(UUID userId) {
        this.userId = Guard.notNull(userId, DomainErrorCode.INVALID_NOTIFICATION_USER_ID, "userId");
    }

    private void setTitle(String title) {
        this.title = Guard.notBlank(title, DomainErrorCode.INVALID_NOTIFICATION_TITLE, "title");
    }

    private void setContent(String content) {
        this.content = Guard.notBlank(content, DomainErrorCode.INVALID_NOTIFICATION_CONTENT, "content");
    }

    private void setType(String type) {
        this.type = normalizeOptionalText(type);
    }

    private void setTargetType(String targetType) {
        this.targetType = normalizeOptionalText(targetType);
    }

    private void setTargetId(UUID targetId) {
        this.targetId = targetId;
    }

    private void setLink(String link) {
        this.link = normalizeOptionalText(link);
    }

    private void setRead(boolean read) {
        this.read = read;
    }

    private void setCreatedAt(Instant createdAt) {
        Instant validCreatedAt = Guard.notInFuture(
                createdAt,
                DomainErrorCode.INVALID_NOTIFICATION_CREATED_AT,
                "createdAt"
        );
        Guard.validateAuditTimestamps(
                validCreatedAt,
                this.updatedAt,
                this.deletedAt,
                DomainErrorCode.INVALID_NOTIFICATION_CREATED_AT,
                DomainErrorCode.INVALID_NOTIFICATION_UPDATED_AT,
                DomainErrorCode.INVALID_NOTIFICATION_DELETED_AT,
                DomainErrorCode.INVALID_NOTIFICATION_AUDIT_ORDER
        );
        this.createdAt = validCreatedAt;
        NotificationRule.requireAuditTimeline(this.createdAt, this.updatedAt, this.readAt, this.deletedAt);
    }

    private void setUpdatedAt(Instant updatedAt) {
        Instant validUpdatedAt = Guard.notInFutureOrNull(
                updatedAt,
                DomainErrorCode.INVALID_NOTIFICATION_UPDATED_AT,
                "updatedAt"
        );
        Guard.validateAuditTimestamps(
                this.createdAt,
                validUpdatedAt,
                this.deletedAt,
                DomainErrorCode.INVALID_NOTIFICATION_CREATED_AT,
                DomainErrorCode.INVALID_NOTIFICATION_UPDATED_AT,
                DomainErrorCode.INVALID_NOTIFICATION_DELETED_AT,
                DomainErrorCode.INVALID_NOTIFICATION_AUDIT_ORDER
        );
        this.updatedAt = validUpdatedAt;
        NotificationRule.requireAuditTimeline(this.createdAt, this.updatedAt, this.readAt, this.deletedAt);
    }

    private void setReadAt(Instant readAt) {
        this.readAt = Guard.notInFutureOrNull(
                readAt,
                DomainErrorCode.INVALID_NOTIFICATION_READ_AT,
                "readAt"
        );
        NotificationRule.requireAuditTimeline(this.createdAt, this.updatedAt, this.readAt, this.deletedAt);
    }

    private void setDeletedAt(Instant deletedAt) {
        Instant validDeletedAt = Guard.notInFutureOrNull(
                deletedAt,
                DomainErrorCode.INVALID_NOTIFICATION_DELETED_AT,
                "deletedAt"
        );
        Guard.validateAuditTimestamps(
                this.createdAt,
                this.updatedAt,
                validDeletedAt,
                DomainErrorCode.INVALID_NOTIFICATION_CREATED_AT,
                DomainErrorCode.INVALID_NOTIFICATION_UPDATED_AT,
                DomainErrorCode.INVALID_NOTIFICATION_DELETED_AT,
                DomainErrorCode.INVALID_NOTIFICATION_AUDIT_ORDER
        );
        this.deletedAt = validDeletedAt;
        NotificationRule.requireAuditTimeline(this.createdAt, this.updatedAt, this.readAt, this.deletedAt);
    }

    private String normalizeOptionalText(String value) {
        if (value == null) {
            return null;
        }

        String normalized = value.trim();
        return normalized.isEmpty() ? null : normalized;
    }
}
