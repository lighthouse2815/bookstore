package com.bookstore.bookstore.presentation.response;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

public record ReadingProgressResponse(
        UUID id,
        UUID userId,
        UUID digitalAssetId,
        Integer currentPage,
        BigDecimal progressPercent,
        String positionData,
        Instant lastReadAt,
        Instant createdAt,
        Instant updatedAt
) {
}
