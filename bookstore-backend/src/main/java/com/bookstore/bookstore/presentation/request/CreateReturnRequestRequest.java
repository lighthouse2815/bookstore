package com.bookstore.bookstore.presentation.request;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import java.math.BigDecimal;

public record CreateReturnRequestRequest(
        @NotBlank(message = "Lý do không được để trống")
        @Size(max = 1000, message = "Lý do không được vượt quá 1000 ký tự")
        String reason,

        @DecimalMin(value = "0.0", inclusive = true, message = "Số tiền yêu cầu hoàn không hợp lệ")
        BigDecimal requestedRefundAmount
) {
}
