package com.bookstore.bookstore.presentation.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record ResetPasswordRequest(
        @NotBlank(message = "reset token khong duoc de trong")
        String resetToken,

        @NotBlank(message = "mat khau moi khong duoc de trong")
        @Size(min = 8, max = 72, message = "mat khau moi phai tu 8 den 72 ky tu")
        String newPassword
) {
}
