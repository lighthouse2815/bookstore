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
        UUID fileAssetId,
        UUID sampleFileAssetId,
        BigDecimal price,
        boolean downloadAllowed,
        boolean purchaseAllowed,
        boolean published
) {
    public CreateDigitalAssetCommand {
        if (bookId == null) {
            throw new ApplicationException(ApplicationErrorCode.INVALID_ARGUMENT, "bookId");
        }
        if (format == null) {
            throw new ApplicationException(ApplicationErrorCode.INVALID_ARGUMENT, "format");
        }
        if (fileAssetId == null) {
            throw new ApplicationException(ApplicationErrorCode.INVALID_ARGUMENT, "fileAssetId");
        }
        if (price == null) {
            throw new ApplicationException(ApplicationErrorCode.INVALID_ARGUMENT, "price");
        }
    }
}
