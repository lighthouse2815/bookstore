package com.bookstore.bookstore.presentation.request;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;

public record UpdateUserRequest(
        @NotBlank(message = "username không được để trống")
        String username,

        @Pattern(regexp = "^\\s*0\\d{9}\\s*$", message = "phoneNumber không hợp lệ")
        String phoneNumber,

        @NotBlank(message = "email không được để trống")
        @Email(message = "email không hợp lệ")
        String email
) {
}

