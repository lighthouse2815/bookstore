package com.bookstore.bookstore.presentation.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record RejectReturnRequestRequest(
        @NotBlank(message = "Lý do từ chối không được để trống")
        @Size(max = 1000, message = "Lý do từ chối không được vượt quá 1000 ký tự")
        String adminNote
) {
}
