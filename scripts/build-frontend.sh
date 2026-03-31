#!/usr/bin/env bash
set -euo pipefail

ROOT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")/.." && pwd)"

: "${NEXT_PUBLIC_BACKEND_URL:=http://localhost:8080}"
: "${NEXT_PUBLIC_TENANT_CODE:=demo}"

cd "${ROOT_DIR}/frontend"

npm ci
NEXT_PUBLIC_BACKEND_URL="${NEXT_PUBLIC_BACKEND_URL}" \
NEXT_PUBLIC_TENANT_CODE="${NEXT_PUBLIC_TENANT_CODE}" \
  npm run build

echo "✅ Frontend build OK (.next)"
