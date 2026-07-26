# Authentication and Session Security

## Session lifecycle

- Refresh tokens are generated from 32 random bytes and stored only as SHA-256 hashes.
- Every refresh record belongs to a `family_id`. Rotation locks the user and token rows, revokes the old token with `ROTATED`, creates a child token in the same family, and adds the child ID as `replaced_by_token_id` in one transaction.
- Reuse of a rotated token revokes every active token in its family with `FAMILY_COMPROMISED`, records `REFRESH_TOKEN_REUSE_DETECTED`, and returns a generic auth failure.
- A password reset revokes all refresh sessions. A signed-in logout revokes only the submitted session; logout-all revokes all sessions.
- Access JWTs issued by the hardened flow contain `sid`. The JWT converter rejects a revoked/expired `sid`, so revoking a session takes effect before access-token expiry. Legacy JWTs without `sid` are accepted only until their normal expiry.

## Browser policy

- Website login, Google login, refresh and logout use `/api/auth/web/*`.
- The refresh token is an HttpOnly `BOOKSTORE_REFRESH` cookie scoped to `/api/auth/web`; it is never returned to browser JavaScript or persisted by the website.
- Access tokens are memory-only. A page reload calls web refresh once and concurrent 401 responses share one refresh promise.
- `BOOKSTORE_CSRF` is a readable double-submit cookie. Web auth writes require `X-CSRF-Token`, a matching cookie, and an explicit allowed `Origin`.
- `AUTH_WEB_COOKIE_SECURE=true` is required for HTTPS production. Use `Lax` unless a deliberately designed cross-site deployment requires another policy.

## Native policy

- Android migrates legacy DataStore values into `EncryptedSharedPreferences` backed by an Android Keystore `MasterKey`; it clears plaintext only after a successful encrypted commit. Android backups are disabled.
- Android and shipper clients persist the rotated refresh token atomically and use a single-flight refresh path. A rejected refresh clears local credentials.
- Desktop keeps tokens only in memory today, uses a `SemaphoreSlim` refresh gate, and calls logout before clearing the in-memory store. It has no persisted refresh token to migrate.
- Shipper uses Expo SecureStore on Android/iOS. Its web fallback is deliberately separate from native storage.

## Login, OTP, and reset controls

- Password login uses one public credential error for unknown account, wrong password, Google-only account, disabled, locked, and deleted account paths.
- Database-backed `auth_login_attempts` tracks hashed account and IP keys. This works across instances; `AUTH_RATE_LIMITED` responses include `Retry-After`.
- Password-reset requests are also rate-limited by hashed email and IP, with the same public response for known and unknown emails.
- OTP records contain attempt count, max attempts, last attempt, verified time, invalidated time, expiry, and purpose. Verification locks the pending OTP row; an incorrect code increments atomically and invalidates at the configured limit.

## Proxy/IP policy

`X-Forwarded-For` is ignored by default. Set `AUTH_TRUSTED_PROXY_ENABLED=true` and explicit `AUTH_TRUSTED_PROXY_CIDRS` only behind a controlled proxy. The resolver then walks the forwarded chain from the trusted edge and keeps the nearest untrusted normalized IPv4/IPv6 address.

## Security events

The audit log never receives passwords, OTPs, access tokens, refresh tokens, reset tokens, or Authorization headers. Auth events include `LOGIN_FAILED`, `LOGIN_THROTTLED`, `LOGIN_SUCCEEDED`, `REFRESH_TOKEN_ROTATED`, `REFRESH_TOKEN_REUSE_DETECTED`, `SESSION_REVOKED`, `ALL_SESSIONS_REVOKED`, `PASSWORD_RESET_REQUESTED`, and `PASSWORD_RESET_COMPLETED`.

Every HTTP response includes `X-Correlation-Id`; production logs include it as a structured field. Clients must not put credentials or tokens in this header.

## Required environment

See `bookstore-backend/.env.example` for all values. Production must set explicit `CORS_ALLOWED_ORIGINS`, `AUTH_WEB_COOKIE_SECURE=true`, a non-empty JWT secret, and proxy trust only when applicable.
