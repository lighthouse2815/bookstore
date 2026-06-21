package com.bookstore.bookstore.presentation.request;

import com.bookstore.bookstore.domain.enums.MessageType;
import jakarta.validation.constraints.NotBlank;

public record SendChatMessageRequest(
        MessageType messageType,
        @NotBlank(message = "content khong duoc de trong")
        String content,
        String attachmentUrl,
        String attachmentName,
        Long attachmentSize
) {
}
