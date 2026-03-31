# PhoenixTask® Ops Runbook (Local)

This is the operational guide for running PhoenixTask® locally.

## Start / Stop / Reset / Smoke
```bash
./scripts/dev-up.sh --force
./scripts/dev-down.sh --force
./scripts/dev-reset.sh
./scripts/dev-smoke.sh
```

## Ports
- Postgres: `5433`
- Backend: `8080` (override via `PHOENIXTASK_BACKEND_PORT`)
- Frontend: `3000` (override via `PHOENIXTASK_FRONTEND_PORT`)

## Secrets & keys (recommended)
- `PHOENIXTASK_SECURITY_KEY_PEPPER` — base64 32‑byte pepper for admin + public API key hashing (raw string accepted for dev, logs a warning).
- `PHOENIXTASK_SECURITY_ENCRYPTION_KEY` — base64 32‑byte key for encrypting Git webhook secrets (invalid values fail startup).
- `PHOENIXTASK_INTEGRATIONS_GIT_VALIDATE_ON_CREATE` — `true` to validate Git tokens at create time.

## Logs
- Backend: `logs/backend.log`
- Frontend: `logs/frontend.log`

## Common issues & fixes
### Port already in use (3000/8080/8081)
```bash
./scripts/dev-down.sh --force
```
If the port is still held by a stray process, kill it manually:
```bash
lsof -t -iTCP:8080 -sTCP:LISTEN | xargs -r kill -9
```

### Backend started on 8081 by mistake
- Ensure `PHOENIXTASK_BACKEND_PORT=8080` (or update `NEXT_PUBLIC_BACKEND_URL`).
- Re‑run: `./scripts/dev-up.sh --force`.

### Frontend points to wrong backend
- `dev-up.sh` sets `NEXT_PUBLIC_BACKEND_URL` automatically.
- If you run frontend manually, export:
  - `NEXT_PUBLIC_BACKEND_URL=http://localhost:8080`

### Postgres not reachable on 5433
- Check Docker: `docker ps` (container `phoenixtask-postgres`)
- Restart: `docker compose up -d postgres`

## Demo tenant validation
- Tenant: `demo`
- Smoke check includes demo tenant health and login.

If demo tenant is not ACTIVE:
1) Run `./scripts/dev-reset.sh`.
2) Re‑run `./scripts/dev-smoke.sh`.

## Scripts overview
- `scripts/dev-up.sh` — start DB + backend + frontend
- `scripts/dev-down.sh` — stop services and free ports
- `scripts/dev-reset.sh` — reset DB volume + reseed demo
- `scripts/dev-smoke.sh` — quick health checks

## Admin channel note
Global controlplane admin flows (tenant suspend/reactivate, public API keys) are protected by admin channels.
See `docs/access-guide.md` for authentication and permissions.
