# MASTER_CONTEXT

## Estado objetivo
Construir PhoenixTask como producto integral de trabajo, planificación, colaboración, rendimiento e integraciones, con base técnica profesional.

## Núcleo
- issue
- project
- team
- user
- role
- permission

## Módulos principales
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
- marketing-site
- audit

## Restricciones de producto
- no MVP
- backend first
- datos estandarizados
- permisos duros
- storage desacoplado
- invitación de usuarios
- audit interno solo para owner


# MASTER_CONTEXT

## Fecha de actualización
2026-04-23

## Estado actual resumido

PhoenixTask se define como una aplicación web all-in-one robusta y profesional, pensada primero para uso propio y después para venta.

### Decisiones ya fijadas
- Backend: monolito modular.
- Frontend: modular por feature.
- Repo único con `backend/`, `frontend/`, `docs/`, `docker/`, `scripts/`.
- `issue` es el núcleo del sistema.
- `kanban`, `scrum`, `calendar` y `gantt` operan sobre `issues`.
- `performance` agrupa `okr`, `kpis` y `scorecards`.
- `messaging` será tipo Slack mejorado, integrado con trabajo real.
- `notifications` y `activity` están separados.
- `audit` es interno y solo visible para el owner de la plataforma.
- `files` es módulo transversal.
- Datos operativos: máximo select, mínimo input libre.
- Auth sin registro público.
- Usuarios por invitación.
- Reset de contraseña por enlace.
- Roles y permisos atómicos.
- Admin interno separado del admin cliente.
- Base de datos actual objetivo: MySQL.
- Portabilidad futura prevista hacia Oracle.
- Storage desacoplado: MinIO ahora, S3 después.
- Backend first. Frontend después.
- No se construirá como MVP.

### Módulos objetivo
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
- marketing-site
- shared
- audit

### Situación actual
- Ya existe una base documental maestra.
- El repositorio actual aún necesita saneamiento técnico antes de reorganización fuerte.
- El siguiente paso inmediato es subir esta base documental al repo en rama propia.