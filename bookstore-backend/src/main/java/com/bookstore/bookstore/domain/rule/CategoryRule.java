package com.bookstore.bookstore.domain.rule;

import com.bookstore.bookstore.domain.enums.CategoryLocale;
import com.bookstore.bookstore.domain.exception.DomainErrorCode;
import com.bookstore.bookstore.domain.exception.DomainException;
import com.bookstore.bookstore.domain.model.CategoryTranslation;
import java.time.Instant;
import java.util.Map;
import java.util.Objects;

public final class CategoryRule {

    private CategoryRule() {
    }

    public static void requireCanUpdate(
            Instant deletedAt,
            String currentCode,
            String currentName,
            String currentDescription,
            Map<CategoryLocale, CategoryTranslation> currentTranslations,
            java.util.UUID currentParentId,
            java.util.UUID currentImageFileAssetId,
            String newCode,
            String newName,
            String newDescription,
            Map<CategoryLocale, CategoryTranslation> newTranslations,
            java.util.UUID newParentId,
            java.util.UUID newImageFileAssetId
    ) {
        if (deletedAt != null) {
            throw new DomainException(DomainErrorCode.CATEGORY_ALREADY_DELETED);
        }

        if (Objects.equals(currentCode, newCode)
                && Objects.equals(currentName, newName)
                && Objects.equals(currentDescription, newDescription)
                && Objects.equals(currentTranslations, newTranslations)
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
