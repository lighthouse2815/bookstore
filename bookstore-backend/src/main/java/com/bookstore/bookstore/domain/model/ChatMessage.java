package com.bookstore.bookstore.domain.model;

import com.bookstore.bookstore.domain.enums.MessageSenderRole;
import com.bookstore.bookstore.domain.enums.MessageType;
import com.bookstore.bookstore.domain.exception.DomainErrorCode;
import com.bookstore.bookstore.domain.rule.ChatMessageRule;
import com.bookstore.bookstore.domain.validation.Guard;
import java.time.Instant;
import java.util.UUID;
import lombok.Getter;

@Getter
public class ChatMessage {

    private UUID id;
    private UUID conversationId;
    private UUID senderId;
    private MessageSenderRole senderRole;
    private MessageType messageType;
    private String content;
    private String attachmentUrl;
    private String attachmentName;
    private Long attachmentSize;
    private Instant createdAt;
    private Instant updatedAt;
    private Instant deletedAt;

    public ChatMessage(
            UUID id,
            UUID conversationId,
            UUID senderId,
            MessageSenderRole senderRole,
            MessageType messageType,
            String content,
            String attachmentUrl,
            String attachmentName,
            Long attachmentSize,
            Instant createdAt,
            Instant updatedAt,
            Instant deletedAt
    ) {
        this.id = Guard.notNull(id, DomainErrorCode.INVALID_CHAT_MESSAGE_ID, "id");
        setConversationId(conversationId);
        setSenderId(senderId);
        setSenderRole(senderRole);
        setMessageType(messageType);
        setContent(content);
        setAttachmentUrl(attachmentUrl);
        setAttachmentName(attachmentName);
        setAttachmentSize(attachmentSize);
        setCreatedAt(createdAt);
        setUpdatedAt(updatedAt);
        setDeletedAt(deletedAt);
        ChatMessageRule.requireAuditTimeline(this.createdAt, this.updatedAt, this.deletedAt);
    }

    public void softDelete() {
        ChatMessageRule.requireCanSoftDelete(deletedAt);
        Instant now = Instant.now();
        setUpdatedAt(now);
        setDeletedAt(now);
    }

    private void setConversationId(UUID conversationId) {
        this.conversationId = Guard.notNull(
                conversationId,
                DomainErrorCode.INVALID_CHAT_MESSAGE_CONVERSATION_ID,
                "conversationId"
        );
    }

    private void setSenderId(UUID senderId) {
        this.senderId = Guard.notNull(senderId, DomainErrorCode.INVALID_CHAT_MESSAGE_SENDER_ID, "senderId");
    }

    private void setSenderRole(MessageSenderRole senderRole) {
        this.senderRole = Guard.notNull(
                senderRole,
                DomainErrorCode.INVALID_CHAT_MESSAGE_SENDER_ROLE,
                "senderRole"
        );
    }

    private void setMessageType(MessageType messageType) {
        this.messageType = Guard.notNull(
                messageType,
                DomainErrorCode.INVALID_CHAT_MESSAGE_TYPE,
                "messageType"
        );
    }

    private void setContent(String content) {
        this.content = Guard.notBlank(content, DomainErrorCode.INVALID_CHAT_MESSAGE_CONTENT, "content");
    }

    private void setAttachmentUrl(String attachmentUrl) {
        this.attachmentUrl = normalizeOptionalText(attachmentUrl);
    }

    private void setAttachmentName(String attachmentName) {
        this.attachmentName = normalizeOptionalText(attachmentName);
    }

    private void setAttachmentSize(Long attachmentSize) {
        if (attachmentSize != null && attachmentSize < 0) {
            throw new com.bookstore.bookstore.domain.exception.DomainException(
                    DomainErrorCode.INVALID_CHAT_MESSAGE_ATTACHMENT_SIZE,
                    "attachmentSize"
            );
        }
        this.attachmentSize = attachmentSize;
    }

    private void setCreatedAt(Instant createdAt) {
        Instant validCreatedAt = Guard.notInFuture(
                createdAt,
                DomainErrorCode.INVALID_CHAT_MESSAGE_CREATED_AT,
                "createdAt"
        );
        Guard.validateAuditTimestamps(
                validCreatedAt,
                this.updatedAt,
                this.deletedAt,
                DomainErrorCode.INVALID_CHAT_MESSAGE_CREATED_AT,
                DomainErrorCode.INVALID_CHAT_MESSAGE_UPDATED_AT,
                DomainErrorCode.INVALID_CHAT_MESSAGE_DELETED_AT,
                DomainErrorCode.INVALID_CHAT_MESSAGE_AUDIT_ORDER
        );
        this.createdAt = validCreatedAt;
    }

    private void setUpdatedAt(Instant updatedAt) {
        Instant validUpdatedAt = Guard.notInFutureOrNull(
                updatedAt,
                DomainErrorCode.INVALID_CHAT_MESSAGE_UPDATED_AT,
                "updatedAt"
        );
        Guard.validateAuditTimestamps(
                this.createdAt,
                validUpdatedAt,
                this.deletedAt,
                DomainErrorCode.INVALID_CHAT_MESSAGE_CREATED_AT,
                DomainErrorCode.INVALID_CHAT_MESSAGE_UPDATED_AT,
                DomainErrorCode.INVALID_CHAT_MESSAGE_DELETED_AT,
                DomainErrorCode.INVALID_CHAT_MESSAGE_AUDIT_ORDER
        );
        this.updatedAt = validUpdatedAt;
        ChatMessageRule.requireAuditTimeline(this.createdAt, this.updatedAt, this.deletedAt);
    }

    private void setDeletedAt(Instant deletedAt) {
        Instant validDeletedAt = Guard.notInFutureOrNull(
                deletedAt,
                DomainErrorCode.INVALID_CHAT_MESSAGE_DELETED_AT,
                "deletedAt"
        );
        Guard.validateAuditTimestamps(
                this.createdAt,
                this.updatedAt,
                validDeletedAt,
                DomainErrorCode.INVALID_CHAT_MESSAGE_CREATED_AT,
                DomainErrorCode.INVALID_CHAT_MESSAGE_UPDATED_AT,
                DomainErrorCode.INVALID_CHAT_MESSAGE_DELETED_AT,
                DomainErrorCode.INVALID_CHAT_MESSAGE_AUDIT_ORDER
        );
        this.deletedAt = validDeletedAt;
        ChatMessageRule.requireAuditTimeline(this.createdAt, this.updatedAt, this.deletedAt);
    }

    private String normalizeOptionalText(String value) {
        if (value == null) {
            return null;
        }

        String normalized = value.trim();
        return normalized.isEmpty() ? null : normalized;
    }
}
