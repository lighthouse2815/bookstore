package com.bookstore.bookstore.application.result;

import com.bookstore.bookstore.domain.enums.DigitalAssetFormat;
import com.bookstore.bookstore.domain.enums.PurchaseItemType;
import java.math.BigDecimal;
import java.util.UUID;

public record CartItemResult(
        UUID id,
        PurchaseItemType itemType,
        UUID bookId,
        UUID digitalAssetId,
        String bookTitle,
        String assetTitle,
        DigitalAssetFormat format,
        String imageUrl,
        BigDecimal price,
        int quantity,
        BigDecimal lineTotal
) {
}
