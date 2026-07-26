package com.bookstore.bookstore.presentation.response.dashboard;

import java.util.UUID;

public record LowStockBookResponse(
        UUID bookId,
        String title,
        int stockQuantity
) {
}
