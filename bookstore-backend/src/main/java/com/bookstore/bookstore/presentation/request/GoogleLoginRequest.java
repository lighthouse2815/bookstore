package com.bookstore.bookstore.presentation.request;

import jakarta.validation.constraints.NotBlank;

public record GoogleLoginRequest(
        @NotBlank(message = "idToken không được để trống")
        String idToken
) {
}

