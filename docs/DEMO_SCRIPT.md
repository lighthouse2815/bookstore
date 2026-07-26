# DEMO_SCRIPT

Tai lieu nay chot flow demo cuoi cung cho `D:\bookstore` tren moi truong demo on dinh.

## 1. Scope

- Khong them feature lon moi.
- Dung DB demo rieng.
- Dung backend profile `prod` + demo seed khi can demo/cham bai.
- Neu can verify rollback coupon/stock, chi lam tren DB smoke/demo rieng.

## 2. Demo env contract

Backend demo deploy nen dung:

```env
SPRING_PROFILES_ACTIVE=prod
APP_SWAGGER_ENABLED=true
APP_DEMO_SEED_ENABLED=true
APP_ADMIN_SEED_ENABLED=true
APP_DEMO_USER_PASSWORD=<set in env>
ADMIN_USERNAME=admin_demo
ADMIN_PASSWORD=<set in env>
ADMIN_EMAIL=admin_demo@example.com
ADMIN_PHONE=0900000001
ADMIN_LAST_NAME=Demo
ADMIN_FIRST_NAME=Admin
```

Docs deploy chi ghi placeholder. Password that chi lay tu env runtime.

## 3. Demo accounts

### Admin

- Username: gia tri `ADMIN_USERNAME`
- Password: gia tri `ADMIN_PASSWORD`
- Email: gia tri `ADMIN_EMAIL`
- Khong hard-code password admin that vao code/docs

### Seeded customer / staff / shipper

- Customer dau tien: `minhanh.nguyen`
- Staff dau tien: `anhtuan.truong`
- Shipper dau tien: `thanhtruc.do`
- Password chung cho customer/staff/shipper seed: gia tri `APP_DEMO_USER_PASSWORD`

## 4. Demo data contract

Fresh demo seed phai tao du:

- catalog co books, images, prices, stock, categories, authors, publishers, suppliers
- user demo co address mac dinh, cart, order history mau
- coupon public active `DOCHEM03` cho best-coupon flow tren fresh demo DB
- stock van du de checkout, dong thoi co mot nhom low-stock de dashboard khong rong
- dashboard co revenue/orders/top books/low stock
- timestamp demo seed bam theo thoi diem seed de card `today` / `this month` khong bi cu

## 5. End-to-end flow

### Flow A - Customer website

1. Login bang `minhanh.nguyen` + password tu `APP_DEMO_USER_PASSWORD`.
2. Mo trang home / books list, verify books co anh + gia.
3. Mo 1 book detail, verify page-detail render du.
4. Quay lai home, verify block recently viewed hien sach vua mo.
5. Toggle wishlist tren book detail hoac book card.
6. Add item vao cart.
7. Mo cart, verify selected cart row IDs va tong tien.
8. Verify best coupon suggestion (`GET /api/cart/best-coupon`) tra coupon seed hop le.
9. Apply coupon goi y.
10. Checkout COD.
11. Mo order detail / order history va giu lai `orderId` moi tao.

### Flow B - Admin website

1. Login bang `ADMIN_USERNAME` + `ADMIN_PASSWORD`.
2. Mo `/admin`. Neu vao `/admin/dashboard` thi phai redirect ve `/admin`.
3. Verify dashboard co summary, revenue, top books, low stock.
4. Mo danh sach orders, tim pending order vua tao o Flow A.
5. Chon 1 trong 2 nhanh:
   - Demo business flow: update `PENDING -> CONFIRMED`
   - Demo ky thuat rollback: update `PENDING -> CANCELLED` tren DB smoke/demo rieng
6. Neu da cancel order trong DB smoke/demo rieng, verify stock va coupon usage da rollback.

### Flow C - Optional shipper / staff

- Shipper app: login `thanhtruc.do` + `APP_DEMO_USER_PASSWORD`
- Desktop/POS: login `anhtuan.truong` + `APP_DEMO_USER_PASSWORD`
- Hai flow nay la optional, khong can chay neu buoi demo chi tap trung backend + website

## 6. Recommended technical proof

Chay nhanh truoc khi demo UI:

```powershell
cd D:\bookstore
.\scripts\smoke-demo.ps1
```

Script nay check:

- `/actuator/health`
- login user
- login admin
- books
- categories
- page detail
- active coupons
- cart + best coupon
- wishlist add/remove
- admin dashboard summary

## 7. Notes

- Neu demo tren shared environment, uu tien update order thanh `CONFIRMED` thay vi `CANCELLED`.
- Neu can demo rollback coupon/stock, dung dedicated smoke DB va ghi ro trong report.
- Tai lieu lien quan:
  - `D:\bookstore\docs\SMOKE_TEST_FLOW.md`
  - `D:\bookstore\docs\DEPLOY_RENDER_AIVEN.md`
  - `D:\bookstore\docs\RUN_PROJECT.md`
  - `D:\bookstore\bookstore-backend\docs\PRODUCTION_DATABASE_SETUP.md`
