package com.bookstore.bookstore.domain.rule;

import com.bookstore.bookstore.domain.enums.PermissionCode;
import com.bookstore.bookstore.domain.exception.DomainErrorCode;
import com.bookstore.bookstore.domain.exception.DomainException;
import java.time.Instant;

public final class PermissionRule {

    private PermissionRule() {
    }

    public static void requireCanUpdate(Instant deletedAt, PermissionCode currentCode, PermissionCode newCode) {
        if (deletedAt != null) {
            throw new DomainException(DomainErrorCode.PERMISSION_ALREADY_DELETED);
        }

        if (currentCode == newCode) {
            throw new DomainException(DomainErrorCode.PERMISSION_CODE_NOT_CHANGED);
        }
    }
}
