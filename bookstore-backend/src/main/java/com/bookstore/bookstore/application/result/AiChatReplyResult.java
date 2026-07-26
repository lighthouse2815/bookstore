package com.bookstore.bookstore.application.result;

public record AiChatReplyResult(
        AiChatReplyStatus status,
        ChatMessageResult message,
        int remainingRequests
) {
}
