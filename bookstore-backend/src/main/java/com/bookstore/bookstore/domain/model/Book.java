package com.bookstore.bookstore.domain.model;

import com.bookstore.bookstore.domain.exception.DomainErrorCode;
import com.bookstore.bookstore.domain.rule.BookRule;
import com.bookstore.bookstore.domain.validation.Guard;
import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;
import lombok.Getter;

@Getter
public class Book {

    private UUID id;
    private String title;
    private String description;
    private BigDecimal price;
    private Integer stockQuantity;
    private String imageUrl;
    private UUID categoryId;
    private UUID authorId;
    private UUID publisherId;
    private Instant createdAt;
    private Instant updatedAt;
    private Instant deletedAt;

    public Book(
            UUID id,
            String title,
            String description,
            BigDecimal price,
            Integer stockQuantity,
            String imageUrl,
            UUID categoryId,
            UUID authorId,
            UUID publisherId,
            Instant createdAt,
            Instant updatedAt,
            Instant deletedAt
    ) {
        this.id = Guard.notNull(id, DomainErrorCode.INVALID_BOOK_ID, "id");
        setTitle(title);
        setDescription(description);
        setPrice(price);
        setStockQuantity(stockQuantity);
        setImageUrl(imageUrl);
        setCategoryId(categoryId);
        setAuthorId(authorId);
        setPublisherId(publisherId);
        setCreatedAt(createdAt);
        setUpdatedAt(updatedAt);
        setDeletedAt(deletedAt);
    }

    public void updateBook(
            String title,
            String description,
            BigDecimal price,
            Integer stockQuantity,
            String imageUrl,
            UUID categoryId,
            UUID authorId,
            UUID publisherId
    ) {
        BookRule.requireCanUpdate(
                deletedAt,
                this.title,
                this.description,
                this.price,
                this.stockQuantity,
                this.imageUrl,
                this.categoryId,
                this.authorId,
                this.publisherId,
                title,
                description,
                price,
                stockQuantity,
                imageUrl,
                categoryId,
                authorId,
                publisherId
        );
        setTitle(title);
        setDescription(description);
        setPrice(price);
        setStockQuantity(stockQuantity);
        setImageUrl(imageUrl);
        setCategoryId(categoryId);
        setAuthorId(authorId);
        setPublisherId(publisherId);
        setUpdatedAt(Instant.now());
    }

    public void softDelete() {
        BookRule.requireCanSoftDelete(deletedAt);
        Instant now = Instant.now();
        setUpdatedAt(now);
        setDeletedAt(now);
    }

    public void decreaseStock(int quantity) {
        BookRule.requirePositiveStockDecreaseQuantity(quantity);
        BookRule.requireEnoughStock(stockQuantity, quantity);
        setStockQuantity(stockQuantity - quantity);
        setUpdatedAt(Instant.now());
    }

    public void increaseStock(int quantity) {
        BookRule.requirePositiveStockIncreaseQuantity(quantity);
        setStockQuantity(stockQuantity + quantity);
        setUpdatedAt(Instant.now());
    }

    private void setTitle(String title) {
        this.title = Guard.notBlank(title, DomainErrorCode.INVALID_BOOK_TITLE, "title");
    }

    private void setDescription(String description) {
        this.description = description;
    }

    private void setPrice(BigDecimal price) {
        BigDecimal normalized = Guard.notNull(price, DomainErrorCode.INVALID_BOOK_PRICE, "price");
        if (normalized.compareTo(BigDecimal.ZERO) < 0) {
            throw new com.bookstore.bookstore.domain.exception.DomainException(
                    DomainErrorCode.INVALID_BOOK_PRICE,
                    "price"
            );
        }
        this.price = normalized;
    }

    private void setStockQuantity(Integer stockQuantity) {
        Integer normalized = Guard.notNull(
                stockQuantity,
                DomainErrorCode.INVALID_BOOK_STOCK_QUANTITY,
                "stockQuantity"
        );
        if (normalized < 0) {
            throw new com.bookstore.bookstore.domain.exception.DomainException(
                    DomainErrorCode.INVALID_BOOK_STOCK_QUANTITY,
                    "stockQuantity"
            );
        }
        this.stockQuantity = normalized;
    }

    private void setImageUrl(String imageUrl) {
        this.imageUrl = imageUrl;
    }

    private void setCategoryId(UUID categoryId) {
        this.categoryId = Guard.notNull(categoryId, DomainErrorCode.INVALID_BOOK_CATEGORY_ID, "categoryId");
    }

    private void setAuthorId(UUID authorId) {
        this.authorId = Guard.notNull(authorId, DomainErrorCode.INVALID_BOOK_AUTHOR_ID, "authorId");
    }

    private void setPublisherId(UUID publisherId) {
        this.publisherId = Guard.notNull(publisherId, DomainErrorCode.INVALID_BOOK_PUBLISHER_ID, "publisherId");
    }

    private void setCreatedAt(Instant createdAt) {
        Instant validCreatedAt = Guard.notInFuture(
                createdAt,
                DomainErrorCode.INVALID_BOOK_CREATED_AT,
                "createdAt"
        );
        Guard.validateAuditTimestamps(
                validCreatedAt,
                this.updatedAt,
                this.deletedAt,
                DomainErrorCode.INVALID_BOOK_CREATED_AT,
                DomainErrorCode.INVALID_BOOK_UPDATED_AT,
                DomainErrorCode.INVALID_BOOK_DELETED_AT,
                DomainErrorCode.INVALID_BOOK_AUDIT_ORDER
        );
        this.createdAt = validCreatedAt;
    }

    private void setUpdatedAt(Instant updatedAt) {
        Instant validUpdatedAt = Guard.notInFutureOrNull(
                updatedAt,
                DomainErrorCode.INVALID_BOOK_UPDATED_AT,
                "updatedAt"
        );
        Guard.validateAuditTimestamps(
                this.createdAt,
                validUpdatedAt,
                this.deletedAt,
                DomainErrorCode.INVALID_BOOK_CREATED_AT,
                DomainErrorCode.INVALID_BOOK_UPDATED_AT,
                DomainErrorCode.INVALID_BOOK_DELETED_AT,
                DomainErrorCode.INVALID_BOOK_AUDIT_ORDER
        );
        this.updatedAt = validUpdatedAt;
    }

    private void setDeletedAt(Instant deletedAt) {
        Instant validDeletedAt = Guard.notInFutureOrNull(
                deletedAt,
                DomainErrorCode.INVALID_BOOK_DELETED_AT,
                "deletedAt"
        );
        Guard.validateAuditTimestamps(
                this.createdAt,
                this.updatedAt,
                validDeletedAt,
                DomainErrorCode.INVALID_BOOK_CREATED_AT,
                DomainErrorCode.INVALID_BOOK_UPDATED_AT,
                DomainErrorCode.INVALID_BOOK_DELETED_AT,
                DomainErrorCode.INVALID_BOOK_AUDIT_ORDER
        );
        this.deletedAt = validDeletedAt;
    }
}
