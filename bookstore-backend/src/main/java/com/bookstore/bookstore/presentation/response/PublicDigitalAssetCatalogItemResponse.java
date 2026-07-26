package com.bookstore.bookstore.presentation.response;

import com.bookstore.bookstore.domain.enums.DigitalAssetFormat;
import java.math.BigDecimal;
import java.util.UUID;

public record PublicDigitalAssetCatalogItemResponse(
        UUID id,
        UUID bookId,
        UUID categoryId,
        UUID authorId,
        UUID publisherId,
        DigitalAssetFormat format,
        String title,
        BigDecimal price,
        boolean downloadAllowed,
        boolean purchaseAllowed,
        boolean sampleAvailable,
        String bookTitle,
        String bookDescription,
        String bookImageUrl
) {
}
