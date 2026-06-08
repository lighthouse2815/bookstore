package com.bookstore.bookstore.domain.rule;

import com.bookstore.bookstore.domain.exception.DomainErrorCode;
import com.bookstore.bookstore.domain.exception.DomainException;
import java.math.BigDecimal;

public final class ImportReceiptItemRule {

    private ImportReceiptItemRule() {
    }

    public static void requireNonNegativeUnitCost(BigDecimal unitCost) {
        if (unitCost.compareTo(BigDecimal.ZERO) < 0) {
            throw new DomainException(DomainErrorCode.INVALID_IMPORT_RECEIPT_ITEM_UNIT_COST, "unitCost");
        }
    }

    public static void requirePositiveQuantity(int quantity) {
        if (quantity <= 0) {
            throw new DomainException(DomainErrorCode.INVALID_IMPORT_RECEIPT_ITEM_QUANTITY, "quantity");
        }
    }

    public static void requireMatchingLineTotal(BigDecimal unitCost, int quantity, BigDecimal lineTotal) {
        BigDecimal expectedLineTotal = unitCost.multiply(BigDecimal.valueOf(quantity));
        if (expectedLineTotal.compareTo(lineTotal) != 0) {
            throw new DomainException(DomainErrorCode.IMPORT_RECEIPT_ITEM_LINE_TOTAL_MISMATCH);
        }
    }
}
