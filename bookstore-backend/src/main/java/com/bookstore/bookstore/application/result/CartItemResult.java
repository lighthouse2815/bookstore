package com.bookstore.bookstore.application.result;

import java.math.BigDecimal;
import java.util.UUID;

public record CartItemResult(
        UUID id,
        UUID bookId,
        String bookTitle,
        String imageUrl,
        BigDecimal price,
        int quantity,
        BigDecimal lineTotal
) {
}
