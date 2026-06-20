package com.bookstore.bookstore.presentation.response;

import com.bookstore.bookstore.domain.enums.DigitalAssetFormat;
import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

public record DigitalAssetResponse(
        UUID id,
        UUID bookId,
        DigitalAssetFormat format,
        String title,
        String fileName,
        String storageKey,
        String mimeType,
        Long fileSize,
        String checksum,
        String sampleStorageKey,
        BigDecimal price,
        boolean downloadAllowed,
        boolean published,
        Instant createdAt,
        Instant updatedAt,
        Instant deletedAt
) {
}
