package com.bookstore.bookstore.application.port.out;

import com.bookstore.bookstore.domain.model.User;
import java.time.Instant;
import java.util.UUID;

public interface IJwtService {

    String generateAccessToken(User user);

    String generateAccessToken(User user, UUID sessionId);

    Instant calculateRefreshTokenExpiresAt(Instant issuedAt);
}
