package com.bookstore.bookstore.application.port.out;

import com.bookstore.bookstore.domain.model.RefreshToken;
import java.util.Optional;
import java.util.UUID;

public interface IRefreshTokenRepository {

    Optional<RefreshToken> findByTokenHash(String tokenHash);

    RefreshToken save(RefreshToken refreshToken);

    void revokeAllByUserId(UUID userId);
}
