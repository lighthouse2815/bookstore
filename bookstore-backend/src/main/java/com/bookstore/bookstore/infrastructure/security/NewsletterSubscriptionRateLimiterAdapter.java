package com.bookstore.bookstore.infrastructure.security;

import com.bookstore.bookstore.application.exception.ApplicationErrorCode;
import com.bookstore.bookstore.application.exception.ApplicationException;
import com.bookstore.bookstore.application.port.out.INewsletterSubscriptionRateLimiter;
import com.bookstore.bookstore.shared.util.StringUtils;
import java.time.Duration;
import java.time.Instant;
import java.util.HashMap;
import java.util.Map;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Component
public class NewsletterSubscriptionRateLimiterAdapter implements INewsletterSubscriptionRateLimiter {

    private static final int DEFAULT_MAX_REQUESTS = 10;
    private static final long DEFAULT_WINDOW_MINUTES = 15;

    private final int maxRequests;
    private final Duration windowDuration;
    private final Map<String, AttemptWindow> attempts = new HashMap<>();

    public NewsletterSubscriptionRateLimiterAdapter(
            @Value("${app.newsletter.rate-limit.max-requests:10}") int configuredMaxRequests,
            @Value("${app.newsletter.rate-limit.window-minutes:15}") long configuredWindowMinutes
    ) {
        this.maxRequests = configuredMaxRequests > 0 ? configuredMaxRequests : DEFAULT_MAX_REQUESTS;
        long windowMinutes = configuredWindowMinutes > 0
                ? configuredWindowMinutes
                : DEFAULT_WINDOW_MINUTES;
        this.windowDuration = Duration.ofMinutes(windowMinutes);
    }

    @Override
    public synchronized void checkAllowed(String clientAddress, String normalizedEmail) {
        Instant now = Instant.now();
        String safeClientAddress = StringUtils.trimToNull(clientAddress);
        String clientKey = "client:" + (safeClientAddress == null ? "unknown" : safeClientAddress);
        String emailKey = "email:" + normalizedEmail;

        resetExpiredWindow(clientKey, now);
        resetExpiredWindow(emailKey, now);

        if (isAtLimit(clientKey) || isAtLimit(emailKey)) {
            throw new ApplicationException(ApplicationErrorCode.NEWSLETTER_RATE_LIMITED);
        }

        increment(clientKey, now);
        increment(emailKey, now);
        removeExpiredEntries(now);
    }

    private void resetExpiredWindow(String key, Instant now) {
        AttemptWindow currentWindow = attempts.get(key);
        if (currentWindow != null && !currentWindow.expiresAt().isAfter(now)) {
            attempts.remove(key);
        }
    }

    private boolean isAtLimit(String key) {
        AttemptWindow currentWindow = attempts.get(key);
        return currentWindow != null && currentWindow.count() >= maxRequests;
    }

    private void increment(String key, Instant now) {
        AttemptWindow currentWindow = attempts.get(key);
        if (currentWindow == null) {
            attempts.put(key, new AttemptWindow(1, now.plus(windowDuration)));
            return;
        }

        attempts.put(key, new AttemptWindow(currentWindow.count() + 1, currentWindow.expiresAt()));
    }

    private void removeExpiredEntries(Instant now) {
        if (attempts.size() < 1_000) {
            return;
        }

        attempts.entrySet().removeIf(entry -> !entry.getValue().expiresAt().isAfter(now));
    }

    private record AttemptWindow(int count, Instant expiresAt) {
    }
}
