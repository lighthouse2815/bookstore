package com.bookstore.bookstore.presentation.request;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import java.math.BigDecimal;
import java.util.UUID;

public record CreateImportReceiptItemRequest(
        @NotNull(message = "bookId không được null")
        UUID bookId,
        @NotNull(message = "unitCost không được null")
        @DecimalMin(value = "0.0", message = "unitCost không được âm")
        BigDecimal unitCost,
        @NotNull(message = "quantity không được null")
        @Positive(message = "quantity phải lớn hơn 0")
        Integer quantity
) {
}

