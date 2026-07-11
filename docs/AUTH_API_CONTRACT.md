# Auth API Contract

All failures use the existing `ApiResponse` envelope and now include `code` for application errors. Clients must branch on `code`, not localized `message` text.

| Method | Path | Client | Auth | Behavior |
| --- | --- | --- | --- | --- |
| POST | `/api/auth/login` | Native/desktop/shipper | Public | Legacy-compatible JSON login; returns access and refresh tokens. |
| POST | `/api/auth/google` | Native/desktop/shipper | Public | Existing Google ID-token exchange; returns native session JSON. |
| POST | `/api/auth/refresh` | Native/desktop/shipper | Public | Accepts `{ refreshToken }`, atomically rotates it, and returns the replacement token. |
| POST | `/api/auth/logout` | Native/desktop/shipper | Public | Accepts `{ refreshToken }` and revokes that session. |
| GET | `/api/auth/web/csrf` | Website | Public | Sets readable CSRF cookie. |
| POST | `/api/auth/web/login` | Website | Public + CSRF | Sets HttpOnly refresh cookie; returns only access token/session metadata. |
| POST | `/api/auth/web/google` | Website | Public + CSRF | Browser-safe Google login; no refresh token in JSON. |
| POST | `/api/auth/web/refresh` | Website | Cookie + CSRF | Rotates refresh cookie; returns new access token only. |
| POST | `/api/auth/web/logout` | Website | Cookie + CSRF | Revokes cookie session and clears cookie. |
| POST | `/api/auth/logout-all` | All | Bearer JWT | Revokes all sessions for current user. |
| GET | `/api/auth/sessions` | All | Bearer JWT | Lists current user’s active sessions; token values/hashes are never exposed. |
| DELETE | `/api/auth/sessions/{sessionId}` | All | Bearer JWT | Revokes a session owned by the caller only. |

Relevant error codes: `AUTH_INVALID_CREDENTIALS`, `AUTH_RATE_LIMITED`, `AUTH_SESSION_EXPIRED`, `AUTH_SESSION_REVOKED`, `AUTH_REFRESH_REUSE_DETECTED`, `AUTH_INVALID_PASSWORD_RESET_TOKEN`, `OTP_INVALID`, `OTP_EXPIRED`, `OTP_LOCKED`, and `AUTH_CSRF_INVALID`.

Backward compatibility: native `/login`, `/google`, `/refresh`, and `/logout` are unchanged. Only website calls the `/web/*` endpoints. Google sign-in remains the server-verified ID-token flow.
