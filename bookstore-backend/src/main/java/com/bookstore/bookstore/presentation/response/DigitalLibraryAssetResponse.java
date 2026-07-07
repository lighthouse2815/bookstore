package com.bookstore.bookstore.presentation.response;

import com.bookstore.bookstore.domain.enums.DigitalAccessStatus;
import com.bookstore.bookstore.domain.enums.DigitalAccessType;
import com.bookstore.bookstore.domain.enums.DigitalAssetFormat;
import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

public record DigitalLibraryAssetResponse(
        UUID digitalAssetId,
        UUID bookId,
        String bookTitle,
        String bookDescription,
        String bookImageUrl,
        String assetTitle,
        DigitalAssetFormat format,
        String fileName,
        String mimeType,
        Long fileSize,
        BigDecimal price,
        boolean downloadAllowed,
        boolean sampleAvailable,
        DigitalAccessType accessType,
        DigitalAccessStatus accessStatus,
        UUID sourceOrderId,
        Instant expiresAt,
        Instant acquiredAt,
        Instant assetUpdatedAt,
        ReadingProgressResponse progress
) {
}
