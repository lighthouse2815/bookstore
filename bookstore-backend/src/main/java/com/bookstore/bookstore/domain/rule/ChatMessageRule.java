package com.bookstore.bookstore.domain.rule;

import com.bookstore.bookstore.domain.exception.DomainErrorCode;
import com.bookstore.bookstore.domain.exception.DomainException;
import com.bookstore.bookstore.domain.validation.Guard;
import java.time.Instant;

public final class ChatMessageRule {

    private ChatMessageRule() {
    }

    public static void requireCanSoftDelete(Instant deletedAt) {
        if (deletedAt != null) {
            throw new DomainException(DomainErrorCode.CHAT_MESSAGE_ALREADY_DELETED);
        }
    }

    public static void requireAuditTimeline(
            Instant createdAt,
            Instant updatedAt,
            Instant deletedAt
    ) {
        Guard.notBefore(updatedAt, createdAt, DomainErrorCode.INVALID_CHAT_MESSAGE_AUDIT_ORDER, "updatedAt", "createdAt");
        Guard.notBefore(deletedAt, createdAt, DomainErrorCode.INVALID_CHAT_MESSAGE_AUDIT_ORDER, "deletedAt", "createdAt");
        Guard.notBefore(deletedAt, updatedAt, DomainErrorCode.INVALID_CHAT_MESSAGE_AUDIT_ORDER, "deletedAt", "updatedAt");
    }
}
