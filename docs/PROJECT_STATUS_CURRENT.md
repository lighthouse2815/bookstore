# PROJECT_STATUS_CURRENT

Last updated: `2026-07-10`

## 1. Current backend migration baseline

- Flyway migration baseline: `V1 -> V6`
- Latest migration present in repo: `V6__create_return_requests.sql`
- `prod` profile requirement:
  - `spring.flyway.enabled=true`
  - `spring.jpa.hibernate.ddl-auto=validate`
- Default `application.yml` still keeps Flyway disabled for non-prod by default, so deploy/demo runtime must set `SPRING_PROFILES_ACTIVE=prod` explicitly.

## 2. Feature areas currently present

- Audit Log
- Order Timeline
- Review Moderation
- Export CSV (orders, revenue, low stock, reviews)
- Return/Refund MVP
- Digital library / digital assets
- Notification + chat surfaces

## 3. Important env notes

### Backend demo/prod-like smoke

Required runtime shape:

```env
SPRING_PROFILES_ACTIVE=prod
DB_HOST=<mysql-host>
DB_PORT=3306
DB_NAME=<db-name>
DB_USER=<db-user>
DB_PASSWORD=<db-password>
JWT_SECRET=<32-plus-char-secret>
CORS_ALLOWED_ORIGINS=http://localhost:5173
APP_SWAGGER_ENABLED=true
APP_ADMIN_SEED_ENABLED=true
ADMIN_USERNAME=<set in env>
ADMIN_PASSWORD=<set in env>
ADMIN_EMAIL=<set in env>
ADMIN_PHONE=<set in env>
ADMIN_FIRST_NAME=<set in env>
ADMIN_LAST_NAME=<set in env>
APP_DEMO_SEED_ENABLED=true
APP_DEMO_USER_PASSWORD=<set in env>
```

### Real production

Required runtime shape:

```env
SPRING_PROFILES_ACTIVE=prod
APP_SWAGGER_ENABLED=false
APP_ADMIN_SEED_ENABLED=false
APP_DEMO_SEED_ENABLED=false
```

Notes:

- Do not rely on `ddl-auto=update` for prod/demo deploy bootstrap.
- Seed/admin passwords must come from runtime env only.
- `VITE_API_BASE_URL` must point to the intended backend, for example:
  - local default: `http://localhost:8080/api`
  - smoke against prod-like backend on port `8081`: `http://localhost:8081/api`

## 4. Latest verified smoke status

Verification date: `2026-07-09`

### Backend

- `D:\bookstore\bookstore-backend`
  - `.\mvnw.cmd --% test`: `PASS` (`317` tests)
  - `.\mvnw.cmd --% -DskipTests compile`: `PASS`
- Prod-like smoke DB: `bookstore_bugfix_smoke`
- Backend runtime used for smoke: `http://localhost:8081`
- `GET /actuator/health`: `UP`
- Flyway empty-schema migrate: `PASS`
- Hibernate validate on `prod`: `PASS`

Schema/runtime proof checked on smoke DB:

- `wishlist_items`: present
- `audit_logs`: present
- `order_timeline_events`: present
- review moderation columns: present
- `return_requests`: present

### Website

- `D:\bookstore\bookstore-website`
  - `npm run lint`: `PASS`
  - `npm run build`: `PASS`
- Route smoke with real backend on local FE origin:
  - `/`: `PASS`
  - `/books`: `PASS`
  - `/cart`: `PASS`
  - `/orders`: `PASS`
  - `/orders/:id`: `PASS`
  - `/admin`: `PASS`
  - `/admin/audit-logs`: `PASS`
  - `/admin/reviews`: `PASS`
  - `/admin/return-requests`: `PASS`
  - `/admin/orders`: `PASS`
- `/book-match`: route not present in current source, skipped

## 5. Stabilization changes verified in this pass

- Return/refund duplicate guard hardened by locking the order row first, then checking active return requests, then creating the new request.
- Pessimistic lock queries were narrowed so they no longer fetch collection graphs while locking:
  - `OrderJpaRepository` / `OrderRepositoryAdapter`
  - `BookJpaRepository` / `BookRepositoryAdapter`
- Real smoke log no longer showed Hibernate follow-on locking warning `HHH000444`.
- Login/register auth shell no longer mounts both Google buttons at once.
- `GoogleAuthButton` was hardened to avoid repeated GIS initialization in normal runtime usage.

## 6. Feature smoke snapshot

- Audit Log: `PASS`
  - API returned audit entries
  - admin audit-log UI rendered
  - smoke actions `RETURN_APPROVED`, `REVIEW_HIDDEN`, `REVIEW_APPROVED`, `REPORT_EXPORTED` were present
- Order Timeline: `PASS`
  - DB persisted timeline events
  - user order-detail UI rendered timeline and return/refund section
- Review Moderation: `PASS`
  - hide removed review from public list
  - approve restored public visibility and summary counts
  - admin review UI rendered
- Export CSV: `PASS`
  - all 4 endpoints returned `200 text/csv`
  - BOM/header verified
  - admin dashboard rendered 4 CSV export actions
- Return/Refund: `PASS`
  - delivered order request created
  - duplicate active request blocked
  - foreign-order request blocked
  - admin approve succeeded
  - stock/timeline/audit/notification evidence present in DB/API

## 7. Known limitations / backlog

- Checkout browser click-flow was not re-smoked end-to-end in the browser during this pass.
  - Backend checkout creation itself passed on the prod-like smoke DB.
- Browser log buffer retained one older Google Identity warning entry during dev-route verification.
  - Source-side hardening was applied in FE, but a clean browser-profile recheck is still useful.
- Audit-log redaction for secret payloads was not re-verified in this pass.
- WebSocket/chat was not re-smoked against a real deploy in this pass.
- File asset cleanup / TODO backlog was not audited in this pass.
- UI route smoke used temporary password resets on the throwaway smoke DB only, so protected-page verification could run end-to-end without changing repo config.

## 8. Working stability call

Current project status after this pass:

- Backend prod-like smoke: `STABLE`
- Website build + main route smoke: `STABLE`
- Remaining work: mostly backlog / recheck items, not active release blockers for the verified scope above

## 9. Roadmap C CI

- `.github/workflows/ci.yml` runs for pull requests and pushes to `main`.
- A change-filter job runs the matching application job only when its app directory or a workflow file changes. Existing backend and website CI remain in the same workflow.
- Mobile (`bookstore-mobile`): Java `21`, Gradle cache, `./gradlew testDebugUnitTest` (`8` tests) and `./gradlew assembleDebug`; publishes the debug APK artifact.
- Desktop (`bookstore-desktop/Bookstore.Desktop`): Windows runner with .NET SDK/runtime `8.0.x`, `dotnet restore`, `dotnet test --configuration Release` (`9` tests), `dotnet build --configuration Release --no-restore`, and a `win-x64` publish artifact. No `DOTNET_ROLL_FORWARD=Major` is used.
- Shipapp (`bookstore-shipapp`): Node `20`, npm cache, `npm ci`, `npm run test` (`6` tests), `npm run typecheck`, and `npx expo export --platform android`; publishes the Android Expo export artifact. CI supplies only `EXPO_PUBLIC_API_BASE_URL=http://10.0.2.2:8080/api`.
- Runtime/device smoke for mobile, desktop, and shipapp remains: `PENDING`.
