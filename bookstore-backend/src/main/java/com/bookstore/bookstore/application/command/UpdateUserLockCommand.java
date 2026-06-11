package com.bookstore.bookstore.application.command;

import com.bookstore.bookstore.application.exception.ApplicationErrorCode;
import com.bookstore.bookstore.application.exception.ApplicationException;
import java.util.UUID;

public record UpdateUserLockCommand(
        UUID userId,
        UUID adminId,
        boolean locked
) {
    public UpdateUserLockCommand {
        if (userId == null) {
            throw new ApplicationException(ApplicationErrorCode.INVALID_ARGUMENT, "userId");
        }

        if (adminId == null) {
            throw new ApplicationException(ApplicationErrorCode.INVALID_ARGUMENT, "adminId");
        }
    }
}
