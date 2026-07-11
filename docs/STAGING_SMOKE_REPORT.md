# Staging smoke report

Updated: 2026-07-11

## Environment result

No deployed staging URL or staging credentials are available in the checked workspace. No live login, payment, Google callback, or SePay callback was attempted, and no secret value was read or logged.

Docker/Testcontainers is now available: `clean -Ptestcontainers verify` passed 364 Surefire tests and 26 Failsafe MySQL tests with no skips, including the V13-to-V14 Flyway upgrade. No local database password is configured for this workspace, so V14 was not applied to the existing local database. The `testcontainers` Maven profile fails fast instead of reporting a green build when Docker is unavailable; applying V14 to staging remains a release-gate action with the staging database credentials.

## Automated smoke prepared

Run from `D:\bookstore\bookstore-website` after supplying only disposable staging-account environment variables:

```powershell
$env:SMOKE_API_BASE_URL = 'https://api-staging.example.com/api'
$env:SMOKE_WEB_ORIGIN = 'https://staging.example.com'
$env:SMOKE_USERNAME = 'staging-smoke-user'
$env:SMOKE_PASSWORD = 'set-at-runtime'
pnpm smoke:staging
```

The script checks health, CSRF rejection, browser login, HttpOnly/SameSite/Secure refresh-cookie flags, absence of `refreshToken` from web JSON, cookie refresh/session recovery, and the session-list token-redaction contract. Set `SMOKE_DESTRUCTIVE=true` only for a disposable account to exercise logout-all.

## Manual/fixture-dependent staging checks

- Browser/Playwright assertion that no access/refresh token exists in `localStorage`.
- Session revoke and logout-all with a disposable user.
- Password reset revocation and login throttle with isolated test identities.
- Google GIS with the real staging browser origin and client ID.
- Checkout double-click, QR countdown/expiry, user cancellation, late IPN, and admin reconciliation with a SePay staging fixture.
- Admin refund lifecycle with manual bank reference/evidence and a paid disposable order.
- Android refresh against the staging API and WebSocket connect/reconnect.

## Required configuration review

Production profile startup now fails fast unless CORS origins are explicit/non-wildcard, secure browser cookies are enabled, SameSite is valid, Google client ID is present, and at least one SePay IPN secret is configured. Trusted forwarded IPs are accepted only when enabled with explicit CIDRs. Flyway is enabled with JPA validation in `prod`.
