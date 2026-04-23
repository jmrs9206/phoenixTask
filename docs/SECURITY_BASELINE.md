# SECURITY_BASELINE

## Identidad
- login por email + contraseña
- sin register público
- usuarios por invitación
- recuperación por enlace, no por contraseña enviada

## Invitaciones
- token único
- expiración
- un solo uso
- reenvío permitido por rol autorizado

## Password reset
- token único
- expiración corta
- un solo uso
- no revelar si el email existe
- rate limiting

## Passwords
- hash seguro
- nunca reversibles
- nunca enviadas en claro como flujo principal

## Permisos
- backend siempre decide
- permisos atómicos
- separación clara entre admin cliente y owner plataforma

## Logging
- activity log funcional
- audit log interno
- trace id
- logs sin datos sensibles en claro
