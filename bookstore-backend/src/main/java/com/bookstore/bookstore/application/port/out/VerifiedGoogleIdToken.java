package com.bookstore.bookstore.application.port.out;

public record VerifiedGoogleIdToken(
        String subject,
        String email,
        boolean emailVerified,
        String givenName,
        String familyName,
        String pictureUrl
) {
}
