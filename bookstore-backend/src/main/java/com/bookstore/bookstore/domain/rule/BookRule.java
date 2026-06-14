package com.bookstore.bookstore.domain.rule;

import com.bookstore.bookstore.domain.exception.DomainErrorCode;
import com.bookstore.bookstore.domain.exception.DomainException;
import com.bookstore.bookstore.domain.model.BookDetail;
import com.bookstore.bookstore.domain.model.BookImage;
import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;
import java.util.Objects;
import java.util.UUID;

public final class BookRule {

    private BookRule() {
    }

    public static void requireCanUpdate(
            Instant deletedAt,
            String currentTitle,
            String currentIsbn,
            String currentDescription,
            BigDecimal currentPrice,
            Integer currentStockQuantity,
            List<BookImage> currentImages,
            BookDetail currentDetail,
            UUID currentCategoryId,
            UUID currentAuthorId,
            UUID currentPublisherId,
            String newTitle,
            String newIsbn,
            String newDescription,
            BigDecimal newPrice,
            Integer newStockQuantity,
            List<BookImage> newImages,
            BookDetail newDetail,
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
                && Objects.equals(currentIsbn, newIsbn)
                && Objects.equals(currentDescription, newDescription)
                && samePrice(currentPrice, newPrice)
                && Objects.equals(currentStockQuantity, newStockQuantity)
                && sameImages(currentImages, newImages)
                && sameDetail(currentDetail, newDetail)
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

    public static void requirePositiveStockDecreaseQuantity(int quantity) {
        if (quantity <= 0) {
            throw new DomainException(DomainErrorCode.INVALID_BOOK_STOCK_DECREASE_QUANTITY, "quantity");
        }
    }

    public static void requirePositiveStockIncreaseQuantity(int quantity) {
        if (quantity <= 0) {
            throw new DomainException(DomainErrorCode.INVALID_BOOK_STOCK_INCREASE_QUANTITY, "quantity");
        }
    }

    public static void requireEnoughStock(int currentStockQuantity, int quantity) {
        if (quantity > currentStockQuantity) {
            throw new DomainException(DomainErrorCode.BOOK_STOCK_NOT_ENOUGH);
        }
    }

    public static void requireImagesBelongToBook(UUID bookId, List<BookImage> images) {
        for (BookImage image : images) {
            if (!Objects.equals(bookId, image.getBookId())) {
                throw new DomainException(DomainErrorCode.BOOK_IMAGE_BOOK_ID_MISMATCH);
            }
        }
    }

    public static void requireDetailBelongsToBook(UUID bookId, BookDetail detail) {
        if (detail != null && !Objects.equals(bookId, detail.getBookId())) {
            throw new DomainException(DomainErrorCode.BOOK_DETAIL_BOOK_ID_MISMATCH);
        }
    }

    public static void requireAtMostOnePrimaryImage(List<BookImage> images) {
        long primaryImages = images.stream()
                .filter(image -> Boolean.TRUE.equals(image.getPrimaryImage()))
                .count();
        if (primaryImages > 1) {
            throw new DomainException(DomainErrorCode.BOOK_HAS_MULTIPLE_PRIMARY_IMAGES);
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

    private static boolean sameImages(List<BookImage> left, List<BookImage> right) {
        if (left.size() != right.size()) {
            return false;
        }

        for (int index = 0; index < left.size(); index++) {
            BookImage current = left.get(index);
            BookImage next = right.get(index);
            if (!Objects.equals(current.getImageUrl(), next.getImageUrl())
                    || !Objects.equals(current.getPrimaryImage(), next.getPrimaryImage())
                    || !Objects.equals(current.getSortOrder(), next.getSortOrder())
                    || !Objects.equals(current.getAltText(), next.getAltText())) {
                return false;
            }
        }

        return true;
    }

    private static boolean sameDetail(BookDetail left, BookDetail right) {
        if (left == null && right == null) {
            return true;
        }
        if (left == null || right == null) {
            return false;
        }

        return Objects.equals(left.getPageCount(), right.getPageCount())
                && Objects.equals(left.getPublicationYear(), right.getPublicationYear())
                && Objects.equals(left.getLanguage(), right.getLanguage())
                && Objects.equals(left.getCoverType(), right.getCoverType())
                && Objects.equals(left.getDimensions(), right.getDimensions())
                && Objects.equals(left.getWeight(), right.getWeight())
                && Objects.equals(left.getTranslator(), right.getTranslator())
                && Objects.equals(left.getEdition(), right.getEdition());
    }
}
