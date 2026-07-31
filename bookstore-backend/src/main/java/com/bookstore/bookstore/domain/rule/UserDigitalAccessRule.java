package com.bookstore.bookstore.domain.rule;

import com.bookstore.bookstore.domain.enums.DigitalAccessStatus;
import com.bookstore.bookstore.domain.exception.DomainErrorCode;
import com.bookstore.bookstore.domain.exception.DomainException;
import java.time.Instant;

public final class UserDigitalAccessRule {

    private UserDigitalAccessRule() {
    }

    public static void requireCanSoftDelete(Instant deletedAt) {
        if (deletedAt != null) {
            throw new DomainException(DomainErrorCode.USER_DIGITAL_ACCESS_ALREADY_DELETED);
        }
    }

    public static void requireCanRevoke(DigitalAccessStatus status) {
        if (status != DigitalAccessStatus.ACTIVE) {
            throw new DomainException(DomainErrorCode.USER_DIGITAL_ACCESS_NOT_ACTIVE);
        }
    }
}
