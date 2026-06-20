package com.bookstore.bookstore.domain.rule;

import com.bookstore.bookstore.domain.enums.ConversationStatus;
import com.bookstore.bookstore.domain.exception.DomainErrorCode;
import com.bookstore.bookstore.domain.exception.DomainException;
import com.bookstore.bookstore.domain.validation.Guard;
import java.time.Instant;

public final class ConversationRule {

    private ConversationRule() {
    }

    public static void requireCanSendMessage(ConversationStatus status, Instant deletedAt) {
        if (deletedAt != null) {
            throw new DomainException(DomainErrorCode.CONVERSATION_ALREADY_DELETED);
        }

        if (ConversationStatus.CLOSED.equals(status)) {
            throw new DomainException(DomainErrorCode.CONVERSATION_CLOSED_CANNOT_SEND_MESSAGE);
        }
    }

    public static void requireCanClose(ConversationStatus status, Instant deletedAt) {
        if (deletedAt != null) {
            throw new DomainException(DomainErrorCode.CONVERSATION_ALREADY_DELETED);
        }

        if (ConversationStatus.CLOSED.equals(status)) {
            throw new DomainException(DomainErrorCode.CONVERSATION_ALREADY_CLOSED);
        }
    }

    public static void requireCanReopen(ConversationStatus status, Instant deletedAt) {
        if (deletedAt != null) {
            throw new DomainException(DomainErrorCode.CONVERSATION_ALREADY_DELETED);
        }

        if (!ConversationStatus.CLOSED.equals(status)) {
            throw new DomainException(DomainErrorCode.CONVERSATION_NOT_CLOSED);
        }
    }

    public static void requireCanMarkRead(Instant deletedAt) {
        if (deletedAt != null) {
            throw new DomainException(DomainErrorCode.CONVERSATION_ALREADY_DELETED);
        }
    }

    public static void requireAuditTimeline(
            Instant createdAt,
            Instant updatedAt,
            Instant lastMessageAt,
            Instant closedAt,
            Instant deletedAt
    ) {
        Guard.notBefore(updatedAt, createdAt, DomainErrorCode.INVALID_CONVERSATION_AUDIT_ORDER, "updatedAt", "createdAt");
        Guard.notBefore(lastMessageAt, createdAt, DomainErrorCode.INVALID_CONVERSATION_AUDIT_ORDER, "lastMessageAt", "createdAt");
        Guard.notBefore(closedAt, createdAt, DomainErrorCode.INVALID_CONVERSATION_AUDIT_ORDER, "closedAt", "createdAt");
        Guard.notBefore(deletedAt, createdAt, DomainErrorCode.INVALID_CONVERSATION_AUDIT_ORDER, "deletedAt", "createdAt");
        Guard.notBefore(updatedAt, lastMessageAt, DomainErrorCode.INVALID_CONVERSATION_AUDIT_ORDER, "updatedAt", "lastMessageAt");
        Guard.notBefore(deletedAt, lastMessageAt, DomainErrorCode.INVALID_CONVERSATION_AUDIT_ORDER, "deletedAt", "lastMessageAt");
        Guard.notBefore(deletedAt, closedAt, DomainErrorCode.INVALID_CONVERSATION_AUDIT_ORDER, "deletedAt", "closedAt");
    }
}
