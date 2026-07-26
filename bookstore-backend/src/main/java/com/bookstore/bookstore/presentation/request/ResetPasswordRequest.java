package com.bookstore.bookstore.presentation.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record ResetPasswordRequest(
        @NotBlank(message = "reset token không được để trống")
        String resetToken,

        @NotBlank(message = "mật khẩu mới không được để trống")
        @Size(min = 8, max = 72, message = "mật khẩu mới phải tu 8 den 72 ký tự")
        String newPassword
) {
}

