package com.bookstore.bookstore.presentation.controller;

import com.bookstore.bookstore.application.port.in.IAuthService;
import com.bookstore.bookstore.presentation.mapper.AuthWebMapper;
import com.bookstore.bookstore.presentation.request.GoogleLoginRequest;
import com.bookstore.bookstore.presentation.request.LoginRequest;
import com.bookstore.bookstore.presentation.request.LogoutRequest;
import com.bookstore.bookstore.presentation.request.RefreshTokenRequest;
import com.bookstore.bookstore.presentation.request.RegisterRequest;
import com.bookstore.bookstore.presentation.request.RequestPasswordResetOtpRequest;
import com.bookstore.bookstore.presentation.request.ResetPasswordRequest;
import com.bookstore.bookstore.presentation.request.VerifyOtpRequest;
import com.bookstore.bookstore.presentation.response.ApiResponse;
import com.bookstore.bookstore.presentation.response.LoginResponse;
import com.bookstore.bookstore.presentation.response.PasswordResetTokenResponse;
import com.bookstore.bookstore.presentation.response.RegisterResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {

    private final IAuthService authService;
    private final AuthWebMapper authWebMapper;

    @PostMapping("/register")
    public ResponseEntity<ApiResponse<RegisterResponse>> register(@Valid @RequestBody RegisterRequest request) {
        var result = authService.register(authWebMapper.toRegisterCommand(request));
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success(authWebMapper.toRegisterResponse(result)));
    }

    @PostMapping("/login")
    public ResponseEntity<ApiResponse<LoginResponse>> login(@Valid @RequestBody LoginRequest request) {
        var result = authService.login(authWebMapper.toLoginCommand(request));
        return ResponseEntity.ok(ApiResponse.success(authWebMapper.toLoginResponse(result)));
    }

    @PostMapping("/google")
    public ResponseEntity<ApiResponse<LoginResponse>> loginWithGoogle(@Valid @RequestBody GoogleLoginRequest request) {
        var result = authService.loginWithGoogle(authWebMapper.toGoogleLoginCommand(request));
        return ResponseEntity.ok(ApiResponse.success(authWebMapper.toLoginResponse(result)));
    }

    @PostMapping("/refresh")
    public ResponseEntity<ApiResponse<LoginResponse>> refresh(@Valid @RequestBody RefreshTokenRequest request) {
        var result = authService.refresh(authWebMapper.toRefreshCommand(request));
        return ResponseEntity.ok(ApiResponse.success(authWebMapper.toLoginResponse(result)));
    }

    @PostMapping("/logout")
    public ApiResponse<Void> logout(@Valid @RequestBody LogoutRequest request) {
        authService.logout(authWebMapper.toLogoutCommand(request));
        return ApiResponse.success("Logged out", null);
    }

    @PostMapping("/forgot-password/request-otp")
    public ApiResponse<Void> requestPasswordResetOtp(@Valid @RequestBody RequestPasswordResetOtpRequest request) {
        authService.requestPasswordResetOtp(authWebMapper.toRequestPasswordResetOtpCommand(request));
        return ApiResponse.success("Nếu email tồn tại, OTP đã được gửi", null);
    }

    @PostMapping("/forgot-password/verify-otp")
    public ApiResponse<PasswordResetTokenResponse> verifyPasswordResetOtp(@Valid @RequestBody VerifyOtpRequest request) {
        var result = authService.verifyPasswordResetOtp(authWebMapper.toVerifyOtpCommand(request));
        return ApiResponse.success(
                "Xác thực OTP thành công",
                authWebMapper.toPasswordResetTokenResponse(result)
        );
    }

    @PostMapping("/forgot-password/reset")
    public ApiResponse<Void> resetPassword(@Valid @RequestBody ResetPasswordRequest request) {
        authService.resetPassword(authWebMapper.toResetPasswordCommand(request));
        return ApiResponse.success("Đặt lại mật khẩu thành công", null);
    }
}

