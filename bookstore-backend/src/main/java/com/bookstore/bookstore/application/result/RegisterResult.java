package com.bookstore.bookstore.application.result;

import java.time.Instant;


public record RegisterResult(
        String username,
        Instant createdAt
) {}
