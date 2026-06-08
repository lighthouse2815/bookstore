package com.bookstore.bookstore.domain.rule;

import com.bookstore.bookstore.domain.exception.DomainErrorCode;
import com.bookstore.bookstore.domain.exception.DomainException;
import com.bookstore.bookstore.domain.model.CartItem;

public final class CartRule {

    private CartRule() {
    }

    public static void requireItemExists(CartItem item) {
        if (item == null) {
            throw new DomainException(DomainErrorCode.CART_ITEM_NOT_FOUND);
        }
    }

    public static void requireQuantityWithinStock(int quantity, int stockQuantity) {
        if (stockQuantity < 0 || quantity > stockQuantity) {
            throw new DomainException(DomainErrorCode.CART_ITEM_QUANTITY_EXCEEDS_STOCK);
        }
    }
}
