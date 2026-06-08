package com.bookstore.bookstore.domain.model;

import com.bookstore.bookstore.domain.exception.DomainErrorCode;
import com.bookstore.bookstore.domain.rule.OrderItemRule;
import com.bookstore.bookstore.domain.validation.Guard;
import java.math.BigDecimal;
import java.util.UUID;
import lombok.Getter;

@Getter
public class OrderItem {

    private UUID id;
    private UUID bookId;
    private String bookTitle;
    private BigDecimal unitPrice;
    private int quantity;
    private BigDecimal lineTotal;

    public OrderItem(
            UUID id,
            UUID bookId,
            String bookTitle,
            BigDecimal unitPrice,
            int quantity,
            BigDecimal lineTotal
    ) {
        this.id = Guard.notNull(id, DomainErrorCode.INVALID_ORDER_ITEM_ID, "id");
        setBookId(bookId);
        setBookTitle(bookTitle);
        setUnitPrice(unitPrice);
        setQuantity(quantity);
        setLineTotal(lineTotal);
    }

    private void setBookId(UUID bookId) {
        this.bookId = Guard.notNull(bookId, DomainErrorCode.INVALID_ORDER_ITEM_BOOK_ID, "bookId");
    }

    private void setBookTitle(String bookTitle) {
        this.bookTitle = Guard.notBlank(bookTitle, DomainErrorCode.INVALID_ORDER_ITEM_BOOK_TITLE, "bookTitle");
    }

    private void setUnitPrice(BigDecimal unitPrice) {
        BigDecimal validUnitPrice = Guard.notNull(
                unitPrice,
                DomainErrorCode.INVALID_ORDER_ITEM_UNIT_PRICE,
                "unitPrice"
        );
        OrderItemRule.requireNonNegativeUnitPrice(validUnitPrice);
        this.unitPrice = validUnitPrice;
    }

    private void setQuantity(int quantity) {
        OrderItemRule.requirePositiveQuantity(quantity);
        this.quantity = quantity;
    }

    private void setLineTotal(BigDecimal lineTotal) {
        BigDecimal validLineTotal = Guard.notNull(
                lineTotal,
                DomainErrorCode.INVALID_ORDER_ITEM_LINE_TOTAL,
                "lineTotal"
        );
        OrderItemRule.requireMatchingLineTotal(unitPrice, quantity, validLineTotal);
        this.lineTotal = validLineTotal;
    }
}
