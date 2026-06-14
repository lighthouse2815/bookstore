package com.bookstore.bookstore.presentation.response;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;
import java.util.UUID;

public record BookPageDetailResponse(
        DetailBookResponse book,
        AuthorResponse author,
        PublisherSummaryResponse publisher,
        List<CategoryTrailItemResponse> categoryTrail,
        RatingSummaryResponse ratingSummary,
        List<CouponResponse> promotions,
        List<BookResponse> relatedBooks
) {

    public record DetailBookResponse(
            UUID id,
            String title,
            String isbn,
            BigDecimal price,
            BigDecimal originalPrice,
            Integer discountPercent,
            Integer stockQuantity,
            long soldCount,
            String description,
            List<BookImageResponse> images,
            BookDetailResponse detail,
            BigDecimal averageRating,
            long reviewCount
    ) {
    }

    public record PublisherSummaryResponse(
            UUID id,
            String name
    ) {
    }

    public record CategoryTrailItemResponse(
            UUID id,
            String name
    ) {
    }

    public record RatingSummaryResponse(
            BigDecimal averageRating,
            long reviewCount,
            Map<Integer, Long> starBreakdown
    ) {
    }
}
