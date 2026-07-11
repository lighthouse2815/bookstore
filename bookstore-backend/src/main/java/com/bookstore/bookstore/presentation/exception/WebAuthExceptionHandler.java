package com.bookstore.bookstore.presentation.exception;

import com.bookstore.bookstore.application.exception.ApplicationException;
import com.bookstore.bookstore.infrastructure.security.AuthSecurityProperties;
import com.bookstore.bookstore.presentation.controller.WebAuthController;
import com.bookstore.bookstore.presentation.response.ApiResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseCookie;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

/** Clears a browser-only refresh cookie if its session cannot be used anymore. */
@RestControllerAdvice(assignableTypes = WebAuthController.class)
@RequiredArgsConstructor
public class WebAuthExceptionHandler {

    private final AuthSecurityProperties properties;

    @ExceptionHandler(ApplicationException.class)
    public ResponseEntity<ApiResponse<Void>> handleApplicationException(ApplicationException exception) {
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                .header(HttpHeaders.SET_COOKIE, expiredRefreshCookie().toString())
                .body(ApiResponse.error(exception.getErrorCode().name(), exception.getMessage()));
    }

    private ResponseCookie expiredRefreshCookie() {
        return ResponseCookie.from("BOOKSTORE_REFRESH", "")
                .httpOnly(true)
                .secure(properties.web().cookieSecure())
                .sameSite(properties.web().cookieSameSite())
                .path("/api/auth/web")
                .maxAge(0)
                .build();
    }
}
