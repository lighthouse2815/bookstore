package com.bookstore.bookstore.application.port.out;

import com.bookstore.bookstore.domain.model.RefreshToken;
import java.util.Optional;
import java.util.List;
import java.util.UUID;

public interface IRefreshTokenRepository {

    Optional<RefreshToken> findByTokenHash(String tokenHash);

    Optional<RefreshToken> findByTokenHashForUpdate(String tokenHash);

    List<RefreshToken> findActiveByUserId(UUID userId);

    Optional<RefreshToken> findByIdForUpdate(UUID id);

    Optional<RefreshToken> findById(UUID id);

    RefreshToken save(RefreshToken refreshToken);

    void revokeAllByUserId(UUID userId, java.time.Instant revokedAt,
                           com.bookstore.bookstore.domain.enums.RefreshTokenRevokeReason reason);

    void revokeFamily(UUID familyId, java.time.Instant revokedAt,
                      com.bookstore.bookstore.domain.enums.RefreshTokenRevokeReason reason);
}
