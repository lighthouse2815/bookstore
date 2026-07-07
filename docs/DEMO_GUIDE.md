# DEMO_GUIDE

## 1. Tong quan project

Bookstore gom 5 thanh phan:

- `bookstore-backend`: Spring Boot + MySQL + Flyway baseline cho prod
- `bookstore-website`: React + Vite storefront/admin
- `bookstore-mobile`: Android app cho customer
- `bookstore-shipapp`: Expo app cho shipper
- `bookstore-desktop`: WPF POS app cho nhan vien tai quay

Vai tro chinh:

- `customer`: mua sach tren website/mobile, quan ly gio hang, checkout, xem don hang
- `admin`: dashboard, books, references, orders, shipments, promotions/coupons
- `shipper`: nhan don da duoc gan, cap nhat trang thai giao
- `staff/desktop POS`: tao don tai quay, tra cuu don, xem ton kho

## 2. Cach chay nhanh local

### Backend

```powershell
cd D:\bookstore\bookstore-backend
Copy-Item .env.example .env
docker-compose up -d
.\mvnw.cmd --% spring-boot:run -Dspring-boot.run.profiles=dev
```

### Website

```powershell
cd D:\bookstore\bookstore-website
Copy-Item .env.example .env
npm install
npm run dev
```

### Mobile Android

```powershell
cd D:\bookstore\bookstore-mobile
.\gradlew.bat assembleDebug
```

Mo project bang Android Studio de run emulator/device.

### Ship app Expo

```powershell
cd D:\bookstore\bookstore-shipapp
Copy-Item .env.example .env
npm install
npm start
```

### Desktop WPF

```powershell
cd D:\bookstore\bookstore-desktop\Bookstore.Desktop
dotnet restore
dotnet build
dotnet run
```

## 3. Tai khoan demo

### Admin

- Tai khoan admin khong hard-code trong code.
- Backend tao admin dau tien khi `ADMIN_SEED_ENABLED=true` va chua co active admin.
- Username/password/email lay tu:
  - `ADMIN_USERNAME`
  - `ADMIN_PASSWORD`
  - `ADMIN_EMAIL`

### Customer / Staff / Shipper

Neu can bo du lieu demo day du cho website/mobile/shipper/POS, dung backend `seed` profile tren DB rong:

```powershell
cd D:\bookstore\bookstore-backend
.\mvnw.cmd --% spring-boot:run -Dspring-boot.run.profiles=seed
```

Dieu kien:

- DB phai la schema moi
- `ADMIN_*` phai du
- `APP_SEED_DEFAULT_PASSWORD` phai du

Tai khoan demo co the dung ngay sau khi seed:

- Customer dau tien: `minhanh.nguyen`
- Shipper dau tien: `thanhtruc.do`
- Staff/POS dau tien: `anhtuan.truong`
- Password chung cua customer/staff/shipper seed: gia tri `APP_SEED_DEFAULT_PASSWORD`

Neu khong chay `seed` profile:

- Customer: tu tao bang luong register + OTP tren website/mobile
- Shipper: tao/gan trong du lieu dev hoac dung seed profile
- Staff/POS: tao bang admin flow hoac dung seed profile

### POS/Desktop user

- Desktop login duoc voi `ADMIN` hoac `STAFF`
- De demo POS an toan, uu tien tai khoan `STAFF` tu seed profile

## 4. Demo flow de xuat

### Flow A - Customer website/mobile

1. Dang ky hoac dang nhap
2. Neu dang ky moi: nhap OTP kich hoat tai khoan
3. Xem danh sach sach
4. Xem chi tiet sach
5. Them vao gio hang
6. Cap nhat so luong
7. Xoa item
8. Ap dung ma giam gia neu co
9. Checkout
10. Xem danh sach va chi tiet don hang

### Flow B - Admin

1. Dang nhap admin
2. Xem dashboard
3. Quan ly books
4. Quan ly category / author / publisher / supplier
5. Quan ly orders
6. Quan ly shipments va gan shipper
7. Quan ly promotions / coupons
8. Xem notifications / admin chat neu can demo them

### Flow C - Shipper

1. Admin gan shipment cho shipper
2. Dang nhap shipper app
3. Xem danh sach `GET /api/shipper/shipments/my`
4. Mo chi tiet shipment
5. Cap nhat trang thai: `PICKED_UP` -> `DELIVERING` -> `DELIVERED`
6. Neu that bai, cap nhat `FAILED`

### Flow D - Desktop / POS

1. Dang nhap bang `STAFF` hoac `ADMIN`
2. Tim sach
3. Them sach vao gio POS
4. Tao don tai quay
5. Xem hoa don preview
6. Xuat hoa don demo `.txt`
7. Tra cuu don
8. Xem ton kho
9. Bao cao rieng: Not implemented yet

## 5. Cac loi thuong gap

- Mobile/ship app tren may that khong duoc dung `localhost`; phai dung IP LAN
- Android emulator nen dung `http://10.0.2.2:8080`
- MySQL chua chay
- Thieu `ADMIN_*` env
- Thieu `APP_SEED_DEFAULT_PASSWORD` khi chay profile `seed`
- Thieu `STORAGE_*` env neu demo upload/presigned URL
- Sai `VITE_API_BASE_URL` hoac `EXPO_PUBLIC_API_BASE_URL`
- CORS chua cho phep origin local
- Cleartext HTTP chi dung cho dev local, khong dung cho production

## 6. Tai lieu lien quan

- `D:\bookstore\docs\RUN_PROJECT.md`
- `D:\bookstore\docs\RELEASE_CHECKLIST.md`
- `D:\bookstore\docs\SMOKE_TEST_FLOW.md`
- `D:\bookstore\bookstore-backend\docs\PRODUCTION_DATABASE_SETUP.md`
