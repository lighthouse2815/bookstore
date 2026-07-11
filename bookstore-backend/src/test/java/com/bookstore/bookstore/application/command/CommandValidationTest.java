package com.bookstore.bookstore.application.command;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertThrows;

import com.bookstore.bookstore.application.exception.ApplicationErrorCode;
import com.bookstore.bookstore.application.exception.ApplicationException;
import com.bookstore.bookstore.domain.enums.Gender;
import com.bookstore.bookstore.domain.enums.PaymentMethod;
import com.bookstore.bookstore.domain.enums.ShippingMethod;
import java.util.List;
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
                        null,
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
                        "test@gmail.com",
                        null
                )
        );

        assertEquals(ApplicationErrorCode.INVALID_AUTH_PASSWORD, exception.getErrorCode());
    }

    @Test
    void registerCommand_allowsWhitespacePassword() {
        assertDoesNotThrow(() ->
                new RegisterCommand(
                        "test@gmail.com",
                        "   "
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

    @Test
    void googleLoginCommand_rejectsNullIdToken() {
        ApplicationException exception = assertThrows(ApplicationException.class, () ->
                new GoogleLoginCommand(null)
        );

        assertEquals(ApplicationErrorCode.INVALID_ARGUMENT, exception.getErrorCode());
    }

    @Test
    void verifyOtpCommand_rejectsNullEmail() {
        ApplicationException exception = assertThrows(ApplicationException.class, () ->
                new VerifyOtpCommand(null, "123456")
        );

        assertEquals(ApplicationErrorCode.INVALID_ARGUMENT, exception.getErrorCode());
    }

    @Test
    void verifyOtpCommand_rejectsNullOtpCode() {
        ApplicationException exception = assertThrows(ApplicationException.class, () ->
                new VerifyOtpCommand("test@gmail.com", null)
        );

        assertEquals(ApplicationErrorCode.INVALID_ARGUMENT, exception.getErrorCode());
    }

    @Test
    void requestPasswordResetOtpCommand_rejectsNullEmail() {
        ApplicationException exception = assertThrows(ApplicationException.class, () ->
                new RequestPasswordResetOtpCommand(null)
        );

        assertEquals(ApplicationErrorCode.INVALID_ARGUMENT, exception.getErrorCode());
    }

    @Test
    void resetPasswordCommand_rejectsNullResetToken() {
        ApplicationException exception = assertThrows(ApplicationException.class, () ->
                new ResetPasswordCommand(null, "new-password")
        );

        assertEquals(ApplicationErrorCode.INVALID_ARGUMENT, exception.getErrorCode());
    }

    @Test
    void resetPasswordCommand_rejectsNullNewPassword() {
        ApplicationException exception = assertThrows(ApplicationException.class, () ->
                new ResetPasswordCommand("reset-token", null)
        );

        assertEquals(ApplicationErrorCode.INVALID_AUTH_PASSWORD, exception.getErrorCode());
    }

    @Test
    void createOrderCommand_rejectsMissingOrMalformedIdempotencyKey() {
        ApplicationException missingKey = assertThrows(ApplicationException.class, () -> createOrderCommand(null));
        ApplicationException malformedKey = assertThrows(ApplicationException.class, () -> createOrderCommand("checkout-key"));

        assertEquals(ApplicationErrorCode.INVALID_ARGUMENT, missingKey.getErrorCode());
        assertEquals(ApplicationErrorCode.INVALID_ARGUMENT, malformedKey.getErrorCode());
    }

    private static CreateOrderCommand createOrderCommand(String idempotencyKey) {
        return new CreateOrderCommand(
                UUID.randomUUID(),
                List.of(UUID.randomUUID()),
                UUID.randomUUID(),
                ShippingMethod.DELIVERY,
                PaymentMethod.COD,
                null,
                null,
                null,
                idempotencyKey
        );
    }
}
