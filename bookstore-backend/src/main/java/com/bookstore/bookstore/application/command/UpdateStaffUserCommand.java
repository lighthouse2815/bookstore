package com.bookstore.bookstore.application.command;

import com.bookstore.bookstore.application.exception.ApplicationErrorCode;
import com.bookstore.bookstore.application.exception.ApplicationException;
import java.util.LinkedHashSet;
import java.util.Set;
import java.util.UUID;

public record UpdateStaffUserCommand(
        UUID userId,
        String phoneNumber,
        String email,
        Set<String> roleNames
) {
    public UpdateStaffUserCommand {
        if (userId == null) {
            throw new ApplicationException(ApplicationErrorCode.INVALID_ARGUMENT, "userId");
        }

        if (roleNames == null || roleNames.isEmpty()) {
            throw new ApplicationException(ApplicationErrorCode.INVALID_ARGUMENT, "roleNames");
        }

        roleNames = new LinkedHashSet<>(roleNames);
    }
}
