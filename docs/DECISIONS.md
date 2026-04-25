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

## D-016

### Fecha

2026-04-23

### Título

Layout ocultable y fijable por el usuario

### Decisión

PhoenixTask permitirá ocultar `topbar`, `navbar` y `sidebar` cuando no se estén usando, para despejar pantalla y priorizar el contenido de trabajo.

Cada una de estas zonas podrá:

- mostrarse al interactuar con ella
- ocultarse automáticamente cuando no esté fijada
- quedarse visible mediante una acción de fijado con chincheta

### Reglas acordadas

- El `topbar` tendrá estilo visual premium, similar a herramientas de gestión modernas.
- El logo será el de PhoenixTask, no uno de referencia externa.
- El avatar del usuario se colocará cerca del área de perfil / cerrar sesión.
- `topbar`, `navbar` y `sidebar` deberán poder ocultarse de forma independiente si el diseño final lo permite.
- La chincheta indicará si el bloque queda fijo o auto-ocultable.
- Si el usuario está interactuando con un bloque, no debe ocultarse de forma molesta.
- El comportamiento debe ser suave y limpio, sin animaciones agresivas.
- En móvil se permitirá comportamiento adaptado, no necesariamente idéntico al de escritorio.

### Persistencia

La preferencia de layout podrá persistirse por usuario.

Preferencias previstas:

- `topbarPinned`
- `navbarPinned`
- `sidebarPinned`
- `compactMode`

### Motivo

- Dar más espacio útil al contenido principal.
- Reducir ruido visual.
- Mejorar foco en Kanban, Scrum, Gantt, Calendar, Issues y vistas de trabajo.
- Hacer que la interfaz se sienta más profesional y flexible.

### Impacto

- Afecta principalmente al frontend y al layout global.
- Backend solo participa si se decide persistir preferencias por usuario.
- No afecta la lógica de negocio principal.

### Alternativas descartadas

- Layout siempre fijo.
- Ocultar solo `sidebar`.
- No persistir preferencias nunca.
