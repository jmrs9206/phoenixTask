# FILE_STORAGE_STRATEGY

## Principio
Nada de base64. Nada de binarios en tablas de negocio.

## Arquitectura
- interfaz `StorageService`
- implementación actual recomendada: MinIO
- implementación futura: AWS S3
- almacenamiento desacoplado

## Flujo
1. usuario sube archivo
2. backend valida tamaño, mime y permisos
3. backend guarda en storage
4. backend guarda metadata en BD
5. backend crea relación con issue, mensaje u otra entidad
6. backend registra activity event si aplica

## Metadata mínima
- file_id
- original_name
- storage_key
- mime_type
- size_bytes
- checksum
- uploaded_by
- created_at
