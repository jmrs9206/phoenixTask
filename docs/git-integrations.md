# Git Integrations (Foundation)

## Status model
- `CONFIGURED`: token stored (hashed) and webhook secret issued. Token **not validated** unless validation is enabled.
- `CONNECTED`: token validated against the provider (when validation is enabled).
- `REVOKED`: integration disabled.

## Providers
- GitHub
- GitLab

## Endpoints

### Create integration
`POST /api/workspace/integrations/git`

Body:
```json
{
  "provider": "github",
  "label": "core-github",
  "token": "ghp_...",
  "validate": true
}
```

Response:
```json
{
  "id": 3,
  "provider": "GITHUB",
  "label": "core-github",
  "status": "CONFIGURED",
  "tokenPrefix": "ghp_demo",
  "webhookSecret": "base64url...",
  "webhookPath": "/api/webhooks/git/demo/github/3",
  "createdAt": "2026-03-28T21:22:43.411646"
}
```

Validation behavior:
- If `validate=true` (or `PHOENIXTASK_INTEGRATIONS_GIT_VALIDATE_ON_CREATE=true`), the backend will call the provider API.
- Success -> status `CONNECTED`. Failure -> request rejected with validation error.

### Link repository
`POST /api/workspace/integrations/git/{integrationId}/projects/{projectId}/repos`

Body:
```json
{
  "repoOwner": "phoenixtask",
  "repoName": "phoenix-core",
  "defaultBranch": "main"
}
```

### Webhooks
`POST /api/webhooks/git/{tenantCode}/github/{integrationId}`  
`POST /api/webhooks/git/{tenantCode}/gitlab/{integrationId}`

Validation:
- GitHub: `X-Hub-Signature-256` (HMAC-SHA256)
- GitLab: `X-Gitlab-Token`

## Notes
- Tokens are **stored as hashes** and never returned by the API.
- Webhook secrets are encrypted at rest when `PHOENIXTASK_SECURITY_ENCRYPTION_KEY` is configured.
- `CONFIGURED` indicates the integration is ready for webhooks but not yet validated with the provider.
