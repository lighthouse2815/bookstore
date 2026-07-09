package com.bookstore.bookstore.application.result;

import java.util.UUID;

public record LowStockReportRowResult(
        UUID bookId,
        String title,
        String isbn,
        int stockQuantity,
        String categoryName
) {
}
