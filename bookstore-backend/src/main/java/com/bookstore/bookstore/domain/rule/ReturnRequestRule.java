package com.bookstore.bookstore.domain.rule;

import com.bookstore.bookstore.domain.enums.ReturnRequestStatus;
import com.bookstore.bookstore.domain.exception.DomainErrorCode;
import com.bookstore.bookstore.domain.exception.DomainException;
import java.math.BigDecimal;

public final class ReturnRequestRule {

    private ReturnRequestRule() {
    }

    public static void requirePending(ReturnRequestStatus status) {
        if (status != ReturnRequestStatus.PENDING) {
            throw new DomainException(DomainErrorCode.RETURN_REQUEST_NOT_PENDING);
        }
    }

    public static void requireNonNegativeRequestedRefundAmount(BigDecimal amount) {
        if (amount != null && amount.compareTo(BigDecimal.ZERO) < 0) {
            throw new DomainException(
                    DomainErrorCode.INVALID_RETURN_REQUEST_REQUESTED_REFUND_AMOUNT,
                    "requestedRefundAmount"
            );
        }
    }

    public static void requireNonNegativeApprovedRefundAmount(BigDecimal amount) {
        if (amount != null && amount.compareTo(BigDecimal.ZERO) < 0) {
            throw new DomainException(
                    DomainErrorCode.INVALID_RETURN_REQUEST_APPROVED_REFUND_AMOUNT,
                    "approvedRefundAmount"
            );
        }
    }
}
