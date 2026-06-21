package com.bookstore.bookstore.domain.rule;

import com.bookstore.bookstore.domain.exception.DomainErrorCode;
import com.bookstore.bookstore.domain.exception.DomainException;
import com.bookstore.bookstore.domain.validation.Guard;
import java.time.Instant;

public final class ConversationParticipantRule {

    private ConversationParticipantRule() {
    }

    public static void requireCanIncrementUnread(Instant leftAt) {
        if (leftAt != null) {
            throw new DomainException(DomainErrorCode.CONVERSATION_PARTICIPANT_ALREADY_LEFT);
        }
    }

    public static void requireCanMarkRead(Instant leftAt) {
        if (leftAt != null) {
            throw new DomainException(DomainErrorCode.CONVERSATION_PARTICIPANT_ALREADY_LEFT);
        }
    }

    public static void requireAuditTimeline(
            Instant joinedAt,
            Instant lastReadAt,
            Instant leftAt
    ) {
        Guard.notBefore(lastReadAt, joinedAt, DomainErrorCode.INVALID_CONVERSATION_PARTICIPANT_AUDIT_ORDER, "lastReadAt", "joinedAt");
        Guard.notBefore(leftAt, joinedAt, DomainErrorCode.INVALID_CONVERSATION_PARTICIPANT_AUDIT_ORDER, "leftAt", "joinedAt");
        Guard.notBefore(leftAt, lastReadAt, DomainErrorCode.INVALID_CONVERSATION_PARTICIPANT_AUDIT_ORDER, "leftAt", "lastReadAt");
    }
}
