package com.bookstore.bookstore.application.command;

import com.bookstore.bookstore.application.exception.ApplicationErrorCode;
import com.bookstore.bookstore.application.exception.ApplicationException;
import com.bookstore.bookstore.domain.enums.PurchaseItemType;
import java.util.UUID;

public record AddCartItemCommand(
        UUID userId,
        PurchaseItemType itemType,
        UUID bookId,
        UUID digitalAssetId,
        int quantity
) {
    public AddCartItemCommand {
        if (userId == null) {
            throw new ApplicationException(ApplicationErrorCode.INVALID_ARGUMENT, "userId");
        }
        if (itemType == null) {
            throw new ApplicationException(ApplicationErrorCode.INVALID_ARGUMENT, "itemType");
        }

        if (itemType == PurchaseItemType.DIGITAL_ASSET) {
            if (digitalAssetId == null) {
                throw new ApplicationException(ApplicationErrorCode.INVALID_ARGUMENT, "digitalAssetId");
            }
            quantity = 1;
        } else {
            if (bookId == null) {
                throw new ApplicationException(ApplicationErrorCode.INVALID_ARGUMENT, "bookId");
            }
            if (quantity <= 0) {
                throw new ApplicationException(ApplicationErrorCode.INVALID_ARGUMENT, "quantity");
            }
        }
    }

    public AddCartItemCommand(UUID userId, UUID bookId, int quantity) {
        this(userId, PurchaseItemType.PHYSICAL_BOOK, bookId, null, quantity);
    }

    public static AddCartItemCommand digital(UUID userId, UUID digitalAssetId) {
        return new AddCartItemCommand(userId, PurchaseItemType.DIGITAL_ASSET, null, digitalAssetId, 1);
    }
}
