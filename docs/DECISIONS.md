# DECISIONS

## D-001
PhoenixTask no se diseña como MVP rápido. Se diseña como producto robusto y vendible.

## D-002
Backend: monolito modular. No microservicios por ahora.

## D-003
Frontend: modular por feature.

## D-004
`issue` es el núcleo funcional del sistema.

## D-005
`kanban`, `scrum`, `calendar` y `gantt` consumen `issues`.

## D-006
No habrá registro libre. El alta será por invitación.

## D-007
Login con recuperación por enlace. No se envían contraseñas por correo.

## D-008
Roles y permisos atómicos. Roles custom permitidos. Admin interno de plataforma separado del admin del cliente.

## D-009
Máxima estandarización de datos. Selects y catálogos antes que texto libre.

## D-010
Storage desacoplado del negocio. Nada de base64.

## D-011
Activity log funcional visible en producto. Audit log interno solo para owner de la plataforma.

## D-012
MySQL ahora. Portabilidad futura a Oracle prevista desde diseño.

## D-013
Repo único con `backend/`, `frontend/`, `docs/`, `docker/`, `scripts/`.

## D-014
Marketing site separado de la app interna autenticada.

## D-015
`performance` sustituye a `okr` como módulo paraguas:
- okr
- kpis
- scorecards
