package com.bookstore.bookstore.presentation.mapper;

import com.bookstore.bookstore.application.command.LoginCommand;
import com.bookstore.bookstore.application.command.LogoutCommand;
import com.bookstore.bookstore.application.command.RefreshAccessTokenCommand;
import com.bookstore.bookstore.application.command.RegisterCommand;
import com.bookstore.bookstore.application.result.LoginResult;
import com.bookstore.bookstore.application.result.RegisterResult;
import com.bookstore.bookstore.presentation.request.LoginRequest;
import com.bookstore.bookstore.presentation.request.LogoutRequest;
import com.bookstore.bookstore.presentation.request.RefreshTokenRequest;
import com.bookstore.bookstore.presentation.request.RegisterRequest;
import com.bookstore.bookstore.presentation.response.LoginResponse;
import com.bookstore.bookstore.presentation.response.RegisterResponse;
import org.springframework.stereotype.Component;

@Component
public class AuthWebMapper {

    public RegisterCommand toRegisterCommand(RegisterRequest request) {
        return new RegisterCommand(
                request.username(),
                request.password(),
                request.phoneNumber(),
                request.email(),
                request.firstName(),
                request.lastName(),
                request.avatarUrl(),
                request.gender(),
                request.dateOfBirth()
        );
    }

    public LoginCommand toLoginCommand(LoginRequest request) {
        return new LoginCommand(request.username(), request.password());
    }

    public RefreshAccessTokenCommand toRefreshCommand(RefreshTokenRequest request) {
        return new RefreshAccessTokenCommand(request.refreshToken());
    }

    public LogoutCommand toLogoutCommand(LogoutRequest request) {
        return new LogoutCommand(request.refreshToken());
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
}
