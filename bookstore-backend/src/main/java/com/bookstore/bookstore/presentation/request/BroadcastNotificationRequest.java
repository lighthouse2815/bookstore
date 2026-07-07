package com.bookstore.bookstore.presentation.request;

import jakarta.validation.constraints.NotBlank;
import java.util.UUID;

public record BroadcastNotificationRequest(
        @NotBlank(message = "title không được để trống")
        String title,
        @NotBlank(message = "content không được để trống")
        String content,
        String type,
        String targetType,
        UUID targetId,
        String link
) {
}

