package com.bookstore.bookstore.domain.rule;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import com.bookstore.bookstore.domain.enums.UserStatus;
import com.bookstore.bookstore.domain.exception.DomainErrorCode;
import com.bookstore.bookstore.domain.exception.DomainException;
import com.bookstore.bookstore.domain.model.Role;
import java.time.Instant;
import java.util.Set;
import java.util.UUID;
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

    @Test
    void requireCanUpdateLockStatus_allowsNotDeletedUser() {
        assertDoesNotThrow(() -> UserRule.requireCanUpdateLockStatus(null));
    }

    @Test
    void requireCanUpdateLockStatus_rejectsDeletedUser() {
        DomainException exception = assertThrows(DomainException.class, () ->
                UserRule.requireCanUpdateLockStatus(Instant.EPOCH)
        );

        assertEquals(DomainErrorCode.DELETED_USER_CANNOT_UPDATE_LOCK_STATUS, exception.getErrorCode());
    }

    @Test
    void requireLockStatusChanged_rejectsSameLockState() {
        DomainException exception = assertThrows(DomainException.class, () ->
                UserRule.requireLockStatusChanged(true, true)
        );

        assertEquals(DomainErrorCode.USER_LOCK_STATUS_NOT_CHANGED, exception.getErrorCode());
    }

    @Test
    void requireCanUpdateManagedInfo_rejectsDeletedUser() {
        DomainException exception = assertThrows(DomainException.class, () ->
                UserRule.requireCanUpdateManagedInfo(
                        Instant.EPOCH,
                        "old@gmail.com",
                        "0123456789",
                        Set.of(buildRole("STAFF")),
                        "new@gmail.com",
                        "0987654321",
                        Set.of(buildRole("ADMIN"))
                )
        );

        assertEquals(DomainErrorCode.DELETED_USER_CANNOT_UPDATE_ACCOUNT_INFO, exception.getErrorCode());
    }

    @Test
    void requireCanUpdateManagedInfo_rejectsUnchangedData() {
        DomainException exception = assertThrows(DomainException.class, () ->
                UserRule.requireCanUpdateManagedInfo(
                        null,
                        "staff@gmail.com",
                        "0123456789",
                        Set.of(buildRole("STAFF"), buildRole("ADMIN")),
                        "staff@gmail.com",
                        "0123456789",
                        Set.of(buildRole("ADMIN"), buildRole("STAFF"))
                )
        );

        assertEquals(DomainErrorCode.USER_MANAGED_INFO_NOT_CHANGED, exception.getErrorCode());
    }

    private static Role buildRole(String roleName) {
        return new Role(
                UUID.randomUUID(),
                roleName,
                "Default role",
                Set.of(),
                Instant.parse("2024-01-01T00:00:00Z"),
                Instant.parse("2024-01-01T00:00:00Z"),
                null
        );
    }
}
