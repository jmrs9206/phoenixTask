# PhoenixTask® — Operativa local rápida

## Arranque
```bash
./scripts/dev-up.sh --force
```

## Parada / limpieza
```bash
./scripts/dev-down.sh --force
```

## Reset + reseed demo
```bash
./scripts/dev-reset.sh
```

## Smoke check
```bash
./scripts/dev-smoke.sh
```

## Puertos por defecto
- Postgres: `5433`
- Backend: `8080` (si necesitas 8081, exporta `PHOENIXTASK_BACKEND_PORT`)
- Frontend: `3000`

## Credenciales demo
- `sofia.ramos@phoenixtask.demo`
- `PhoenixTask2026!`
- Tenant code: `demo`

## Troubleshooting rápido
- Puerto ocupado: `./scripts/dev-down.sh --force`
- Backend no responde: revisar `logs/backend.log`
- Frontend no responde: revisar `logs/frontend.log`
- Postgres no responde: `docker compose up -d postgres`

Documentación extendida: `docs/runbook-ops.md`.
