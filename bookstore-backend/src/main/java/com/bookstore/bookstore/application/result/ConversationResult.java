package com.bookstore.bookstore.application.result;

import com.bookstore.bookstore.domain.enums.ConversationPriority;
import com.bookstore.bookstore.domain.enums.ConversationStatus;
import com.bookstore.bookstore.domain.enums.ConversationTargetType;
import java.time.Instant;
import java.util.UUID;

public record ConversationResult(
        UUID conversationId,
        UUID customerId,
        String customerName,
        String customerEmail,
        UUID assignedStaffId,
        String assignedStaffName,
        String assignedStaffEmail,
        ConversationStatus status,
        String subject,
        ConversationPriority priority,
        ConversationTargetType targetType,
        UUID targetId,
        UUID lastMessageId,
        String lastMessagePreview,
        Instant lastMessageAt,
        long myUnreadCount,
        Instant createdAt,
        Instant updatedAt,
        Instant closedAt
) {
}
