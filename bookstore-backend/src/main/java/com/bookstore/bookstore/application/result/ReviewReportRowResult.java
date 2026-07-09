package com.bookstore.bookstore.application.result;

import com.bookstore.bookstore.domain.enums.ReviewStatus;
import java.time.Instant;

public record ReviewReportRowResult(
        String bookTitle,
        String username,
        int rating,
        ReviewStatus status,
        Instant createdAt,
        String moderationReason
) {
}
