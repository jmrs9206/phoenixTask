#!/usr/bin/env bash
set -euo pipefail

ROOT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")/.." && pwd)"
# shellcheck source=/dev/null
source "${ROOT_DIR}/scripts/_env.sh"

FORCE=0
while [[ $# -gt 0 ]]; do
  case "$1" in
    --force)
      FORCE=1
      shift
      ;;
    *)
      shift
      ;;
  esac
done

ATTACHMENTS_ROOT="${PHOENIXTASK_ATTACHMENTS_ROOT:-${ROOT_DIR}/backend/storage/attachments}"
TENANT_CODE="${PHOENIXTASK_DEMO_TENANT:-demo}"

if ! docker ps --format '{{.Names}}' | grep -q "^${PHOENIXTASK_POSTGRES_CONTAINER}$"; then
  echo "❌ Contenedor Postgres ${PHOENIXTASK_POSTGRES_CONTAINER} no está levantado."
  exit 1
fi

QUERY="SELECT issue_id, stored_filename, original_filename, mime_type, size_bytes FROM issue_attachments ORDER BY id;"
ROWS=""
table_exists() {
  docker exec -i "${PHOENIXTASK_POSTGRES_CONTAINER}" \
    psql -U "${POSTGRES_USER}" -d "${PHOENIXTASK_WS_DB_NAME}" -t -A \
    -c "SELECT to_regclass('public.issue_attachments') IS NOT NULL;" 2>/dev/null | tr -d '[:space:]'
}

for _ in {1..20}; do
  if [[ "$(table_exists)" == "t" ]]; then
    break
  fi
  sleep 1
done

if [[ "$(table_exists)" != "t" ]]; then
  echo "ℹ️ Tabla issue_attachments no disponible en ${PHOENIXTASK_WS_DB_NAME}; se omite carga de attachments demo."
  exit 0
fi

for _ in {1..10}; do
  ROWS=$(docker exec -i "${PHOENIXTASK_POSTGRES_CONTAINER}" \
    psql -U "${POSTGRES_USER}" -d "${PHOENIXTASK_WS_DB_NAME}" -t -A -F '|' -c "${QUERY}" 2>/dev/null || true)
  if [[ -n "${ROWS}" ]]; then
    break
  fi
  sleep 2
done

if [[ -z "${ROWS}" ]]; then
  echo "⚠️ No hay attachments en la DB demo."
  exit 0
fi

mkdir -p "${ATTACHMENTS_ROOT}"

TOTAL=0
CREATED=0
while IFS='|' read -r issue_id stored_filename original_filename mime_type size_bytes; do
  [[ -z "${issue_id}" ]] && continue
  TOTAL=$((TOTAL + 1))

  ISSUE_DIR="${ATTACHMENTS_ROOT}/${TENANT_CODE}/issues/${issue_id}"
  FILE_PATH="${ISSUE_DIR}/${stored_filename}"

  if [[ -f "${FILE_PATH}" && "${FORCE}" -eq 0 ]]; then
    continue
  fi

  mkdir -p "${ISSUE_DIR}"

  case "${mime_type}" in
    image/png)
      printf '%s' 'iVBORw0KGgoAAAANSUhEUgAAAAEAAAABCAQAAAC1HAwCAAAAC0lEQVR42mP8/x8AAwMCAO5+aXcAAAAASUVORK5CYII=' | base64 -d > "${FILE_PATH}"
      ;;
    text/plain)
      printf 'PhoenixTask demo attachment: %s\n' "${original_filename}" > "${FILE_PATH}"
      ;;
    application/pdf)
      printf 'PhoenixTask demo attachment: %s\n' "${original_filename}" > "${FILE_PATH}"
      ;;
    *)
      printf 'PhoenixTask demo attachment: %s\n' "${original_filename}" > "${FILE_PATH}"
      ;;
  esac

  if [[ -n "${size_bytes}" ]]; then
    truncate -s "${size_bytes}" "${FILE_PATH}" 2>/dev/null || true
  fi

  CREATED=$((CREATED + 1))
done <<< "${ROWS}"

echo "✅ Demo attachments listos: ${CREATED}/${TOTAL} creados en ${ATTACHMENTS_ROOT}/${TENANT_CODE}/issues"
