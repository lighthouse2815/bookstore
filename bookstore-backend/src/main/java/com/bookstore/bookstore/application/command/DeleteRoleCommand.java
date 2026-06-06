package com.bookstore.bookstore.application.command;

import com.bookstore.bookstore.application.exception.ApplicationErrorCode;
import com.bookstore.bookstore.application.exception.ApplicationException;
import java.util.UUID;

public record DeleteRoleCommand(UUID roleId) {
    public DeleteRoleCommand {
        if (roleId == null) {
            throw new ApplicationException(ApplicationErrorCode.INVALID_ARGUMENT, "roleId");
        }
    }
}
