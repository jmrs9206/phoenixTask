# IAM_MODEL

## Principio
Permisos atómicos, roles por defecto, roles personalizados y scopes claros.

## Niveles
### Interno plataforma
- platform_owner
- acceso a audit interno
- no asignable a clientes

### Roles del cliente
- manager
- developer
- qa
- viewer
- custom roles

## Permisos atómicos ejemplo
- user.create
- user.update
- user.deactivate
- team.create
- team.update
- project.create
- project.update
- issue.create
- issue.update
- issue.assign
- issue.change_status
- issue.comment
- scrum.create_sprint
- scrum.start_sprint
- scrum.close_sprint
- performance.objective.create
- performance.kpi.view
- analytics.view
- integration.github.connect
- integration.gitlab.connect
- api.token.create
- role.create
- role.update
- role.assign

## Scopes
- global platform
- workspace/company
- team
- project
- resource contextual si aplica

## Regla
Admin del cliente no equivale a platform owner.
