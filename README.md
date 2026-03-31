# PhoenixTask®

PhoenixTask® is a multi-tenant work OS focused on execution: issues, projects, scrum, kanban, gantt, and project-scoped OKR with integrated analytics.

## Quick start (local)
- Start: `./scripts/dev-up.sh --force`
- Stop: `./scripts/dev-down.sh --force`
- Reset demo: `./scripts/dev-reset.sh`
- Smoke check: `./scripts/dev-smoke.sh`

Ports (defaults): Postgres `5433`, backend `8080`, frontend `3000`.

Demo credentials:
- Email: `sofia.ramos@phoenixtask.demo`
- Password: `PhoenixTask2026!`
- Tenant code: `demo`

## Documentation index
- Product runbook: `docs/runbook-product.md`
- Ops runbook: `docs/runbook-ops.md`
- Deployment & CI/CD runbook: `docs/runbook-deploy.md`
- Access & permissions guide: `docs/access-guide.md`
- Known limitations & honesty notes: `docs/limitations.md`
- Developer onboarding: `docs/onboarding.md`
- Public API: `docs/public-api.md`
- Git integrations: `docs/git-integrations.md`
- Architecture notes: `docs/architecture.md`

## Frontend
The frontend is started by `./scripts/dev-up.sh`. It auto-points `NEXT_PUBLIC_BACKEND_URL` to the backend port.

## Public API
Docs: `docs/public-api.md`.

## Git integrations
Docs: `docs/git-integrations.md`.
