package com.bookstore.bookstore.domain.rule;

import com.bookstore.bookstore.domain.exception.DomainErrorCode;
import com.bookstore.bookstore.domain.exception.DomainException;
import java.time.Instant;
import java.util.Objects;

public final class CategoryRule {

    private CategoryRule() {
    }

    public static void requireCanUpdate(
            Instant deletedAt,
            String currentName,
            String currentDescription,
            java.util.UUID currentParentId,
            java.util.UUID currentImageFileAssetId,
            String newName,
            String newDescription,
            java.util.UUID newParentId,
            java.util.UUID newImageFileAssetId
    ) {
        if (deletedAt != null) {
            throw new DomainException(DomainErrorCode.CATEGORY_ALREADY_DELETED);
        }

        if (Objects.equals(currentName, newName)
                && Objects.equals(currentDescription, newDescription)
                && Objects.equals(currentParentId, newParentId)
                && Objects.equals(currentImageFileAssetId, newImageFileAssetId)) {
            throw new DomainException(DomainErrorCode.CATEGORY_DATA_NOT_CHANGED);
        }
    }

    public static void requireCanSoftDelete(Instant deletedAt) {
        if (deletedAt != null) {
            throw new DomainException(DomainErrorCode.CATEGORY_ALREADY_DELETED);
        }
    }
}
