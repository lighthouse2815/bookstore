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
        UUID fileAssetId,
        UUID sampleFileAssetId,
        String fileName,
        String mimeType,
        Long fileSize,
        String checksum,
        BigDecimal price,
        boolean downloadAllowed,
        boolean purchaseAllowed,
        boolean published,
        Instant createdAt,
        Instant updatedAt,
        Instant deletedAt
) {
}
