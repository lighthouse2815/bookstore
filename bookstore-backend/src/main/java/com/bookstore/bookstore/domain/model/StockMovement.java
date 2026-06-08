package com.bookstore.bookstore.domain.model;

import com.bookstore.bookstore.domain.enums.StockMovementType;
import com.bookstore.bookstore.domain.exception.DomainErrorCode;
import com.bookstore.bookstore.domain.rule.StockMovementRule;
import com.bookstore.bookstore.domain.validation.Guard;
import java.time.Instant;
import java.util.UUID;
import lombok.Getter;

@Getter
public class StockMovement {

    private UUID id;
    private UUID bookId;
    private StockMovementType type;
    private Integer quantity;
    private Integer beforeQuantity;
    private Integer afterQuantity;
    private UUID referenceId;
    private String referenceType;
    private String note;
    private Instant createdAt;
    private UUID createdBy;

    public StockMovement(
            UUID id,
            UUID bookId,
            StockMovementType type,
            Integer quantity,
            Integer beforeQuantity,
            Integer afterQuantity,
            UUID referenceId,
            String referenceType,
            String note,
            Instant createdAt,
            UUID createdBy
    ) {
        this.id = Guard.notNull(id, DomainErrorCode.INVALID_STOCK_MOVEMENT_ID, "id");
        setBookId(bookId);
        setType(type);
        setQuantity(quantity);
        setBeforeQuantity(beforeQuantity);
        setAfterQuantity(afterQuantity);
        StockMovementRule.requireConsistentQuantities(
                this.type,
                this.quantity,
                this.beforeQuantity,
                this.afterQuantity
        );
        setReferenceId(referenceId);
        setReferenceType(referenceType);
        setNote(note);
        setCreatedAt(createdAt);
        setCreatedBy(createdBy);
    }

    private void setBookId(UUID bookId) {
        this.bookId = Guard.notNull(bookId, DomainErrorCode.INVALID_STOCK_MOVEMENT_BOOK_ID, "bookId");
    }

    private void setType(StockMovementType type) {
        this.type = Guard.notNull(type, DomainErrorCode.INVALID_STOCK_MOVEMENT_TYPE, "type");
    }

    private void setQuantity(Integer quantity) {
        Integer validQuantity = Guard.notNull(quantity, DomainErrorCode.INVALID_STOCK_MOVEMENT_QUANTITY, "quantity");
        StockMovementRule.requirePositiveQuantity(validQuantity);
        this.quantity = validQuantity;
    }

    private void setBeforeQuantity(Integer beforeQuantity) {
        Integer validBeforeQuantity = Guard.notNull(
                beforeQuantity,
                DomainErrorCode.INVALID_STOCK_MOVEMENT_BEFORE_QUANTITY,
                "beforeQuantity"
        );
        StockMovementRule.requireNonNegativeBeforeQuantity(validBeforeQuantity);
        this.beforeQuantity = validBeforeQuantity;
    }

    private void setAfterQuantity(Integer afterQuantity) {
        Integer validAfterQuantity = Guard.notNull(
                afterQuantity,
                DomainErrorCode.INVALID_STOCK_MOVEMENT_AFTER_QUANTITY,
                "afterQuantity"
        );
        StockMovementRule.requireNonNegativeAfterQuantity(validAfterQuantity);
        this.afterQuantity = validAfterQuantity;
    }

    private void setReferenceId(UUID referenceId) {
        this.referenceId = referenceId;
    }

    private void setReferenceType(String referenceType) {
        this.referenceType = Guard.notBlank(
                referenceType,
                DomainErrorCode.INVALID_STOCK_MOVEMENT_REFERENCE_TYPE,
                "referenceType"
        );
    }

    private void setNote(String note) {
        this.note = Guard.notBlankOrNull(note, DomainErrorCode.INVALID_STOCK_MOVEMENT_NOTE, "note");
    }

    private void setCreatedAt(Instant createdAt) {
        this.createdAt = Guard.notInFuture(
                createdAt,
                DomainErrorCode.INVALID_STOCK_MOVEMENT_CREATED_AT,
                "createdAt"
        );
    }

    private void setCreatedBy(UUID createdBy) {
        this.createdBy = Guard.notNull(
                createdBy,
                DomainErrorCode.INVALID_STOCK_MOVEMENT_CREATED_BY,
                "createdBy"
        );
    }
}
