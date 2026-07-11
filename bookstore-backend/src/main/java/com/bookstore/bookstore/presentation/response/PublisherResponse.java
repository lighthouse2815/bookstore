package com.bookstore.bookstore.presentation.response;

import java.time.Instant;
import java.util.UUID;

public record PublisherResponse(
        UUID id,
        String name,
        String description,
        UUID logoFileAssetId,
        String logoUrl,
        Instant createdAt,
        Instant updatedAt
) {
}
