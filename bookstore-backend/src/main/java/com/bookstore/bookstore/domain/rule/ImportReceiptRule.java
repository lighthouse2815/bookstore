package com.bookstore.bookstore.domain.rule;

import com.bookstore.bookstore.domain.exception.DomainErrorCode;
import com.bookstore.bookstore.domain.exception.DomainException;
import com.bookstore.bookstore.domain.model.ImportReceiptItem;
import java.math.BigDecimal;
import java.util.List;

public final class ImportReceiptRule {

    private ImportReceiptRule() {
    }

    public static void requireHasItems(List<ImportReceiptItem> items) {
        if (items.isEmpty()) {
            throw new DomainException(DomainErrorCode.IMPORT_RECEIPT_MUST_HAVE_AT_LEAST_ONE_ITEM);
        }
    }

    public static void requireNonNegativeTotalAmount(BigDecimal totalAmount) {
        if (totalAmount.compareTo(BigDecimal.ZERO) < 0) {
            throw new DomainException(DomainErrorCode.INVALID_IMPORT_RECEIPT_TOTAL_AMOUNT, "totalAmount");
        }
    }

    public static void requireMatchingTotalAmount(List<ImportReceiptItem> items, BigDecimal totalAmount) {
        BigDecimal actualTotalAmount = items.stream()
                .map(ImportReceiptItem::getLineTotal)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        if (actualTotalAmount.compareTo(totalAmount) != 0) {
            throw new DomainException(DomainErrorCode.IMPORT_RECEIPT_TOTAL_AMOUNT_MISMATCH);
        }
    }
}
