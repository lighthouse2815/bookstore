package com.bookstore.bookstore.domain.model;

import com.bookstore.bookstore.domain.exception.DomainErrorCode;
import com.bookstore.bookstore.domain.rule.ImportReceiptItemRule;
import com.bookstore.bookstore.domain.validation.Guard;
import java.math.BigDecimal;
import java.util.UUID;
import lombok.Getter;

@Getter
public class ImportReceiptItem {

    private UUID id;
    private UUID bookId;
    private String bookTitle;
    private BigDecimal unitCost;
    private int quantity;
    private BigDecimal lineTotal;

    public ImportReceiptItem(
            UUID id,
            UUID bookId,
            String bookTitle,
            BigDecimal unitCost,
            int quantity,
            BigDecimal lineTotal
    ) {
        this.id = Guard.notNull(id, DomainErrorCode.INVALID_IMPORT_RECEIPT_ITEM_ID, "id");
        setBookId(bookId);
        setBookTitle(bookTitle);
        setUnitCost(unitCost);
        setQuantity(quantity);
        setLineTotal(lineTotal);
    }

    private void setBookId(UUID bookId) {
        this.bookId = Guard.notNull(bookId, DomainErrorCode.INVALID_IMPORT_RECEIPT_ITEM_BOOK_ID, "bookId");
    }

    private void setBookTitle(String bookTitle) {
        this.bookTitle = Guard.notBlank(
                bookTitle,
                DomainErrorCode.INVALID_IMPORT_RECEIPT_ITEM_BOOK_TITLE,
                "bookTitle"
        );
    }

    private void setUnitCost(BigDecimal unitCost) {
        BigDecimal validUnitCost = Guard.notNull(
                unitCost,
                DomainErrorCode.INVALID_IMPORT_RECEIPT_ITEM_UNIT_COST,
                "unitCost"
        );
        ImportReceiptItemRule.requireNonNegativeUnitCost(validUnitCost);
        this.unitCost = validUnitCost;
    }

    private void setQuantity(int quantity) {
        ImportReceiptItemRule.requirePositiveQuantity(quantity);
        this.quantity = quantity;
    }

    private void setLineTotal(BigDecimal lineTotal) {
        BigDecimal validLineTotal = Guard.notNull(
                lineTotal,
                DomainErrorCode.INVALID_IMPORT_RECEIPT_ITEM_LINE_TOTAL,
                "lineTotal"
        );
        ImportReceiptItemRule.requireMatchingLineTotal(unitCost, quantity, validLineTotal);
        this.lineTotal = validLineTotal;
    }
}
