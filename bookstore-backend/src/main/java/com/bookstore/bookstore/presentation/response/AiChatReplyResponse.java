package com.bookstore.bookstore.presentation.response;

import com.bookstore.bookstore.application.result.AiChatReplyStatus;

public record AiChatReplyResponse(
        AiChatReplyStatus status,
        ChatMessageResponse message,
        int remainingRequests
) {
}
