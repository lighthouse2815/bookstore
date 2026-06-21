package com.bookstore.bookstore.presentation.response;

import com.bookstore.bookstore.domain.enums.DigitalAccessStatus;
import com.bookstore.bookstore.domain.enums.DigitalAccessType;
import com.bookstore.bookstore.domain.enums.DigitalAssetFormat;
import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

public record DigitalLibraryItemResponse(
        UUID digitalAssetId,
        UUID bookId,
        String bookTitle,
        String bookImageUrl,
        String assetTitle,
        DigitalAssetFormat format,
        BigDecimal price,
        boolean downloadAllowed,
        String sampleStorageKey,
        DigitalAccessType accessType,
        DigitalAccessStatus accessStatus,
        UUID sourceOrderId,
        Instant expiresAt,
        Instant acquiredAt,
        ReadingProgressResponse progress
) {
}
