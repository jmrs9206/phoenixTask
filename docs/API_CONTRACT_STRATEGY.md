# API_CONTRACT_STRATEGY

## Objetivo
Evitar drift entre backend y frontend y dejar una convención estable para API interna y externa.

## Reglas
- Un endpoint, un contrato claro.
- Naming consistente.
- Verb correcto.
- Error uniforme.
- DTOs explícitos.
- Paginación coherente.
- Filtros explícitos.
- Idempotencia en mutaciones críticas.
- Auth y permisos documentados.

## Convenciones
- `GET` para lectura
- `POST` para creación
- `PUT` para reemplazo fuerte
- `PATCH` para actualización parcial
- acciones específicas con `POST /{id}/action` o `PATCH` si solo cambia estado parcial

## Errores
Formato uniforme:
- error
- message
- traceId
- status
- details opcional

## API externa
- tokens por integración
- scopes finos
- rate limit
- auditoría
- endpoints de catálogos
- keys/ids estables, no texto ambiguo
