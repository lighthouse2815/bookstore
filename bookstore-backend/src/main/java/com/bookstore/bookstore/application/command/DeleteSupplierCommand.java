package com.bookstore.bookstore.application.command;

import com.bookstore.bookstore.application.exception.ApplicationErrorCode;
import com.bookstore.bookstore.application.exception.ApplicationException;
import java.util.UUID;

public record DeleteSupplierCommand(
        UUID supplierId
) {
    public DeleteSupplierCommand {
        if (supplierId == null) {
            throw new ApplicationException(ApplicationErrorCode.INVALID_ARGUMENT, "supplierId");
        }
    }
}
