package com.bookstore.bookstore.application.command;

import com.bookstore.bookstore.application.exception.ApplicationErrorCode;
import com.bookstore.bookstore.application.exception.ApplicationException;
import com.bookstore.bookstore.domain.enums.Gender;
import java.time.LocalDate;
import java.util.UUID;

public record UpdateProfileCommand(
        UUID userId,
        String lastName,
        String firstName,
        UUID avatarFileAssetId,
        Gender gender,
        LocalDate dateOfBirth
) {
    public UpdateProfileCommand {
        if (userId == null) {
            throw new ApplicationException(ApplicationErrorCode.INVALID_ARGUMENT, "userId");
        }
    }
}
