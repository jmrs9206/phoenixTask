# PhoenixTask® Developer Onboarding

## Repo layout
- `backend/` — Spring Boot (controlplane + workspace)
- `frontend/` — Next.js app
- `scripts/` — local ops scripts
- `docs/` — product + ops documentation

## Local setup (recommended)
```bash
./scripts/dev-up.sh --force
./scripts/dev-smoke.sh
```

## Demo data
- Tenant: `demo`
- Login: `sofia.ramos@phoenixtask.demo`
- Password: `PhoenixTask2026!`

## Verify environment health
- Backend status: `http://localhost:8080/api/system/status`
- Frontend: `http://localhost:3000`
- Smoke check: `./scripts/dev-smoke.sh`

## Useful flows to validate
- Login → Home
- Issues list → Issue detail
- Scrum / Kanban / Gantt overview
- OKR per project: `/okr/projects/{projectId}`

## Stop / Reset
```bash
./scripts/dev-down.sh --force
./scripts/dev-reset.sh
```
