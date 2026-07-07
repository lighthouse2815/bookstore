package com.bookstore.bookstore.application.command;

import com.bookstore.bookstore.application.exception.ApplicationErrorCode;
import com.bookstore.bookstore.application.exception.ApplicationException;
import com.bookstore.bookstore.domain.enums.FilePurpose;
import com.bookstore.bookstore.domain.enums.FileVisibility;
import java.util.UUID;

public record PresignUploadCommand(
        UUID requesterId,
        boolean admin,
        FilePurpose purpose,
        FileVisibility visibility,
        String fileName,
        String contentType,
        Long sizeBytes,
        UUID bookId,
        UUID authorId,
        UUID digitalAssetId,
        UUID reviewId,
        UUID orderId
) {
    public PresignUploadCommand {
        if (requesterId == null) {
            throw new ApplicationException(ApplicationErrorCode.INVALID_ARGUMENT, "requesterId");
        }
        if (purpose == null) {
            throw new ApplicationException(ApplicationErrorCode.INVALID_ARGUMENT, "purpose");
        }
        if (visibility == null) {
            throw new ApplicationException(ApplicationErrorCode.INVALID_ARGUMENT, "visibility");
        }
        if (fileName == null || fileName.isBlank()) {
            throw new ApplicationException(ApplicationErrorCode.INVALID_ARGUMENT, "fileName");
        }
        if (contentType == null || contentType.isBlank()) {
            throw new ApplicationException(ApplicationErrorCode.INVALID_ARGUMENT, "contentType");
        }
        if (sizeBytes == null || sizeBytes <= 0) {
            throw new ApplicationException(ApplicationErrorCode.INVALID_ARGUMENT, "sizeBytes");
        }
    }
}
