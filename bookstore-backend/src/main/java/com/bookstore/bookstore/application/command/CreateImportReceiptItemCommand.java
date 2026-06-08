package com.bookstore.bookstore.application.command;

import java.math.BigDecimal;
import java.util.UUID;

public record CreateImportReceiptItemCommand(
        UUID bookId,
        BigDecimal unitCost,
        Integer quantity
) {
}
