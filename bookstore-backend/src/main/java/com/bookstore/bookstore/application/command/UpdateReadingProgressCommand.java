package com.bookstore.bookstore.application.command;

import com.bookstore.bookstore.application.exception.ApplicationErrorCode;
import com.bookstore.bookstore.application.exception.ApplicationException;
import java.math.BigDecimal;
import java.util.UUID;

public record UpdateReadingProgressCommand(
        UUID userId,
        UUID digitalAssetId,
        Integer currentPage,
        BigDecimal progressPercent,
        String positionData
) {
    public UpdateReadingProgressCommand {
        if (userId == null) {
            throw new ApplicationException(ApplicationErrorCode.INVALID_ARGUMENT, "userId");
        }
        if (digitalAssetId == null) {
            throw new ApplicationException(ApplicationErrorCode.INVALID_ARGUMENT, "digitalAssetId");
        }
        if (progressPercent == null) {
            throw new ApplicationException(ApplicationErrorCode.INVALID_ARGUMENT, "progressPercent");
        }
    }
}
