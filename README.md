# Kho mã nguồn Bookstore

Hướng dẫn chạy các dự án trong `D:\bookstore` bằng terminal.

## Cấu trúc thư mục

- `bookstore-backend`: backend Spring Boot và MySQL
- `bookstore-website`: website React và Vite
- `bookstore-mobile`: ứng dụng Android
- `bookstore-desktop`: ứng dụng desktop WPF
- `bookstore-shipapp`: ứng dụng giao hàng riêng, không thuộc phạm vi của README này

## Yêu cầu môi trường

- Docker Desktop
- Java 21
- Node.js 22.13 trở lên
- pnpm 11.11.0 thông qua Corepack
- Android Studio và Android SDK
- .NET 10 SDK trên Windows

## 1. Chạy MySQL bằng Docker

Mở terminal tại thư mục backend:

```powershell
cd D:\bookstore\bookstore-backend
docker-compose up -d
```

Kiểm tra container:

```powershell
docker ps
```

Dừng MySQL:

```powershell
cd D:\bookstore\bookstore-backend
docker-compose down
```

## 2. Chạy backend

Từ thư mục `bookstore-backend`:

```powershell
cd D:\bookstore\bookstore-backend
if (-not (Test-Path .env)) { Copy-Item .env.example .env }
docker compose up -d
.\mvnw.cmd spring-boot:run
```

Nếu muốn biên dịch nhanh và bỏ qua kiểm thử:

```powershell
cd D:\bookstore\bookstore-backend
.\mvnw.cmd --% -q -DskipTests compile
```

Địa chỉ backend mặc định:

```txt
http://localhost:8080
```

Swagger UI:

```txt
http://localhost:8080/swagger-ui/index.html
```

Lưu ý:

- Trước khi chạy backend, hãy sao chép `.env.example` thành `.env` trong `D:\bookstore\bookstore-backend` và thay các giá trị mẫu cần thiết.
- `.env.example` đặt `SPRING_PROFILES_ACTIVE=dev`, vì vậy lệnh `.\mvnw.cmd spring-boot:run` sẽ tự nạp đúng cấu hình phát triển, bao gồm tài khoản MySQL.
- Backend sử dụng MySQL trong Docker, vì vậy thường phải chạy `docker compose up -d` trước.
- Môi trường production phải ghi đè thành `SPRING_PROFILES_ACTIVE=prod` và cung cấp `JWT_SECRET`, `DB_USER`, `DB_PASSWORD` qua biến môi trường.

## 3. Chạy website

Từ thư mục `bookstore-website`:

```powershell
cd D:\bookstore\bookstore-website
corepack enable
pnpm install --frozen-lockfile
pnpm dev
```

Biên dịch bản production:

```powershell
cd D:\bookstore\bookstore-website
pnpm build
```

Địa chỉ website mặc định:

```txt
http://localhost:5173
```

Lưu ý:

- Trước khi chạy website, hãy bảo đảm đã sao chép `.env.example` thành `.env` trong `D:\bookstore\bookstore-website`.
- Website sử dụng pnpm 11.11.0 theo trường `packageManager` và file `pnpm-lock.yaml`. Không chạy `npm install` để tránh làm lệch lockfile.
- Biến `VITE_API_BASE_URL` nên trỏ đến backend, thường là `http://localhost:8080/api`.
- Backend cần cho phép CORS với `http://localhost:5173`.
- Website dùng refresh cookie HttpOnly để xác thực. Không lưu refresh token hoặc access token trong `localStorage`; xem [AUTH_SESSION_SECURITY.md](docs/AUTH_SESSION_SECURITY.md) và [AUTH_API_CONTRACT.md](docs/AUTH_API_CONTRACT.md).

## 4. Chạy ứng dụng Android

### Cách 1: Chạy bằng Android Studio

```powershell
cd D:\bookstore\bookstore-mobile
```

Sau đó mở thư mục này bằng Android Studio, đợi đồng bộ Gradle, chọn trình giả lập hoặc thiết bị thật rồi bấm Run.

### Cách 2: Biên dịch bằng terminal

```powershell
cd D:\bookstore\bookstore-mobile
.\gradlew.bat assembleDebug
```

Cài APK vào thiết bị hoặc trình giả lập:

```powershell
cd D:\bookstore\bookstore-mobile
.\gradlew.bat installDebug
```

APK debug nằm tại:

```txt
D:\bookstore\bookstore-mobile\app\build\outputs\apk\debug\app-debug.apk
```

Lưu ý:

- Lệnh `installDebug` cần có trình giả lập đang chạy hoặc điện thoại đã kết nối.
- Nếu dùng trình giả lập Android để gọi backend cục bộ, thường dùng địa chỉ `10.0.2.2:8080`.
- Nếu dùng điện thoại thật, base URL phải là địa chỉ IP LAN của máy chạy backend, ví dụ `http://192.168.1.10:8080`.

## 5. Chạy ứng dụng desktop

Ứng dụng desktop nằm trong dự án:

```txt
D:\bookstore\bookstore-desktop\Bookstore.Desktop
```

Chạy bằng terminal:

```powershell
cd D:\bookstore\bookstore-desktop\Bookstore.Desktop
dotnet restore
dotnet build
dotnet run
```

Lưu ý:

- Cần Windows và .NET 10 SDK.
- Ứng dụng desktop gọi backend qua `http://localhost:8080` theo mặc định.
- Nếu cần đổi URL backend, hãy thay đổi trong màn hình cài đặt của ứng dụng sau khi đăng nhập.

## Thứ tự chạy để demo toàn hệ thống

1. Chạy MySQL bằng Docker.
2. Chạy backend.
3. Chạy website hoặc ứng dụng desktop.
4. Nếu cần ứng dụng mobile, hãy chạy trình giả lập trước rồi biên dịch hoặc cài đặt ứng dụng.

## Lệnh nhanh

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
