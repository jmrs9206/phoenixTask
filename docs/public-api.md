# PhoenixTask Public API (v1 implicit)

Base path: `/api/public` (v1 implicit for now)

## Authentication
- Header: `X-Phoenix-Api-Key: <api_key>`
- Optional tenant hint header (must match the key tenant): `X-Tenant-Code: <tenant_code>`
- Request correlation: `X-Request-Id` (generated if omitted)

## Scopes
- `projects.read` — read projects
- `issues.read` — read issues

## Endpoints

### GET /api/public/projects
List projects.

Query params:
- `page` (default 1)
- `pageSize` (default 20, max 100)
- `status` (optional)
- `query` (optional; matches project key or name)

Response (200):
```json
{
  "items": [
    {
      "id": 12,
      "projectKey": "PROJ",
      "name": "Phoenix Core",
      "status": "ACTIVE"
    }
  ],
  "page": 1,
  "pageSize": 20,
  "total": 1,
  "totalPages": 1
}
```

### GET /api/public/projects/{projectId}
Project detail.

Response (200):
```json
{
  "id": 12,
  "projectKey": "PROJ",
  "name": "Phoenix Core",
  "status": "ACTIVE",
  "description": "Core platform work"
}
```

### GET /api/public/issues
List issues.

Query params:
- `page` (default 1)
- `pageSize` (default 20, max 100)
- `projectId` (optional)
- `status` (optional)
- `priority` (optional)
- `query` (optional; matches issue key or title)

Response (200):
```json
{
  "items": [
    {
      "id": 101,
      "issueKey": "PROJ-101",
      "title": "Fix tenant onboarding",
      "projectKey": "PROJ",
      "status": "OPEN",
      "priority": "HIGH"
    }
  ],
  "page": 1,
  "pageSize": 20,
  "total": 1,
  "totalPages": 1
}
```

### GET /api/public/issues/{issueId}
Issue detail.

Response (200):
```json
{
  "id": 101,
  "issueKey": "PROJ-101",
  "title": "Fix tenant onboarding",
  "projectKey": "PROJ",
  "status": "OPEN",
  "priority": "HIGH",
  "dueDate": "2026-04-01",
  "description": "Ensure provisioning retries are stable."
}
```

## Errors (consistent model)
Error payload:
```json
{
  "timestamp": "2026-03-28T10:15:30+01:00",
  "status": 403,
  "error": "FORBIDDEN",
  "message": "Missing scope: issues.read",
  "path": "/api/public/issues"
}
```

Common cases:
- 401 `UNAUTHORIZED`: missing/invalid/revoked API key
- 403 `FORBIDDEN`: missing scope or tenant mismatch
- 404 `NOT_FOUND`: resource not found (or not in tenant DB)
- 409 `TENANT_INACTIVE`: tenant suspended/inactive

## Tenant lifecycle behavior
If the tenant is not ACTIVE, Public API requests are blocked with `409 TENANT_INACTIVE`.

## Notes
- Public API keys are created and revoked via controlplane endpoints.
- This is v1 implicit; future versions will use `/api/public/v{n}`.
