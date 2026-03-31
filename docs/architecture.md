# PhoenixTask® Architecture

## Dominios
- controlplane: administración global de la plataforma SaaS.
- workspace: lógica interna de cada tenant/empresa.
- integrations: API pública e integraciones externas.

## Capas por dominio
- application
- domain
- infrastructure
- interfaces

## Estrategia multi-tenant
- DB central: `phoenixtask_local` (controlplane).
- DB tenant demo: `phoenixtask_demo` (workspace demo).
- Un único backend Spring Boot.
- Datasource fijo para controlplane y mecanismo preparado para datasource por tenant.
- El tenant demo se registra explícitamente en `tenant_registry` y se bootstrappea localmente.

## Separación de datos
- No se mezclan tablas de controlplane y workspace en la misma base.
- Migraciones Flyway separadas para controlplane y workspace.

## Continuidad de producto
La base técnica se rehace desde cero preservando el nombre PhoenixTask®, módulos objetivo y línea de producto. No hay rediseño ni cambio de branding en esta fase.

## Modelo base (Fase 2)
- Memberships separadas: `team_memberships` y `project_memberships`.
- Rol base/global: `users.primary_role_id`.
- Roles de contexto: `team_memberships.role_id` y `project_memberships.role_id`.
- Owner: rol interno (`code=OWNER`, no asignable) y `company.owner_user_id`.
- Permisos granulares definidos en `permissions` y asignados por rol en `role_permissions`.
- Seed demo con company, settings, roles, permissions, usuarios, equipos, proyectos y memberships.

## Vertical slice (Fase 5)
- Issues: tabla `issues` con `issue_key` único global por tenant y `projects.issue_counter` por proyecto.
- `issue_key` generado en backend en la misma transacción (proyecto + contador); se aceptan huecos si falla el insert.
- Memberships: endpoints de alta y lectura para equipos y proyectos, validando `is_assignable=true` (Owner excluido).

## Messages (Fase 6)
- Tablas: `message_threads` y `messages`.
- `message_threads` soporta `TEAM`, `PROJECT`, `DIRECT` con constraints y unicidad por tipo.
- Team/Project threads existen siempre; `last_message_at` puede ser NULL hasta el primer mensaje.
- Direct threads normalizados por `min/max` de usuarios y sin duplicidad.
- Acceso: memberships para team/project y participantes para direct.
- Estrategia temporal de usuario actual: header `X-User-Id` (frontend lo gestiona vía selector y `localStorage`).

## Scrum / Kanban / OKR / Gantt / Analytics
- Scrum: `sprints` por proyecto; backlog = issues sin sprint; 1 sprint `ACTIVE` por proyecto.
- Kanban: board derivado de issues por status (`OPEN`, `IN_PROGRESS`, `BLOCKED`, `DONE`).
- OKR: **project‑scoped** (`okr_objectives.project_id` + KR/check‑ins por proyecto).
- Gantt: timeline real usando fechas en `projects` e `issues`.
- Analytics: métricas derivadas (issues por status/prioridad/proyecto, backlog vs active sprint, counts de teams/projects/users/messages).
- Advanced insights: aproximaciones documentadas (CFD/MTTR/cycle time). Ver `docs/limitations.md`.
