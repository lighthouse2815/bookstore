package com.bookstore.bookstore.domain.model;

import com.bookstore.bookstore.domain.enums.PurchaseItemType;
import com.bookstore.bookstore.domain.exception.DomainErrorCode;
import com.bookstore.bookstore.domain.exception.DomainException;
import com.bookstore.bookstore.domain.rule.CartItemRule;
import com.bookstore.bookstore.domain.validation.Guard;
import java.time.Instant;
import java.util.UUID;
import lombok.Getter;

@Getter
public class CartItem {

    private UUID id;
    private PurchaseItemType itemType;
    private UUID bookId;
    private UUID digitalAssetId;
    private int quantity;
    private Instant createdAt;
    private Instant updatedAt;

    public CartItem(
            UUID id,
            PurchaseItemType itemType,
            UUID bookId,
            UUID digitalAssetId,
            int quantity,
            Instant createdAt,
            Instant updatedAt
    ) {
        this.id = Guard.notNull(id, DomainErrorCode.INVALID_CART_ITEM_ID, "id");
        setItemType(itemType);
        setReferences(bookId, digitalAssetId);
        setQuantity(quantity);
        setCreatedAt(createdAt);
        setUpdatedAt(updatedAt);
    }

    public CartItem(
            UUID id,
            UUID bookId,
            int quantity,
            Instant createdAt,
            Instant updatedAt
    ) {
        this(id, PurchaseItemType.PHYSICAL_BOOK, bookId, null, quantity, createdAt, updatedAt);
    }

    public void increaseQuantity(int quantityToAdd, int stockQuantity) {
        requirePhysicalItem();
        CartItemRule.requirePositiveQuantity(quantityToAdd);
        long nextQuantity = (long) this.quantity + quantityToAdd;
        CartItemRule.requireQuantityWithinStock(nextQuantity, stockQuantity);
        this.quantity = (int) nextQuantity;
        setUpdatedAt(Instant.now());
    }

    public void updateQuantity(int quantity, int stockQuantity) {
        requirePhysicalItem();
        CartItemRule.requirePositiveQuantity(quantity);
        CartItemRule.requireQuantityWithinStock(quantity, stockQuantity);
        setQuantity(quantity);
        setUpdatedAt(Instant.now());
    }

    public boolean isPhysicalBook() {
        return itemType == PurchaseItemType.PHYSICAL_BOOK;
    }

    public boolean isDigitalAsset() {
        return itemType == PurchaseItemType.DIGITAL_ASSET;
    }

    private void requirePhysicalItem() {
        if (!isPhysicalBook()) {
            throw new DomainException(DomainErrorCode.INVALID_CART_ITEM_QUANTITY, "quantity");
        }
    }

    private void setItemType(PurchaseItemType itemType) {
        this.itemType = Guard.notNull(itemType, DomainErrorCode.INVALID_CART_ITEM_TYPE, "itemType");
    }

    private void setReferences(UUID bookId, UUID digitalAssetId) {
        if (itemType == PurchaseItemType.DIGITAL_ASSET) {
            this.bookId = bookId;
            this.digitalAssetId = Guard.notNull(
                    digitalAssetId,
                    DomainErrorCode.INVALID_CART_ITEM_DIGITAL_ASSET_ID,
                    "digitalAssetId"
            );
            return;
        }

        this.bookId = Guard.notNull(bookId, DomainErrorCode.INVALID_CART_ITEM_BOOK_ID, "bookId");
        this.digitalAssetId = digitalAssetId;
    }

    private void setQuantity(int quantity) {
        if (itemType == PurchaseItemType.DIGITAL_ASSET) {
            if (quantity != 1) {
                throw new DomainException(DomainErrorCode.INVALID_CART_ITEM_QUANTITY, "quantity");
            }
            this.quantity = 1;
            return;
        }

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
