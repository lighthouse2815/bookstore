package com.bookstore.bookstore.presentation.request;

import jakarta.validation.constraints.NotBlank;

public record GoogleLoginRequest(
        @NotBlank(message = "idToken khong duoc de trong")
        String idToken
) {
}
