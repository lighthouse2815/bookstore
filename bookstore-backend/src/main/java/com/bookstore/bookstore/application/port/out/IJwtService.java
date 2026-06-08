package com.bookstore.bookstore.application.port.out;

import com.bookstore.bookstore.domain.model.User;
import java.time.Instant;

public interface IJwtService {

    String generateAccessToken(User user);

    Instant calculateRefreshTokenExpiresAt(Instant issuedAt);
}
