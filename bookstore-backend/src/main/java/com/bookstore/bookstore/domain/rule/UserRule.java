package com.bookstore.bookstore.domain.rule;

import com.bookstore.bookstore.domain.model.Role;
import java.time.Instant;
import java.util.Collections;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

import com.bookstore.bookstore.domain.enums.UserStatus;
import com.bookstore.bookstore.domain.exception.DomainErrorCode;
import com.bookstore.bookstore.domain.exception.DomainException;

public class UserRule {

    private UserRule(){}

    public static void requireCanActivate(
            UserStatus status,
            boolean locked,
            Instant deletedAt
    ) {
        if (status == UserStatus.ACTIVE) {
            throw new DomainException(DomainErrorCode.USER_ALREADY_ACTIVE);
        }

        if (locked) {
            throw new DomainException(DomainErrorCode.BLOCKED_USER_CANNOT_BE_ACTIVATED);
        }

        if (deletedAt != null) {
            throw new DomainException(DomainErrorCode.DELETED_USER_CANNOT_BE_ACTIVATED);
        }
    }

    public static void requireCanSoftDelete(Instant deletedAt){
        if (deletedAt != null) {
            throw new DomainException(DomainErrorCode.USER_ALREADY_DELETED);
        }
    }

    public static void requireCanLogin(
            UserStatus status,
            boolean locked,
            Instant deletedAt
    ) {
        if (status != UserStatus.ACTIVE) {
            throw new DomainException(DomainErrorCode.USER_NOT_ACTIVE_CANNOT_LOGIN);
        }

        if (locked) {
            throw new DomainException(DomainErrorCode.BLOCKED_USER_CANNOT_LOGIN);
        }

        if (deletedAt != null) {
            throw new DomainException(DomainErrorCode.DELETED_USER_CANNOT_LOGIN);
        }
    }

    public static void requireCanUpdateLockStatus(Instant deletedAt) {
        if (deletedAt != null) {
            throw new DomainException(DomainErrorCode.DELETED_USER_CANNOT_UPDATE_LOCK_STATUS);
        }
    }

    public static void requireLockStatusChanged(boolean currentLocked, boolean newLocked) {
        if (currentLocked == newLocked) {
            throw new DomainException(DomainErrorCode.USER_LOCK_STATUS_NOT_CHANGED);
        }
    }

    public static void requireCanUpdateManagedInfo(
            Instant deletedAt,
            String currentEmail,
            String currentPhoneNumber,
            Set<Role> currentRoles,
            String newEmail,
            String newPhoneNumber,
            Set<Role> newRoles
    ) {
        if (deletedAt != null) {
            throw new DomainException(DomainErrorCode.DELETED_USER_CANNOT_UPDATE_ACCOUNT_INFO);
        }

        boolean unchangedEmail = Objects.equals(currentEmail, newEmail);
        boolean unchangedPhoneNumber = Objects.equals(currentPhoneNumber, newPhoneNumber);
        boolean unchangedRoles = toRoleNames(currentRoles).equals(toRoleNames(newRoles));

        if (unchangedEmail && unchangedPhoneNumber && unchangedRoles) {
            throw new DomainException(DomainErrorCode.USER_MANAGED_INFO_NOT_CHANGED);
        }
    }

    public static void requireCanUpdateAccountInfo(
        UserStatus status,
        boolean locked, 
        Instant deletedAt,
        String username,
        String email,
        String phoneNumber,
        String newUsername,
        String newEmail,
        String newPhoneNumber
        ) {
        if (status != UserStatus.ACTIVE) {
            throw new DomainException(DomainErrorCode.USER_NOT_ACTIVE_CANNOT_UPDATE_ACCOUNT_INFO);
        }

        if (locked) {
            throw new DomainException(DomainErrorCode.BLOCKED_USER_CANNOT_UPDATE_ACCOUNT_INFO);
        }

        if (deletedAt != null) {
            throw new DomainException(DomainErrorCode.DELETED_USER_CANNOT_UPDATE_ACCOUNT_INFO);
        }
    }

    private static Set<String> toRoleNames(Set<Role> roles) {
        if (roles == null) {
            return Collections.emptySet();
        }

        return roles.stream()
                .map(Role::getName)
                .collect(Collectors.toSet());
    }
}
