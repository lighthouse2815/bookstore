package com.bookstore.bookstore.infrastructure.security;

import com.bookstore.bookstore.application.exception.AuthRateLimitException;
import com.bookstore.bookstore.application.port.out.IAuthThrottleService;
import com.bookstore.bookstore.infrastructure.persistence.entity.AuthLoginAttemptJpaEntity;
import com.bookstore.bookstore.infrastructure.persistence.repository.AuthLoginAttemptJpaRepository;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.time.Duration;
import java.time.Instant;
import java.util.HexFormat;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class DatabaseAuthThrottleService implements IAuthThrottleService {

    private static final String ACCOUNT = "LOGIN_ACCOUNT";
    private static final String IP = "LOGIN_IP";
    private static final String RESET_EMAIL = "RESET_EMAIL";
    private static final String RESET_IP = "RESET_IP";

    private final AuthLoginAttemptJpaRepository repository;
    private final AuthSecurityProperties properties;

    @Override
    @Transactional(propagation = Propagation.REQUIRES_NEW, readOnly = true)
    public void assertLoginAllowed(String normalizedIdentifier, String normalizedIp) {
        Instant now = Instant.now();
        assertAttemptAllowed(ACCOUNT, hash(normalizedIdentifier), now);
        assertAttemptAllowed(IP, hash(normalizedIp), now);
    }

    @Override
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void recordLoginFailure(String normalizedIdentifier, String normalizedIp) {
        Instant now = Instant.now();
        record(ACCOUNT, hash(normalizedIdentifier), properties.login().maxFailuresPerAccount(),
                properties.login().windowMinutes(), properties.login().lockMinutes(), now);
        record(IP, hash(normalizedIp), properties.login().maxFailuresPerIp(),
                properties.login().windowMinutes(), properties.login().lockMinutes(), now);
    }

    @Override
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void clearLoginFailures(String normalizedIdentifier) {
        repository.deleteByAttemptTypeAndSubjectHash(ACCOUNT, hash(normalizedIdentifier));
    }

    @Override
    @Transactional(readOnly = true)
    public void assertPasswordResetAllowed(String normalizedEmail, String normalizedIp) {
        Instant now = Instant.now();
        assertAttemptAllowed(RESET_EMAIL, hash(normalizedEmail), now);
        assertAttemptAllowed(RESET_IP, hash(normalizedIp), now);
    }

    @Override
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void recordPasswordResetRequest(String normalizedEmail, String normalizedIp) {
        Instant now = Instant.now();
        record(RESET_EMAIL, hash(normalizedEmail), properties.reset().maxRequestsPerEmail(),
                properties.reset().windowMinutes(), properties.reset().lockMinutes(), now);
        record(RESET_IP, hash(normalizedIp), properties.reset().maxRequestsPerIp(),
                properties.reset().windowMinutes(), properties.reset().lockMinutes(), now);
    }

    private void assertAttemptAllowed(String type, String subjectHash, Instant now) {
        AuthLoginAttemptJpaEntity attempt = repository.findByAttemptTypeAndSubjectHash(type, subjectHash).orElse(null);
        if (attempt == null || attempt.getLockedUntil() == null || !attempt.getLockedUntil().isAfter(now)) {
            return;
        }
        long seconds = Math.max(1, Duration.between(now, attempt.getLockedUntil()).toSeconds());
        throw new AuthRateLimitException(seconds);
    }

    private void record(String type, String subjectHash, int maxFailures, long windowMinutes, long lockMinutes, Instant now) {
        int safeMaxFailures = Math.max(1, maxFailures);
        Instant windowStart = now.minus(Duration.ofMinutes(Math.max(1, windowMinutes)));
        Instant lockUntil = now.plus(Duration.ofMinutes(Math.max(1, lockMinutes)));
        Instant initialLock = safeMaxFailures <= 1 ? lockUntil : null;
        repository.upsertFailure(UUID.randomUUID(), type, subjectHash, now, windowStart, initialLock, lockUntil, safeMaxFailures);
    }

    private String hash(String value) {
        String safeValue = value == null ? "" : value;
        try {
            return HexFormat.of().formatHex(MessageDigest.getInstance("SHA-256")
                    .digest(safeValue.getBytes(StandardCharsets.UTF_8)));
        } catch (Exception exception) {
            throw new IllegalStateException("Unable to hash authentication limiter key", exception);
        }
    }
}
