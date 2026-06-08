package com.bookstore.bookstore.application.result;

import java.math.BigDecimal;
import java.util.UUID;

public record ImportReceiptItemResult(
        UUID id,
        UUID bookId,
        String bookTitle,
        BigDecimal unitCost,
        Integer quantity,
        BigDecimal lineTotal
) {
}
