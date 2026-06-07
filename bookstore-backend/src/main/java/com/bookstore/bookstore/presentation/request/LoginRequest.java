package com.bookstore.bookstore.presentation.request;

import jakarta.validation.constraints.NotBlank;

public record LoginRequest(
        @NotBlank(message = "username khong duoc de trong")
        String username,

        @NotBlank(message = "password khong duoc de trong")
        String password
) {
}
