package com.bookstore.bookstore.domain.rule;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import com.bookstore.bookstore.domain.enums.DigitalAccessStatus;
import com.bookstore.bookstore.domain.exception.DomainErrorCode;
import com.bookstore.bookstore.domain.exception.DomainException;
import org.junit.jupiter.api.Test;

class UserDigitalAccessRuleTest {

    @Test
    void requireCanRevoke_rejectsInactiveAccess() {
        DomainException exception = assertThrows(
                DomainException.class,
                () -> UserDigitalAccessRule.requireCanRevoke(DigitalAccessStatus.REVOKED)
        );

        assertEquals(DomainErrorCode.USER_DIGITAL_ACCESS_NOT_ACTIVE, exception.getErrorCode());
    }
}
