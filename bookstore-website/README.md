# Bookstore Website

React 19 + Vite 8 storefront/admin frontend for the bookstore project.

## Tooling

- Node.js 20+
- npm
- React 19
- TypeScript
- Vite 8

This checkout currently has both `package-lock.json` and `pnpm-lock.yaml`, but the documented workflow here is `npm`.

## Setup

```powershell
cd D:\bookstore\bookstore-website
Copy-Item .env.example .env
npm install
```

## Run locally

```powershell
cd D:\bookstore\bookstore-website
npm run dev
```

Default URL:

```txt
http://localhost:5173
```

## Verification

```powershell
cd D:\bookstore\bookstore-website
npm run build
npm test
```

## Required env

- `VITE_API_BASE_URL=http://localhost:8080/api`
- `VITE_GOOGLE_CLIENT_ID=...` for Google login

## Related docs

- Monorepo run guide: `D:\bookstore\docs\RUN_PROJECT.md`
- Backend production DB note: `D:\bookstore\bookstore-backend\docs\PRODUCTION_DATABASE_SETUP.md`
