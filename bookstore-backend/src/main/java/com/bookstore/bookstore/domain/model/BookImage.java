package com.bookstore.bookstore.domain.model;

import com.bookstore.bookstore.domain.exception.DomainErrorCode;
import com.bookstore.bookstore.domain.exception.DomainException;
import com.bookstore.bookstore.domain.validation.Guard;
import java.time.Instant;
import java.util.UUID;
import lombok.Getter;

@Getter
public class BookImage {

    private UUID id;
    private UUID bookId;
    private FileAsset fileAsset;
    private Boolean primaryImage;
    private Integer sortOrder;
    private String altText;
    private Instant createdAt;

    public BookImage(
            UUID id,
            UUID bookId,
            FileAsset fileAsset,
            Boolean primaryImage,
            Integer sortOrder,
            String altText,
            Instant createdAt
    ) {
        this.id = Guard.notNull(id, DomainErrorCode.INVALID_BOOK_IMAGE_ID, "id");
        this.bookId = Guard.notNull(bookId, DomainErrorCode.INVALID_BOOK_IMAGE_BOOK_ID, "bookId");
        setFileAsset(fileAsset);
        setPrimaryImage(primaryImage);
        setSortOrder(sortOrder);
        setAltText(altText);
        setCreatedAt(createdAt);
    }

    public UUID getFileAssetId() {
        return fileAsset.getId();
    }

    public String getImageUrl() {
        return fileAsset.getPublicUrl();
    }

    private void setFileAsset(FileAsset fileAsset) {
        this.fileAsset = Guard.notNull(
                fileAsset,
                DomainErrorCode.INVALID_BOOK_IMAGE_FILE_ASSET_ID,
                "fileAssetId"
        );
    }

    private void setPrimaryImage(Boolean primaryImage) {
        this.primaryImage = Guard.notNull(
                primaryImage,
                DomainErrorCode.INVALID_BOOK_IMAGE_PRIMARY,
                "primaryImage"
        );
    }

    private void setSortOrder(Integer sortOrder) {
        int normalized = sortOrder == null ? 0 : sortOrder;
        if (normalized < 0) {
            throw new DomainException(DomainErrorCode.INVALID_BOOK_IMAGE_SORT_ORDER, "sortOrder");
        }
        this.sortOrder = normalized;
    }

    private void setAltText(String altText) {
        this.altText = Guard.notBlankOrNull(altText, DomainErrorCode.INVALID_BOOK_IMAGE_ALT_TEXT, "altText");
    }

    private void setCreatedAt(Instant createdAt) {
        this.createdAt = Guard.notInFuture(
                createdAt,
                DomainErrorCode.INVALID_BOOK_IMAGE_CREATED_AT,
                "createdAt"
        );
    }
}
