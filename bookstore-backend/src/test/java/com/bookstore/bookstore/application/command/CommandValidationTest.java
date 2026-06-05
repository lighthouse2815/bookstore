package com.bookstore.bookstore.application.command;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertThrows;

import com.bookstore.bookstore.application.exception.ApplicationErrorCode;
import com.bookstore.bookstore.application.exception.ApplicationException;
import com.bookstore.bookstore.domain.enums.Gender;
import java.time.LocalDate;
import java.util.UUID;
import org.junit.jupiter.api.Test;

class CommandValidationTest {

    @Test
    void updateUserCommand_rejectsNullUserId() {
        ApplicationException exception = assertThrows(ApplicationException.class, () ->
                new UpdateUserCommand(null, "username", "0123456789", "test@gmail.com")
        );

        assertEquals(ApplicationErrorCode.INVALID_ARGUMENT, exception.getErrorCode());
    }

    @Test
    void updateProfileCommand_rejectsNullUserId() {
        ApplicationException exception = assertThrows(ApplicationException.class, () ->
                new UpdateProfileCommand(
                        null,
                        "last",
                        "first",
                        "avatar",
                        Gender.MALE,
                        LocalDate.now()
                )
        );

        assertEquals(ApplicationErrorCode.INVALID_ARGUMENT, exception.getErrorCode());
    }

    @Test
    void deleteUserCommand_rejectsNullUserId() {
        ApplicationException exception = assertThrows(ApplicationException.class, () ->
                new DeleteUserCommand(null, UUID.randomUUID())
        );

        assertEquals(ApplicationErrorCode.INVALID_ARGUMENT, exception.getErrorCode());
    }

    @Test
    void deleteUserCommand_normalizesNullAdminIdToUserId() {
        UUID userId = UUID.randomUUID();

        DeleteUserCommand command = new DeleteUserCommand(userId, null);

        assertEquals(userId, command.userId());
        assertEquals(userId, command.adminId());
    }

    @Test
    void registerCommand_rejectsNullPassword() {
        ApplicationException exception = assertThrows(ApplicationException.class, () ->
                new RegisterCommand(
                        "username",
                        null,
                        "0123456789",
                        "test@gmail.com",
                        "first",
                        "last",
                        null,
                        Gender.MALE,
                        LocalDate.now()
                )
        );

        assertEquals(ApplicationErrorCode.INVALID_AUTH_PASSWORD, exception.getErrorCode());
    }

    @Test
    void registerCommand_allowsWhitespacePassword() {
        assertDoesNotThrow(() ->
                new RegisterCommand(
                        "username",
                        "   ",
                        "0123456789",
                        "test@gmail.com",
                        "first",
                        "last",
                        null,
                        Gender.MALE,
                        LocalDate.now()
                )
        );
    }

    @Test
    void loginCommand_rejectsNullUsername() {
        ApplicationException exception = assertThrows(ApplicationException.class, () ->
                new LoginCommand(null, "password")
        );

        assertEquals(ApplicationErrorCode.INVALID_ARGUMENT, exception.getErrorCode());
    }

    @Test
    void loginCommand_rejectsNullPassword() {
        ApplicationException exception = assertThrows(ApplicationException.class, () ->
                new LoginCommand("username", null)
        );

        assertEquals(ApplicationErrorCode.INVALID_AUTH_PASSWORD, exception.getErrorCode());
    }
}
