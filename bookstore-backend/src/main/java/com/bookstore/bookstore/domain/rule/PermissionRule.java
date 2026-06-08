package com.bookstore.bookstore.domain.rule;

import com.bookstore.bookstore.domain.enums.PermissionCode;
import com.bookstore.bookstore.domain.exception.DomainErrorCode;
import com.bookstore.bookstore.domain.exception.DomainException;
import java.time.Instant;
import java.util.Objects;

public final class PermissionRule {

    private PermissionRule() {
    }

    public static void requireCanUpdate(
            Instant deletedAt,
            PermissionCode currentCode,
            String currentDescription,
            PermissionCode newCode,
            String newDescription
    ) {
        if (deletedAt != null) {
            throw new DomainException(DomainErrorCode.PERMISSION_ALREADY_DELETED);
        }

        if (Objects.equals(currentCode, newCode) && Objects.equals(currentDescription, newDescription)) {
            throw new DomainException(DomainErrorCode.PERMISSION_DATA_NOT_CHANGED);
        }
    }
}
