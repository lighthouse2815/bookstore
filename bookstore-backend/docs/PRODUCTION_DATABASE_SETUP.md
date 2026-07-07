# PRODUCTION_DATABASE_SETUP

## Current status

As of this revision:

- `src/main/resources/application-prod.yml` still uses `spring.jpa.hibernate.ddl-auto=validate`
- Production profile now enables Flyway migrations from `src/main/resources/db/migration`
- Baseline schema for a fresh MySQL database is provided by `src/main/resources/db/migration/V1__init_schema.sql`
- `docs/migration/2026-06-25-file-assets-backfill.sql` remains a separate one-off backfill script, not the baseline schema bootstrap

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
   - `ADMIN_SEED_ENABLED=true` if you want the backend to create the initial admin account
   - `ADMIN_USERNAME`
   - `ADMIN_PASSWORD`
   - `ADMIN_EMAIL`
   - `ADMIN_PHONE` if you want a seeded phone number
   - `ADMIN_LAST_NAME`
   - `ADMIN_FIRST_NAME`
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

5. On startup, Flyway will create `flyway_schema_history`, apply `V1__init_schema.sql`, then Hibernate will validate the resulting schema.
6. If `ADMIN_SEED_ENABLED=true` and there is still no active `ADMIN` user, the code seed will create the first admin account after migration.

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
