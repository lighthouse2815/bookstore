package com.bookstore.bookstore.presentation.request;

import jakarta.validation.constraints.NotBlank;

public record RefreshTokenRequest(
        @NotBlank(message = "refreshToken không được để trống")
        String refreshToken
) {
}

