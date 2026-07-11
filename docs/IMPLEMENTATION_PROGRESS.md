# IMPLEMENTATION_PROGRESS

Updated: 2026-07-11

## P1 package: payment expiry, user cancellation, and late-IPN reconciliation

The checkout idempotency baseline remains intact: `POST /api/orders/checkout` requires a canonical UUID `Idempotency-Key`, replays the original order for the same key and payload, and returns `409 Conflict` for a payload mismatch. Checkout keeps the cart pessimistic lock.

### Database and configuration

- `V9__add_order_checkout_idempotency.sql` remains the latest baseline migration before this package.
- `V10__add_payment_expiry.sql` adds `payments.expires_at`, `payments.expired_at`, the `EXPIRED` payment status, and the `(status, expires_at)` pending-expiry index.
- `V11__create_payment_reconciliation_issues.sql` adds the reconciliation issue aggregate, a unique deduplication key, foreign keys, and status/payment/order indexes.
- `V12__add_expired_order_payment_status.sql` extends `orders.payment_status` with `EXPIRED`, so the order response remains consistent with an expired QR payment.
- `BANK_TRANSFER_EXPIRATION_MINUTES` is validated from 5 to 1440 minutes. `PAYMENT_EXPIRY_JOB_ENABLED`, `PAYMENT_EXPIRY_JOB_DELAY_MS`, and `PAYMENT_EXPIRY_JOB_BATCH_SIZE` are configurable and documented in `.env.example`.

### Backend behavior

- QR/SePay checkout creates a `PENDING` payment with a fixed `paymentExpiresAt`; COD has no expiry. Idempotency replay returns the original payment expiry and never extends it.
- `PaymentExpiryJob` reads only a bounded batch of expired pending IDs. Each ID runs in its own transaction through `PaymentExpiryProcessor`.
- Cancellation and expiry use the same `OrderCancellationService`: lock payment first, then order, then physical books in deterministic ID order, then coupons. The status is rechecked after every lock. Stock movement, coupon usage, timeline, audit, digital access revocation, and notification are handled once under the transaction. The order payment snapshot is changed to `CANCELLED` or `EXPIRED` in the same transaction.
- `PUT /api/orders/{orderId}/cancel` takes an owner-derived JWT identity and a required trimmed reason (maximum 500 characters). It hides non-owner ownership with `404`, rejects invalid state with `409`, and never cancels paid/shipping/delivered orders.
- SePay IPN validates configured authorization and duplicate notifications, locks payment then order, and never resurrects expired/cancelled orders. A late or invalid transfer keeps its external transaction reference on the terminal payment and creates one reconciliation issue instead of marking the order paid.
- Admin reconciliation endpoints are `GET /api/admin/payment-reconciliation`, `GET /api/admin/payment-reconciliation/{id}`, and `PUT /api/admin/payment-reconciliation/{id}/resolve`; they are ADMIN-only, paginated/filterable, require a resolution note, and do not refund automatically.

### Website and Android

- Website checkout carries `paymentExpiresAt` into the confirmation URL and shows a live `MM:SS` countdown while payment is pending.
- Website order detail shows a cancel action only for pending orders, opens a reason/confirmation dialog, disables duplicate submissions, refreshes the detail after success, and surfaces cancelled/expired payment states.
- Website admin has `/admin/payment-reconciliation` with status/type/date filters, issue detail drawer, order link, and resolve-with-note flow.
- Android exposes `PUT /api/orders/{id}/cancel`, shows the action only for pending orders, uses a confirmation dialog with a required reason, disables duplicate submissions, and reloads the order/timeline after success.
- Existing website and Android checkout idempotency keys remain stable across retries.

### Tests

- Unit/service coverage includes QR expiry, COD expiry omission, idempotency replay behavior, owner/other-user cancellation, paid-state protection, expiry transitions, stock rollback, late IPN reconciliation, duplicate issue protection, and resolution validation.
- MySQL concurrency coverage is in `PaymentExpiryMySqlConcurrencyIT.java` and uses Testcontainers, real transactions, `CountDownLatch`, `ExecutorService`, `SELECT ... FOR UPDATE`, unique deduplication, and post-thread reloads. It covers same-cart checkout, same-key replay, insufficient stock, coupon consumption, duplicate cancel, cancel/expiry, IPN/expiry, IPN/user-cancel, dual expiry workers, and duplicate late IPN.

## Verification performed

## P2 package: authentication and session security hardening

### Implemented

- `V13__harden_auth_sessions.sql` renames the legacy refresh hash column, backfills one family per existing token, adds rotation/device/session metadata and indexes, hardens reset-token metadata, extends OTP attempts, and creates the database-backed auth limiter table.
- Refresh rotation locks the user and refresh record, creates a child token in the same family, records `ROTATED`, detects reuse, revokes a compromised family, and carries a `sid` claim so API access is rejected after session revocation.
- Password-reset confirmation locks and consumes the hash-only reset record in the same transaction as password change and global session revocation. Reset requests are public-response-safe and database-rate-limited by hashed email/IP.
- OTP verification locks the pending record, persists failure counts, invalidates at `OTP_MAX_ATTEMPTS`, and records sanitized security audits.
- Local password login uses a shared public credential error and database-backed account/IP throttling. Forwarded IPs are accepted only from configured trusted proxy CIDRs.
- Website uses `/api/auth/web/*`, CSRF double-submit protection, an HttpOnly scoped refresh cookie, in-memory access token, and profile session management UI.
- Android moves legacy tokens into Keystore-backed encrypted preferences and disables backups. Desktop keeps credentials only in memory; shipper already uses SecureStore on native platforms.

### Auth-specific configuration and documents

- `docs/AUTH_SESSION_SECURITY.md` documents lifecycle, browser/native policy, proxy trust, audit redaction, and required environment.
- `docs/AUTH_API_CONTRACT.md` documents the web/native split and new session endpoints.
- `.env.example` documents cookie, throttle, reset, OTP, and trusted-proxy variables.

### Verification in this continuation

| Command | Result |
| --- | --- |
| `bookstore-backend\.mvnw.cmd test` | PASS — 358 tests, 0 failures/errors (2026-07-11) |
| `bookstore-backend\.mvnw.cmd -DskipTests compile` | PASS (2026-07-11) |
| `bookstore-backend\.mvnw.cmd -Ptestcontainers verify` | PASS — 17 MySQL concurrency tests, including 7 auth races (2026-07-11) |
| `bookstore-website\pnpm lint` | PASS (2026-07-11) |
| `bookstore-website\pnpm test` | PASS — 46 tests (2026-07-11) |
| `bookstore-website\pnpm build` | PASS (2026-07-11) |
| `bookstore-mobile\.\gradlew.bat testDebugUnitTest` | PASS (2026-07-11) |
| `bookstore-mobile\.\gradlew.bat assembleDebug` | PASS (2026-07-11) |
| `bookstore-mobile\.\gradlew.bat assembleRelease` | PASS (2026-07-11) |
| `bookstore-shipapp\npm test; npm run typecheck; npx expo export --platform android` | PASS — 6 tests (2026-07-11) |
| `bookstore-desktop\Bookstore.Desktop\dotnet build --configuration Release --no-restore` | PASS (2026-07-11) |
| `dotnet test --configuration Release` | BLOCKED locally: .NET 8 runtime missing; only .NET 10 runtime is installed. |

| Command | Result |
| --- | --- |
| `bookstore-backend\.mvnw.cmd test` | PASS — 358 tests, 0 failures/errors (2026-07-11) |
| `bookstore-backend\.mvnw.cmd -DskipTests compile` | PASS (2026-07-11) |
| `bookstore-backend\.mvnw.cmd -Ptestcontainers verify` | PASS — 10 MySQL concurrency tests, Docker Desktop 29.2.0 available (2026-07-11) |
| `bookstore-website\corepack enable; pnpm install --frozen-lockfile` | PASS (2026-07-11) |
| `bookstore-website\pnpm lint` | PASS (2026-07-11) |
| `bookstore-website\pnpm test` | PASS — 46 tests (2026-07-11) |
| `bookstore-website\pnpm build` | PASS (2026-07-11) |
| `bookstore-mobile\.\gradlew.bat testDebugUnitTest` | PASS (2026-07-11) |
| `bookstore-mobile\.\gradlew.bat assembleDebug` | PASS (2026-07-11) |

The dedicated `*MySqlConcurrencyIT` suite runs through Failsafe (`integration-test` + `verify`); the default `mvn test` does not execute it, so the tests are not duplicated. The Windows classpath remains stable and the full `-Ptestcontainers verify` command passed with Docker.

## P3 package: refund ledger, transactional outbox, and observability

- `V14__add_refund_ledger_and_transactional_outbox.sql` adds the financial refund ledger, transactional outbox, and delivery deduplication after V13.
- Refunds use a constrained state machine, payment-first locking, active-reservation and success-amount limits, create idempotency, mandatory success reference/evidence, audit/timeline records, and outbox notifications.
- Paid simple cancellation returns `409` with `ORDER_PAID_REFUND_REQUIRED`; it cannot trigger stock/coupon rollback. Return approval/restock is separate and no longer creates a false refund-approved event.
- The outbox now covers checkout/POS creation, cancellation, QR expiry, payment paid, late reconciliation, and refund state changes. Worker batching, reclaim, exponential retry, dead-letter, dedupe delivery, health and metrics are implemented.
- Website admin now exposes `/admin/refunds` and `/admin/outbox`; environment smoke is scripted in `bookstore-website/scripts/staging-smoke.mjs`.

## Verification in this continuation

| Command | Result |
| --- | --- |
| `bookstore-backend\.mvnw.cmd test` | PASS — 364 Surefire tests, 0 failures/errors/skipped (2026-07-11) |
| `bookstore-website\pnpm build` | PASS (2026-07-11) |
| `bookstore-backend\.mvnw.cmd clean -Ptestcontainers verify` | PASS — 364 Surefire tests + 26 Failsafe MySQL tests, 0 failures/errors/skipped (2026-07-11). This includes the V13-to-V14 Flyway upgrade on populated MySQL data. |
| `bookstore-backend\.mvnw.cmd -DskipTests compile` | PASS (2026-07-11). |
| JaCoCo unit-test baseline | PASS — 48.99% line coverage and 33.04% branch coverage; CI gate is line >= 45%, branch >= 30% (2026-07-11). |
| `bookstore-website\pnpm lint; pnpm test; pnpm build` | PASS — 46 website tests, lint and production build (2026-07-11). |
| `bookstore-mobile\.\gradlew.bat testDebugUnitTest; assembleDebug; assembleRelease` | PASS (2026-07-11). |
| `bookstore-shipapp\npm test; npm run typecheck; npx expo export --platform android` | PASS — 6 tests, typecheck and Android export (2026-07-11). |
| `bookstore-desktop\dotnet build / dotnet test` | PASS — Release build and 9 desktop tests after installing the user-local .NET 8 runtime (2026-07-11). |

## Remaining risks

- Latest local recheck: `bookstore-backend\.mvnw.cmd clean -Ptestcontainers verify` passed 364 Surefire tests and 26 Failsafe MySQL tests with no skips. The profile includes a Docker preflight so a future unavailable-Docker condition fails instead of being reported as a successful MySQL verification.
- No deployed staging URL, disposable staging identity, Google fixture, SePay callback fixture, or provider/admin credential is present locally. See `STAGING_SMOKE_REPORT.md`.
