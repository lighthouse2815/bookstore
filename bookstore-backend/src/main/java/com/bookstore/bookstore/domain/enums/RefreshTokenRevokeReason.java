package com.bookstore.bookstore.domain.enums;

public enum RefreshTokenRevokeReason {
    ROTATED,
    LOGOUT,
    LOGOUT_ALL,
    SESSION_REVOKED,
    PASSWORD_RESET,
    FAMILY_COMPROMISED,
    LEGACY_REVOKED
}
