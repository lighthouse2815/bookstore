# RELEASE_CHECKLIST

Dung checklist nay truoc buoi demo, nop bai, hoac deploy thu.

## Backend

- [ ] Da copy `bookstore-backend/.env.example` thanh `.env`
- [ ] `DB_HOST/DB_PORT/DB_NAME/DB_USER/DB_PASSWORD` dung
- [ ] MySQL dang chay
- [ ] Flyway migrate PASS tren profile `prod`
- [ ] `spring.jpa.hibernate.ddl-auto=validate` van giu o `prod`
- [ ] `http://localhost:8080/actuator/health` tra `{"status":"UP"}` tren runtime `prod`
- [ ] `spring.jpa.open-in-view=false` van startup va smoke PASS
- [ ] `APP_SWAGGER_ENABLED=false` cho prod that; chi bat `true` cho demo/cham bai khi can
- [ ] `/v3/api-docs` va `/swagger-ui/index.html` chi mo khi `APP_SWAGGER_ENABLED=true`
- [ ] `APP_ADMIN_SEED_ENABLED` dung voi muc dich deploy (`false` cho prod that, `true` neu bootstrap admin/demo)
- [ ] `ADMIN_USERNAME/ADMIN_PASSWORD/ADMIN_EMAIL` da cau hinh neu `APP_ADMIN_SEED_ENABLED=true`
- [ ] Password admin/demo chi duoc lay tu env runtime, khong ghi secret that vao docs/repo
- [ ] Admin seed khong reset password khi app restart
- [ ] `APP_DEMO_SEED_ENABLED=false` cho prod that, chi bat khi can demo seed
- [ ] `APP_DEMO_USER_PASSWORD` da set neu can chay `seed` profile hoac demo seed tren `prod`
- [ ] `.\mvnw.cmd --% test` PASS
- [ ] `.\mvnw.cmd --% -DskipTests compile` PASS
- [ ] Best-coupon + coupon apply smoke PASS tren DB smoke rieng, khong can insert coupon thu cong
- [ ] Demo accounts / flow docs da cap nhat trong `docs/DEMO_SCRIPT.md`

## Website

- [ ] Da copy `bookstore-website/.env.example` thanh `.env`
- [ ] `VITE_API_BASE_URL` dung
- [ ] `VITE_GOOGLE_CLIENT_ID` dung neu demo Google login
- [ ] Thong tin bank transfer trong env chi la demo placeholder, khong phai secret that
- [ ] `npm run lint` PASS
- [ ] `npm run build` PASS
- [ ] `npm test` PASS
- [ ] Login duoc
- [ ] Cart/checkout duoc

## Mobile

- [ ] Backend URL la IP LAN/backend that khi test tren may that
- [ ] Khong dung `localhost` tren dien thoai that
- [ ] Cart update/remove dung `itemId`
- [ ] Checkout gui `bookCouponCode/shippingCouponCode` dung
- [ ] `.\gradlew.bat assembleDebug` PASS

## Ship app

- [ ] Da copy `bookstore-shipapp/.env.example` thanh `.env`
- [ ] `EXPO_PUBLIC_API_BASE_URL` dung
- [ ] Login shipper duoc
- [ ] Shipper xem duoc shipment da duoc admin gan
- [ ] Khong dung `localhost` tren dien thoai that
- [ ] `npm run typecheck` PASS
- [ ] Ghi chu: repo hien chi co `typecheck`, khong co script `build` rieng

## Desktop

- [ ] Project target `.NET 8`
- [ ] `dotnet build` PASS
- [ ] Login/API call duoc
- [ ] POS tao don duoc
- [ ] Receipt export `.txt` duoc neu can demo

## Security / Basic release hygiene

- [ ] Khong commit password/token that vao repo
- [ ] Khong hard-code admin password moi
- [ ] Khong hard-code seed password moi trong code
- [ ] Khong dung secret demo cho prod
- [ ] Production khong tro vao H2/dev DB
- [ ] CORS cau hinh hop ly
- [ ] Storage/upload config da du neu demo file upload

## Deployment notes

- [ ] Production DB moi co the bootstrap schema bang Flyway
- [ ] Da doc `bookstore-backend/docs/PRODUCTION_DATABASE_SETUP.md`
- [ ] Da doc `docs/DEPLOY_RENDER_AIVEN.md` neu deploy Render/Aiven
- [ ] Demo deploy dung `APP_SWAGGER_ENABLED=true`, `APP_DEMO_SEED_ENABLED=true`, `APP_ADMIN_SEED_ENABLED=true`
- [ ] Real production dung `APP_SWAGGER_ENABLED=false`, `APP_DEMO_SEED_ENABLED=false`, `APP_ADMIN_SEED_ENABLED=false`
- [ ] Sau khi bootstrap admin tren production that, tat lai `APP_ADMIN_SEED_ENABLED`
- [ ] Da xac nhan environment prod dung truoc khi deploy that

## Evidence

- Ket qua verify va cac phan chua xac minh bang runtime that phai duoc ghi trung thuc trong `D:\bookstore\FIX_REPORT.md`
- Neu co chay `D:\bookstore\scripts\smoke-demo.ps1`, ghi ro PASS/FAIL va env da dung

## Latest verified runtime snapshot

- Date: `2026-07-08`
- MySQL prod smoke: `PASS`
- Docker prod smoke: `PASS`
- Seed idempotent: `PASS`
- API smoke: `PASS`
- Coupon checkout + cancel rollback on MySQL 8 smoke DB: `PASS`
- Demo coupon seed: `PASS` (`GET /api/cart/best-coupon` tra ve coupon seed hop le khi `APP_DEMO_SEED_ENABLED=true`)
- Hibernate pagination warning `HHH90003004`: `PASS` (page IDs truoc, fetch graph theo IDs sau; khong con apply pagination in memory)
- Coupon smoke command set: login demo user -> add cart item -> `GET /api/cart/best-coupon` -> apply coupon seed tra ve -> checkout -> xem order -> cancel order -> doi chieu `coupon.used_count` va `books.stock_quantity` tren smoke DB local
