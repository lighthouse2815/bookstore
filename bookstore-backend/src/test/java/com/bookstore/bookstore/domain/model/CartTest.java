package com.bookstore.bookstore.domain.model;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import com.bookstore.bookstore.domain.exception.DomainErrorCode;
import com.bookstore.bookstore.domain.exception.DomainException;
import java.time.Instant;
import java.util.List;
import java.util.UUID;
import org.junit.jupiter.api.Test;

class CartTest {

    @Test
    void addItem_mergesQuantityWhenBookAlreadyExists() {
        UUID bookId = UUID.randomUUID();
        Cart cart = cart(List.of(item(bookId, 1)));

        cart.addItem(bookId, 2, 5);

        assertEquals(1, cart.getItems().size());
        assertEquals(3, cart.getItems().get(0).getQuantity());
    }

    @Test
    void addItem_rejectsQuantityExceedingStock() {
        Cart cart = cart(List.of());

        DomainException exception = assertThrows(
                DomainException.class,
                () -> cart.addItem(UUID.randomUUID(), 3, 2)
        );

        assertEquals(DomainErrorCode.CART_ITEM_QUANTITY_EXCEEDS_STOCK, exception.getErrorCode());
    }

    @Test
    void removeItem_rejectsMissingBook() {
        Cart cart = cart(List.of());

        DomainException exception = assertThrows(
                DomainException.class,
                () -> cart.removeItem(UUID.randomUUID())
        );

        assertEquals(DomainErrorCode.CART_ITEM_NOT_FOUND, exception.getErrorCode());
    }

    private static Cart cart(List<CartItem> items) {
        Instant now = Instant.EPOCH;
        return new Cart(
                UUID.randomUUID(),
                UUID.randomUUID(),
                items,
                now,
                now
        );
    }

    private static CartItem item(UUID bookId, int quantity) {
        Instant now = Instant.EPOCH;
        return new CartItem(
                UUID.randomUUID(),
                bookId,
                quantity,
                now,
                now
        );
    }
}
