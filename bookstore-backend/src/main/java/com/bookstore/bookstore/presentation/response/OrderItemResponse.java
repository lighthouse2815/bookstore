package com.bookstore.bookstore.presentation.response;

import com.bookstore.bookstore.domain.enums.PurchaseItemType;
import java.math.BigDecimal;
import java.util.UUID;

public record OrderItemResponse(
        UUID id,
        PurchaseItemType itemType,
        UUID bookId,
        UUID digitalAssetId,
        String bookTitle,
        BigDecimal unitPrice,
        int quantity,
        BigDecimal lineTotal
) {
}
