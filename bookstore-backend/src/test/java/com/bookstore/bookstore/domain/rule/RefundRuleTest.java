package com.bookstore.bookstore.domain.rule;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import com.bookstore.bookstore.domain.enums.RefundStatus;
import com.bookstore.bookstore.domain.exception.DomainErrorCode;
import com.bookstore.bookstore.domain.exception.DomainException;
import java.math.BigDecimal;
import org.junit.jupiter.api.Test;

class RefundRuleTest {

    @Test
    void requirePositiveAmount_rejectsZero() {
        DomainException exception = assertThrows(
                DomainException.class,
                () -> RefundRule.requirePositiveAmount(BigDecimal.ZERO)
        );

        assertEquals(DomainErrorCode.INVALID_REFUND_AMOUNT, exception.getErrorCode());
    }

    @Test
    void requireCanTransition_rejectsSkippingApproval() {
        DomainException exception = assertThrows(
                DomainException.class,
                () -> RefundRule.requireCanTransition(RefundStatus.REQUESTED, RefundStatus.PROCESSING)
        );

        assertEquals(DomainErrorCode.INVALID_REFUND_STATUS_TRANSITION, exception.getErrorCode());
    }
}
