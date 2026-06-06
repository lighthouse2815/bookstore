package com.bookstore.bookstore.application.command;

import com.bookstore.bookstore.application.exception.ApplicationErrorCode;
import com.bookstore.bookstore.application.exception.ApplicationException;
import com.bookstore.bookstore.domain.enums.PermissionCode;
import java.util.Set;

public record CreateRoleCommand(
        String name,
        String description,
        Set<PermissionCode> permissionCodes
) {
    public CreateRoleCommand {
        if (name == null || name.isBlank()) {
            throw new ApplicationException(ApplicationErrorCode.INVALID_ARGUMENT, "name");
        }
    }
}
