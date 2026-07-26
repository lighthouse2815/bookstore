package com.bookstore.bookstore.presentation.request;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record RegisterRequest(
        @NotBlank(message = "email không được để trống")
        @Email(message = "email không hợp lệ")
        String email,

        @NotBlank(message = "password không được để trống")
        @Size(min = 8, max = 72, message = "password phải tu 8 den 72 ký tự")
        String password
) {
}

