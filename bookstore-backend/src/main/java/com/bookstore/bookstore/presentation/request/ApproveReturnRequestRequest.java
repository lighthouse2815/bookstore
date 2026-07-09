package com.bookstore.bookstore.presentation.request;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Size;
import java.math.BigDecimal;

public record ApproveReturnRequestRequest(
        @Size(max = 1000, message = "Ghi chú không được vượt quá 1000 ký tự")
        String adminNote,

        @DecimalMin(value = "0.0", inclusive = true, message = "Số tiền hoàn không hợp lệ")
        BigDecimal approvedRefundAmount,

        boolean restock
) {
}
