package com.bookstore.bookstore.application.result;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

public record ReviewResult(
        UUID reviewId,
        UUID userId,
        UUID bookId,
        UUID orderItemId,
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
