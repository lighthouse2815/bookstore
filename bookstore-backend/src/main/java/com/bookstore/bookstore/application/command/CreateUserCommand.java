package com.bookstore.bookstore.application.command;

import com.bookstore.bookstore.application.exception.ApplicationErrorCode;
import com.bookstore.bookstore.application.exception.ApplicationException;
import com.bookstore.bookstore.domain.enums.Gender;
import java.time.LocalDate;

public record CreateUserCommand(
        String username,
        String password,
        String phoneNumber,
        String email,
        String firstName,
        String lastName,
        String avatarUrl,
        Gender gender,
        LocalDate dateOfBirth,
        String roleName
) {
    public CreateUserCommand {
        if (password == null) {
            throw new ApplicationException(ApplicationErrorCode.INVALID_AUTH_PASSWORD);
        }
    }
}
