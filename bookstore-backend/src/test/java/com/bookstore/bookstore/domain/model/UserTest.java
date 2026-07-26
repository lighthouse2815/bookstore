package com.bookstore.bookstore.domain.model;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import com.bookstore.bookstore.domain.enums.UserStatus;
import com.bookstore.bookstore.domain.exception.DomainErrorCode;
import com.bookstore.bookstore.domain.exception.DomainException;
import java.time.Instant;
import java.util.LinkedHashSet;
import java.util.Set;
import java.util.UUID;
import org.junit.jupiter.api.Test;

class UserTest {

    @Test
    void updateLockStatus_rejectsDeletedUser() {
        User user = deletedUser();

        DomainException exception = assertThrows(DomainException.class, () -> user.updateLockStatus(true));

        assertEquals(DomainErrorCode.DELETED_USER_CANNOT_UPDATE_LOCK_STATUS, exception.getErrorCode());
    }

    @Test
    void updateLockStatus_rejectsUnchangedState() {
        User user = activeUser(true, null, "USER");

        DomainException exception = assertThrows(DomainException.class, () -> user.updateLockStatus(true));

        assertEquals(DomainErrorCode.USER_LOCK_STATUS_NOT_CHANGED, exception.getErrorCode());
    }

    @Test
    void updateManagedInfo_rejectsUnchangedData() {
        User user = activeUser(false, null, "STAFF", "ADMIN");

        DomainException exception = assertThrows(DomainException.class, () ->
                user.updateManagedInfo(
                        "user@gmail.com",
                        "0123456789",
                        new LinkedHashSet<>(Set.of(
                                buildRole("ADMIN"),
                                buildRole("STAFF")
                        ))
                )
        );

        assertEquals(DomainErrorCode.USER_MANAGED_INFO_NOT_CHANGED, exception.getErrorCode());
    }

    private static User deletedUser() {
        Instant deletedAt = Instant.EPOCH;
        return activeUser(false, deletedAt, "USER");
    }

    private static User activeUser(boolean locked, Instant deletedAt, String... roleNames) {
        Set<Role> roles = new LinkedHashSet<>();
        for (String roleName : roleNames) {
            roles.add(buildRole(roleName));
        }

        Instant createdAt = deletedAt == null ? Instant.parse("2024-01-01T00:00:00Z") : deletedAt;
        Instant updatedAt = deletedAt == null ? Instant.parse("2024-01-01T00:00:00Z") : deletedAt;

        return new User(
                UUID.randomUUID(),
                "user",
                "password_hash",
                "0123456789",
                "user@gmail.com",
                UserStatus.ACTIVE,
                locked,
                roles,
                createdAt,
                updatedAt,
                deletedAt
        );
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
