# PhoenixTask® — Known Limitations & Honesty Notes

This section documents the current product limits so usage and interpretation stay honest.

## Advanced insights (approximations)
- **CFD**: derived from current status and completion data; it is an **approximation** when full historical transitions are unavailable.
- **Cycle time variability**: equivalent to a control chart using completed issues (created → updated date).
- **MTTR**: calculated from issue created → updated date; this is a **proxy** for resolution time.

## Tech debt ratio
- Based on issues flagged as tech debt (internal marker). It is **not** a code‑level analysis.

## Kanban flow metrics
- **Throughput**: count of issues moved to `DONE` in the last 7 days (uses updated date).
- **WIP age**: based on last status update timestamp, not true “time in progress”.

## Scrum maturity signals
- **Say/Do**: completed ÷ committed from the captured snapshot.
- **Burndown**: derived from snapshot points, not continuous time‑series telemetry.

## Gantt analytics
- **Critical path**: finish‑to‑start dependencies with planned dates; items without dates default to 1 day.
- **Resource load**: sum of planned durations for open issues per assignee.

## Git integrations
- GitHub/GitLab integrations default to **CONFIGURED** unless validation is enabled (`PHOENIXTASK_INTEGRATIONS_GIT_VALIDATE_ON_CREATE=true`).
- When validation is enabled and succeeds, status becomes **CONNECTED**.
- Webhooks are supported once a secret is configured.

## Secrets & keys
- Controlplane admin keys and Public API keys are hashed. For enterprise use, configure `PHOENIXTASK_SECURITY_KEY_PEPPER` (base64 32‑byte recommended).
- Git webhook secrets are encrypted only when `PHOENIXTASK_SECURITY_ENCRYPTION_KEY` is set (base64 32‑byte). Without it, secrets are stored in plaintext.
- Controlplane admin key rotation is manual (bootstrap a new key and revoke the old one in DB).

## OKR scope
- OKR is **project‑scoped** (not company/team‑scoped). Access is based on project membership + OKR permissions.

## Public API
- Scopes are limited to `projects.read` and `issues.read` in the current version.
- Tenant lifecycle (suspended/inactive) blocks Public API with `409 TENANT_INACTIVE`.
