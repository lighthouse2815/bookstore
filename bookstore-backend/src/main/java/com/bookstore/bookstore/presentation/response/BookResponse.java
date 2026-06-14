package com.bookstore.bookstore.presentation.response;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;
import java.util.Map;
import java.util.UUID;

public record BookResponse(
        UUID id,
        String title,
        String isbn,
        String description,
        BigDecimal price,
        Integer stockQuantity,
        long soldCount,
        BigDecimal averageRating,
        long reviewCount,
        Map<Integer, Long> starBreakdown,
        String imageUrl,
        List<BookImageResponse> images,
        BookDetailResponse detail,
        UUID categoryId,
        UUID authorId,
        UUID publisherId,
        Instant createdAt,
        Instant updatedAt
) {
}
