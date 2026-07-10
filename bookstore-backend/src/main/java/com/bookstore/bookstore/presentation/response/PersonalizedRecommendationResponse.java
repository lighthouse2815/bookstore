package com.bookstore.bookstore.presentation.response;

import java.time.Instant;
import java.util.List;

public record PersonalizedRecommendationResponse(
        List<RecommendedBookResponse> items,
        String strategy,
        boolean hasPersonalSignals,
        Instant generatedAt
) {
}
