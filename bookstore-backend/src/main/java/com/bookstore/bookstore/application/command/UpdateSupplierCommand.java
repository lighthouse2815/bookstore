package com.bookstore.bookstore.application.command;

import com.bookstore.bookstore.application.exception.ApplicationErrorCode;
import com.bookstore.bookstore.application.exception.ApplicationException;
import java.util.UUID;

public record UpdateSupplierCommand(
        UUID supplierId,
        String name,
        String phone,
        String email,
        String address,
        String note
) {
    public UpdateSupplierCommand {
        if (supplierId == null) {
            throw new ApplicationException(ApplicationErrorCode.INVALID_ARGUMENT, "supplierId");
        }
        if (name == null || name.isBlank()) {
            throw new ApplicationException(ApplicationErrorCode.INVALID_ARGUMENT, "name");
        }
    }
}
