package com.bookstore.bookstore.infrastructure.security;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import com.bookstore.bookstore.application.exception.ApplicationErrorCode;
import com.bookstore.bookstore.application.exception.ApplicationException;
import org.junit.jupiter.api.Test;

class NewsletterSubscriptionRateLimiterAdapterTest {

    @Test
    void checkAllowed_whenLimitExceeded_rejectsRequest() {
        NewsletterSubscriptionRateLimiterAdapter rateLimiter =
                new NewsletterSubscriptionRateLimiterAdapter(2, 15);

        assertDoesNotThrow(() -> rateLimiter.checkAllowed("127.0.0.1", "first@example.com"));
        assertDoesNotThrow(() -> rateLimiter.checkAllowed("127.0.0.1", "second@example.com"));

        ApplicationException exception = assertThrows(
                ApplicationException.class,
                () -> rateLimiter.checkAllowed("127.0.0.1", "third@example.com")
        );

        assertEquals(ApplicationErrorCode.NEWSLETTER_RATE_LIMITED, exception.getErrorCode());
    }
}
