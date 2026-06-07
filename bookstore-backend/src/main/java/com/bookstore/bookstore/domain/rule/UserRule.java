package com.bookstore.bookstore.domain.rule;

import java.time.Instant;

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
}
