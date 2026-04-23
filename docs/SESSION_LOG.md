# SESSION_LOG

## Sesión 2026-04-23
### Rama
docs/foundation-architecture-pack

### Objetivo
Dejar la base documental maestra de PhoenixTask antes de tocar estructura y código.

### Hecho
- Se definió la arquitectura objetivo de PhoenixTask.
- Se fijó backend como monolito modular.
- Se fijó frontend modular por feature.
- Se definió `issue` como núcleo del sistema.
- Se acordó que `kanban`, `scrum`, `calendar` y `gantt` consumen `issues`.
- Se definió `performance` como módulo paraguas para `okr`, `kpis` y `scorecards`.
- Se definieron `files`, `activity`, `audit`, `iam`, `integration-api`, `analytics`, `branding` y `marketing-site`.
- Se fijó política de datos: máximo select, mínimo input libre.
- Se fijó auth sin registro público: usuarios por invitación + reset por enlace.
- Se fijó activity log visible y audit log interno solo para el owner de plataforma.
- Se fijó storage desacoplado: MinIO ahora, S3 después.
- Se fijó MySQL como motor actual, con portabilidad futura prevista.
- Se generó el paquete documental base y la librería IA en `docs/ai/`.

### Archivos tocados
- README.md
- docs/ROADMAP.md
- docs/ARCHITECTURE.md
- docs/DECISIONS.md
- docs/MASTER_CONTEXT.md
- docs/SESSION_LOG.md
- docs/DATA_MODEL.md
- docs/API_CONTRACT_STRATEGY.md
- docs/IAM_MODEL.md
- docs/UX_DATA_CAPTURE_POLICY.md
- docs/FILE_STORAGE_STRATEGY.md
- docs/SECURITY_BASELINE.md
- docs/ai/README.md
- docs/ai/USAGE_RULES.md
- docs/ai/ROLE_LIBRARY.md
- docs/ai/PROMPT_TEMPLATES.md
- docs/ai/STRICT_RESPONSE_POLICY.md

### Tests ejecutados
No aplica. Bloque documental.

### Decisiones tomadas
- No MVP.
- No microservicios por ahora.
- Repo único con backend y frontend.
- Backend first.
- Sin register público.
- Permisos atómicos y roles custom.
- Admin interno separado del admin cliente.

### Bloqueos
- El repositorio actual todavía no está en baseline green.
- Falta decidir si se reconstruye limpio dentro del repo o si se sanea el árbol actual por fases.

### Siguiente paso exacto
Subir la base documental al repo en una rama propia y dejarla como referencia oficial del proyecto.

### Prompt siguiente recomendado
Auditoría/mapeo físico de carpetas objetivo backend y frontend contra el estado actual del repo.