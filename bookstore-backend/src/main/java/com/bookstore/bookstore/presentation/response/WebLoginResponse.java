package com.bookstore.bookstore.presentation.response;

import com.bookstore.bookstore.domain.enums.UserStatus;
import java.util.Set;
import java.util.UUID;

/** Browser response intentionally excludes the refresh token. */
public record WebLoginResponse(
        UUID userId,
        UserStatus status,
        Set<String> roles,
        String accessToken
) {}
