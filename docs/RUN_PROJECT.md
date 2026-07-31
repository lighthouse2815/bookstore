# RUN_PROJECT

Tai lieu nay la entrypoint de chay cac module trong `D:\bookstore` theo trang thai code hien tai.

## Modules

- `bookstore-backend`: Spring Boot backend + MySQL
- `bookstore-website`: React 19 + Vite 8 storefront/admin frontend
- `bookstore-mobile`: Android app
- `bookstore-desktop`: WPF desktop app
- `bookstore-shipapp`: Expo shipper app

## Prerequisites

- Docker Desktop
- Java 21
- Node.js 22.13+
- npm
- Android Studio + Android SDK
- .NET 10 SDK on Windows

## 1. Backend

Copy env:

```powershell
cd D:\bookstore\bookstore-backend
Copy-Item .env.example .env
```

Start MySQL:

```powershell
cd D:\bookstore\bookstore-backend
docker-compose up -d
```

Run dev profile:

```powershell
cd D:\bookstore\bookstore-backend
.\mvnw.cmd --% spring-boot:run -Dspring-boot.run.profiles=dev
```

Useful verification commands:

```powershell
cd D:\bookstore\bookstore-backend
.\mvnw.cmd --% test
.\mvnw.cmd --% -DskipTests compile
```

Seed a fresh demo dataset on an empty local DB:

```powershell
cd D:\bookstore\bookstore-backend
.\mvnw.cmd --% spring-boot:run -Dspring-boot.run.profiles=seed
```

Notes:

- `.env` must live in `D:\bookstore\bookstore-backend`.
- Backend default URL: `http://localhost:8080`
- Readiness endpoint: `http://localhost:8080/actuator/health`
- Swagger UI chi co khi `APP_SWAGGER_ENABLED=true`: `http://localhost:8080/swagger-ui/index.html`
- Admin seed is controlled by `APP_ADMIN_SEED_ENABLED` + `ADMIN_*`. The safe default is `APP_ADMIN_SEED_ENABLED=false`.
- If `APP_ADMIN_SEED_ENABLED=true` and no active `ADMIN` user exists, the app creates one admin account.
- The `seed` profile expects a fresh schema and uses `APP_DEMO_USER_PASSWORD` for customer/staff/shipper demo accounts.
- For demo deploy on profile `prod`, set `APP_DEMO_SEED_ENABLED=true` and `APP_ADMIN_SEED_ENABLED=true` explicitly. Real production should keep both false unless you intentionally bootstrap the first admin.
- `spring.jpa.open-in-view=false` is now part of the runtime config and was re-smoked successfully on MySQL 8.
- Storage-related uploads/presigned URLs require the `STORAGE_*` variables from `.env.example`.

## 1.1 Backend prod smoke / deploy verification

Demo deploy env on `prod`:

```env
SPRING_PROFILES_ACTIVE=prod
DB_HOST=localhost
DB_PORT=3306
DB_NAME=bookstore_demo_prod
DB_USER=<db-user>
DB_PASSWORD=<db-password>
JWT_SECRET=<32-plus-char-secret>
CORS_ALLOWED_ORIGINS=http://localhost:5173
APP_SWAGGER_ENABLED=true
APP_ADMIN_SEED_ENABLED=true
ADMIN_USERNAME=admin_demo
ADMIN_PASSWORD=<set in env>
ADMIN_EMAIL=admin_demo@example.com
ADMIN_PHONE=0900000001
ADMIN_FIRST_NAME=Admin
ADMIN_LAST_NAME=Demo
APP_DEMO_SEED_ENABLED=true
APP_DEMO_USER_PASSWORD=<set in env>
```

Real production env:

```env
SPRING_PROFILES_ACTIVE=prod
DB_HOST=<mysql-or-aiven-host>
DB_PORT=3306
DB_NAME=bookstore_db
DB_USER=<db-user>
DB_PASSWORD=<db-password>
JWT_SECRET=<32-plus-char-secret>
CORS_ALLOWED_ORIGINS=https://your-frontend.example.com
APP_SWAGGER_ENABLED=false
APP_ADMIN_SEED_ENABLED=false
APP_DEMO_SEED_ENABLED=false
```

Prod runtime proof:

- `GET /actuator/health` must return `UP`
- `GET /v3/api-docs` and `GET /swagger-ui/index.html` should return `404` when `APP_SWAGGER_ENABLED=false`
- `GET /v3/api-docs` and `GET /swagger-ui/index.html` may stay enabled on demo deploys by setting `APP_SWAGGER_ENABLED=true`
- Final demo walkthrough: `D:\bookstore\docs\DEMO_SCRIPT.md`
- Demo smoke helper: `D:\bookstore\scripts\smoke-demo.ps1`
- Render/Aiven deploy note: `D:\bookstore\docs\DEPLOY_RENDER_AIVEN.md`

Current verified snapshot on MySQL 8:

- Date: `2026-07-09`
- MySQL prod-like smoke from empty DB: `PASS`
- Flyway `V1 -> V6` on `prod`: `PASS`
- Hibernate validate on `prod`: `PASS`
- Backend tests / compile: `PASS`
- Audit Log / Order Timeline / Review Moderation / Export CSV / Return-Refund smoke: `PASS`
- Return/refund duplicate guard hardening: `PASS`
- Hibernate follow-on locking warning `HHH000444`: `PASS` after narrowing lock queries and fetching graphs separately
- Website lint/build + main route smoke: `PASS`
- For the detailed latest snapshot, read `D:\bookstore\docs\PROJECT_STATUS_CURRENT.md`.

## 2. Website

This repo currently contains both `package-lock.json` and `pnpm-lock.yaml`, but the documented path for this checkout is `npm`.

Copy env:

```powershell
cd D:\bookstore\bookstore-website
Copy-Item .env.example .env
```

Install and run:

```powershell
cd D:\bookstore\bookstore-website
npm install
npm run dev
```

Build and test:

```powershell
cd D:\bookstore\bookstore-website
npm run build
npm test
```

Notes:

- Default website URL: `http://localhost:5173`
- `VITE_API_BASE_URL` should normally be `http://localhost:8080/api`
- `VITE_GOOGLE_CLIENT_ID` is required for Google sign-in

## 3. Mobile Android

Open `D:\bookstore\bookstore-mobile` in Android Studio, wait for Gradle sync, then run the `app` target.

CLI build:

```powershell
cd D:\bookstore\bookstore-mobile
.\gradlew.bat assembleDebug
```

Install to a running emulator/device:

```powershell
cd D:\bookstore\bookstore-mobile
.\gradlew.bat installDebug
```

Notes:

- Real phone: do not use `localhost`. Set the API base URL to the LAN IP of the machine running backend, for example `http://192.168.x.x:8080`
- Android emulator: use `http://10.0.2.2:8080`
- Cleartext HTTP is acceptable for local dev only. Release builds should use HTTPS
- Mobile checkout currently exposes one coupon input and maps it to backend field `bookCouponCode`. `shippingCouponCode` is sent as `null`

## 4. Ship App (Expo)

Copy env:

```powershell
cd D:\bookstore\bookstore-shipapp
Copy-Item .env.example .env
```

Install and start:

```powershell
cd D:\bookstore\bookstore-shipapp
npm install
npm start
```

Useful commands:

```powershell
cd D:\bookstore\bookstore-shipapp
npm run android
npm run web
npm run typecheck
```

Notes:

- `EXPO_PUBLIC_API_BASE_URL` must point to the real backend or LAN IP when testing on a real phone
- Do not use `localhost` on a real phone

## 5. Desktop

Project path:

```txt
D:\bookstore\bookstore-desktop\Bookstore.Desktop
```

Build and run:

```powershell
cd D:\bookstore\bookstore-desktop\Bookstore.Desktop
dotnet restore
dotnet build
dotnet run
```

Publish helper:

```powershell
cd D:\bookstore\bookstore-desktop\Bookstore.Desktop
.\run-publish.ps1
```

Notes:

- The desktop app targets `.NET 10` only
- Desktop default backend base URL is `http://localhost:8080`

## Production database note

Backend production profile still uses `spring.jpa.hibernate.ddl-auto=validate`, but empty production schema bootstrap is now handled by Flyway.
Read `D:\bookstore\bookstore-backend\docs\PRODUCTION_DATABASE_SETUP.md` before deploying with `SPRING_PROFILES_ACTIVE=prod`.

## Prod smoke cleanup

Do not run these against any real production database/container. These are only for the local smoke environment:

```powershell
docker stop bookstore-backend-prod-smoke bookstore-prod-smoke-mysql
docker rm bookstore-backend-prod-smoke bookstore-prod-smoke-mysql
docker network rm bookstore-prod-smoke-net
```

If you also want to remove the local smoke schema only:

```sql
DROP DATABASE bookstore_prod_polish;
```

Do not run schema/database deletion against Aiven or any shared MySQL instance.
