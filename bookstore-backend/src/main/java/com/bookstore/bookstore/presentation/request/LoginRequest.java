package com.bookstore.bookstore.presentation.request;

import jakarta.validation.constraints.NotBlank;

public record LoginRequest(
        @NotBlank(message = "username không được để trống")
        String username,

        @NotBlank(message = "password không được để trống")
        String password
) {
}

