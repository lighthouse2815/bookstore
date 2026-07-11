package com.bookstore.bookstore.presentation.mapper;

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
import com.bookstore.bookstore.application.command.AuthRequestMetadata;
import com.bookstore.bookstore.presentation.request.GoogleLoginRequest;
import com.bookstore.bookstore.presentation.request.LoginRequest;
import com.bookstore.bookstore.presentation.request.LogoutRequest;
import com.bookstore.bookstore.presentation.request.RefreshTokenRequest;
import com.bookstore.bookstore.presentation.request.RegisterRequest;
import com.bookstore.bookstore.presentation.request.RequestPasswordResetOtpRequest;
import com.bookstore.bookstore.presentation.request.ResetPasswordRequest;
import com.bookstore.bookstore.presentation.request.VerifyOtpRequest;
import com.bookstore.bookstore.presentation.response.LoginResponse;
import com.bookstore.bookstore.presentation.response.PasswordResetTokenResponse;
import com.bookstore.bookstore.presentation.response.RegisterResponse;
import org.springframework.stereotype.Component;

@Component
public class AuthWebMapper {

    public RegisterCommand toRegisterCommand(RegisterRequest request) {
        return new RegisterCommand(
                request.email(),
                request.password()
        );
    }

    public LoginCommand toLoginCommand(LoginRequest request, AuthRequestMetadata metadata) {
        return new LoginCommand(request.username(), request.password(), metadata);
    }

    public GoogleLoginCommand toGoogleLoginCommand(GoogleLoginRequest request) {
        return new GoogleLoginCommand(request.idToken());
    }

    public RefreshAccessTokenCommand toRefreshCommand(RefreshTokenRequest request, AuthRequestMetadata metadata) {
        return new RefreshAccessTokenCommand(request.refreshToken(), metadata);
    }

    public LogoutCommand toLogoutCommand(LogoutRequest request, AuthRequestMetadata metadata) {
        return new LogoutCommand(request.refreshToken(), metadata);
    }

    public RequestPasswordResetOtpCommand toRequestPasswordResetOtpCommand(
            RequestPasswordResetOtpRequest request,
            AuthRequestMetadata metadata
    ) {
        return new RequestPasswordResetOtpCommand(request.email(), metadata);
    }

    public VerifyOtpCommand toVerifyOtpCommand(VerifyOtpRequest request) {
        return new VerifyOtpCommand(request.email(), request.otpCode());
    }

    public ResetPasswordCommand toResetPasswordCommand(ResetPasswordRequest request) {
        return new ResetPasswordCommand(request.resetToken(), request.newPassword());
    }

    public RegisterResponse toRegisterResponse(RegisterResult result) {
        return new RegisterResponse(result.username(), result.createdAt());
    }

    public LoginResponse toLoginResponse(LoginResult result) {
        return new LoginResponse(
                result.userId(),
                result.status(),
                result.roles(),
                result.accessToken(),
                result.refreshToken()
        );
    }

    public PasswordResetTokenResponse toPasswordResetTokenResponse(PasswordResetTokenResult result) {
        return new PasswordResetTokenResponse(result.resetToken(), result.expiresAt());
    }

    public com.bookstore.bookstore.presentation.response.SessionResponse toSessionResponse(SessionResult result) {
        return new com.bookstore.bookstore.presentation.response.SessionResponse(
                result.sessionId(), result.deviceName(), result.deviceId(), result.userAgent(), result.ipAddress(),
                result.createdAt(), result.lastUsedAt(), result.expiresAt(), result.currentSession()
        );
    }
}
