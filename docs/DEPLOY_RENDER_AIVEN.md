# DEPLOY_RENDER_AIVEN

Tai lieu nay chot env va thu tu deploy cho backend Render + MySQL Aiven.

## 1. Architecture

- Aiven: host MySQL production/demo DB
- Render Web Service: host `bookstore-backend`
- Render Static Site hoac frontend hosting khac: host `bookstore-website`

## 2. Aiven mapping

Map truc tiep thong tin MySQL cua Aiven vao:

```env
DB_HOST=<aiven-host>
DB_PORT=3306
DB_NAME=<aiven-db-name>
DB_USER=<aiven-username>
DB_PASSWORD=<aiven-password>
```

Khong doi `spring.jpa.hibernate.ddl-auto=validate` o profile `prod`.
Flyway se bootstrap schema truoc, Hibernate chi validate.

## 3. Backend env on Render

Bat buoc cho moi deploy:

```env
SPRING_PROFILES_ACTIVE=prod
DB_HOST=<aiven-host>
DB_PORT=3306
DB_NAME=<aiven-db-name>
DB_USER=<aiven-username>
DB_PASSWORD=<aiven-password>
JWT_SECRET=<set in env>
CORS_ALLOWED_ORIGINS=https://your-frontend.example.com
```

### Demo deploy

```env
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

Dung cho moi truong demo/cham bai. Seed tao catalog + demo accounts + dashboard data.

### Real production

```env
APP_SWAGGER_ENABLED=false
APP_DEMO_SEED_ENABLED=false
APP_ADMIN_SEED_ENABLED=false
```

Neu can bootstrap admin lan dau tren production that:

1. Tam bat `APP_ADMIN_SEED_ENABLED=true`
2. Set day du `ADMIN_*`
3. Deploy 1 lan
4. Xac nhan admin da tao
5. Tat lai `APP_ADMIN_SEED_ENABLED=false`

Khong bat `APP_DEMO_SEED_ENABLED` tren production that.

## 4. Frontend env

Website can tro ve backend Render URL:

```env
VITE_API_BASE_URL=https://your-backend.onrender.com/api
VITE_GOOGLE_CLIENT_ID=<set in env if used>
```

`CORS_ALLOWED_ORIGINS` cua backend phai khop frontend URL that.

## 5. Readiness and smoke

### Payment-expiry environment variables

Set these backend variables for the QR-payment lifecycle. `BANK_TRANSFER_EXPIRATION_MINUTES` must be from `5` to `1440`; do not set the values in application code.

```text
BANK_TRANSFER_EXPIRATION_MINUTES=20
PAYMENT_EXPIRY_JOB_ENABLED=true
PAYMENT_EXPIRY_JOB_DELAY_MS=60000
PAYMENT_EXPIRY_JOB_BATCH_SIZE=100
```

Before release, run `mvnw.cmd -Ptestcontainers verify` with Docker Desktop running. The profile starts MySQL 8 and exercises the checkout/cancel/expiry/IPN lock and deduplication harness.

Sau khi deploy backend:

```powershell
Invoke-RestMethod https://your-backend.onrender.com/actuator/health
```

Ky vong:

- `status = UP`

Demo deploy co the verify nhanh bang:

```powershell
cd D:\bookstore
.\scripts\smoke-demo.ps1 -BaseUrl https://your-backend.onrender.com
```

## 6. Swagger expectation

- Demo deploy: `APP_SWAGGER_ENABLED=true`, co the mo `/swagger-ui/index.html`
- Real production: `APP_SWAGGER_ENABLED=false`, `/v3/api-docs` va `/swagger-ui/index.html` nen tra `404`

## 7. Related docs

- `D:\bookstore\docs\DEMO_SCRIPT.md`
- `D:\bookstore\docs\SMOKE_TEST_FLOW.md`
- `D:\bookstore\docs\RELEASE_CHECKLIST.md`
- `D:\bookstore\bookstore-backend\docs\PRODUCTION_DATABASE_SETUP.md`
