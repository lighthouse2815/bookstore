package com.bookstore.bookstore.application.port.out;

import com.bookstore.bookstore.domain.model.PasswordResetToken;
import java.time.Instant;
import java.util.Optional;
import java.util.UUID;

public interface IPasswordResetTokenRepository {

    Optional<PasswordResetToken> findByTokenHash(String tokenHash);

    void markUnusedByUserIdAsUsed(UUID userId, Instant usedAt);

    PasswordResetToken save(PasswordResetToken passwordResetToken);
}
