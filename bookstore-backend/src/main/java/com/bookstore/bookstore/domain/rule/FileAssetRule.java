package com.bookstore.bookstore.domain.rule;

import com.bookstore.bookstore.domain.enums.FileStatus;
import com.bookstore.bookstore.domain.exception.DomainErrorCode;
import com.bookstore.bookstore.domain.exception.DomainException;
import java.time.Instant;

public final class FileAssetRule {

    private FileAssetRule() {
    }

    public static void requireNotDeleted(FileStatus status, Instant deletedAt) {
        if (status == FileStatus.DELETED || deletedAt != null) {
            throw new DomainException(DomainErrorCode.FILE_ASSET_ALREADY_DELETED);
        }
    }

    public static void requireNonNegativeSize(Long sizeBytes) {
        if (sizeBytes != null && sizeBytes < 0) {
            throw new DomainException(DomainErrorCode.INVALID_FILE_ASSET_SIZE_BYTES, "sizeBytes");
        }
    }
}
