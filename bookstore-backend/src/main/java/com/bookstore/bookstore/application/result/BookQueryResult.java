package com.bookstore.bookstore.application.result;

import com.bookstore.bookstore.domain.model.Book;

public record BookQueryResult(
        Book book,
        long soldCount,
        BookRatingSummaryResult ratingSummary
) {
}
