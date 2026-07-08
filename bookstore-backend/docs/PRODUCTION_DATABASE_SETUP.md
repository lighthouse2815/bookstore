# PRODUCTION_DATABASE_SETUP

## Current status

As of this revision:

- `src/main/resources/application-prod.yml` uses `spring.jpa.hibernate.ddl-auto=validate`
- `spring.jpa.open-in-view=false` is explicitly set and re-smoked successfully
- Production profile now enables Flyway migrations from `src/main/resources/db/migration`
- Baseline schema for a fresh MySQL database is provided by `src/main/resources/db/migration/V1__init_schema.sql`
- Wishlist persistence is added by `src/main/resources/db/migration/V2__create_wishlist_items.sql`
- `docs/migration/2026-06-25-file-assets-backfill.sql` remains a separate one-off backfill script, not the baseline schema bootstrap
- `APP_SWAGGER_ENABLED` controls Springdoc availability; real `prod` defaults to `false`
- Only `/actuator/health` is exposed from Actuator for deploy readiness
- Production startup now fails fast if critical env is missing, if `JWT_SECRET` is weak/placeholder, or if `CORS_ALLOWED_ORIGINS` contains `*`

That means a fresh production MySQL database can now be initialized by Flyway before Hibernate validation runs.

## First deploy to a new production database

1. Prepare an empty MySQL database.
2. Set the required production environment variables:
   - `DB_HOST`
   - `DB_PORT`
   - `DB_NAME`
   - `DB_USER`
   - `DB_PASSWORD`
   - `JWT_SECRET`
   - `CORS_ALLOWED_ORIGINS`
   - `APP_ADMIN_SEED_ENABLED=true` only if you want the backend to create the initial admin account
   - `ADMIN_USERNAME`, `ADMIN_PASSWORD`, `ADMIN_EMAIL`, `ADMIN_PHONE`, `ADMIN_LAST_NAME`, `ADMIN_FIRST_NAME` when `APP_ADMIN_SEED_ENABLED=true`
   - `APP_DEMO_SEED_ENABLED=true` only for demo deploys that need seeded catalog/order data
   - `APP_DEMO_USER_PASSWORD` when `APP_DEMO_SEED_ENABLED=true`
   - `GOOGLE_CLIENT_ID` if Google login is enabled
   - `RESEND_*` if OTP/password-reset emails are enabled
   - `SEPAY_*` if SePay webhook/payment flow is enabled
   - `STORAGE_*` if file storage/presigned URLs are enabled
3. Use a strong production-only admin password. Do not reuse any demo/dev password.
4. Start the app with:

```powershell
cd D:\bookstore\bookstore-backend
.\mvnw.cmd --% spring-boot:run -Dspring-boot.run.profiles=prod
```

5. On startup, Flyway will create `flyway_schema_history`, apply every migration in order (`V1__init_schema.sql`, `V2__create_wishlist_items.sql`, ...), then Hibernate will validate the resulting schema.
6. If `APP_ADMIN_SEED_ENABLED=true` and there is still no active `ADMIN` user, the code seed will create the first admin account after migration.

## Recommended env sets

### Real production

```env
SPRING_PROFILES_ACTIVE=prod
DB_HOST=<aiven-or-mysql-host>
DB_PORT=3306
DB_NAME=bookstore_db
DB_USER=<db-user>
DB_PASSWORD=<db-password>
JWT_SECRET=<32-plus-char-secret>
CORS_ALLOWED_ORIGINS=https://your-website.example.com
APP_SWAGGER_ENABLED=false
APP_ADMIN_SEED_ENABLED=false
APP_DEMO_SEED_ENABLED=false
```

### Demo deploy on `prod`

```env
SPRING_PROFILES_ACTIVE=prod
DB_HOST=<aiven-or-mysql-host>
DB_PORT=3306
DB_NAME=bookstore_db
DB_USER=<db-user>
DB_PASSWORD=<db-password>
JWT_SECRET=<32-plus-char-secret>
CORS_ALLOWED_ORIGINS=https://your-demo-frontend.example.com
APP_SWAGGER_ENABLED=true
APP_ADMIN_SEED_ENABLED=true
ADMIN_USERNAME=admin_demo
ADMIN_PASSWORD=<set in env>
ADMIN_PHONE=0900000001
ADMIN_EMAIL=admin_demo@example.com
ADMIN_LAST_NAME=Demo
ADMIN_FIRST_NAME=Admin
APP_DEMO_SEED_ENABLED=true
APP_DEMO_USER_PASSWORD=<set in env>
```

### Render + Aiven example

Render service env:

```env
SPRING_PROFILES_ACTIVE=prod
DB_HOST=<aiven-host>
DB_PORT=3306
DB_NAME=defaultdb
DB_USER=<aiven-username>
DB_PASSWORD=<aiven-password>
JWT_SECRET=<32-plus-char-secret>
CORS_ALLOWED_ORIGINS=https://your-render-frontend.onrender.com
APP_SWAGGER_ENABLED=false
APP_ADMIN_SEED_ENABLED=false
APP_DEMO_SEED_ENABLED=false
```

Aiven note:

- Use the Aiven MySQL host/user/password/database values directly in the `DB_*` variables above.
- Keep demo seed off for the real database unless you intentionally want a demo environment.
- For a full Render + Aiven checklist, read `D:\bookstore\docs\DEPLOY_RENDER_AIVEN.md`.

## Demo account contract

- Admin username/email come from `ADMIN_*`.
- Admin password must come from `ADMIN_PASSWORD` at runtime.
- Fresh demo seed creates:
  - customer `minhanh.nguyen`
  - staff `anhtuan.truong`
  - shipper `thanhtruc.do`
- Shared seeded non-admin password must come from `APP_DEMO_USER_PASSWORD`.
- Do not write the real password values into docs or tracked files.

## Readiness proof

Prefer `/actuator/health` as the deploy readiness endpoint instead of Swagger/OpenAPI URLs.

PowerShell check:

```powershell
Invoke-RestMethod http://localhost:8080/actuator/health
```

Expected result:

- `status = UP`

Swagger / OpenAPI behavior:

- Demo deploy: set `APP_SWAGGER_ENABLED=true` if reviewers need `swagger-ui`
- Real production: keep `APP_SWAGGER_ENABLED=false`; `/v3/api-docs` and `/swagger-ui/index.html` should not be exposed

## How to verify Flyway ran

- Check the application log for Flyway migrate messages and a successful app startup.
- Query the Flyway history table:

```sql
SELECT installed_rank, version, description, success
FROM flyway_schema_history
ORDER BY installed_rank;
```

Expected first result:

- `version = 1`
- `description = init schema`
- `success = 1`

## Notes

- `spring.jpa.hibernate.ddl-auto=validate` stays enabled in `prod`; Flyway is responsible for creating schema before validation.
- For an already populated database that does not yet have `flyway_schema_history`, do not point this config at it blindly. Review and adopt a Flyway baseline plan first.
- One-off data/backfill scripts under `docs/migration/` are still separate from the baseline schema migration.
- `APP_ADMIN_SEED_ENABLED` and `APP_DEMO_SEED_ENABLED` default to `false` in `prod`; they must be enabled explicitly for demo/bootstrap scenarios.
- After the first real-production admin is created, turn `APP_ADMIN_SEED_ENABLED` back to `false`.
- Keep `APP_DEMO_SEED_ENABLED=false` on real production after demo/bootstrap work is finished.

## Verified smoke results

Latest runtime verification on `2026-07-08`:

- MySQL prod smoke: `PASS`
- Docker prod smoke: `PASS`
- Seed idempotent: `PASS`
- API smoke: `PASS`
- Coupon checkout smoke: `PASS`
- Demo coupon seed active smoke: `PASS`
- Hibernate pagination warning `HHH90003004`: `PASS`

Coupon smoke note:

- With `APP_DEMO_SEED_ENABLED=true`, demo seed now refreshes a canonical public `DOCHEMxx` coupon with relative validity based on seed runtime, so best-coupon smoke no longer depends on manual coupon inserts.
- Demo seed order/user timestamps are now anchored relative to seed runtime so dashboard `today` / `this month` cards stay useful on a fresh demo DB.
- Recommended smoke command flow on the dedicated local smoke schema:
  - `GET /actuator/health`
  - login demo user
  - add book to cart
  - `GET /api/cart/best-coupon`
  - apply the returned seeded coupon
  - checkout with that coupon
  - inspect order detail
  - cancel order on the smoke DB only if rollback verification is needed
- Verified results on the smoke schema:
  - discount amount matched order totals
  - `coupon.used_count` incremented on checkout and rolled back once on cancel
  - stock decreased on checkout and returned on cancel
