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
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import jakarta.servlet.http.HttpServletRequest;
import com.bookstore.bookstore.presentation.support.ClientRequestMetadataResolver;
import com.bookstore.bookstore.presentation.response.SessionResponse;
import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {

    private final IAuthService authService;
    private final AuthWebMapper authWebMapper;
    private final ClientRequestMetadataResolver clientRequestMetadataResolver;

    @PostMapping("/register")
    public ResponseEntity<ApiResponse<RegisterResponse>> register(@Valid @RequestBody RegisterRequest request) {
        var result = authService.register(authWebMapper.toRegisterCommand(request));
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success(authWebMapper.toRegisterResponse(result)));
    }

    @PostMapping("/login")
    public ResponseEntity<ApiResponse<LoginResponse>> login(@Valid @RequestBody LoginRequest request, HttpServletRequest httpRequest) {
        var result = authService.login(authWebMapper.toLoginCommand(request, clientRequestMetadataResolver.resolve(httpRequest)));
        return ResponseEntity.ok(ApiResponse.success(authWebMapper.toLoginResponse(result)));
    }

    @PostMapping("/google")
    public ResponseEntity<ApiResponse<LoginResponse>> loginWithGoogle(@Valid @RequestBody GoogleLoginRequest request) {
        var result = authService.loginWithGoogle(authWebMapper.toGoogleLoginCommand(request));
        return ResponseEntity.ok(ApiResponse.success(authWebMapper.toLoginResponse(result)));
    }

    @PostMapping("/refresh")
    public ResponseEntity<ApiResponse<LoginResponse>> refresh(@Valid @RequestBody RefreshTokenRequest request, HttpServletRequest httpRequest) {
        var result = authService.refresh(authWebMapper.toRefreshCommand(request, clientRequestMetadataResolver.resolve(httpRequest)));
        return ResponseEntity.ok(ApiResponse.success(authWebMapper.toLoginResponse(result)));
    }

    @PostMapping("/logout")
    public ApiResponse<Void> logout(@Valid @RequestBody LogoutRequest request, HttpServletRequest httpRequest) {
        authService.logout(authWebMapper.toLogoutCommand(request, clientRequestMetadataResolver.resolve(httpRequest)));
        return ApiResponse.success("Logged out", null);
    }

    @PostMapping("/logout-all")
    public ApiResponse<Void> logoutAll(@AuthenticationPrincipal Jwt jwt) {
        authService.logoutAll(userId(jwt));
        return ApiResponse.success("Logged out", null);
    }

    @GetMapping("/sessions")
    public ApiResponse<List<SessionResponse>> getSessions(@AuthenticationPrincipal Jwt jwt) {
        return ApiResponse.success(authService.getSessions(userId(jwt), sessionId(jwt)).stream()
                .map(authWebMapper::toSessionResponse)
                .toList());
    }

    @DeleteMapping("/sessions/{sessionId}")
    public ApiResponse<Void> revokeSession(@AuthenticationPrincipal Jwt jwt, @PathVariable UUID sessionId) {
        authService.revokeSession(userId(jwt), sessionId, sessionId(jwt));
        return ApiResponse.success("Session revoked", null);
    }

    @PostMapping("/forgot-password/request-otp")
    public ApiResponse<Void> requestPasswordResetOtp(
            @Valid @RequestBody RequestPasswordResetOtpRequest request,
            HttpServletRequest httpRequest
    ) {
        authService.requestPasswordResetOtp(authWebMapper.toRequestPasswordResetOtpCommand(
                request, clientRequestMetadataResolver.resolve(httpRequest)
        ));
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

    private UUID userId(Jwt jwt) {
        return UUID.fromString(jwt.getSubject());
    }

    private UUID sessionId(Jwt jwt) {
        String sid = jwt.getClaimAsString("sid");
        if (sid == null || sid.isBlank()) {
            return null;
        }
        try {
            return UUID.fromString(sid);
        } catch (IllegalArgumentException ignored) {
            return null;
        }
    }
}

