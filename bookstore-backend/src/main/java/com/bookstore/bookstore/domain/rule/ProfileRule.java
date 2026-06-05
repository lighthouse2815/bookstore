package com.bookstore.bookstore.domain.rule;

import java.time.Instant;

import com.bookstore.bookstore.domain.exception.DomainErrorCode;
import com.bookstore.bookstore.domain.exception.DomainException;

public class ProfileRule {

    private ProfileRule() {
    }

    public static void requireCanUpdateProfileInfo(Instant deletedAt) {
        if (deletedAt != null) {
            throw new DomainException(DomainErrorCode.DELETED_PROFILE_CANNOT_UPDATE_PROFILE_INFO);
        }
    }

    public static void requireCanSoftDelete(Instant deletedAt) {
        if (deletedAt != null) {
            throw new DomainException(DomainErrorCode.PROFILE_ALREADY_DELETED);
        }
    }
}
