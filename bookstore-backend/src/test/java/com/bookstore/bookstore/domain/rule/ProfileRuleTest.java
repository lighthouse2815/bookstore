package com.bookstore.bookstore.domain.rule;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import com.bookstore.bookstore.domain.exception.DomainErrorCode;
import com.bookstore.bookstore.domain.exception.DomainException;
import java.time.Instant;
import org.junit.jupiter.api.Test;

class ProfileRuleTest {

    @Test
    void requireCanUpdateProfileInfo_rejectsDeletedProfile() {
        DomainException exception = assertThrows(DomainException.class, () ->
                ProfileRule.requireCanUpdateProfileInfo(Instant.EPOCH)
        );

        assertEquals(DomainErrorCode.DELETED_PROFILE_CANNOT_UPDATE_PROFILE_INFO, exception.getErrorCode());
    }

    @Test
    void requireCanSoftDelete_rejectsDeletedProfile() {
        DomainException exception = assertThrows(DomainException.class, () ->
                ProfileRule.requireCanSoftDelete(Instant.EPOCH)
        );

        assertEquals(DomainErrorCode.PROFILE_ALREADY_DELETED, exception.getErrorCode());
    }
}
