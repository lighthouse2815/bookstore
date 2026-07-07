# FIX_REPORT

## 1. Danh sach file da sua

- `D:\bookstore\bookstore-mobile\app\src\main\java\com\bookstore\mobile\core\network\ApiService.kt`
- `D:\bookstore\bookstore-mobile\app\src\main\java\com\bookstore\mobile\feature\cart\data\CartRepository.kt`
- `D:\bookstore\bookstore-mobile\app\src\main\java\com\bookstore\mobile\feature\cart\viewmodel\CartViewModel.kt`
- `D:\bookstore\bookstore-mobile\app\src\main\java\com\bookstore\mobile\feature\cart\ui\CartScreen.kt`
- `D:\bookstore\bookstore-mobile\app\src\main\java\com\bookstore\mobile\feature\checkout\data\dto\CheckoutRequest.kt`
- `D:\bookstore\bookstore-mobile\app\src\main\java\com\bookstore\mobile\feature\checkout\data\CheckoutRepository.kt`
- `D:\bookstore\bookstore-mobile\app\src\main\java\com\bookstore\mobile\feature\checkout\viewmodel\CheckoutViewModel.kt`
- `D:\bookstore\bookstore-mobile\README.md`
- `D:\bookstore\bookstore-desktop\Bookstore.Desktop\Bookstore.Desktop.csproj`
- `D:\bookstore\bookstore-desktop\Bookstore.Desktop\run-publish.ps1`
- `D:\bookstore\bookstore-backend\pom.xml`
- `D:\bookstore\bookstore-backend\src\main\java\com\bookstore\bookstore\infrastructure\persistence\PersistenceDataInitializer.java`
- `D:\bookstore\bookstore-backend\src\main\java\com\bookstore\bookstore\infrastructure\persistence\DevelopmentDataSeeder.java`
- `D:\bookstore\bookstore-backend\src\main\resources\application.yml`
- `D:\bookstore\bookstore-backend\src\main\resources\application-dev.yml`
- `D:\bookstore\bookstore-backend\src\main\resources\application-prod.yml`
- `D:\bookstore\bookstore-backend\src\main\resources\application-seed.yml`
- `D:\bookstore\bookstore-backend\src\main\resources\db\migration\V1__init_schema.sql`
- `D:\bookstore\bookstore-backend\.env.example`
- `D:\bookstore\bookstore-backend\docs\PRODUCTION_DATABASE_SETUP.md`
- `D:\bookstore\bookstore-backend\src\test\java\com\bookstore\bookstore\application\assembler\CartAssemblerTest.java`
- `D:\bookstore\bookstore-backend\src\test\java\com\bookstore\bookstore\application\service\AuthServiceTest.java`
- `D:\bookstore\bookstore-backend\src\test\java\com\bookstore\bookstore\application\service\StockMovementServiceTest.java`
- `D:\bookstore\bookstore-backend\src\test\java\com\bookstore\bookstore\infrastructure\persistence\PersistenceDataInitializerTest.java`
- `D:\bookstore\bookstore-backend\src\test\resources\application.yml`
- `D:\bookstore\bookstore-website\.env.example`
- `D:\bookstore\bookstore-website\README.md`
- `D:\bookstore\bookstore-shipapp\.env.example`
- `D:\bookstore\bookstore-desktop\README.md`
- `D:\bookstore\docs\RUN_PROJECT.md`
- `D:\bookstore\docs\DEMO_GUIDE.md`
- `D:\bookstore\docs\RELEASE_CHECKLIST.md`
- `D:\bookstore\docs\SMOKE_TEST_FLOW.md`

## 2. Loi ban dau la gi

- Mobile cart dang goi `PUT/DELETE /api/cart/items/{bookId}` trong khi backend thao tac theo `itemId`.
- Mobile checkout request dang gui `couponCode`, khong khop DTO backend (`bookCouponCode`, `shippingCouponCode`).
- Desktop app target ca `net8.0-windows` va `net10.0-windows`, trong khi yeu cau muc tieu la on dinh voi .NET 8.
- Backend seed admin dang hard-code username/password/email/phone/name va ghi de password admin moi lan app start.
- Backend production profile dang `ddl-auto=validate` nhung repo chua co baseline migration Flyway cho empty database.
- `.env.example` chua khai bao day du `ADMIN_*` va `STORAGE_*`.
- `bookstore-website/README.md` dang la noi dung mau Next.js/v0, khong dung voi project hien tai.
- Backend test suite dang fail do mot so test cu khong con khop contract/behavior hien tai.

## 3. Da sua nhu the nao

- Mobile cart:
  - Doi path variable trong `ApiService` sang `itemId`.
  - Doi repository/viewmodel/screen su dung `item.id` thay vi `item.bookId` khi update quantity va remove.
- Mobile checkout:
  - Doi `CheckoutRequest` sang `bookCouponCode` + `shippingCouponCode`.
  - UI mobile van giu 1 o coupon, map gia tri do vao `bookCouponCode`, de `shippingCouponCode = null`.
- Desktop:
  - Doi `Bookstore.Desktop.csproj` sang 1 `TargetFramework` duy nhat: `net8.0-windows`.
  - Doi `run-publish.ps1` sang publish `net8.0-windows`.
- Backend admin seed:
  - Bo hard-code admin credentials trong `PersistenceDataInitializer`.
  - Doc `ADMIN_*` tu config/env.
  - Them `app.admin.*` vao `application.yml`.
  - Them default local-only cho `dev` trong `application-dev.yml`.
  - Chi seed khi `ADMIN_SEED_ENABLED=true` va chua co active ADMIN user.
  - Khong reset password admin neu admin da ton tai.
  - Fail fast neu seed duoc bat nhung thieu `ADMIN_*`.
  - Dong bo `DevelopmentDataSeeder` voi admin/env hien tai thay vi hard-code `giamdocdang` va password demo co dinh.
  - Them `APP_SEED_DEFAULT_PASSWORD` vao `application-seed.yml` + `.env.example` de bo customer/staff/shipper demo dung chung 1 cau hinh seed password.
- Production DB / Flyway:
  - Them `flyway-core` va `flyway-mysql` vao `pom.xml`.
  - Bat Flyway rieng cho `prod` trong `application-prod.yml`, giu `ddl-auto=validate`.
  - Giu Flyway tat o config goc de dev/test hien tai khong bi anh huong.
  - Sinh baseline schema MySQL tu JPA mapping hien tai va luu thanh `D:\bookstore\bookstore-backend\src\main\resources\db\migration\V1__init_schema.sql`.
  - Khoa `spring.flyway.enabled=false` trong `src/test/resources/application.yml` de test H2 khong co gang chay migration MySQL.
  - Cap nhat `D:\bookstore\bookstore-backend\docs\PRODUCTION_DATABASE_SETUP.md` de mo ta quy trinh deploy moi voi Flyway.
- Env/docs:
  - Bo sung `ADMIN_*` va `STORAGE_*` vao `.env.example`.
  - Them `D:\bookstore\docs\RUN_PROJECT.md` lam huong dan chay full monorepo.
  - Sua `bookstore-website/README.md` ve dung stack/lenh hien tai.
  - Cap nhat `bookstore-mobile/README.md` cho dung `itemId`, coupon mapping va LAN IP note.
  - Bo sung note/demo placeholders vao `bookstore-website\.env.example` va `bookstore-shipapp\.env.example`.
  - Viet lai `bookstore-desktop\README.md` cho dung .NET 8, backend URL, va demo account flow.
  - Them `D:\bookstore\docs\DEMO_GUIDE.md`, `D:\bookstore\docs\RELEASE_CHECKLIST.md`, `D:\bookstore\docs\SMOKE_TEST_FLOW.md`.
- Backend tests:
  - Dong bo test suite voi behavior hien tai cua `CartAssembler`, `AuthService`, `StockMovementService`, va `PersistenceDataInitializer`.

## 4. Lenh da chay de kiem tra

- `cd D:\bookstore\bookstore-website && npm run build`
- `cd D:\bookstore\bookstore-website && npm test`
- `cd D:\bookstore\bookstore-mobile && .\gradlew.bat assembleDebug`
- `cd D:\bookstore\bookstore-desktop\Bookstore.Desktop && dotnet build`
- `cd D:\bookstore\bookstore-backend && .\mvnw.cmd --% -Dtest=CartAssemblerTest,AuthServiceTest,StockMovementServiceTest,PersistenceDataInitializerTest test`
- `cd D:\bookstore\bookstore-backend && .\mvnw.cmd --% test`
- `cd D:\bookstore\bookstore-backend && .\mvnw.cmd --% -DskipTests compile`
- `cd D:\bookstore\bookstore-backend && .\mvnw.cmd --% -DskipTests package`
- `cd D:\bookstore\bookstore-backend && spring-boot:run` voi DB tam `bookstore_flyway_baseline`, `ddl-auto=create`, `ADMIN_SEED_ENABLED=false` de sinh schema MySQL tu entity hien tai
- `docker exec bookstore-mysql mysqldump ... bookstore_flyway_baseline` de dump schema baseline
- `cd D:\bookstore\bookstore-backend && spring-boot:run` voi profile `prod` tren DB sach `bookstore_flyway_smoke` de smoke test Flyway + Hibernate validate + admin seed
- `cd D:\bookstore\bookstore-backend && spring-boot:run` lan 2 voi cung DB `bookstore_flyway_smoke` nhung doi `ADMIN_PASSWORD` env de verify seed khong reset password admin khi app restart
- `cd D:\bookstore\bookstore-backend && java -jar target\bookstore-backend-0.0.1-SNAPSHOT.jar --spring.profiles.active=seed ...` tren DB sach `bookstore_release_seed_smoke` de smoke test seed dataset cho demo
- `cd D:\bookstore\bookstore-shipapp && npm ci`
- `cd D:\bookstore\bookstore-shipapp && npm run typecheck`

## 5. Ket qua pass/fail

- `bookstore-website`:
  - `npm run build`: PASS
  - `npm test`: PASS
- `bookstore-mobile`:
  - `.\gradlew.bat assembleDebug`: PASS
- `bookstore-desktop`:
  - `dotnet build`: PASS
- `bookstore-shipapp`:
  - `npm run typecheck`: PASS
- `bookstore-backend`:
  - `.\mvnw.cmd --% -Dtest=CartAssemblerTest,AuthServiceTest,StockMovementServiceTest,PersistenceDataInitializerTest test`: PASS
  - `.\mvnw.cmd --% test`: PASS
  - `.\mvnw.cmd --% -DskipTests compile`: PASS
  - `.\mvnw.cmd --% -DskipTests package`: PASS
  - Flyway baseline da them: PASS
  - Smoke `prod` voi DB sach MySQL 8 (`bookstore_flyway_smoke`): PASS
  - Verify restart tren cung DB voi `ADMIN_PASSWORD` env khac, `admin_count = 1`, `password_hash` khong doi: PASS
  - Smoke `seed` voi DB sach MySQL 8 (`bookstore_release_seed_smoke`): PASS

Ghi chu:

- Luan `.\mvnw.cmd --% test` dau tien fail vi test suite cu khong con khop behavior hien tai. Da sua test va rerun PASS.
- Smoke prod duoc thuc hien tren MySQL 8 local trong Docker, khong phai managed production database thuc te.
- Lan `npm run typecheck` dau tien cua `bookstore-shipapp` fail do workspace chua co `node_modules`; sau `npm ci` thi rerun PASS.

## 6. Viec con lai chua lam duoc neu co

- `bookstore-website` van dang ton tai ca `package-lock.json` va `pnpm-lock.yaml`. Trong tai lieu minh da chuan hoa cach chay bang `npm`, nhung chua xoa lockfile con lai de tranh mo rong scope.
- Chua smoke test duoc tren managed production database/hosting thuc te; cac smoke DB o turn nay deu duoc chay tren MySQL 8 local trong Docker.

## 7. Release readiness pass

- Docs release/demo da them:
  - `D:\bookstore\docs\DEMO_GUIDE.md`
  - `D:\bookstore\docs\RELEASE_CHECKLIST.md`
  - `D:\bookstore\docs\SMOKE_TEST_FLOW.md`
- Demo account va seed da duoc chuan hoa:
  - Admin doc tu `ADMIN_*`, khong hard-code, khong reset password neu account ADMIN da ton tai.
  - Seed profile dung `APP_SEED_DEFAULT_PASSWORD` cho customer/staff/shipper demo.
  - Sample dataset smoke PASS tren DB sach `bookstore_release_seed_smoke` voi it nhat admin, customer, shipper, staff, category, book, coupon, order, shipment.
  - Tai khoan mau xac nhan tu dataset seed: `admin_demo` (ADMIN), `minhanh.nguyen` (USER), `thanhtruc.do` (SHIPPER), `anhtuan.truong` (STAFF).
- Ket qua verify release:
  - Backend: `.\mvnw.cmd --% test`, `.\mvnw.cmd --% -DskipTests compile`, `.\mvnw.cmd --% -DskipTests package`, Flyway prod smoke, seed smoke: PASS.
  - Website: `npm run build`, `npm test`: PASS.
  - Mobile: `.\gradlew.bat assembleDebug`: PASS.
  - Desktop: `dotnet build`: PASS.
  - Ship app: `npm ci`, `npm run typecheck`: PASS.
- Ghi chu trung thuc:
  - Da gap mot vai lan thu `spring-boot:run` voi env override khong on dinh khi smoke seed; lan xac nhan cuoi cung duoc chay thanh cong bang `java -jar` tren artifact da build.
  - Chua co bang chung deploy smoke tren production database/managed service thuc te, nen release readiness hien tai moi duoc xac nhan o muc local build/test/smoke.
