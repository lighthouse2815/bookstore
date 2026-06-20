package com.bookstore.bookstore.presentation.request;

import jakarta.validation.constraints.NotBlank;
import java.util.UUID;

public record BroadcastNotificationRequest(
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
