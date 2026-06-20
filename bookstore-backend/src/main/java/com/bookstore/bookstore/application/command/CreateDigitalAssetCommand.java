package com.bookstore.bookstore.application.command;

import com.bookstore.bookstore.application.exception.ApplicationErrorCode;
import com.bookstore.bookstore.application.exception.ApplicationException;
import com.bookstore.bookstore.domain.enums.DigitalAssetFormat;
import java.math.BigDecimal;
import java.util.UUID;

public record CreateDigitalAssetCommand(
        UUID bookId,
        DigitalAssetFormat format,
        String title,
        String fileName,
        String storageKey,
        String mimeType,
        Long fileSize,
        String checksum,
        String sampleStorageKey,
        BigDecimal price,
        boolean downloadAllowed,
        boolean published
) {
    public CreateDigitalAssetCommand {
        if (bookId == null) {
            throw new ApplicationException(ApplicationErrorCode.INVALID_ARGUMENT, "bookId");
        }
        if (format == null) {
            throw new ApplicationException(ApplicationErrorCode.INVALID_ARGUMENT, "format");
        }
        if (fileSize == null) {
            throw new ApplicationException(ApplicationErrorCode.INVALID_ARGUMENT, "fileSize");
        }
        if (price == null) {
            throw new ApplicationException(ApplicationErrorCode.INVALID_ARGUMENT, "price");
        }
    }
}
