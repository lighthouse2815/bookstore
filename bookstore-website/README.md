# Website Bookstore

Frontend cửa hàng và trang quản trị của dự án Bookstore, được xây dựng bằng React 19 và Vite 8.

## Công cụ và công nghệ

- Node.js 22.13 trở lên
- pnpm 11.11.0
- React 19
- TypeScript
- Vite 8

Repository hiện có cả `package-lock.json` và `pnpm-lock.yaml`, nhưng quy trình chuẩn sử dụng pnpm theo trường `packageManager` trong `package.json`.

## Cài đặt

```powershell
cd D:\bookstore\bookstore-website
Copy-Item .env.example .env
corepack enable
pnpm install --frozen-lockfile
```

## Chạy cục bộ

```powershell
cd D:\bookstore\bookstore-website
pnpm dev
```

Địa chỉ mặc định:

```txt
http://localhost:5173
```

## Kiểm tra

```powershell
cd D:\bookstore\bookstore-website
pnpm build
pnpm test
```

## Biến môi trường bắt buộc

- `VITE_API_BASE_URL=http://localhost:8080/api`
- `VITE_GOOGLE_CLIENT_ID=...` dùng cho chức năng đăng nhập bằng Google

## Tài liệu liên quan

- Hướng dẫn chạy toàn bộ repository: `D:\bookstore\docs\RUN_PROJECT.md`
- Hướng dẫn cơ sở dữ liệu production của backend: `D:\bookstore\bookstore-backend\docs\PRODUCTION_DATABASE_SETUP.md`
