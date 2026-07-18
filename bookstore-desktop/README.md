# Bookstore Desktop POS

Ứng dụng desktop bán hàng tại quầy dành cho nhân viên Bookstore.

## Công nghệ

- C# .NET 8
- WPF
- MVVM
- HttpClient gọi REST API của backend Spring Boot
- `CommunityToolkit.Mvvm`

Ứng dụng desktop chỉ là client. Dữ liệu vẫn được lưu trên backend và MySQL.

## Chạy backend

```powershell
cd D:\bookstore\bookstore-backend
docker-compose up -d
.\mvnw.cmd --% spring-boot:run -Dspring-boot.run.profiles=dev
```

Địa chỉ backend mặc định:

```txt
http://localhost:8080
```

## Chạy ứng dụng desktop

Cần máy Windows có .NET 8 SDK và workload WPF.

```powershell
cd D:\bookstore\bookstore-desktop\Bookstore.Desktop
dotnet restore
dotnet build
dotnet run
```

Chạy script hỗ trợ publish:

```powershell
cd D:\bookstore\bookstore-desktop\Bookstore.Desktop
.\run-publish.ps1
```

## Cấu hình base URL của backend

Ứng dụng desktop mặc định gọi backend tại:

```txt
http://localhost:8080
```

Sau khi đăng nhập, vào màn hình `Cài đặt` để đổi Backend Base URL. Chỉ nhập URL gốc, không cần thêm `/api`.

## Tài khoản demo

- Tài khoản quản trị demo được backend tạo khi `APP_ADMIN_SEED_ENABLED=true`. Tên đăng nhập và mật khẩu lấy từ `ADMIN_USERNAME` và `ADMIN_PASSWORD`.
- Tài khoản nhân viên demo cho POS: nếu chạy backend với profile `seed`, tài khoản nhân viên đầu tiên là `anhtuan.truong`.
- Tất cả tài khoản khách hàng, nhân viên và người giao hàng của profile `seed` dùng chung mật khẩu từ `APP_DEMO_USER_PASSWORD`.

Không commit mật khẩu thật vào repository. Hãy đặt giá trị demo trong file `.env` cục bộ.

## Luồng demo

1. Đăng nhập bằng tài khoản có vai trò `ADMIN` hoặc `STAFF`.
2. Vào màn hình `POS bán hàng`.
3. Tìm sách theo từ khóa.
4. Thêm sách vào giỏ hàng.
5. Tăng, giảm hoặc xóa sản phẩm và xem tổng tiền cập nhật ngay.
6. Chọn phương thức thanh toán `CASH`, `BANK_TRANSFER` hoặc `COD`.
7. Bấm `Tạo đơn / Thanh toán`.
8. Xem hóa đơn demo và bấm `In hóa đơn demo` để xuất file `.txt`.
9. Vào `Tra cứu đơn` để tìm theo ID đơn hàng hoặc mã đơn hàng.
10. Vào `Tồn kho` để tìm sách và xem số lượng tồn kho từ API.

## Các API đang sử dụng

- `POST /api/auth/login`
- `GET /api/users/me`
- `GET /api/books`
- `GET /api/books/search?keyword=...`
- `POST /api/staff/pos/orders`
- `GET /api/staff/pos/orders`
- `GET /api/staff/pos/orders/{id}`

## Giới hạn của bản demo

- Không có đồng bộ ngoại tuyến.
- Không có cơ sở dữ liệu cục bộ.
- Không tích hợp máy in nhiệt ESC/POS.
- Hóa đơn demo được xuất dưới dạng `.txt` trong `%LOCALAPPDATA%\BookstorePOS\Receipts`.
- Chưa có màn hình báo cáo doanh thu riêng. Dùng `Tra cứu đơn` và `Tồn kho` để demo nghiệp vụ hỗ trợ.
