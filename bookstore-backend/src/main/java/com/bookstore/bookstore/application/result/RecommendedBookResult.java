package com.bookstore.bookstore.application.result;

import java.util.List;

public record RecommendedBookResult(
        BookQueryResult book,
        List<RecommendationReasonCode> reasonCodes
) {
}
