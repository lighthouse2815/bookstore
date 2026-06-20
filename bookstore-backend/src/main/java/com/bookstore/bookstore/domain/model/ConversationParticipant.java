package com.bookstore.bookstore.domain.model;

import com.bookstore.bookstore.domain.enums.MessageSenderRole;
import com.bookstore.bookstore.domain.exception.DomainErrorCode;
import com.bookstore.bookstore.domain.rule.ConversationParticipantRule;
import com.bookstore.bookstore.domain.validation.Guard;
import java.time.Instant;
import java.util.UUID;
import lombok.Getter;

@Getter
public class ConversationParticipant {

    private UUID id;
    private UUID conversationId;
    private UUID userId;
    private MessageSenderRole role;
    private UUID lastReadMessageId;
    private Instant lastReadAt;
    private int unreadCount;
    private Instant joinedAt;
    private Instant leftAt;

    public ConversationParticipant(
            UUID id,
            UUID conversationId,
            UUID userId,
            MessageSenderRole role,
            UUID lastReadMessageId,
            Instant lastReadAt,
            int unreadCount,
            Instant joinedAt,
            Instant leftAt
    ) {
        this.id = Guard.notNull(id, DomainErrorCode.INVALID_CONVERSATION_PARTICIPANT_ID, "id");
        setConversationId(conversationId);
        setUserId(userId);
        setRole(role);
        setLastReadMessageId(lastReadMessageId);
        setLastReadAt(lastReadAt);
        setUnreadCount(unreadCount);
        setJoinedAt(joinedAt);
        setLeftAt(leftAt);
        ConversationParticipantRule.requireAuditTimeline(this.joinedAt, this.lastReadAt, this.leftAt);
    }

    public void incrementUnread() {
        ConversationParticipantRule.requireCanIncrementUnread(leftAt);
        setUnreadCount(unreadCount + 1);
    }

    public void markRead(UUID lastReadMessageId) {
        ConversationParticipantRule.requireCanMarkRead(leftAt);
        this.lastReadMessageId = lastReadMessageId;
        setLastReadAt(Instant.now());
        setUnreadCount(0);
    }

    private void setConversationId(UUID conversationId) {
        this.conversationId = Guard.notNull(
                conversationId,
                DomainErrorCode.INVALID_CONVERSATION_PARTICIPANT_CONVERSATION_ID,
                "conversationId"
        );
    }

    private void setUserId(UUID userId) {
        this.userId = Guard.notNull(
                userId,
                DomainErrorCode.INVALID_CONVERSATION_PARTICIPANT_USER_ID,
                "userId"
        );
    }

    private void setRole(MessageSenderRole role) {
        this.role = Guard.notNull(
                role,
                DomainErrorCode.INVALID_CONVERSATION_PARTICIPANT_ROLE,
                "role"
        );
    }

    private void setLastReadMessageId(UUID lastReadMessageId) {
        this.lastReadMessageId = lastReadMessageId;
    }

    private void setLastReadAt(Instant lastReadAt) {
        this.lastReadAt = Guard.notInFutureOrNull(
                lastReadAt,
                DomainErrorCode.INVALID_CONVERSATION_PARTICIPANT_LAST_READ_AT,
                "lastReadAt"
        );
        ConversationParticipantRule.requireAuditTimeline(this.joinedAt, this.lastReadAt, this.leftAt);
    }

    private void setUnreadCount(int unreadCount) {
        if (unreadCount < 0) {
            throw new com.bookstore.bookstore.domain.exception.DomainException(
                    DomainErrorCode.INVALID_CONVERSATION_PARTICIPANT_UNREAD_COUNT,
                    "unreadCount"
            );
        }
        this.unreadCount = unreadCount;
    }

    private void setJoinedAt(Instant joinedAt) {
        this.joinedAt = Guard.notInFuture(
                joinedAt,
                DomainErrorCode.INVALID_CONVERSATION_PARTICIPANT_JOINED_AT,
                "joinedAt"
        );
        ConversationParticipantRule.requireAuditTimeline(this.joinedAt, this.lastReadAt, this.leftAt);
    }

    private void setLeftAt(Instant leftAt) {
        this.leftAt = Guard.notInFutureOrNull(
                leftAt,
                DomainErrorCode.INVALID_CONVERSATION_PARTICIPANT_LEFT_AT,
                "leftAt"
        );
        ConversationParticipantRule.requireAuditTimeline(this.joinedAt, this.lastReadAt, this.leftAt);
    }
}
