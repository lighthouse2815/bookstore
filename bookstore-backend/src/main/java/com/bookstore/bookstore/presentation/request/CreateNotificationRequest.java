package com.bookstore.bookstore.presentation.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.util.UUID;

public record CreateNotificationRequest(
        @NotNull(message = "userId không được null")
        UUID userId,
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

