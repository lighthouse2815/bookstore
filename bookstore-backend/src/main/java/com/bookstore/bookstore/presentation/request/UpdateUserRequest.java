package com.bookstore.bookstore.presentation.request;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;

public record UpdateUserRequest(
        @NotBlank(message = "username khong duoc de trong")
        String username,

        @Pattern(regexp = "^\\s*0\\d{9}\\s*$", message = "phoneNumber khong hop le")
        String phoneNumber,

        @NotBlank(message = "email khong duoc de trong")
        @Email(message = "email khong hop le")
        String email
) {
}
