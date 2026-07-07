package com.bookstore.bookstore.domain.model;

import com.bookstore.bookstore.domain.enums.PurchaseItemType;
import com.bookstore.bookstore.domain.exception.DomainErrorCode;
import com.bookstore.bookstore.domain.exception.DomainException;
import com.bookstore.bookstore.domain.rule.OrderItemRule;
import com.bookstore.bookstore.domain.validation.Guard;
import java.math.BigDecimal;
import java.util.UUID;
import lombok.Getter;

@Getter
public class OrderItem {

    private UUID id;
    private PurchaseItemType itemType;
    private UUID bookId;
    private UUID digitalAssetId;
    private String bookTitle;
    private BigDecimal unitPrice;
    private int quantity;
    private BigDecimal lineTotal;

    public OrderItem(
            UUID id,
            PurchaseItemType itemType,
            UUID bookId,
            UUID digitalAssetId,
            String bookTitle,
            BigDecimal unitPrice,
            int quantity,
            BigDecimal lineTotal
    ) {
        this.id = Guard.notNull(id, DomainErrorCode.INVALID_ORDER_ITEM_ID, "id");
        setItemType(itemType);
        setBookId(bookId);
        setDigitalAssetId(digitalAssetId);
        setBookTitle(bookTitle);
        setUnitPrice(unitPrice);
        setQuantity(quantity);
        setLineTotal(lineTotal);
    }

    public OrderItem(
            UUID id,
            UUID bookId,
            String bookTitle,
            BigDecimal unitPrice,
            int quantity,
            BigDecimal lineTotal
    ) {
        this(id, PurchaseItemType.PHYSICAL_BOOK, bookId, null, bookTitle, unitPrice, quantity, lineTotal);
    }

    public boolean isDigitalAsset() {
        return itemType == PurchaseItemType.DIGITAL_ASSET;
    }

    private void setItemType(PurchaseItemType itemType) {
        this.itemType = Guard.notNull(itemType, DomainErrorCode.INVALID_ORDER_ITEM_TYPE, "itemType");
    }

    private void setBookId(UUID bookId) {
        this.bookId = Guard.notNull(bookId, DomainErrorCode.INVALID_ORDER_ITEM_BOOK_ID, "bookId");
    }

    private void setDigitalAssetId(UUID digitalAssetId) {
        if (itemType == PurchaseItemType.DIGITAL_ASSET) {
            this.digitalAssetId = Guard.notNull(
                    digitalAssetId,
                    DomainErrorCode.INVALID_ORDER_ITEM_DIGITAL_ASSET_ID,
                    "digitalAssetId"
            );
            return;
        }
        this.digitalAssetId = digitalAssetId;
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
        if (itemType == PurchaseItemType.DIGITAL_ASSET) {
            if (quantity != 1) {
                throw new DomainException(DomainErrorCode.INVALID_ORDER_ITEM_QUANTITY, "quantity");
            }
            this.quantity = 1;
            return;
        }

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
