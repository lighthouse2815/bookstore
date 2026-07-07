# RELEASE_CHECKLIST

Dung checklist nay truoc buoi demo, nop bai, hoac deploy thu.

## Backend

- [ ] Da copy `bookstore-backend/.env.example` thanh `.env`
- [ ] `DB_HOST/DB_PORT/DB_NAME/DB_USER/DB_PASSWORD` dung
- [ ] MySQL dang chay
- [ ] Flyway migrate PASS tren profile `prod`
- [ ] `spring.jpa.hibernate.ddl-auto=validate` van giu o `prod`
- [ ] `ADMIN_USERNAME/ADMIN_PASSWORD/ADMIN_EMAIL` da cau hinh
- [ ] Admin seed khong reset password khi app restart
- [ ] `APP_SEED_DEFAULT_PASSWORD` da set neu can chay `seed` profile
- [ ] `.\mvnw.cmd --% test` PASS
- [ ] `.\mvnw.cmd --% -DskipTests compile` PASS

## Website

- [ ] Da copy `bookstore-website/.env.example` thanh `.env`
- [ ] `VITE_API_BASE_URL` dung
- [ ] `VITE_GOOGLE_CLIENT_ID` dung neu demo Google login
- [ ] Thong tin bank transfer trong env chi la demo placeholder, khong phai secret that
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
- [ ] Da xac nhan environment prod dung truoc khi deploy that

## Evidence

- Ket qua verify va cac phan chua xac minh bang runtime that phai duoc ghi trung thuc trong `D:\bookstore\FIX_REPORT.md`
