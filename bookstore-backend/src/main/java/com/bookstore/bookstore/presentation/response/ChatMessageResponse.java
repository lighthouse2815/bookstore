package com.bookstore.bookstore.presentation.response;

import com.bookstore.bookstore.domain.enums.MessageSenderRole;
import com.bookstore.bookstore.domain.enums.MessageType;
import java.time.Instant;
import java.util.UUID;

public record ChatMessageResponse(
        UUID messageId,
        UUID conversationId,
        UUID senderId,
        MessageSenderRole senderRole,
        MessageType messageType,
        String content,
        String attachmentUrl,
        String attachmentName,
        Long attachmentSize,
        Instant createdAt,
        Instant updatedAt
) {
}
