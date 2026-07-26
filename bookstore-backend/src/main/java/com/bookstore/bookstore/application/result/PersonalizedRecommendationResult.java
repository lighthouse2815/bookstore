package com.bookstore.bookstore.application.result;

import java.time.Instant;
import java.util.List;

public record PersonalizedRecommendationResult(
        List<RecommendedBookResult> items,
        RecommendationStrategy strategy,
        boolean hasPersonalSignals,
        Instant generatedAt
) {
}
