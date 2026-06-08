package com.bookstore.bookstore.application.result;

import java.math.BigDecimal;
import java.util.UUID;

public record OrderItemResult(
        UUID id,
        UUID bookId,
        String bookTitle,
        BigDecimal unitPrice,
        int quantity,
        BigDecimal lineTotal
) {
}
