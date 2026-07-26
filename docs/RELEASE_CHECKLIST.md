# RELEASE_CHECKLIST

Dung checklist nay truoc buoi demo, nop bai, hoac deploy thu.

## Backend

- [ ] Da copy `bookstore-backend/.env.example` thanh `.env`
- [ ] `DB_HOST/DB_PORT/DB_NAME/DB_USER/DB_PASSWORD` dung
- [ ] MySQL dang chay
- [ ] Flyway migrate PASS tren profile `prod`
- [ ] Flyway da ap dung lien tuc den `V14__add_refund_ledger_and_transactional_outbox.sql`; khong sua hoac bo qua migration cu
- [ ] `spring.jpa.hibernate.ddl-auto=validate` van giu o `prod`
- [ ] `http://localhost:8080/actuator/health` tra `{"status":"UP"}` tren runtime `prod`
- [ ] `spring.jpa.open-in-view=false` van startup va smoke PASS
- [ ] `APP_SWAGGER_ENABLED=false` cho prod that; chi bat `true` cho demo/cham bai khi can
- [ ] `/v3/api-docs` va `/swagger-ui/index.html` chi mo khi `APP_SWAGGER_ENABLED=true`
- [ ] `APP_ADMIN_SEED_ENABLED` dung voi muc dich deploy (`false` cho prod that, `true` neu bootstrap admin/demo)
- [ ] `ADMIN_USERNAME/ADMIN_PASSWORD/ADMIN_EMAIL` da cau hinh neu `APP_ADMIN_SEED_ENABLED=true`
- [ ] Password admin/demo chi duoc lay tu env runtime, khong ghi secret that vao docs/repo
- [ ] Admin seed khong reset password khi app restart
- [ ] `APP_DEMO_SEED_ENABLED=false` cho prod that, chi bat khi can demo seed
- [ ] `APP_DEMO_USER_PASSWORD` da set neu can chay `seed` profile hoac demo seed tren `prod`
- [ ] `.\mvnw.cmd --% test` PASS
- [ ] `.\mvnw.cmd --% -DskipTests compile` PASS
- [ ] `mvnw.cmd clean -Ptestcontainers verify` PASS on a machine with Docker Desktop running, with `0` MySQL tests skipped (the profile now fails fast when Docker is unavailable)
- [ ] JaCoCo bundle coverage gate PASS: line >= 45%, branch >= 30%; report at `bookstore-backend/target/site/jacoco/index.html`
- [ ] QR expiry/cancel/late-IPN smoke follows `docs/SMOKE_TEST_FLOW.md`
- [ ] API contract and terminal-state behavior checked against `docs/ORDER_PAYMENT_API_CONTRACT.md` and `docs/ORDER_PAYMENT_STATE_TRANSITIONS.md`
- [ ] Best-coupon + coupon apply smoke PASS tren DB smoke rieng, khong can insert coupon thu cong
- [ ] Demo accounts / flow docs da cap nhat trong `docs/DEMO_SCRIPT.md`

## Website

- [ ] Da copy `bookstore-website/.env.example` thanh `.env`
- [ ] `VITE_API_BASE_URL` dung
- [ ] `VITE_GOOGLE_CLIENT_ID` dung neu demo Google login
- [ ] Thong tin bank transfer trong env chi la demo placeholder, khong phai secret that
- [ ] `corepack enable; pnpm install --frozen-lockfile` PASS
- [ ] `pnpm lint` PASS
- [ ] `pnpm build` PASS
- [ ] `pnpm test` PASS
- [ ] `pnpm smoke:staging` PASS voi tai khoan staging disposable; ket qua ghi trong `docs/STAGING_SMOKE_REPORT.md`
- [ ] Login duoc
- [ ] Cart/checkout duoc

## Mobile

- [ ] Backend URL la IP LAN/backend that khi test tren may that
- [ ] Khong dung `localhost` tren dien thoai that
- [ ] Cart update/remove dung `itemId`
- [ ] Checkout gui `bookCouponCode/shippingCouponCode` dung
- [ ] `.\gradlew.bat assembleDebug` PASS
- [ ] `.\gradlew.bat testDebugUnitTest` va `.\gradlew.bat assembleRelease` PASS

## Ship app

- [ ] Da copy `bookstore-shipapp/.env.example` thanh `.env`
- [ ] `EXPO_PUBLIC_API_BASE_URL` dung
- [ ] Login shipper duoc
- [ ] Shipper xem duoc shipment da duoc admin gan
- [ ] Khong dung `localhost` tren dien thoai that
- [ ] `npm run typecheck` PASS
- [ ] `npm test` va `npx expo export --platform android` PASS
- [ ] Ghi chu: repo hien chi co Expo export, khong co script `build` rieng

## Desktop

- [ ] Project target `.NET 8`
- [ ] `dotnet build` PASS
- [ ] .NET 8 runtime co san (system-wide hoac user-local voi `DOTNET_ROOT`/`DOTNET_ROOT_X64`) va `dotnet test` PASS
- [ ] Login/API call duoc
- [ ] POS tao don duoc
- [ ] Receipt export `.txt` duoc neu can demo

## Security / Basic release hygiene

- [ ] Khong commit password/token that vao repo
- [ ] Khong hard-code admin password moi
- [ ] Khong hard-code seed password moi trong code
- [ ] Khong dung secret demo cho prod
- [ ] Production khong tro vao H2/dev DB
- [ ] CORS cau hinh hop ly
- [ ] Storage/upload config da du neu demo file upload

## Deployment notes

- [ ] Production DB moi co the bootstrap schema bang Flyway
- [ ] Da doc `bookstore-backend/docs/PRODUCTION_DATABASE_SETUP.md`
- [ ] Da doc `docs/DEPLOY_RENDER_AIVEN.md` neu deploy Render/Aiven
- [ ] Demo deploy dung `APP_SWAGGER_ENABLED=true`, `APP_DEMO_SEED_ENABLED=true`, `APP_ADMIN_SEED_ENABLED=true`
- [ ] Real production dung `APP_SWAGGER_ENABLED=false`, `APP_DEMO_SEED_ENABLED=false`, `APP_ADMIN_SEED_ENABLED=false`
- [ ] Sau khi bootstrap admin tren production that, tat lai `APP_ADMIN_SEED_ENABLED`
- [ ] Da xac nhan environment prod dung truoc khi deploy that

## Evidence

- Ket qua verify va cac phan chua xac minh bang runtime that phai duoc ghi trung thuc trong `D:\bookstore\FIX_REPORT.md`
- Neu co chay `D:\bookstore\scripts\smoke-demo.ps1`, ghi ro PASS/FAIL va env da dung

## Latest verified runtime snapshot

- Date: `2026-07-09`
- MySQL prod-like smoke from empty DB: `PASS`
- Flyway `V1 -> V6` on `prod`: `PASS`
- Hibernate validate on `prod`: `PASS`
- Backend tests / compile: `PASS`
- Audit Log / Order Timeline / Review Moderation / Export CSV / Return-Refund smoke: `PASS`
- Return/refund duplicate guard hardening: `PASS`
- Hibernate follow-on locking warning `HHH000444`: `PASS`
- Website lint/build + main route smoke: `PASS`
- Detailed snapshot and known backlog: `D:\bookstore\docs\PROJECT_STATUS_CURRENT.md`
## Authentication/session release additions

- [ ] Production has explicit `CORS_ALLOWED_ORIGINS`; it never contains `*` when credentials are enabled.
- [ ] `AUTH_WEB_COOKIE_SECURE=true`, HTTPS is enforced, and `AUTH_WEB_COOKIE_SAME_SITE` matches the deployment topology.
- [ ] `AUTH_TRUSTED_PROXY_ENABLED` is false unless controlled proxy CIDRs are set and verified.
- [ ] Browser DevTools confirms no refresh token in Local/Session/IndexedDB and confirms the refresh cookie is HttpOnly/Secure/SameSite as configured.
- [ ] Password reset, OTP lockout, login throttle, Google login, logout-all and cross-device session revoke are smoke-tested on staging.

## Refund/outbox release additions

- [ ] Flyway is continuous through `V14__add_refund_ledger_and_transactional_outbox.sql`; existing migrations were not edited.
- [ ] `GOOGLE_CLIENT_ID` and at least one of `SEPAY_WEBHOOK_API_KEY` / `SEPAY_SECRET_KEY` are set without logging their values.
- [ ] `OUTBOX_ENABLED=true`; `/actuator/health` reports acceptable `outbox` pending/failed/dead counts.
- [ ] Admin refund flow is smoke-tested with a paid disposable order, manual bank reference, evidence, failure/retry, and partial amount protection.
- [ ] The team reviewed `REFUND_STATE_TRANSITIONS.md`, `TRANSACTIONAL_OUTBOX.md`, and `OPERATIONS_RUNBOOK.md`.
