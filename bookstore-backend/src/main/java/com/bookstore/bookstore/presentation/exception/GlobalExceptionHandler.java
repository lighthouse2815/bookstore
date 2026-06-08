package com.bookstore.bookstore.presentation.exception;

import com.bookstore.bookstore.application.exception.ApplicationErrorCode;
import com.bookstore.bookstore.application.exception.ApplicationException;
import com.bookstore.bookstore.domain.exception.DomainErrorCode;
import com.bookstore.bookstore.domain.exception.DomainException;
import com.bookstore.bookstore.presentation.response.ApiResponse;
import jakarta.validation.ConstraintViolationException;
import java.util.Objects;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(ApplicationException.class)
    public ResponseEntity<ApiResponse<Void>> handleApplicationException(ApplicationException exception) {
        HttpStatus status = mapApplicationStatus(exception.getErrorCode());
        return ResponseEntity.status(status).body(ApiResponse.error(exception.getMessage()));
    }

    @ExceptionHandler(DomainException.class)
    public ResponseEntity<ApiResponse<Void>> handleDomainException(DomainException exception) {
        HttpStatus status = mapDomainStatus(exception.getErrorCode());
        return ResponseEntity.status(status).body(ApiResponse.error(exception.getMessage()));
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ApiResponse<Void>> handleMethodArgumentNotValidException(
            MethodArgumentNotValidException exception
    ) {
        String message = exception.getBindingResult()
                .getFieldErrors()
                .stream()
                .map(FieldError::getDefaultMessage)
                .filter(Objects::nonNull)
                .findFirst()
                .orElse("Invalid request");

        return ResponseEntity.badRequest().body(ApiResponse.error(message));
    }

    @ExceptionHandler(ConstraintViolationException.class)
    public ResponseEntity<ApiResponse<Void>> handleConstraintViolationException(
            ConstraintViolationException exception
    ) {
        String message = exception.getConstraintViolations()
                .stream()
                .map(violation -> violation.getMessage())
                .findFirst()
                .orElse("Invalid request");

        return ResponseEntity.badRequest().body(ApiResponse.error(message));
    }

    @ExceptionHandler(HttpMessageNotReadableException.class)
    public ResponseEntity<ApiResponse<Void>> handleHttpMessageNotReadableException(
            HttpMessageNotReadableException exception
    ) {
        return ResponseEntity.badRequest().body(ApiResponse.error("Du lieu khong hop le"));
    }

    @ExceptionHandler({IllegalArgumentException.class, MethodArgumentTypeMismatchException.class})
    public ResponseEntity<ApiResponse<Void>> handleIllegalArgumentException(RuntimeException exception) {
        return ResponseEntity.badRequest().body(ApiResponse.error("Du lieu khong hop le"));
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ApiResponse<Void>> handleException(Exception exception) {
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(ApiResponse.error("Internal server error"));
    }

    private HttpStatus mapApplicationStatus(ApplicationErrorCode errorCode) {
        return switch (errorCode) {
            case INVALID_ARGUMENT, INVALID_AUTH_PASSWORD -> HttpStatus.BAD_REQUEST;
            case AUTH_USER_NOT_FOUND, AUTH_INVALID_PASSWORD, AUTH_INVALID_REFRESH_TOKEN, AUTH_REFRESH_TOKEN_EXPIRED ->
                    HttpStatus.UNAUTHORIZED;
            case USER_NOT_FOUND, ROLE_NOT_FOUND, CATEGORY_NOT_FOUND, AUTHOR_NOT_FOUND, PUBLISHER_NOT_FOUND,
                 SUPPLIER_NOT_FOUND,
                 IMPORT_RECEIPT_NOT_FOUND, COUPON_NOT_FOUND,
                 BOOK_NOT_FOUND, CART_NOT_FOUND, ORDER_NOT_FOUND, USER_ADDRESS_NOT_FOUND, NOTIFICATION_NOT_FOUND,
                 REVIEW_NOT_FOUND,
                 PERMISSION_NOT_FOUND, PROFILE_NOT_FOUND, PROFILE_USER_NOT_FOUND ->
                    HttpStatus.NOT_FOUND;
            case USER_NOT_ADMIN, REVIEW_BOOK_NOT_PURCHASED -> HttpStatus.FORBIDDEN;
            case CART_EMPTY -> HttpStatus.CONFLICT;
            case USER_ALREADY_EXISTS, USER_USERNAME_ALREADY_EXISTS, USER_PHONE_ALREADY_EXISTS, USER_EMAIL_ALREADY_EXISTS,
                 ROLE_ALREADY_EXISTS, ROLE_NAME_ALREADY_EXISTS,
                 CATEGORY_NAME_ALREADY_EXISTS,
                 AUTHOR_NAME_ALREADY_EXISTS,
                 PUBLISHER_NAME_ALREADY_EXISTS,
                 SUPPLIER_NAME_ALREADY_EXISTS,
                 COUPON_CODE_ALREADY_EXISTS,
                 REVIEW_ALREADY_EXISTS,
                 PERMISSION_ALREADY_EXISTS, PERMISSION_CODE_ALREADY_EXISTS,
                 PERMISSION_IN_USE,
                 PROFILE_ALREADY_EXISTS, PROFILE_USER_ALREADY_HAS_PROFILE -> HttpStatus.CONFLICT;
        };
    }

    private HttpStatus mapDomainStatus(DomainErrorCode errorCode) {
        String name = errorCode.name();

        if (name.startsWith("INVALID_")) {
            return HttpStatus.BAD_REQUEST;
        }

        if (name.contains("_NOT_FOUND")) {
            return HttpStatus.NOT_FOUND;
        }

        if (name.endsWith("_ALREADY_EXISTS")
                || name.endsWith("_ALREADY_DELETED")
                || name.endsWith("_NOT_CHANGED")
                || name.endsWith("_IN_USE")
                || name.endsWith("_ALREADY_ACTIVE")) {
            return HttpStatus.CONFLICT;
        }

        if (name.contains("_CANNOT_") || name.startsWith("DELETED_") || name.startsWith("BLOCKED_")) {
            return HttpStatus.FORBIDDEN;
        }

        return HttpStatus.BAD_REQUEST;
    }
}
