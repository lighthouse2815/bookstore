package com.bookstore.bookstore.domain.model;

import com.bookstore.bookstore.domain.exception.DomainErrorCode;
import com.bookstore.bookstore.domain.rule.CartItemRule;
import com.bookstore.bookstore.domain.validation.Guard;
import java.time.Instant;
import java.util.UUID;
import lombok.Getter;

@Getter
public class CartItem {

    private UUID id;
    private UUID bookId;
    private int quantity;
    private Instant createdAt;
    private Instant updatedAt;

    public CartItem(
            UUID id,
            UUID bookId,
            int quantity,
            Instant createdAt,
            Instant updatedAt
    ) {
        this.id = Guard.notNull(id, DomainErrorCode.INVALID_CART_ITEM_ID, "id");
        setBookId(bookId);
        setQuantity(quantity);
        setCreatedAt(createdAt);
        setUpdatedAt(updatedAt);
    }

    public void increaseQuantity(int quantityToAdd, int stockQuantity) {
        CartItemRule.requirePositiveQuantity(quantityToAdd);
        long nextQuantity = (long) this.quantity + quantityToAdd;
        CartItemRule.requireQuantityWithinStock(nextQuantity, stockQuantity);
        this.quantity = (int) nextQuantity;
        setUpdatedAt(Instant.now());
    }

    public void updateQuantity(int quantity, int stockQuantity) {
        CartItemRule.requirePositiveQuantity(quantity);
        CartItemRule.requireQuantityWithinStock(quantity, stockQuantity);
        setQuantity(quantity);
        setUpdatedAt(Instant.now());
    }

    private void setBookId(UUID bookId) {
        this.bookId = Guard.notNull(bookId, DomainErrorCode.INVALID_CART_ITEM_BOOK_ID, "bookId");
    }

    private void setQuantity(int quantity) {
        CartItemRule.requirePositiveQuantity(quantity);
        this.quantity = quantity;
    }

    private void setCreatedAt(Instant createdAt) {
        Instant validCreatedAt = Guard.notInFuture(
                createdAt,
                DomainErrorCode.INVALID_CART_ITEM_CREATED_AT,
                "createdAt"
        );
        Guard.validateAuditTimestamps(
                validCreatedAt,
                this.updatedAt,
                null,
                DomainErrorCode.INVALID_CART_ITEM_CREATED_AT,
                DomainErrorCode.INVALID_CART_ITEM_UPDATED_AT,
                DomainErrorCode.INVALID_CART_ITEM_UPDATED_AT,
                DomainErrorCode.INVALID_CART_ITEM_AUDIT_ORDER
        );
        this.createdAt = validCreatedAt;
    }

    private void setUpdatedAt(Instant updatedAt) {
        Instant validUpdatedAt = Guard.notInFutureOrNull(
                updatedAt,
                DomainErrorCode.INVALID_CART_ITEM_UPDATED_AT,
                "updatedAt"
        );
        Guard.validateAuditTimestamps(
                this.createdAt,
                validUpdatedAt,
                null,
                DomainErrorCode.INVALID_CART_ITEM_CREATED_AT,
                DomainErrorCode.INVALID_CART_ITEM_UPDATED_AT,
                DomainErrorCode.INVALID_CART_ITEM_UPDATED_AT,
                DomainErrorCode.INVALID_CART_ITEM_AUDIT_ORDER
        );
        this.updatedAt = validUpdatedAt;
    }

}
