package com.bookstore.bookstore.domain.rule;

import com.bookstore.bookstore.domain.exception.DomainErrorCode;
import com.bookstore.bookstore.domain.exception.DomainException;
import java.math.BigDecimal;

public final class OrderItemRule {

    private OrderItemRule() {
    }

    public static void requireNonNegativeUnitPrice(BigDecimal unitPrice) {
        if (unitPrice.compareTo(BigDecimal.ZERO) < 0) {
            throw new DomainException(DomainErrorCode.INVALID_ORDER_ITEM_UNIT_PRICE, "unitPrice");
        }
    }

    public static void requirePositiveQuantity(int quantity) {
        if (quantity <= 0) {
            throw new DomainException(DomainErrorCode.INVALID_ORDER_ITEM_QUANTITY, "quantity");
        }
    }

    public static void requireMatchingLineTotal(BigDecimal unitPrice, int quantity, BigDecimal lineTotal) {
        BigDecimal expectedLineTotal = unitPrice.multiply(BigDecimal.valueOf(quantity));
        if (expectedLineTotal.compareTo(lineTotal) != 0) {
            throw new DomainException(DomainErrorCode.ORDER_ITEM_LINE_TOTAL_MISMATCH);
        }
    }
}
