package com.bookstore.bookstore.application.result;

import com.bookstore.bookstore.domain.model.Author;
import com.bookstore.bookstore.domain.model.Category;
import com.bookstore.bookstore.domain.model.Coupon;
import com.bookstore.bookstore.domain.model.Publisher;
import java.util.List;

public record BookPageDetailResult(
        BookQueryResult book,
        Author author,
        Publisher publisher,
        List<Category> categoryTrail,
        BookRatingSummaryResult ratingSummary,
        List<Coupon> promotions,
        List<BookQueryResult> relatedBooks
) {
}
