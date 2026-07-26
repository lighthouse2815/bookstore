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
            Object currentAvatarFileAssetId,
            Integer currentBirthYear,
            Integer currentDeathYear,
            String newName,
            String newBiography,
            Object newAvatarFileAssetId,
            Integer newBirthYear,
            Integer newDeathYear
    ) {
        if (deletedAt != null) {
            throw new DomainException(DomainErrorCode.AUTHOR_ALREADY_DELETED);
        }

        if (Objects.equals(currentName, newName)
                && Objects.equals(currentBiography, newBiography)
                && Objects.equals(currentAvatarFileAssetId, newAvatarFileAssetId)
                && Objects.equals(currentBirthYear, newBirthYear)
                && Objects.equals(currentDeathYear, newDeathYear)) {
            throw new DomainException(DomainErrorCode.AUTHOR_DATA_NOT_CHANGED);
        }
    }

    public static void requireCanSoftDelete(Instant deletedAt) {
        if (deletedAt != null) {
            throw new DomainException(DomainErrorCode.AUTHOR_ALREADY_DELETED);
        }
    }
}
