package com.bookstore.bookstore.domain.rule;

import com.bookstore.bookstore.domain.exception.DomainErrorCode;
import com.bookstore.bookstore.domain.exception.DomainException;
import java.math.BigDecimal;
import java.time.Instant;
import java.util.Objects;
import java.util.UUID;

public final class BookRule {

    private BookRule() {
    }

    public static void requireCanUpdate(
            Instant deletedAt,
            String currentTitle,
            String currentDescription,
            BigDecimal currentPrice,
            Integer currentStockQuantity,
            String currentImageUrl,
            UUID currentCategoryId,
            UUID currentAuthorId,
            UUID currentPublisherId,
            String newTitle,
            String newDescription,
            BigDecimal newPrice,
            Integer newStockQuantity,
            String newImageUrl,
            UUID newCategoryId,
            UUID newAuthorId,
            UUID newPublisherId
    ) {
        if (deletedAt != null) {
            throw new DomainException(DomainErrorCode.BOOK_ALREADY_DELETED);
        }

        requireNonNegativePrice(newPrice);
        requireNonNegativeStockQuantity(newStockQuantity);

        if (Objects.equals(currentTitle, newTitle)
                && Objects.equals(currentDescription, newDescription)
                && samePrice(currentPrice, newPrice)
                && Objects.equals(currentStockQuantity, newStockQuantity)
                && Objects.equals(currentImageUrl, newImageUrl)
                && Objects.equals(currentCategoryId, newCategoryId)
                && Objects.equals(currentAuthorId, newAuthorId)
                && Objects.equals(currentPublisherId, newPublisherId)) {
            throw new DomainException(DomainErrorCode.BOOK_DATA_NOT_CHANGED);
        }
    }

    public static void requireCanSoftDelete(Instant deletedAt) {
        if (deletedAt != null) {
            throw new DomainException(DomainErrorCode.BOOK_ALREADY_DELETED);
        }
    }

    private static void requireNonNegativePrice(BigDecimal price) {
        if (price.compareTo(BigDecimal.ZERO) < 0) {
            throw new DomainException(DomainErrorCode.INVALID_BOOK_PRICE);
        }
    }

    private static void requireNonNegativeStockQuantity(Integer stockQuantity) {
        if (stockQuantity < 0) {
            throw new DomainException(DomainErrorCode.INVALID_BOOK_STOCK_QUANTITY);
        }
    }

    private static boolean samePrice(BigDecimal left, BigDecimal right) {
        return left.compareTo(right) == 0;
    }
}
