package com.bookstore.bookstore.presentation.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record UnsubscribeNewsletterRequest(
        @NotBlank(message = "Mã hủy đăng ký không được để trống")
        @Size(max = 100, message = "Mã hủy đăng ký không hợp lệ")
        String token
) {
}
