package com.bookstore.bookstore.domain.rule;

import com.bookstore.bookstore.domain.exception.DomainErrorCode;
import com.bookstore.bookstore.domain.exception.DomainException;
import java.time.Instant;
import java.util.Objects;

public final class AuthorRule {

    private AuthorRule() {
    }

    public static void requireCanUpdate(
            Instant deletedAt,
            String currentName,
            String currentBiography,
            String newName,
            String newBiography
    ) {
        if (deletedAt != null) {
            throw new DomainException(DomainErrorCode.AUTHOR_ALREADY_DELETED);
        }

        if (Objects.equals(currentName, newName) && Objects.equals(currentBiography, newBiography)) {
            throw new DomainException(DomainErrorCode.AUTHOR_DATA_NOT_CHANGED);
        }
    }

    public static void requireCanSoftDelete(Instant deletedAt) {
        if (deletedAt != null) {
            throw new DomainException(DomainErrorCode.AUTHOR_ALREADY_DELETED);
        }
    }
}
