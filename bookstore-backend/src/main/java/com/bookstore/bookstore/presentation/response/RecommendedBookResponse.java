package com.bookstore.bookstore.presentation.response;

import java.util.List;

public record RecommendedBookResponse(
        BookResponse book,
        List<String> reasonCodes
) {
}
