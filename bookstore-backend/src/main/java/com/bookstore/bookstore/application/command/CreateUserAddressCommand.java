package com.bookstore.bookstore.application.command;

import com.bookstore.bookstore.application.exception.ApplicationErrorCode;
import com.bookstore.bookstore.application.exception.ApplicationException;
import java.util.UUID;

public record CreateUserAddressCommand(
        UUID userId,
        String receiverName,
        String receiverPhone,
        String receiverAddress
) {
    public CreateUserAddressCommand {
        if (userId == null) {
            throw new ApplicationException(ApplicationErrorCode.INVALID_ARGUMENT, "userId");
        }
        if (receiverName == null || receiverName.isBlank()) {
            throw new ApplicationException(ApplicationErrorCode.INVALID_ARGUMENT, "receiverName");
        }
        if (receiverPhone == null || receiverPhone.isBlank()) {
            throw new ApplicationException(ApplicationErrorCode.INVALID_ARGUMENT, "receiverPhone");
        }
        if (receiverAddress == null || receiverAddress.isBlank()) {
            throw new ApplicationException(ApplicationErrorCode.INVALID_ARGUMENT, "receiverAddress");
        }
    }
}
