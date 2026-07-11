package com.bookstore.bookstore.application.port.in;

import com.bookstore.bookstore.application.command.GoogleLoginCommand;
import com.bookstore.bookstore.application.command.LoginCommand;
import com.bookstore.bookstore.application.command.LogoutCommand;
import com.bookstore.bookstore.application.command.RefreshAccessTokenCommand;
import com.bookstore.bookstore.application.command.RegisterCommand;
import com.bookstore.bookstore.application.command.RequestPasswordResetOtpCommand;
import com.bookstore.bookstore.application.command.ResetPasswordCommand;
import com.bookstore.bookstore.application.command.VerifyOtpCommand;
import com.bookstore.bookstore.application.result.LoginResult;
import com.bookstore.bookstore.application.result.PasswordResetTokenResult;
import com.bookstore.bookstore.application.result.RegisterResult;
import com.bookstore.bookstore.application.result.SessionResult;
import java.util.List;
import java.util.UUID;

public interface IAuthService {

    RegisterResult register(RegisterCommand command);

    LoginResult loginWithGoogle(GoogleLoginCommand command);

    LoginResult login(LoginCommand command);

    LoginResult refresh(RefreshAccessTokenCommand command);

    void logout(LogoutCommand command);

    void logoutAll(UUID userId);

    List<SessionResult> getSessions(UUID userId, UUID currentSessionId);

    void revokeSession(UUID userId, UUID sessionId, UUID currentSessionId);

    void requestPasswordResetOtp(RequestPasswordResetOtpCommand command);

    PasswordResetTokenResult verifyPasswordResetOtp(VerifyOtpCommand command);

    void resetPassword(ResetPasswordCommand command);
}
