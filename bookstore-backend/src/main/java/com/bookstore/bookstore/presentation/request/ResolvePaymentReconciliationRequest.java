package com.bookstore.bookstore.presentation.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record ResolvePaymentReconciliationRequest(
        @NotBlank(message = "Ghi chú xử lý đối soát không được để trống")
        @Size(max = 1000, message = "Ghi chú xử lý đối soát không được vượt quá 1000 ký tự")
        String resolutionNote
) {
}
