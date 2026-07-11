# Bookstore Monorepo

Huong dan chay cac du an trong `D:\bookstore` bang terminal.

## Cau truc thu muc

- `bookstore-backend`: Spring Boot backend + MySQL
- `bookstore-website`: React + Vite website
- `bookstore-mobile`: Android app
- `bookstore-desktop`: WPF desktop app
- `bookstore-shipapp`: ship app rieng, khong nam trong scope README nay

## Yeu cau moi truong

- Docker Desktop
- Java 21
- Node.js 20+
- npm
- Android Studio + Android SDK
- .NET 8 SDK tren Windows

## 1. Chay Docker MySQL

Mo terminal tai thu muc backend:

```powershell
cd D:\bookstore\bookstore-backend
docker-compose up -d
```

Kiem tra container:

```powershell
docker ps
```

Dung MySQL:

```powershell
cd D:\bookstore\bookstore-backend
docker-compose down
```

## 2. Chay Backend

Tu thu muc `bookstore-backend`:

```powershell
cd D:\bookstore\bookstore-backend
.\mvnw.cmd --% spring-boot:run -Dspring-boot.run.profiles=dev
```

Neu muon compile nhanh bo qua test:

```powershell
cd D:\bookstore\bookstore-backend
.\mvnw.cmd --% -q -DskipTests compile
```

Backend mac dinh:

```txt
http://localhost:8080
```

Swagger UI:

```txt
http://localhost:8080/swagger-ui/index.html
```

Luu y:

- Truoc khi chay backend, dam bao da copy `.env.example` thanh `.env` trong `D:\bookstore\bookstore-backend`.
- Backend dung MySQL trong Docker, nen thuong phai chay `docker-compose up -d` truoc.
- Local development nen chay voi profile `dev`; production can set `SPRING_PROFILES_ACTIVE=prod` va phai cung cap `JWT_SECRET`, `DB_USER`, `DB_PASSWORD` tu environment.

## 3. Chay Website

Tu thu muc `bookstore-website`:

```powershell
cd D:\bookstore\bookstore-website
corepack enable
pnpm install --frozen-lockfile
pnpm dev
```

Build production:

```powershell
cd D:\bookstore\bookstore-website
pnpm build
```

Website mac dinh:

```txt
http://localhost:5173
```

Luu y:

- Truoc khi chay web, dam bao da copy `.env.example` thanh `.env` trong `D:\bookstore\bookstore-website`.
- Website dung pnpm 11.11.0 theo `packageManager` va `pnpm-lock.yaml`; khong chay `npm install` de tranh lam lech lockfile.
- Bien `VITE_API_BASE_URL` nen tro den backend, thuong la `http://localhost:8080/api`.
- Backend can cho phep CORS voi `http://localhost:5173`.
- Website authentication uses an HttpOnly refresh cookie. Do not add refresh or access tokens to `localStorage`; see [AUTH_SESSION_SECURITY.md](docs/AUTH_SESSION_SECURITY.md) and [AUTH_API_CONTRACT.md](docs/AUTH_API_CONTRACT.md).

## 4. Chay Mobile Android

### Cach 1: Chay bang Android Studio

```powershell
cd D:\bookstore\bookstore-mobile
```

Sau do mo thu muc nay bang Android Studio, doi sync Gradle, chon emulator hoac may that roi bam Run.

### Cach 2: Build bang terminal

```powershell
cd D:\bookstore\bookstore-mobile
.\gradlew.bat assembleDebug
```

Cai APK vao may/emulator:

```powershell
cd D:\bookstore\bookstore-mobile
.\gradlew.bat installDebug
```

APK debug nam o:

```txt
D:\bookstore\bookstore-mobile\app\build\outputs\apk\debug\app-debug.apk
```

Luu y:

- `installDebug` can co emulator dang chay hoac dien thoai da ket noi.
- Neu dung Android emulator de goi backend local, thuong dung host `10.0.2.2:8080`.
- Neu dung dien thoai that, base URL phai la IP LAN cua may chay backend, vi du `http://192.168.1.10:8080`.

## 5. Chay Desktop

Desktop app nam trong project:

```txt
D:\bookstore\bookstore-desktop\Bookstore.Desktop
```

Chay bang terminal:

```powershell
cd D:\bookstore\bookstore-desktop\Bookstore.Desktop
dotnet restore
dotnet build
dotnet run
```

Luu y:

- Can Windows va .NET 8 SDK.
- App desktop goi backend qua `http://localhost:8080` theo mac dinh.
- Neu can doi backend URL, doi trong man hinh cai dat cua app sau khi dang nhap.

## Thu tu chay de demo full he thong

1. Chay MySQL bang Docker.
2. Chay backend.
3. Chay website hoac desktop.
4. Neu can mobile, chay emulator truoc roi build/install app.

## Lenh nhanh

### Backend

```powershell
cd D:\bookstore\bookstore-backend
docker-compose up -d
.\mvnw.cmd --% spring-boot:run -Dspring-boot.run.profiles=dev
```

### Website

```powershell
cd D:\bookstore\bookstore-website
corepack enable
pnpm install --frozen-lockfile
pnpm dev
```

### Mobile

```powershell
cd D:\bookstore\bookstore-mobile
.\gradlew.bat assembleDebug
```

### Desktop

```powershell
cd D:\bookstore\bookstore-desktop\Bookstore.Desktop
dotnet run
```
