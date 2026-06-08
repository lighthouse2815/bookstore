package com.bookstore.bookstore.domain.model;

import com.bookstore.bookstore.domain.exception.DomainErrorCode;
import com.bookstore.bookstore.domain.rule.CartRule;
import com.bookstore.bookstore.domain.validation.Guard;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import lombok.Getter;

@Getter
public class Cart {

    private UUID id;
    private UUID userId;
    private List<CartItem> items = new ArrayList<>();
    private Instant createdAt;
    private Instant updatedAt;

    public Cart(
            UUID id,
            UUID userId,
            List<CartItem> items,
            Instant createdAt,
            Instant updatedAt
    ) {
        this.id = Guard.notNull(id, DomainErrorCode.INVALID_CART_ID, "id");
        setUserId(userId);
        setItems(items);
        setCreatedAt(createdAt);
        setUpdatedAt(updatedAt);
    }

    public void addItem(UUID bookId, int quantity, int stockQuantity) {
        Guard.notNull(bookId, DomainErrorCode.INVALID_CART_ITEM_BOOK_ID, "bookId");

        CartItem existingItem = findItemByBookId(bookId);
        if (existingItem != null) {
            existingItem.increaseQuantity(quantity, stockQuantity);
        } else {
            CartRule.requireQuantityWithinStock(quantity, stockQuantity);
            Instant now = Instant.now();
            items.add(new CartItem(
                    UUID.randomUUID(),
                    bookId,
                    quantity,
                    now,
                    now
            ));
        }

        setUpdatedAt(Instant.now());
    }

    public void updateItem(UUID bookId, int quantity, int stockQuantity) {
        Guard.notNull(bookId, DomainErrorCode.INVALID_CART_ITEM_BOOK_ID, "bookId");
        CartItem existingItem = requireExistingItem(bookId);
        existingItem.updateQuantity(quantity, stockQuantity);
        setUpdatedAt(Instant.now());
    }

    public void removeItem(UUID bookId) {
        Guard.notNull(bookId, DomainErrorCode.INVALID_CART_ITEM_BOOK_ID, "bookId");
        CartItem existingItem = requireExistingItem(bookId);
        items.remove(existingItem);
        setUpdatedAt(Instant.now());
    }

    public void clear() {
        if (items.isEmpty()) {
            return;
        }

        items.clear();
        setUpdatedAt(Instant.now());
    }

    private CartItem requireExistingItem(UUID bookId) {
        CartItem existingItem = findItemByBookId(bookId);
        CartRule.requireItemExists(existingItem);
        return existingItem;
    }

    private CartItem findItemByBookId(UUID bookId) {
        return items.stream()
                .filter(item -> item.getBookId().equals(bookId))
                .findFirst()
                .orElse(null);
    }

    private void setUserId(UUID userId) {
        this.userId = Guard.notNull(userId, DomainErrorCode.INVALID_CART_USER_ID, "userId");
    }

    private void setItems(List<CartItem> items) {
        this.items = new ArrayList<>(Guard.noNullElements(items, DomainErrorCode.INVALID_CART_ITEMS, "items"));
    }

    private void setCreatedAt(Instant createdAt) {
        Instant validCreatedAt = Guard.notInFuture(
                createdAt,
                DomainErrorCode.INVALID_CART_CREATED_AT,
                "createdAt"
        );
        Guard.validateAuditTimestamps(
                validCreatedAt,
                this.updatedAt,
                null,
                DomainErrorCode.INVALID_CART_CREATED_AT,
                DomainErrorCode.INVALID_CART_UPDATED_AT,
                DomainErrorCode.INVALID_CART_UPDATED_AT,
                DomainErrorCode.INVALID_CART_AUDIT_ORDER
        );
        this.createdAt = validCreatedAt;
    }

    private void setUpdatedAt(Instant updatedAt) {
        Instant validUpdatedAt = Guard.notInFutureOrNull(
                updatedAt,
                DomainErrorCode.INVALID_CART_UPDATED_AT,
                "updatedAt"
        );
        Guard.validateAuditTimestamps(
                this.createdAt,
                validUpdatedAt,
                null,
                DomainErrorCode.INVALID_CART_CREATED_AT,
                DomainErrorCode.INVALID_CART_UPDATED_AT,
                DomainErrorCode.INVALID_CART_UPDATED_AT,
                DomainErrorCode.INVALID_CART_AUDIT_ORDER
        );
        this.updatedAt = validUpdatedAt;
    }

}
