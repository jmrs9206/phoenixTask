# PhoenixTask® Frontend

Base frontend conectada para PhoenixTask® (Fase 4) con pulido de UX/visual en Fase 8.

## Arranque local

1. Instalar dependencias:
   - `npm install`

2. Configurar variables (opcional):
   - `NEXT_PUBLIC_BACKEND_URL` (por defecto `http://localhost:8080`)
   - `NEXT_PUBLIC_TENANT_CODE` (por defecto `demo`)

3. Levantar frontend:
   - `npm run dev`

## Estructura

- `src/app` rutas y layouts (App Router)
- `src/components` layout y UI base
- `src/lib/api` cliente centralizado
- `src/lib/config` configuración de entorno
- `src/types` tipado de datos

## Messages (Fase 6)

- Ruta: `/messages`
- Selector de usuario actual persistido en `localStorage` (`phoenixtask.currentUserId`).

## Fase 8 (pulido visual)

Convenciones base:
- Copy de estados: `Loading`, `Something went wrong`, `No data yet` con mensajes consistentes.
- Responsive: sidebar drawer en tablet/móvil, tablas con scroll, mensajes en layout vertical/toggle.
- Componentes consolidados: `EmptyState`, `SimpleTable` con empty state integrado, formularios con acciones alineadas.
