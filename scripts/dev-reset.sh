#!/usr/bin/env bash
set -euo pipefail

ROOT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")/.." && pwd)"
# shellcheck source=/dev/null
source "${ROOT_DIR}/scripts/_env.sh"

echo "⚠️  Reset completo del entorno local (DB + demo)."

"${ROOT_DIR}/scripts/dev-down.sh" --force

docker compose down -v
PHOENIXTASK_PG_PORT="${PHOENIXTASK_PG_PORT}" docker compose up -d postgres

echo "⏳ Esperando Postgres..."
for _ in {1..30}; do
  if docker exec "${PHOENIXTASK_POSTGRES_CONTAINER}" pg_isready -U "${POSTGRES_USER}" -d "${POSTGRES_DB}" >/dev/null 2>&1; then
    break
  fi
  sleep 1
done

"${ROOT_DIR}/scripts/dev-up.sh" --skip-down
