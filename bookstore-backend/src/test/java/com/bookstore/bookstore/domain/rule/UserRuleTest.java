package com.bookstore.bookstore.domain.rule;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import com.bookstore.bookstore.domain.enums.UserStatus;
import com.bookstore.bookstore.domain.exception.DomainErrorCode;
import com.bookstore.bookstore.domain.exception.DomainException;
import java.time.Instant;
import org.junit.jupiter.api.Test;

class UserRuleTest {

    @Test
    void requireCanUpdateAccountInfo_allowsValidStateAndDifferentValues() {
        assertDoesNotThrow(() ->
                UserRule.requireCanUpdateAccountInfo(
                        UserStatus.ACTIVE,
                        false,
                        null,
                        "old-username",
                        "old@gmail.com",
                        "0123456789",
                        "new-username",
                        "new@gmail.com",
                        "0987654321"
                )
        );
    }

    @Test
    void requireCanUpdateAccountInfo_rejectsInactiveAccount() {
        DomainException exception = assertThrows(DomainException.class, () ->
                UserRule.requireCanUpdateAccountInfo(
                        UserStatus.INACTIVE,
                        false,
                        null,
                        "old-username",
                        "old@gmail.com",
                        "0123456789",
                        "new-username",
                        "new@gmail.com",
                        "0987654321"
                )
        );

        assertEquals(DomainErrorCode.USER_NOT_ACTIVE_CANNOT_UPDATE_ACCOUNT_INFO, exception.getErrorCode());
    }

    @Test
    void requireCanUpdateAccountInfo_rejectsLockedAccount() {
        DomainException exception = assertThrows(DomainException.class, () ->
                UserRule.requireCanUpdateAccountInfo(
                        UserStatus.ACTIVE,
                        true,
                        null,
                        "old-username",
                        "old@gmail.com",
                        "0123456789",
                        "new-username",
                        "new@gmail.com",
                        "0987654321"
                )
        );

        assertEquals(DomainErrorCode.BLOCKED_USER_CANNOT_UPDATE_ACCOUNT_INFO, exception.getErrorCode());
    }

    @Test
    void requireCanUpdateAccountInfo_rejectsDeletedAccount() {
        DomainException exception = assertThrows(DomainException.class, () ->
                UserRule.requireCanUpdateAccountInfo(
                        UserStatus.ACTIVE,
                        false,
                        Instant.EPOCH,
                        "old-username",
                        "old@gmail.com",
                        "0123456789",
                        "new-username",
                        "new@gmail.com",
                        "0987654321"
                )
        );

        assertEquals(DomainErrorCode.DELETED_USER_CANNOT_UPDATE_ACCOUNT_INFO, exception.getErrorCode());
    }

    @Test
    void requireCanUpdateAccountInfo_allowsUnchangedPhoneNumber() {
        assertDoesNotThrow(() ->
                UserRule.requireCanUpdateAccountInfo(
                        UserStatus.ACTIVE,
                        false,
                        null,
                        "old-username",
                        "old@gmail.com",
                        "0123456789",
                        "new-username",
                        "new@gmail.com",
                        "0123456789"
                )
        );
    }

    @Test
    void requireCanUpdateAccountInfo_allowsUnchangedEmail() {
        assertDoesNotThrow(() ->
                UserRule.requireCanUpdateAccountInfo(
                        UserStatus.ACTIVE,
                        false,
                        null,
                        "old-username",
                        "old@gmail.com",
                        "0123456789",
                        "new-username",
                        "old@gmail.com",
                        "0987654321"
                )
        );
    }

    @Test
    void requireCanUpdateAccountInfo_allowsUnchangedUsername() {
        assertDoesNotThrow(() ->
                UserRule.requireCanUpdateAccountInfo(
                        UserStatus.ACTIVE,
                        false,
                        null,
                        "old-username",
                        "old@gmail.com",
                        "0123456789",
                        "old-username",
                        "new@gmail.com",
                        "0987654321"
                )
        );
    }

    @Test
    void requireCanLogin_allowsActiveUnlockedAndNotDeletedUser() {
        assertDoesNotThrow(() -> UserRule.requireCanLogin(UserStatus.ACTIVE, false, null));
    }

    @Test
    void requireCanLogin_rejectsInactiveUser() {
        DomainException exception = assertThrows(DomainException.class, () ->
                UserRule.requireCanLogin(UserStatus.INACTIVE, false, null)
        );

        assertEquals(DomainErrorCode.USER_NOT_ACTIVE_CANNOT_LOGIN, exception.getErrorCode());
    }

    @Test
    void requireCanLogin_rejectsLockedUser() {
        DomainException exception = assertThrows(DomainException.class, () ->
                UserRule.requireCanLogin(UserStatus.ACTIVE, true, null)
        );

        assertEquals(DomainErrorCode.BLOCKED_USER_CANNOT_LOGIN, exception.getErrorCode());
    }

    @Test
    void requireCanLogin_rejectsDeletedUser() {
        DomainException exception = assertThrows(DomainException.class, () ->
                UserRule.requireCanLogin(UserStatus.ACTIVE, false, Instant.EPOCH)
        );

        assertEquals(DomainErrorCode.DELETED_USER_CANNOT_LOGIN, exception.getErrorCode());
    }
}
