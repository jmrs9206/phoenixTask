# FRONTEND_UX_RULES

## Fecha de actualización

2026-04-23

## Objetivo

Definir reglas visuales y de comportamiento del frontend de PhoenixTask para mantener una experiencia limpia, operativa y profesional.

---

## 1. Principios generales

- La interfaz debe priorizar el trabajo, no el ruido visual.
- El contenido principal debe tener prioridad sobre navegación persistente.
- La navegación debe ser accesible, pero no invasiva.
- El sistema debe sentirse premium, limpio y rápido.
- La UI debe guiar al usuario para reducir errores.

---

## 2. Layout global

### Reglas

- `topbar`, `navbar` y `sidebar` podrán ocultarse cuando no se estén usando.
- Cada bloque podrá fijarse con una chincheta.
- Si un bloque no está fijado, podrá ocultarse automáticamente.
- Si el usuario está interactuando con un bloque, no debe ocultarse de forma molesta.
- Las transiciones deben ser suaves y discretas.
- El comportamiento en móvil podrá ser distinto al de escritorio.

### Preferencias previstas

- `topbarPinned`
- `navbarPinned`
- `sidebarPinned`
- `compactMode`

### Persistencia

- Si se decide persistir preferencias, deberán guardarse por usuario.
- Si no hay persistencia backend al inicio, podrá usarse `localStorage`.

---

## 3. Topbar

### Reglas

- Debe tener estilo visual premium, similar a herramientas modernas de gestión.
- Debe usar el logo oficial de PhoenixTask.
- El avatar del usuario debe estar cerca de perfil / cerrar sesión.
- Los accesos de sesión/perfil deben ser visibles, pero no ocupar más espacio del necesario.
- Debe poder ocultarse si no está fijado.

---

## 4. Navbar y Sidebar

### Reglas

- Deben poder ocultarse para despejar pantalla.
- Deben mostrar navegación clara por módulos.
- No deben saturarse con texto innecesario.
- Deben soportar icono + texto.
- Deben poder mostrar estado activo de sección.

---

## 5. Cards de issues

### Reglas base

- Cada card debe mostrar el mínimo contenido útil.
- No debe parecer una mini página dentro del tablero.
- La tarjeta debe ser legible de un vistazo.

### Elementos mínimos

- `issueKey`
- título
- avatar del asignado
- badges relevantes
- prioridad visible
- fecha límite si aplica
- iconos de adjuntos / comentarios si aplica

### Prioridad

- La prioridad se representará mediante el borde principal de la tarjeta.
- Ejemplo de intención visual:
  - `critical / urgent` → rojo
  - `high` → naranja
  - `medium` → tono intermedio
  - `low` → tono neutro o gris

### Estado

- El estado no usará el borde principal.
- El estado se representará con badge/chip/label.

### Epic

- Si `issue.type = EPIC`, la tarjeta tendrá un efecto visual especial adicional.
- Ese efecto será un halo o borde dorado animado.
- La animación afectará al borde/halo, no a la tarjeta completa.
- La animación debe ser elegante y sutil.
- El efecto Epic no debe sustituir el borde de prioridad, sino complementarlo.

### Blocked

- `Blocked` debe verse claramente.
- No se usará animación “bonita” para comunicar bloqueo.
- Debe sentirse como advertencia o fricción, no como premio visual.

---

## 6. Formularios

### Regla principal

- Máximo select.
- Mínimo input libre.

### Reglas

- Todo dato operativo debe venir de catálogo o selector siempre que sea posible.
- Texto libre solo para contenido humano real:
  - título
  - descripción
  - comentario
  - mensaje
  - observación
- Los formularios deben guiar, no exigir memoria al usuario.
- Deben existir placeholders útiles, valores por defecto y validación clara.

---

## 7. Avatares

### Reglas

- Toda entidad con responsable visible debe mostrar avatar o iniciales.
- En cards, el avatar del asignado debe ser visible sin ocupar demasiado espacio.
- En ausencia de imagen, usar iniciales consistentes.
- El avatar debe poder reutilizarse en issues, mensajes, comentarios y actividad.

---

## 8. Adjuntos e imágenes

### Reglas

- Debe permitirse pegar imágenes en los contextos donde aporte valor.
- Los adjuntos deben mostrarse con indicador visual claro.
- La UI no debe trabajar con base64.
- El frontend tratará archivos como objetos subidos y referenciados por backend.

---

## 9. Historial funcional

### Reglas

- Dentro del issue debe existir una vista clara de actividad.
- Cada evento debe mostrar:
  - quién
  - qué
  - fecha y hora
- Debe ser legible en formato timeline o lista cronológica.
- No debe mezclarse con el audit log interno.

---

## 10. Navegación pública vs privada

### Público

- La home pública vende la herramienta.
- No debe ser un dashboard interno.
- Debe explicar beneficios, módulos y propuesta de valor.

### Privado

- La app autenticada prioriza trabajo operativo.
- La entrada autenticada no tiene por qué ser analytics.
- Debe priorizar tareas, contexto y trabajo actual.

---

## 11. Módulo Messaging

### Reglas

- Debe sentirse tipo Slack, pero mejor integrado con trabajo.
- Debe soportar:
  - canales
  - direct messages
  - hilos
  - menciones
  - adjuntos
  - unread
- Debe integrarse con:
  - issues
  - proyectos
  - equipos
  - performance cuando aplique

---

## 12. Accesibilidad y claridad

### Reglas

- No abusar de animaciones.
- Respetar `prefers-reduced-motion` cuando aplique.
- Mantener contraste correcto.
- No basar significado solo en color.
- Los iconos deben apoyar, no sustituir completamente el texto.

---

## 13. Consistencia visual

### Reglas

- Un mismo concepto debe verse siempre igual.
- Prioridad, estado, tipo, bloqueo y asignación no deben mezclarse visualmente.
- Cada dimensión debe tener su canal visual:
  - prioridad → borde
  - estado → badge
  - tipo especial (Epic) → efecto adicional
  - asignado → avatar
  - adjuntos/comentarios → iconos secundarios
