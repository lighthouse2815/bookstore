package com.bookstore.bookstore.domain.rule;

import com.bookstore.bookstore.domain.exception.DomainErrorCode;
import com.bookstore.bookstore.domain.exception.DomainException;

public final class CartItemRule {

    private CartItemRule() {
    }

    public static void requirePositiveQuantity(int quantity) {
        if (quantity <= 0) {
            throw new DomainException(DomainErrorCode.INVALID_CART_ITEM_QUANTITY, "quantity");
        }
    }

    public static void requireQuantityWithinStock(long quantity, int stockQuantity) {
        if (stockQuantity < 0 || quantity > stockQuantity) {
            throw new DomainException(DomainErrorCode.CART_ITEM_QUANTITY_EXCEEDS_STOCK);
        }
    }
}
