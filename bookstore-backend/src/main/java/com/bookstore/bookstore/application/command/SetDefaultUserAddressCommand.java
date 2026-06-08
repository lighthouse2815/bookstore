package com.bookstore.bookstore.application.command;

import com.bookstore.bookstore.application.exception.ApplicationErrorCode;
import com.bookstore.bookstore.application.exception.ApplicationException;
import java.util.UUID;

public record SetDefaultUserAddressCommand(
        UUID userId,
        UUID addressId
) {
    public SetDefaultUserAddressCommand {
        if (userId == null) {
            throw new ApplicationException(ApplicationErrorCode.INVALID_ARGUMENT, "userId");
        }
        if (addressId == null) {
            throw new ApplicationException(ApplicationErrorCode.INVALID_ARGUMENT, "addressId");
        }
    }
}
