package com.bookstore.bookstore.domain.rule;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import com.bookstore.bookstore.domain.enums.ReturnRequestStatus;
import com.bookstore.bookstore.domain.exception.DomainErrorCode;
import com.bookstore.bookstore.domain.exception.DomainException;
import java.math.BigDecimal;
import org.junit.jupiter.api.Test;

class ReturnRequestRuleTest {

    @Test
    void requirePending_rejectsProcessedRequest() {
        DomainException exception = assertThrows(
                DomainException.class,
                () -> ReturnRequestRule.requirePending(ReturnRequestStatus.APPROVED)
        );

        assertEquals(DomainErrorCode.RETURN_REQUEST_NOT_PENDING, exception.getErrorCode());
    }

    @Test
    void requireNonNegativeApprovedRefundAmount_rejectsNegativeValue() {
        DomainException exception = assertThrows(
                DomainException.class,
                () -> ReturnRequestRule.requireNonNegativeApprovedRefundAmount(BigDecimal.valueOf(-1))
        );

        assertEquals(
                DomainErrorCode.INVALID_RETURN_REQUEST_APPROVED_REFUND_AMOUNT,
                exception.getErrorCode()
        );
    }
}
