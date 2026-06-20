package com.bookstore.bookstore.application.result.dashboard;

import java.util.UUID;

public record LowStockBookResult(
        UUID bookId,
        String title,
        int stockQuantity
) {
}
