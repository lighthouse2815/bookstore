package com.bookstore.bookstore.application.command;

import com.bookstore.bookstore.application.exception.ApplicationErrorCode;
import com.bookstore.bookstore.application.exception.ApplicationException;
import java.util.UUID;

public record CompleteFileUploadCommand(
        UUID requesterId,
        boolean admin,
        UUID fileAssetId,
        String checksumSha256
) {
    public CompleteFileUploadCommand {
        if (requesterId == null) {
            throw new ApplicationException(ApplicationErrorCode.INVALID_ARGUMENT, "requesterId");
        }
        if (fileAssetId == null) {
            throw new ApplicationException(ApplicationErrorCode.INVALID_ARGUMENT, "fileAssetId");
        }
    }
}
