package com.bookstore.bookstore.presentation.request;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;

public record RequestRegistrationOtpRequest(
        @NotBlank(message = "email không được để trống")
        @Email(message = "email không hợp lệ")
        String email
) {
}

