package com.bookstore.bookstore.presentation.request;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record RegisterRequest(
        @NotBlank(message = "email khong duoc de trong")
        @Email(message = "email khong hop le")
        String email,

        @NotBlank(message = "password khong duoc de trong")
        @Size(min = 8, max = 72, message = "password phai tu 8 den 72 ky tu")
        String password
) {
}
