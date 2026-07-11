package com.bookstore.bookstore.presentation.exception;

import com.bookstore.bookstore.application.exception.ApplicationErrorCode;
import com.bookstore.bookstore.application.exception.ApplicationException;
import com.bookstore.bookstore.application.exception.OtpRateLimitException;
import com.bookstore.bookstore.application.exception.AuthRateLimitException;
import com.bookstore.bookstore.domain.exception.DomainErrorCode;
import com.bookstore.bookstore.domain.exception.DomainException;
import com.bookstore.bookstore.presentation.response.ApiResponse;
import jakarta.validation.ConstraintViolationException;
import java.util.Map;
import java.util.Objects;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AnonymousAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.validation.FieldError;
import org.springframework.web.ErrorResponse;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.servlet.NoHandlerFoundException;
import org.springframework.web.servlet.resource.NoResourceFoundException;

@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(AuthRateLimitException.class)
    public ResponseEntity<ApiResponse<java.util.Map<String, Long>>> handleAuthRateLimitException(AuthRateLimitException exception) {
        return ResponseEntity.status(HttpStatus.TOO_MANY_REQUESTS)
                .header(HttpHeaders.RETRY_AFTER, String.valueOf(exception.getRetryAfterSeconds()))
                .body(ApiResponse.error(
                        ApplicationErrorCode.AUTH_RATE_LIMITED.name(),
                        exception.getMessage(),
                        java.util.Map.of("retryAfterSeconds", exception.getRetryAfterSeconds())
                ));
    }

    @ExceptionHandler(OtpRateLimitException.class)
    public ResponseEntity<ApiResponse<Map<String, Long>>> handleOtpRateLimitException(OtpRateLimitException exception) {
        return ResponseEntity.status(HttpStatus.TOO_MANY_REQUESTS)
                .header(HttpHeaders.RETRY_AFTER, String.valueOf(exception.getRetryAfterSeconds()))
                .body(ApiResponse.error(
                        exception.getMessage(),
                        Map.of("retryAfterSeconds", exception.getRetryAfterSeconds())
                ));
    }

    @ExceptionHandler(ApplicationException.class)
    public ResponseEntity<ApiResponse<Void>> handleApplicationException(ApplicationException exception) {
        HttpStatus status = mapApplicationStatus(exception.getErrorCode());
        return ResponseEntity.status(status).body(ApiResponse.error(exception.getErrorCode().getCode(), exception.getMessage()));
    }

    @ExceptionHandler(DomainException.class)
    public ResponseEntity<ApiResponse<Void>> handleDomainException(DomainException exception) {
        HttpStatus status = mapDomainStatus(exception.getErrorCode());
        return ResponseEntity.status(status).body(ApiResponse.error(exception.getErrorCode().name(), exception.getMessage()));
    }

    @ExceptionHandler(AccessDeniedException.class)
    public ResponseEntity<ApiResponse<Void>> handleAccessDeniedException(AccessDeniedException exception) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        boolean authenticated = authentication != null
                && authentication.isAuthenticated()
                && !(authentication instanceof AnonymousAuthenticationToken);
        HttpStatus status = authenticated ? HttpStatus.FORBIDDEN : HttpStatus.UNAUTHORIZED;
        String message = authenticated ? "Bạn không có quyền truy cập" : "Bạn chưa đăng nhập";
        return ResponseEntity.status(status).body(ApiResponse.error(message));
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
        return ResponseEntity.badRequest().body(ApiResponse.error("Dữ liệu không hợp lệ"));
    }

    @ExceptionHandler({IllegalArgumentException.class, MethodArgumentTypeMismatchException.class})
    public ResponseEntity<ApiResponse<Void>> handleIllegalArgumentException(RuntimeException exception) {
        return ResponseEntity.badRequest().body(ApiResponse.error("Dữ liệu không hợp lệ"));
    }

    @ExceptionHandler({NoHandlerFoundException.class, NoResourceFoundException.class})
    public ResponseEntity<ApiResponse<Void>> handleNotFoundException(Exception exception) {
        return ResponseEntity.status(HttpStatus.NOT_FOUND)
                .body(ApiResponse.error("API không tồn tại"));
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ApiResponse<Void>> handleException(Exception exception) {
        if (exception instanceof ErrorResponse errorResponse) {
            HttpStatusCode statusCode = errorResponse.getStatusCode();
            HttpStatus status = HttpStatus.resolve(statusCode.value());
            String message = errorResponse.getBody().getDetail();

            if (status != null) {
                if (message == null || message.isBlank()) {
                    message = status.getReasonPhrase();
                }

                return ResponseEntity.status(status).body(ApiResponse.error(message));
            }
        }

        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(ApiResponse.error("Internal server error"));
    }

    private HttpStatus mapApplicationStatus(ApplicationErrorCode errorCode) {
        return switch (errorCode) {
            case INVALID_ARGUMENT, INVALID_AUTH_PASSWORD, USER_ROLE_NOT_ALLOWED, OTP_INVALID, OTP_EXPIRED, OTP_LOCKED,
                 OTP_NOT_VERIFIED, COUPON_TYPE_NOT_MATCH, CHAT_ASSIGNEE_ROLE_INVALID,
                 FILE_ASSET_INVALID_PURPOSE, FILE_ASSET_INVALID_VISIBILITY,
                 FILE_ASSET_CONTENT_TYPE_NOT_ALLOWED, FILE_ASSET_SIZE_EXCEEDED,
                 FILE_ASSET_DOWNLOAD_NOT_ALLOWED, FILE_ASSET_OBJECT_NOT_FOUND,
                 FILE_ASSET_OBJECT_METADATA_MISMATCH, RETURN_REQUEST_REFUND_AMOUNT_INVALID,
                 RETURN_REQUEST_REJECT_NOTE_REQUIRED, REFUND_AMOUNT_INVALID, REFUND_EVIDENCE_REQUIRED,
                 REFUND_FAILURE_REASON_REQUIRED, REFUND_CURRENCY_INVALID, OUTBOX_PAYLOAD_INVALID ->
                    HttpStatus.BAD_REQUEST;
            case OTP_RATE_LIMITED, AUTH_RATE_LIMITED -> HttpStatus.TOO_MANY_REQUESTS;
            case AUTH_USER_NOT_FOUND, AUTH_INVALID_PASSWORD, AUTH_INVALID_CREDENTIALS,
                 AUTH_INVALID_REFRESH_TOKEN, AUTH_REFRESH_TOKEN_EXPIRED, AUTH_SESSION_EXPIRED,
                 AUTH_SESSION_REVOKED, AUTH_REFRESH_REUSE_DETECTED,
                 AUTH_INVALID_PASSWORD_RESET_TOKEN, AUTH_PASSWORD_RESET_TOKEN_EXPIRED,
                 AUTH_PASSWORD_LOGIN_NOT_AVAILABLE, AUTH_GOOGLE_INVALID_ID_TOKEN, AUTH_GOOGLE_EMAIL_NOT_VERIFIED,
                 AUTH_CSRF_INVALID ->
                    HttpStatus.UNAUTHORIZED;
            case PAYMENT_WEBHOOK_UNAUTHORIZED -> HttpStatus.UNAUTHORIZED;
            case USER_NOT_FOUND, STAFF_NOT_FOUND, SHIPPER_NOT_FOUND, ROLE_NOT_FOUND, CATEGORY_NOT_FOUND, AUTHOR_NOT_FOUND, PUBLISHER_NOT_FOUND,
                 SUPPLIER_NOT_FOUND,
                 IMPORT_RECEIPT_NOT_FOUND, AUDIT_LOG_NOT_FOUND, COUPON_NOT_FOUND,
                 BOOK_NOT_FOUND, BOOKSHELF_NOT_FOUND, READING_JOURNAL_ENTRY_NOT_FOUND, DIGITAL_ASSET_NOT_FOUND, FILE_ASSET_NOT_FOUND, CART_NOT_FOUND, CART_ITEM_NOT_FOUND, ORDER_NOT_FOUND, SHIPMENT_NOT_FOUND, PAYMENT_NOT_FOUND, PAYMENT_RECONCILIATION_NOT_FOUND, USER_ADDRESS_NOT_FOUND,
                  NOTIFICATION_NOT_FOUND,
                  CHAT_CONVERSATION_NOT_FOUND, CHAT_MESSAGE_NOT_FOUND, CHAT_ASSIGNEE_NOT_FOUND,
                  REVIEW_NOT_FOUND, RETURN_REQUEST_NOT_FOUND, REFUND_NOT_FOUND, OUTBOX_EVENT_NOT_FOUND,
                  PERMISSION_NOT_FOUND, PROFILE_NOT_FOUND, PROFILE_USER_NOT_FOUND ->
                    HttpStatus.NOT_FOUND;
            case USER_NOT_ADMIN, USER_SELF_MANAGEMENT_NOT_ALLOWED, REVIEW_BOOK_NOT_PURCHASED,
                 FILE_ASSET_ACCESS_DENIED,
                 CHAT_CONVERSATION_FORBIDDEN, CHAT_ADMIN_ROLE_REQUIRED, CHAT_CUSTOMER_ROLE_REQUIRED ->
                    HttpStatus.FORBIDDEN;
            case OTP_EMAIL_NOT_CONFIGURED, FILE_STORAGE_NOT_CONFIGURED -> HttpStatus.INTERNAL_SERVER_ERROR;
            case OTP_EMAIL_SEND_FAILED -> HttpStatus.BAD_GATEWAY;
            case CART_EMPTY, ORDER_PAYMENT_NOT_PAID, SHIPMENT_ORDER_NOT_READY, SHIPMENT_ORDER_ALREADY_HAS_ACTIVE_ASSIGNMENT,
                 ORDER_IDEMPOTENCY_PAYLOAD_MISMATCH,
                 ORDER_CANCELLATION_NOT_ALLOWED, ORDER_PAID_REFUND_REQUIRED,
                 REFUND_ORDER_NOT_PAID, REFUND_AMOUNT_EXCEEDS_REMAINING, REFUND_INVALID_TRANSITION,
                 REFUND_RETURN_REQUEST_INVALID, OUTBOX_EVENT_RETRY_NOT_ALLOWED,
                 PAYMENT_RECONCILIATION_NOT_OPEN,
                 DIGITAL_ASSET_PURCHASE_NOT_ALLOWED,
                 FILE_ASSET_UPLOAD_NOT_COMPLETED,
                 CHAT_CONVERSATION_CLOSED, RETURN_REQUEST_ORDER_NOT_DELIVERED, RETURN_REQUEST_ALREADY_EXISTS,
                 RETURN_REQUEST_NOT_PENDING -> HttpStatus.CONFLICT;
            case USER_ALREADY_EXISTS, USER_USERNAME_ALREADY_EXISTS, USER_PHONE_ALREADY_EXISTS, USER_EMAIL_ALREADY_EXISTS,
                 AUTH_GOOGLE_ACCOUNT_ALREADY_LINKED,
                 ROLE_ALREADY_EXISTS, ROLE_NAME_ALREADY_EXISTS,
                 CATEGORY_NAME_ALREADY_EXISTS,
                 AUTHOR_NAME_ALREADY_EXISTS,
                 PUBLISHER_NAME_ALREADY_EXISTS,
                 SUPPLIER_NAME_ALREADY_EXISTS,
                 BOOKSHELF_NAME_ALREADY_EXISTS,
                 READING_JOURNAL_ENTRY_ALREADY_EXISTS,
                 COUPON_CODE_ALREADY_EXISTS,
                 REVIEW_ALREADY_EXISTS,
                 PERMISSION_ALREADY_EXISTS, PERMISSION_CODE_ALREADY_EXISTS,
                 PERMISSION_IN_USE,
                 PROFILE_ALREADY_EXISTS, PROFILE_USER_ALREADY_HAS_PROFILE,
                 FILE_ASSET_IN_USE -> HttpStatus.CONFLICT;
            case BOOKSHELF_REORDER_INVALID -> HttpStatus.BAD_REQUEST;
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

