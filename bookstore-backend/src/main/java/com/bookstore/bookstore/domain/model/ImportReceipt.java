package com.bookstore.bookstore.domain.model;

import com.bookstore.bookstore.domain.exception.DomainErrorCode;
import com.bookstore.bookstore.domain.rule.ImportReceiptRule;
import com.bookstore.bookstore.domain.validation.Guard;
import java.math.BigDecimal;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import lombok.Getter;

@Getter
public class ImportReceipt {

    private UUID id;
    private UUID supplierId;
    private List<ImportReceiptItem> items = new ArrayList<>();
    private BigDecimal totalAmount;
    private String note;
    private Instant createdAt;
    private Instant updatedAt;
    private UUID createdBy;

    public ImportReceipt(
            UUID id,
            UUID supplierId,
            List<ImportReceiptItem> items,
            BigDecimal totalAmount,
            String note,
            Instant createdAt,
            Instant updatedAt,
            UUID createdBy
    ) {
        this.id = Guard.notNull(id, DomainErrorCode.INVALID_IMPORT_RECEIPT_ID, "id");
        setSupplierId(supplierId);
        setItems(items);
        setTotalAmount(totalAmount);
        setNote(note);
        setCreatedAt(createdAt);
        setUpdatedAt(updatedAt);
        setCreatedBy(createdBy);
    }

    private void setSupplierId(UUID supplierId) {
        this.supplierId = Guard.notNull(
                supplierId,
                DomainErrorCode.INVALID_IMPORT_RECEIPT_SUPPLIER_ID,
                "supplierId"
        );
    }

    private void setItems(List<ImportReceiptItem> items) {
        List<ImportReceiptItem> validItems = new ArrayList<>(
                Guard.noNullElements(items, DomainErrorCode.INVALID_IMPORT_RECEIPT_ITEMS, "items")
        );
        ImportReceiptRule.requireHasItems(validItems);
        this.items = validItems;
    }

    private void setTotalAmount(BigDecimal totalAmount) {
        BigDecimal validTotalAmount = Guard.notNull(
                totalAmount,
                DomainErrorCode.INVALID_IMPORT_RECEIPT_TOTAL_AMOUNT,
                "totalAmount"
        );
        ImportReceiptRule.requireNonNegativeTotalAmount(validTotalAmount);
        ImportReceiptRule.requireMatchingTotalAmount(items, validTotalAmount);
        this.totalAmount = validTotalAmount;
    }

    private void setNote(String note) {
        this.note = Guard.notBlankOrNull(note, DomainErrorCode.INVALID_IMPORT_RECEIPT_NOTE, "note");
    }

    private void setCreatedAt(Instant createdAt) {
        Instant validCreatedAt = Guard.notInFuture(
                createdAt,
                DomainErrorCode.INVALID_IMPORT_RECEIPT_CREATED_AT,
                "createdAt"
        );
        Guard.validateAuditTimestamps(
                validCreatedAt,
                this.updatedAt,
                null,
                DomainErrorCode.INVALID_IMPORT_RECEIPT_CREATED_AT,
                DomainErrorCode.INVALID_IMPORT_RECEIPT_UPDATED_AT,
                DomainErrorCode.INVALID_IMPORT_RECEIPT_UPDATED_AT,
                DomainErrorCode.INVALID_IMPORT_RECEIPT_AUDIT_ORDER
        );
        this.createdAt = validCreatedAt;
    }

    private void setUpdatedAt(Instant updatedAt) {
        Instant validUpdatedAt = Guard.notInFutureOrNull(
                updatedAt,
                DomainErrorCode.INVALID_IMPORT_RECEIPT_UPDATED_AT,
                "updatedAt"
        );
        Guard.validateAuditTimestamps(
                this.createdAt,
                validUpdatedAt,
                null,
                DomainErrorCode.INVALID_IMPORT_RECEIPT_CREATED_AT,
                DomainErrorCode.INVALID_IMPORT_RECEIPT_UPDATED_AT,
                DomainErrorCode.INVALID_IMPORT_RECEIPT_UPDATED_AT,
                DomainErrorCode.INVALID_IMPORT_RECEIPT_AUDIT_ORDER
        );
        this.updatedAt = validUpdatedAt;
    }

    private void setCreatedBy(UUID createdBy) {
        this.createdBy = Guard.notNull(
                createdBy,
                DomainErrorCode.INVALID_IMPORT_RECEIPT_CREATED_BY,
                "createdBy"
        );
    }
}
