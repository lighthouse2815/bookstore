package com.bookstore.bookstore.domain.enums;

public enum PaymentReconciliationIssueType {
    PAYMENT_AFTER_EXPIRY,
    PAYMENT_AFTER_CANCELLATION,
    AMOUNT_MISMATCH,
    PAYMENT_WITH_INVALID_ORDER_STATE
}
