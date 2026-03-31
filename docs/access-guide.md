# PhoenixTask® Access & Permissions Guide

This guide summarizes authentication channels, roles, and access patterns.

## Authentication channels
- **Internal workspace auth**: user session via `/api/auth/login` (cookie/session). Used by the UI.
- **Controlplane admin channel**: admin key for global lifecycle operations (tenant suspend/reactivate, public API keys). Not tied to a tenant session.
- **Public API key**: external integration access via `X-Phoenix-Api-Key` scoped per tenant.

## Access model (core rule)
Access is **project‑scoped** for delivery modules:
- Scrum / Kanban / Gantt / OKR are **project‑scoped**.
- A user must be **member of the project** and have the **module permission** to access.

## Demo roles (summary)
- **Owner**: full access (all modules + admin‑level permissions).
- **Team Leader**: project management + OKR create/update/close.
- **Developer**: execution focus; no OKR permissions.
- **Support / QA**: read‑only OKR.

## Sensitive access
- **Tenant lifecycle** (suspend/reactivate): controlplane admin channel only.
- **Audit/logs/analytics sensitive views**: require explicit permissions (see roles above).
- **Public API keys**: manage via controlplane admin channel.

## Common permission outcomes
- **401**: unauthenticated session or invalid admin/API key.
- **403**: authenticated but missing permission or membership.
- **409**: tenant inactive (for public API / lifecycle‑restricted flows).

For Public API scopes and errors, see `docs/public-api.md`.
