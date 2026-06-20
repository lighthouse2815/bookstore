package com.bookstore.bookstore.application.command;

import com.bookstore.bookstore.domain.enums.MessageType;
import java.util.UUID;

public record SendChatMessageCommand(
        UUID conversationId,
        UUID senderId,
        MessageType messageType,
        String content,
        String attachmentUrl,
        String attachmentName,
        Long attachmentSize
) {
}
