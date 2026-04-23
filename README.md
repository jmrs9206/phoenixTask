# PhoenixTask

PhoenixTask es una aplicación web all-in-one, robusta y profesional, diseñada primero para uso propio y después para venta.

## Principios base
- Backend: monolito modular.
- Frontend: modular por feature.
- `issue` es el núcleo.
- `kanban`, `scrum`, `calendar` y `gantt` consumen `issues`.
- Roles y permisos atómicos.
- Máximo dato estandarizado, mínimo input libre.
- Branding/white-label separado.
- Activity log funcional.
- Audit log interno solo para owner.
- Backend y base de datos primero.
- Storage desacoplado: MinIO ahora, S3 después.
- MySQL ahora, preparado para portabilidad futura.

## Módulos objetivo
### Plataforma
- auth
- iam
- admin
- settings
- branding
- marketing-site
- shared

### Core
- projects
- issues
- teams
- files
- activity

### Ejecución
- kanban
- scrum
- calendar
- gantt
- performance

### Colaboración
- messaging
- notifications

### Integraciones
- integration-api
- integrations/github
- integrations/gitlab

### Explotación
- analytics

### Interno plataforma
- audit
