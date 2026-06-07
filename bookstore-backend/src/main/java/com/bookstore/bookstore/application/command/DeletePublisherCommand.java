package com.bookstore.bookstore.application.command;

import com.bookstore.bookstore.application.exception.ApplicationErrorCode;
import com.bookstore.bookstore.application.exception.ApplicationException;
import java.util.UUID;

public record DeletePublisherCommand(
        UUID publisherId
) {
    public DeletePublisherCommand {
        if (publisherId == null) {
            throw new ApplicationException(ApplicationErrorCode.INVALID_ARGUMENT, "publisherId");
        }
    }
}
