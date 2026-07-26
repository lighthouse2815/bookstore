package com.bookstore.bookstore.application.result;

import com.bookstore.bookstore.domain.enums.ReviewStatus;
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
        ReviewStatus status,
        String moderationReason,
        UUID moderatedBy,
        String moderatedByName,
        Instant moderatedAt,
        Instant createdAt,
        Instant updatedAt
) {
}
