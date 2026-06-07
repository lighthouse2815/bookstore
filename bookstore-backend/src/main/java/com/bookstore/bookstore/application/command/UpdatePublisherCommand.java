package com.bookstore.bookstore.application.command;

import com.bookstore.bookstore.application.exception.ApplicationErrorCode;
import com.bookstore.bookstore.application.exception.ApplicationException;
import java.util.UUID;

public record UpdatePublisherCommand(
        UUID publisherId,
        String name,
        String description
) {
    public UpdatePublisherCommand {
        if (publisherId == null) {
            throw new ApplicationException(ApplicationErrorCode.INVALID_ARGUMENT, "publisherId");
        }

        if (name == null || name.isBlank()) {
            throw new ApplicationException(ApplicationErrorCode.INVALID_ARGUMENT, "name");
        }
    }
}
