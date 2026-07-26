package com.bookstore.bookstore.presentation.response;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

public record PublicReviewResponse(
        UUID reviewId,
        String reviewerName,
        String reviewerAvatarUrl,
        boolean verifiedPurchase,
        List<String> reviewImages,
        long helpfulCount,
        int rating,
        String comment,
        Instant createdAt,
        Instant updatedAt
) {
}
