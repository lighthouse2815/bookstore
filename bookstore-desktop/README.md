# Bookstore Desktop POS

Desktop app demo bán hàng tại quầy cho nhân viên Bookstore.

## Công nghệ

- C# .NET 8
- WPF
- MVVM
- `HttpClient` gọi REST API backend Spring Boot
- `System.Text.Json`
- `CommunityToolkit.Mvvm`

Desktop app chỉ là client, không có database riêng và không gọi trực tiếp MySQL.

## Cách chạy backend

```bash
cd ../bookstore-backend
docker-compose up -d
./mvnw.cmd spring-boot:run
```

Backend mặc định chạy tại:

```txt
http://localhost:8080
```

Backend đã có seed role `ADMIN`, `STAFF` và admin demo trong `PersistenceDataInitializer`. Không ghi password seed vào README; nếu cần tài khoản demo, kiểm tra file seed nội bộ hoặc tạo tài khoản staff/admin bằng API admin.

## Cách chạy desktop app

Cần máy Windows có .NET 8 SDK và workload WPF.

```bash
cd Bookstore.Desktop
dotnet restore
dotnet build
dotnet run
```

Nếu `dotnet` không được nhận diện, cài .NET 8 SDK hoặc thêm `dotnet.exe` vào PATH.

## Cấu hình Base URL

Mặc định desktop gọi backend:

```txt
http://localhost:8080
```

Sau khi login, vào màn hình `Cài đặt` để đổi Backend Base URL. Nhập URL gốc, không cần thêm `/api`.

## Luồng demo

1. Login bằng tài khoản có role `ADMIN` hoặc `STAFF`.
2. Vào `POS bán hàng`.
3. Search sách theo keyword.
4. Thêm sách vào giỏ.
5. Tăng/giảm/xóa item, xem tổng tiền cập nhật ngay.
6. Chọn phương thức thanh toán `CASH`, `BANK_TRANSFER` hoặc `COD`.
7. Bấm `Tạo đơn / Thanh toán`.
8. Xem hóa đơn demo và bấm `In hóa đơn demo` để xuất file `.txt`.
9. Vào `Tra cứu đơn` để tìm theo order id hoặc order code.
10. Vào `Tồn kho` để search sách và xem tồn kho từ API.

## API đang dùng

- `POST /api/auth/login`
- `GET /api/users/me`
- `GET /api/books`
- `GET /api/books/search?keyword=...`
- `POST /api/staff/pos/orders`
- `GET /api/staff/pos/orders`
- `GET /api/staff/pos/orders/{id}`

## Backend POS endpoint mới

Desktop dùng endpoint POS tối thiểu:

```http
POST /api/staff/pos/orders
```

Request:

```json
{
  "customerName": "Khách lẻ",
  "customerPhone": null,
  "paymentMethod": "CASH",
  "couponCode": null,
  "items": [
    {
      "bookId": "uuid",
      "quantity": 2
    }
  ]
}
```

Endpoint này dùng domain/backend hiện có để kiểm tra sách, trừ tồn kho, lưu order, payment và stock movement.

## Giới hạn demo

- Không có offline sync.
- Không có database local.
- Không tích hợp máy in nhiệt ESC/POS.
- Hóa đơn demo xuất `.txt` trong `%LOCALAPPDATA%\BookstorePOS\Receipts`.
- Search sách phụ thuộc API hiện có của backend.
- POS dùng khách lẻ mặc định, chưa có quản lý ca bán hàng.
- Checkout web cũ không bị đổi; POS dùng endpoint riêng.
