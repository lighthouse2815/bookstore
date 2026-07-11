package com.bookstore.bookstore.presentation.controller;

import com.bookstore.bookstore.application.command.RefreshAccessTokenCommand;
import com.bookstore.bookstore.application.command.LogoutCommand;
import com.bookstore.bookstore.application.port.in.IAuthService;
import com.bookstore.bookstore.application.result.LoginResult;
import com.bookstore.bookstore.infrastructure.security.AuthSecurityProperties;
import com.bookstore.bookstore.infrastructure.security.WebAuthCsrfFilter;
import com.bookstore.bookstore.presentation.mapper.AuthWebMapper;
import com.bookstore.bookstore.presentation.request.GoogleLoginRequest;
import com.bookstore.bookstore.presentation.request.LoginRequest;
import com.bookstore.bookstore.presentation.response.ApiResponse;
import com.bookstore.bookstore.presentation.response.WebLoginResponse;
import com.bookstore.bookstore.presentation.support.ClientRequestMetadataResolver;
import jakarta.servlet.http.HttpServletRequest;
import java.security.SecureRandom;
import java.util.Base64;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseCookie;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CookieValue;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import jakarta.validation.Valid;

@RestController
@RequestMapping("/api/auth/web")
@RequiredArgsConstructor
public class WebAuthController {

    private static final String REFRESH_COOKIE = "BOOKSTORE_REFRESH";
    private static final SecureRandom SECURE_RANDOM = new SecureRandom();

    private final IAuthService authService;
    private final AuthWebMapper authWebMapper;
    private final ClientRequestMetadataResolver clientRequestMetadataResolver;
    private final AuthSecurityProperties authSecurityProperties;

    @GetMapping("/csrf")
    public ResponseEntity<ApiResponse<Void>> csrf() {
        return ResponseEntity.ok()
                .header(HttpHeaders.SET_COOKIE, csrfCookie().toString())
                .body(ApiResponse.success(null));
    }

    @PostMapping("/login")
    public ResponseEntity<ApiResponse<WebLoginResponse>> login(
            @Valid @RequestBody LoginRequest request,
            HttpServletRequest httpRequest
    ) {
        LoginResult result = authService.login(authWebMapper.toLoginCommand(request, clientRequestMetadataResolver.resolve(httpRequest)));
        return responseWithSession(result);
    }

    @PostMapping("/google")
    public ResponseEntity<ApiResponse<WebLoginResponse>> loginWithGoogle(
            @Valid @RequestBody GoogleLoginRequest request
    ) {
        LoginResult result = authService.loginWithGoogle(authWebMapper.toGoogleLoginCommand(request));
        return responseWithSession(result);
    }

    @PostMapping("/refresh")
    public ResponseEntity<ApiResponse<WebLoginResponse>> refresh(
            @CookieValue(name = REFRESH_COOKIE, required = false) String refreshToken,
            HttpServletRequest httpRequest
    ) {
        LoginResult result = authService.refresh(new RefreshAccessTokenCommand(
                refreshToken, clientRequestMetadataResolver.resolve(httpRequest)
        ));
        return responseWithSession(result);
    }

    @PostMapping("/logout")
    public ResponseEntity<ApiResponse<Void>> logout(
            @CookieValue(name = REFRESH_COOKIE, required = false) String refreshToken,
            HttpServletRequest httpRequest
    ) {
        if (refreshToken != null && !refreshToken.isBlank()) {
            authService.logout(new LogoutCommand(refreshToken, clientRequestMetadataResolver.resolve(httpRequest)));
        }
        return ResponseEntity.ok()
                .header(HttpHeaders.SET_COOKIE, clearRefreshCookie().toString())
                .body(ApiResponse.success("Logged out", null));
    }

    private ResponseEntity<ApiResponse<WebLoginResponse>> responseWithSession(LoginResult result) {
        return ResponseEntity.ok()
                .header(HttpHeaders.SET_COOKIE, refreshCookie(result.refreshToken()).toString())
                .body(ApiResponse.success(new WebLoginResponse(
                        result.userId(), result.status(), result.roles(), result.accessToken()
                )));
    }

    private ResponseCookie refreshCookie(String token) {
        return ResponseCookie.from(REFRESH_COOKIE, token)
                .httpOnly(true)
                .secure(authSecurityProperties.web().cookieSecure())
                .sameSite(authSecurityProperties.web().cookieSameSite())
                .path("/api/auth/web")
                .maxAge(authSecurityProperties.web().cookieMaxAgeSeconds())
                .build();
    }

    private ResponseCookie clearRefreshCookie() {
        return ResponseCookie.from(REFRESH_COOKIE, "")
                .httpOnly(true)
                .secure(authSecurityProperties.web().cookieSecure())
                .sameSite(authSecurityProperties.web().cookieSameSite())
                .path("/api/auth/web")
                .maxAge(0)
                .build();
    }

    private ResponseCookie csrfCookie() {
        byte[] bytes = new byte[32];
        SECURE_RANDOM.nextBytes(bytes);
        return ResponseCookie.from(WebAuthCsrfFilter.CSRF_COOKIE, Base64.getUrlEncoder().withoutPadding().encodeToString(bytes))
                .httpOnly(false)
                .secure(authSecurityProperties.web().cookieSecure())
                .sameSite(authSecurityProperties.web().cookieSameSite())
                .path("/")
                .maxAge(authSecurityProperties.web().cookieMaxAgeSeconds())
                .build();
    }
}
