# ARCHITECTURE

## Visión
PhoenixTask se construye como monolito modular backend + frontend modular por feature.

## Backend
- auth
- iam
- projects
- issues
- kanban
- scrum
- calendar
- gantt
- teams
- messaging
- notifications
- activity
- performance
- analytics
- integration-api
- integrations/github
- integrations/gitlab
- files
- admin
- settings
- branding
- shared
- audit

## Frontend
- app
- branding
- shared
- marketing-site
- features/auth
- features/projects
- features/issues
- features/kanban
- features/scrum
- features/calendar
- features/gantt
- features/teams
- features/messaging
- features/notifications
- features/performance
- features/analytics
- features/admin
- features/settings
- features/integrations

## Reglas
- `issue` es entidad núcleo.
- `kanban`, `scrum`, `calendar` y `gantt` operan sobre `issues`.
- `messaging` se relaciona con `teams`, pero no vive dentro de `teams`.
- `notifications` no sustituye `activity`.
- `audit` es interno, no visible al cliente.
- `files` es transversal.
- `performance` contiene `okr`, `kpis` y `scorecards`.
- `analytics` explota datos; no define métricas operativas.
