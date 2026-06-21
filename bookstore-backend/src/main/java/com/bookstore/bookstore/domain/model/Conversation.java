package com.bookstore.bookstore.domain.model;

import com.bookstore.bookstore.domain.enums.ConversationPriority;
import com.bookstore.bookstore.domain.enums.ConversationStatus;
import com.bookstore.bookstore.domain.enums.ConversationTargetType;
import com.bookstore.bookstore.domain.exception.DomainErrorCode;
import com.bookstore.bookstore.domain.rule.ConversationRule;
import com.bookstore.bookstore.domain.validation.Guard;
import java.time.Instant;
import java.util.UUID;
import lombok.Getter;

@Getter
public class Conversation {

    private UUID id;
    private UUID customerId;
    private UUID assignedStaffId;
    private ConversationStatus status;
    private String subject;
    private ConversationPriority priority;
    private ConversationTargetType targetType;
    private UUID targetId;
    private UUID lastMessageId;
    private String lastMessagePreview;
    private Instant lastMessageAt;
    private Instant createdAt;
    private Instant updatedAt;
    private Instant closedAt;
    private Instant deletedAt;

    public Conversation(
            UUID id,
            UUID customerId,
            UUID assignedStaffId,
            ConversationStatus status,
            String subject,
            ConversationPriority priority,
            ConversationTargetType targetType,
            UUID targetId,
            UUID lastMessageId,
            String lastMessagePreview,
            Instant lastMessageAt,
            Instant createdAt,
            Instant updatedAt,
            Instant closedAt,
            Instant deletedAt
    ) {
        this.id = Guard.notNull(id, DomainErrorCode.INVALID_CONVERSATION_ID, "id");
        setCustomerId(customerId);
        setAssignedStaffId(assignedStaffId);
        setStatus(status);
        setSubject(subject);
        setPriority(priority);
        setTargetType(targetType);
        setTargetId(targetId);
        setLastMessageId(lastMessageId);
        setLastMessagePreview(lastMessagePreview);
        setLastMessageAt(lastMessageAt);
        setCreatedAt(createdAt);
        setUpdatedAt(updatedAt);
        setClosedAt(closedAt);
        setDeletedAt(deletedAt);
        ConversationRule.requireAuditTimeline(
                this.createdAt,
                this.updatedAt,
                this.lastMessageAt,
                this.closedAt,
                this.deletedAt
        );
    }

    public void applyMessage(UUID messageId, String preview, Instant messageAt) {
        ConversationRule.requireCanSendMessage(status, deletedAt);
        Instant now = Guard.notInFuture(messageAt, DomainErrorCode.INVALID_CONVERSATION_LAST_MESSAGE_AT, "lastMessageAt");
        setLastMessageId(messageId);
        setLastMessagePreview(preview);
        setLastMessageAt(now);
        setUpdatedAt(now);
    }

    public void assignStaff(UUID assignedStaffId) {
        setAssignedStaffId(assignedStaffId);
        setUpdatedAt(Instant.now());
    }

    public void close() {
        ConversationRule.requireCanClose(status, deletedAt);
        Instant now = Instant.now();
        setStatus(ConversationStatus.CLOSED);
        setClosedAt(now);
        setUpdatedAt(now);
    }

    public void reopen() {
        ConversationRule.requireCanReopen(status, deletedAt);
        Instant now = Instant.now();
        setStatus(ConversationStatus.OPEN);
        setClosedAt(null);
        setUpdatedAt(now);
    }

    private void setCustomerId(UUID customerId) {
        this.customerId = Guard.notNull(customerId, DomainErrorCode.INVALID_CONVERSATION_CUSTOMER_ID, "customerId");
    }

    private void setAssignedStaffId(UUID assignedStaffId) {
        this.assignedStaffId = assignedStaffId;
    }

    private void setStatus(ConversationStatus status) {
        this.status = Guard.notNull(status, DomainErrorCode.INVALID_CONVERSATION_STATUS, "status");
    }

    private void setSubject(String subject) {
        this.subject = Guard.notBlank(subject, DomainErrorCode.INVALID_CONVERSATION_SUBJECT, "subject");
    }

    private void setPriority(ConversationPriority priority) {
        this.priority = Guard.notNull(priority, DomainErrorCode.INVALID_CONVERSATION_PRIORITY, "priority");
    }

    private void setTargetType(ConversationTargetType targetType) {
        this.targetType = Guard.notNull(targetType, DomainErrorCode.INVALID_CONVERSATION_TARGET_TYPE, "targetType");
    }

    private void setTargetId(UUID targetId) {
        this.targetId = targetId;
    }

    private void setLastMessageId(UUID lastMessageId) {
        this.lastMessageId = lastMessageId;
    }

    private void setLastMessagePreview(String lastMessagePreview) {
        if (lastMessagePreview == null) {
            this.lastMessagePreview = null;
            return;
        }

        String normalized = lastMessagePreview.trim();
        this.lastMessagePreview = normalized.isEmpty() ? null : normalized;
    }

    private void setLastMessageAt(Instant lastMessageAt) {
        this.lastMessageAt = Guard.notInFuture(
                lastMessageAt,
                DomainErrorCode.INVALID_CONVERSATION_LAST_MESSAGE_AT,
                "lastMessageAt"
        );
    }

    private void setCreatedAt(Instant createdAt) {
        Instant validCreatedAt = Guard.notInFuture(
                createdAt,
                DomainErrorCode.INVALID_CONVERSATION_CREATED_AT,
                "createdAt"
        );
        Guard.validateAuditTimestamps(
                validCreatedAt,
                this.updatedAt,
                this.deletedAt,
                DomainErrorCode.INVALID_CONVERSATION_CREATED_AT,
                DomainErrorCode.INVALID_CONVERSATION_UPDATED_AT,
                DomainErrorCode.INVALID_CONVERSATION_DELETED_AT,
                DomainErrorCode.INVALID_CONVERSATION_AUDIT_ORDER
        );
        this.createdAt = validCreatedAt;
    }

    private void setUpdatedAt(Instant updatedAt) {
        Instant validUpdatedAt = Guard.notInFutureOrNull(
                updatedAt,
                DomainErrorCode.INVALID_CONVERSATION_UPDATED_AT,
                "updatedAt"
        );
        Guard.validateAuditTimestamps(
                this.createdAt,
                validUpdatedAt,
                this.deletedAt,
                DomainErrorCode.INVALID_CONVERSATION_CREATED_AT,
                DomainErrorCode.INVALID_CONVERSATION_UPDATED_AT,
                DomainErrorCode.INVALID_CONVERSATION_DELETED_AT,
                DomainErrorCode.INVALID_CONVERSATION_AUDIT_ORDER
        );
        this.updatedAt = validUpdatedAt;
        ConversationRule.requireAuditTimeline(
                this.createdAt,
                this.updatedAt,
                this.lastMessageAt,
                this.closedAt,
                this.deletedAt
        );
    }

    private void setClosedAt(Instant closedAt) {
        this.closedAt = Guard.notInFutureOrNull(
                closedAt,
                DomainErrorCode.INVALID_CONVERSATION_CLOSED_AT,
                "closedAt"
        );
        ConversationRule.requireAuditTimeline(
                this.createdAt,
                this.updatedAt,
                this.lastMessageAt,
                this.closedAt,
                this.deletedAt
        );
    }

    private void setDeletedAt(Instant deletedAt) {
        Instant validDeletedAt = Guard.notInFutureOrNull(
                deletedAt,
                DomainErrorCode.INVALID_CONVERSATION_DELETED_AT,
                "deletedAt"
        );
        Guard.validateAuditTimestamps(
                this.createdAt,
                this.updatedAt,
                validDeletedAt,
                DomainErrorCode.INVALID_CONVERSATION_CREATED_AT,
                DomainErrorCode.INVALID_CONVERSATION_UPDATED_AT,
                DomainErrorCode.INVALID_CONVERSATION_DELETED_AT,
                DomainErrorCode.INVALID_CONVERSATION_AUDIT_ORDER
        );
        this.deletedAt = validDeletedAt;
        ConversationRule.requireAuditTimeline(
                this.createdAt,
                this.updatedAt,
                this.lastMessageAt,
                this.closedAt,
                this.deletedAt
        );
    }
}
