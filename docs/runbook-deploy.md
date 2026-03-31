# PhoenixTask® Deployment & CI/CD Runbook

This runbook defines how PhoenixTask® is built, configured, and validated across environments.

## Environment strategy
PhoenixTask® is deployed as two services plus Postgres:
- **Backend** (Spring Boot)
- **Frontend** (Next.js)
- **Postgres** (controlplane + workspace databases)

### Environments
- **local**: Dockerized Postgres, backend+frontend started via scripts.
- **dev**: Shared dev environment with seeded demo tenant; managed Postgres.
- **staging**: Prod-like config and data isolation; managed Postgres and separate secrets.
- **prod**: Strict isolation, immutable builds, no demo seed.

### What changes per environment
- **Database hosts/ports/credentials**
- **Controlplane admin bootstrap key**
- **Frontend public base URL**
- **CORS allowed origins** (prod/staging should be locked down)

## Build (reproducible)
### Backend
```bash
./scripts/build-backend.sh
```
Produces `backend/target/*.jar` using `./mvnw -DskipTests package`.

### Frontend
```bash
./scripts/build-frontend.sh
```
Uses `npm ci` and `npm run build` with:
- `NEXT_PUBLIC_BACKEND_URL`
- `NEXT_PUBLIC_TENANT_CODE`

## CI (minimal, useful)
CI mirrors local build steps:
```bash
./scripts/ci-check.sh
```
Expected to run on every PR/commit.

## Variables & secrets
Reference: `.env.example`

### Backend (required)
- `PHOENIXTASK_DB_HOST`, `PHOENIXTASK_DB_PORT`
- `PHOENIXTASK_CONTROL_DB`, `PHOENIXTASK_DB_USER`, `PHOENIXTASK_DB_PASSWORD`
- `PHOENIXTASK_WS_DB_HOST`, `PHOENIXTASK_WS_DB_PORT`
- `PHOENIXTASK_WS_DB_NAME`, `PHOENIXTASK_WS_DB_USER`, `PHOENIXTASK_WS_DB_PASSWORD`
- `PHOENIXTASK_ATTACHMENTS_ROOT`, `PHOENIXTASK_ATTACHMENTS_MAX_BYTES`
- `PHOENIXTASK_CONTROLPLANE_ADMIN_BOOTSTRAP_KEY` (sensitive)
- `PHOENIXTASK_SECURITY_KEY_PEPPER` (sensitive, base64 32‑byte; raw string accepted for dev with warning)
- `PHOENIXTASK_SECURITY_ENCRYPTION_KEY` (sensitive, base64 32‑byte; invalid values fail startup)

### Frontend (public)
- `NEXT_PUBLIC_BACKEND_URL`
- `NEXT_PUBLIC_TENANT_CODE`

### Optional (integrations)
- `PHOENIXTASK_INTEGRATIONS_GIT_VALIDATE_ON_CREATE` (set `true` in staging/prod if outbound access allowed)

## Smoke & health strategy
### Local smoke
```bash
./scripts/dev-smoke.sh
```
Checks:
- Postgres
- backend health (`/api/system/status`)
- frontend response
- demo tenant ACTIVE
- demo login

### Post-deploy smoke (dev/staging/prod)
Recommended sequence:
1. `/api/system/status` returns 200.
2. Frontend root returns 200.
3. Controlplane DB reachable.
4. Tenant registry contains expected tenants.

## Local bootstrap
```bash
./scripts/dev-up.sh --force
./scripts/dev-smoke.sh
```

## Troubleshooting (deploy)
- Backend can’t reach DB: validate `PHOENIXTASK_DB_HOST/PORT` and credentials.
- Frontend points to wrong backend: set `NEXT_PUBLIC_BACKEND_URL`.
- Missing admin key: set `PHOENIXTASK_CONTROLPLANE_ADMIN_BOOTSTRAP_KEY` before boot to ensure recoverable admin access.
