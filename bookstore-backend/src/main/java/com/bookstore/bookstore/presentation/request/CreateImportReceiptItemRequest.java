package com.bookstore.bookstore.presentation.request;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import java.math.BigDecimal;
import java.util.UUID;

public record CreateImportReceiptItemRequest(
        @NotNull(message = "bookId khong duoc null")
        UUID bookId,
        @NotNull(message = "unitCost khong duoc null")
        @DecimalMin(value = "0.0", message = "unitCost khong duoc am")
        BigDecimal unitCost,
        @NotNull(message = "quantity khong duoc null")
        @Positive(message = "quantity phai lon hon 0")
        Integer quantity
) {
}
