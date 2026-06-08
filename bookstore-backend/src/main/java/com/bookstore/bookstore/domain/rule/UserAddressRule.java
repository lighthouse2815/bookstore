package com.bookstore.bookstore.domain.rule;

import com.bookstore.bookstore.domain.exception.DomainErrorCode;
import com.bookstore.bookstore.domain.exception.DomainException;
import java.time.Instant;

public final class UserAddressRule {

    private UserAddressRule() {
    }

    public static void requireCanUpdate(Instant deletedAt) {
        if (deletedAt != null) {
            throw new DomainException(DomainErrorCode.DELETED_USER_ADDRESS_CANNOT_UPDATE);
        }
    }

    public static void requireCanSoftDelete(Instant deletedAt) {
        if (deletedAt != null) {
            throw new DomainException(DomainErrorCode.USER_ADDRESS_ALREADY_DELETED);
        }
    }

    public static void requireCanChangeDefault(Instant deletedAt) {
        if (deletedAt != null) {
            throw new DomainException(DomainErrorCode.DELETED_USER_ADDRESS_CANNOT_SET_DEFAULT);
        }
    }
}
