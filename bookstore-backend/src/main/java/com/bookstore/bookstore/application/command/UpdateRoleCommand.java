package com.bookstore.bookstore.application.command;

import com.bookstore.bookstore.application.exception.ApplicationErrorCode;
import com.bookstore.bookstore.application.exception.ApplicationException;
import com.bookstore.bookstore.domain.enums.PermissionCode;
import java.util.Set;
import java.util.UUID;

public record UpdateRoleCommand(
        UUID roleId,
        String name,
        String description,
        Set<PermissionCode> permissionCodes
) {
    public UpdateRoleCommand {
        if (roleId == null) {
            throw new ApplicationException(ApplicationErrorCode.INVALID_ARGUMENT, "roleId");
        }

        if (name == null || name.isBlank()) {
            throw new ApplicationException(ApplicationErrorCode.INVALID_ARGUMENT, "name");
        }
    }
}
