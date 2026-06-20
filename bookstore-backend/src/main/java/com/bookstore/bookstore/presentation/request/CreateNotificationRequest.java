package com.bookstore.bookstore.presentation.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.util.UUID;

public record CreateNotificationRequest(
        @NotNull(message = "userId khong duoc null")
        UUID userId,
        @NotBlank(message = "title khong duoc de trong")
        String title,
        @NotBlank(message = "content khong duoc de trong")
        String content,
        String type,
        String targetType,
        UUID targetId,
        String link
) {
}
