# Bookstore Desktop POS

Desktop app ban hang tai quay cho nhan vien Bookstore.

## Cong nghe

- C# .NET 8
- WPF
- MVVM
- HttpClient goi REST API backend Spring Boot
- `CommunityToolkit.Mvvm`

Desktop app chi la client. Du lieu van nam tren backend/MySQL.

## Chay backend

```powershell
cd D:\bookstore\bookstore-backend
docker-compose up -d
.\mvnw.cmd --% spring-boot:run -Dspring-boot.run.profiles=dev
```

Backend mac dinh:

```txt
http://localhost:8080
```

## Chay desktop app

Can may Windows co .NET 8 SDK va WPF workload.

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

## Cau hinh backend base URL

Desktop mac dinh goi backend:

```txt
http://localhost:8080
```

Sau khi login, vao man hinh `Cai dat` de doi Backend Base URL. Nhap URL goc, khong can them `/api`.

## Tai khoan demo

- Admin demo: duoc tao boi backend khi `APP_ADMIN_SEED_ENABLED=true`. Username/password lay tu `ADMIN_USERNAME` va `ADMIN_PASSWORD`.
- Staff demo cho POS: neu chay backend voi profile `seed`, tai khoan staff dau tien la `anhtuan.truong`.
- Tat ca tai khoan customer/staff/shipper cua profile `seed` dung chung password tu `APP_DEMO_USER_PASSWORD`.

Khong commit password that vao repo. Dat gia tri demo trong `.env` local.

## Luong demo

1. Login bang tai khoan co role `ADMIN` hoac `STAFF`.
2. Vao `POS ban hang`.
3. Search sach theo keyword.
4. Them sach vao gio.
5. Tang/giam/xoa item, xem tong tien cap nhat ngay.
6. Chon phuong thuc thanh toan `CASH`, `BANK_TRANSFER` hoac `COD`.
7. Bam `Tao don / Thanh toan`.
8. Xem hoa don demo va bam `In hoa don demo` de xuat file `.txt`.
9. Vao `Tra cuu don` de tim theo order id hoac order code.
10. Vao `Ton kho` de search sach va xem ton kho tu API.

## API dang dung

- `POST /api/auth/login`
- `GET /api/users/me`
- `GET /api/books`
- `GET /api/books/search?keyword=...`
- `POST /api/staff/pos/orders`
- `GET /api/staff/pos/orders`
- `GET /api/staff/pos/orders/{id}`

## Gioi han demo

- Khong co offline sync.
- Khong co database local.
- Khong tich hop may in nhiet ESC/POS.
- Hoa don demo xuat `.txt` trong `%LOCALAPPDATA%\BookstorePOS\Receipts`.
- Chua co man hinh bao cao doanh thu rieng. Dung `Tra cuu don` va `Ton kho` de demo nghiep vu ho tro.
