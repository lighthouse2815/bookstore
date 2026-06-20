package com.bookstore.bookstore.presentation.response;

import com.bookstore.bookstore.domain.enums.DigitalAssetFormat;
import java.math.BigDecimal;
import java.util.UUID;

public record PublishedDigitalAssetResponse(
        UUID id,
        UUID bookId,
        DigitalAssetFormat format,
        String title,
        String fileName,
        String sampleStorageKey,
        BigDecimal price,
        boolean downloadAllowed
) {
}
