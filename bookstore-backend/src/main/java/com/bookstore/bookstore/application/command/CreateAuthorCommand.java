package com.bookstore.bookstore.application.command;

import com.bookstore.bookstore.application.exception.ApplicationErrorCode;
import com.bookstore.bookstore.application.exception.ApplicationException;
import java.util.UUID;

public record CreateAuthorCommand(
        String name,
        String biography,
        UUID avatarFileAssetId,
        Integer birthYear,
        Integer deathYear
) {
    public CreateAuthorCommand {
        if (name == null || name.isBlank()) {
            throw new ApplicationException(ApplicationErrorCode.INVALID_ARGUMENT, "name");
        }
    }
}
