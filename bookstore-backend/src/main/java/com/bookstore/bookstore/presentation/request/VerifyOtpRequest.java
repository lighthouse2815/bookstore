package com.bookstore.bookstore.presentation.request;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;

public record VerifyOtpRequest(
        @NotBlank(message = "email không được để trống")
        @Email(message = "email không hợp lệ")
        String email,

        @NotBlank(message = "otp không được để trống")
        @Pattern(regexp = "\\d{6}", message = "otp phải gồm đúng 6 chữ số")
        String otpCode
) {
}

