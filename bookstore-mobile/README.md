# Bookstore Mobile

Android native demo app cho khach hang mua sach, goi truc tiep Spring Boot backend hien co.

## Cong nghe

- Kotlin
- Android native
- Jetpack Compose
- Material 3
- MVVM + StateFlow
- Kotlin Coroutines
- Navigation Compose
- Retrofit + OkHttp
- Kotlinx Serialization
- DataStore Preferences
- Coil
- Gradle Kotlin DSL

## Chay backend

Tu thu muc `bookstore-backend/`:

```powershell
docker-compose up -d
mvn spring-boot:run
```

Backend mac dinh chay tai:

```txt
http://localhost:8080
```

Lenh kiem tra build backend:

```powershell
mvn -q -DskipTests compile
```

## Mo va chay mobile

1. Mo thu muc `bookstore-mobile/` bang Android Studio.
2. Doi sync Gradle xong.
3. Chon emulator Android.
4. Run app `app`.

Hoac build debug APK bang PowerShell:

```powershell
cd D:\bookstore\bookstore-mobile
.\gradlew.bat assembleDebug
```

APK debug nam o:

```txt
app/build/outputs/apk/debug/app-debug.apk
```

## API base URL

Mac dinh ban build hien tai dung may that:

```txt
http://192.168.x.x:8080
```

Ghi chu:

- Android emulator dung `10.0.2.2` de goi `localhost` cua may host.
- Neu doi Wi-Fi/IP LAN, vao link `Cai dat API cho may that` tren man Login hoac vao `Profile > Settings` sau khi dang nhap, doi base URL thanh IP LAN cua may chay backend, vi du:

```txt
http://192.168.x.x:8080
```

## Luong demo

1. Login hoac Register.
2. Register se tao user `INACTIVE`, backend gui OTP email, sau do nhap OTP de kich hoat.
3. Xem Home va danh sach sach.
4. Xem chi tiet sach.
5. Them sach vao gio.
6. Cap nhat/xoa item trong gio.
7. Checkout bang dia chi giao hang.
8. Xem man hinh dat hang thanh cong.
9. Xem danh sach don hang va chi tiet don.
10. Xem/cap nhat profile co ban va logout.

## Tai khoan demo

Co 2 cach demo tai khoan customer tren mobile:

1. Dung seed dataset:
   - Chay backend voi profile `seed`.
   - Tai khoan customer dau tien: `minhanh.nguyen`
   - Password dung gia tri `APP_DEMO_USER_PASSWORD` trong `bookstore-backend/.env`
2. Tu tao tai khoan moi trong app:
   - Dang ky email moi trong app
   - Nhap OTP duoc backend gui qua email
   - Dang nhap bang email va mat khau vua tao

Neu database chua co du lieu sach, tao toi thieu:

- 1 category
- 1 author
- 1 publisher
- 5 sach co `stockQuantity > 0`
- 1 dia chi se duoc app tao luc checkout

## API dang dung

Auth:

- `POST /api/auth/login`
- `POST /api/auth/register`
- `POST /api/auth/refresh`
- `POST /api/auth/logout`
- `POST /api/otp/request`
- `POST /api/otp/verify`
- `GET /api/users/me`

Book/reference:

- `GET /api/books`
- `GET /api/books/search?keyword=...`
- `GET /api/books/{id}`
- `GET /api/books/{id}/page-detail`
- `GET /api/categories`
- `GET /api/authors`
- `GET /api/publishers`

Cart:

- `GET /api/cart`
- `POST /api/cart/items`
- `PUT /api/cart/items/{itemId}`
- `DELETE /api/cart/items/{itemId}`
- `DELETE /api/cart/items`

Checkout/address/order:

- `GET /api/user-addresses`
- `POST /api/user-addresses`
- `POST /api/orders/checkout`
- `GET /api/orders/my`
- `GET /api/orders/{id}`

Checkout note:

- Backend checkout request uses `bookCouponCode` and `shippingCouponCode`
- Mobile UI currently has one coupon box and maps that value to `bookCouponCode`

Profile:

- `GET /api/profiles/me`
- `PUT /api/profiles/me`
- `PUT /api/users/me`

## Gioi han demo

- Khong co offline mode/Room database.
- Khong tich hop payment gateway that.
- Checkout dung `BANK_TRANSFER_QR`, vi backend hien tai validate payment method nay trong `OrderService`.
- App tao dia chi giao hang toi thieu truoc khi checkout neu can.
- Khong co admin mobile.
- Khong co push notification, deep link, wishlist, chat support.
