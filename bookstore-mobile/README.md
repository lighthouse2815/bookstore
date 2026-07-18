# Bookstore Mobile

Ứng dụng Android thuần dùng để demo quy trình mua sách của khách hàng và gọi trực tiếp đến backend Spring Boot hiện có.

## Công nghệ

- Kotlin
- Android thuần
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

## Chạy backend

Từ thư mục `bookstore-backend/`:

```powershell
docker-compose up -d
.\mvnw.cmd --% spring-boot:run -Dspring-boot.run.profiles=dev
```

Backend mặc định chạy tại:

```txt
http://localhost:8080
```

Lệnh kiểm tra bản biên dịch backend:

```powershell
.\mvnw.cmd --% -q -DskipTests compile
```

## Mở và chạy ứng dụng mobile

1. Mở thư mục `bookstore-mobile/` bằng Android Studio.
2. Đợi đồng bộ Gradle hoàn tất.
3. Chọn trình giả lập Android hoặc thiết bị thật.
4. Chạy target `app`.

Hoặc biên dịch APK debug bằng PowerShell:

```powershell
cd D:\bookstore\bookstore-mobile
.\gradlew.bat assembleDebug
```

APK debug nằm tại:

```txt
app/build/outputs/apk/debug/app-debug.apk
```

## Base URL của API

Bản build hiện tại mặc định dùng địa chỉ dành cho thiết bị thật:

```txt
http://192.168.x.x:8080
```

Ghi chú:

- Trình giả lập Android dùng `10.0.2.2` để gọi `localhost` của máy host.
- Nếu đổi Wi-Fi hoặc địa chỉ IP LAN, hãy chọn `Cài đặt API cho máy thật` trên màn hình đăng nhập hoặc vào `Hồ sơ > Cài đặt` sau khi đăng nhập. Sau đó đổi base URL thành địa chỉ IP LAN của máy chạy backend, ví dụ:

```txt
http://192.168.x.x:8080
```

## Luồng demo

1. Đăng nhập hoặc đăng ký.
2. Khi đăng ký, backend tạo người dùng ở trạng thái `INACTIVE` và gửi OTP qua email. Nhập OTP để kích hoạt tài khoản.
3. Xem trang chủ và danh sách sách.
4. Xem chi tiết sách.
5. Thêm sách vào giỏ hàng.
6. Cập nhật hoặc xóa sản phẩm trong giỏ hàng.
7. Thanh toán bằng địa chỉ giao hàng.
8. Xem màn hình đặt hàng thành công.
9. Xem danh sách đơn hàng và chi tiết đơn hàng.
10. Xem hoặc cập nhật hồ sơ cơ bản, sau đó đăng xuất.

## Tài khoản demo

Có hai cách dùng tài khoản khách hàng để demo trên mobile:

1. Dùng bộ dữ liệu seed:
   - Chạy backend với profile `seed`.
   - Tài khoản khách hàng đầu tiên: `minhanh.nguyen`.
   - Mật khẩu dùng giá trị `APP_DEMO_USER_PASSWORD` trong `bookstore-backend/.env`.
2. Tự tạo tài khoản mới trong ứng dụng:
   - Đăng ký bằng email mới trong ứng dụng.
   - Nhập OTP được backend gửi qua email.
   - Đăng nhập bằng email và mật khẩu vừa tạo.

Nếu cơ sở dữ liệu chưa có dữ liệu sách, cần tạo tối thiểu:

- 1 danh mục
- 1 tác giả
- 1 nhà xuất bản
- 5 sách có `stockQuantity > 0`
- 1 địa chỉ; ứng dụng có thể tạo địa chỉ này trong lúc thanh toán

## Các API đang sử dụng

Xác thực:

- `POST /api/auth/login`
- `POST /api/auth/register`
- `POST /api/auth/refresh`
- `POST /api/auth/logout`
- `POST /api/otp/request`
- `POST /api/otp/verify`
- `GET /api/users/me`

Sách và dữ liệu tham chiếu:

- `GET /api/books`
- `GET /api/books/search?keyword=...`
- `GET /api/books/{id}`
- `GET /api/books/{id}/page-detail`
- `GET /api/categories`
- `GET /api/authors`
- `GET /api/publishers`

Giỏ hàng:

- `GET /api/cart`
- `POST /api/cart/items`
- `PUT /api/cart/items/{itemId}`
- `DELETE /api/cart/items/{itemId}`
- `DELETE /api/cart/items`

Thanh toán, địa chỉ và đơn hàng:

- `GET /api/user-addresses`
- `POST /api/user-addresses`
- `POST /api/orders/checkout`
- `GET /api/orders/my`
- `GET /api/orders/{id}`

Ghi chú thanh toán:

- Yêu cầu thanh toán của backend dùng hai trường `bookCouponCode` và `shippingCouponCode`.
- Giao diện mobile hiện có một ô mã giảm giá và ánh xạ giá trị đó vào `bookCouponCode`.

Hồ sơ:

- `GET /api/profiles/me`
- `PUT /api/profiles/me`
- `PUT /api/users/me`

## Giới hạn của bản demo

- Không có chế độ ngoại tuyến hoặc cơ sở dữ liệu Room.
- Không tích hợp cổng thanh toán thật.
- Luồng thanh toán dùng `BANK_TRANSFER_QR` vì backend hiện kiểm tra phương thức thanh toán này trong `OrderService`.
- Ứng dụng tạo địa chỉ giao hàng tối thiểu trước khi thanh toán nếu cần.
- Không có trang quản trị trên mobile.
- Không có thông báo đẩy, liên kết sâu, danh sách yêu thích hoặc chat hỗ trợ.
