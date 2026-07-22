package com.bookstore.bookstore.presentation.response;

import java.time.Instant;
import java.util.UUID;
import java.util.Map;

public record CategoryResponse(
        UUID id,
        String code,
        String name,
        String description,
        Map<String, CategoryTranslationResponse> translations,
        UUID parentId,
        UUID imageFileAssetId,
        String imageUrl,
        Instant createdAt,
        Instant updatedAt
) {
}
