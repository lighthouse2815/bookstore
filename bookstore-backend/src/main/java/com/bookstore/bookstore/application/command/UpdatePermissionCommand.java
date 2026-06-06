package com.bookstore.bookstore.application.command;

import com.bookstore.bookstore.application.exception.ApplicationErrorCode;
import com.bookstore.bookstore.application.exception.ApplicationException;
import com.bookstore.bookstore.domain.enums.PermissionCode;
import java.util.UUID;

public record UpdatePermissionCommand(
        UUID permissionId,
        PermissionCode code,
        String description
) {
    public UpdatePermissionCommand {
        if (permissionId == null) {
            throw new ApplicationException(ApplicationErrorCode.INVALID_ARGUMENT, "permissionId");
        }
    }
}
