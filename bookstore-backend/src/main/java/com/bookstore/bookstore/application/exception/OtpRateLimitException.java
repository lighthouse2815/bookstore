package com.bookstore.bookstore.application.exception;

public class OtpRateLimitException extends ApplicationException {

    private final long retryAfterSeconds;

    public OtpRateLimitException(long retryAfterSeconds) {
        super(ApplicationErrorCode.OTP_RATE_LIMITED, retryAfterSeconds);
        this.retryAfterSeconds = retryAfterSeconds;
    }

    public long getRetryAfterSeconds() {
        return retryAfterSeconds;
    }
}
