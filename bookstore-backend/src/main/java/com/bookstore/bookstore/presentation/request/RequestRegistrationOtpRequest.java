package com.bookstore.bookstore.presentation.request;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;

public record RequestRegistrationOtpRequest(
        @NotBlank(message = "email khong duoc de trong")
        @Email(message = "email khong hop le")
        String email
) {
}
