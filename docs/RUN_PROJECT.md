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
- Node.js 20+
- npm
- Android Studio + Android SDK
- .NET 8 SDK on Windows

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
- Swagger UI: `http://localhost:8080/swagger-ui/index.html`
- Dev admin seed is controlled by `ADMIN_*` variables. If `ADMIN_SEED_ENABLED=true` and no active ADMIN user exists, the app creates one admin account.
- In `dev`, `application-dev.yml` provides local-only fallback values for `ADMIN_*` if you do not define them.
- The `seed` profile expects a fresh schema and uses `APP_SEED_DEFAULT_PASSWORD` for customer/staff/shipper demo accounts.
- Storage-related uploads/presigned URLs require the `STORAGE_*` variables from `.env.example`.

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

- The desktop app now targets `.NET 8` only
- Desktop default backend base URL is `http://localhost:8080`

## Production database note

Backend production profile still uses `spring.jpa.hibernate.ddl-auto=validate`, but empty production schema bootstrap is now handled by Flyway.
Read `D:\bookstore\bookstore-backend\docs\PRODUCTION_DATABASE_SETUP.md` before deploying with `SPRING_PROFILES_ACTIVE=prod`.
