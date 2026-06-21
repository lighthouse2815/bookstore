package com.bookstore.bookstore.application.command;

import com.bookstore.bookstore.application.exception.ApplicationErrorCode;
import com.bookstore.bookstore.application.exception.ApplicationException;
import java.util.UUID;

public record DeleteDigitalAssetCommand(
        UUID bookId,
        UUID digitalAssetId
) {
    public DeleteDigitalAssetCommand {
        if (bookId == null) {
            throw new ApplicationException(ApplicationErrorCode.INVALID_ARGUMENT, "bookId");
        }
        if (digitalAssetId == null) {
            throw new ApplicationException(ApplicationErrorCode.INVALID_ARGUMENT, "digitalAssetId");
        }
    }
}
