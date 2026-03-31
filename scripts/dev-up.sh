#!/usr/bin/env bash
set -euo pipefail

ROOT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")/.." && pwd)"
# shellcheck source=/dev/null
source "${ROOT_DIR}/scripts/_env.sh"

FORCE=0
SKIP_DOWN=0
while [[ $# -gt 0 ]]; do
  case "$1" in
    --force)
      FORCE=1
      shift
      ;;
    --skip-down)
      SKIP_DOWN=1
      shift
      ;;
    *)
      shift
      ;;
  esac
done

mkdir -p "${PID_DIR}" "${LOG_DIR}"

if [[ "${SKIP_DOWN}" -eq 0 ]]; then
  if [[ "${FORCE}" -eq 1 ]]; then
    "${ROOT_DIR}/scripts/dev-down.sh" --force
  else
    "${ROOT_DIR}/scripts/dev-down.sh"
  fi
fi

port_in_use() {
  local port="$1"
  if command -v lsof >/dev/null 2>&1; then
    lsof -iTCP:"${port}" -sTCP:LISTEN -n -P >/dev/null 2>&1
  else
    ss -lntp | grep -q ":${port} " 2>/dev/null
  fi
}

for port in "${PHOENIXTASK_BACKEND_PORT}" "${PHOENIXTASK_FRONTEND_PORT}"; do
  if port_in_use "${port}"; then
    echo "❌ Puerto ${port} en uso. Ejecuta ./scripts/dev-down.sh --force e inténtalo de nuevo."
    exit 1
  fi
done

PHOENIXTASK_PG_PORT="${PHOENIXTASK_PG_PORT}" docker compose up -d postgres

echo "⏳ Esperando Postgres..."
for _ in {1..30}; do
  if docker exec "${PHOENIXTASK_POSTGRES_CONTAINER}" pg_isready -U "${POSTGRES_USER}" -d "${POSTGRES_DB}" >/dev/null 2>&1; then
    break
  fi
  sleep 1
done

echo "🚀 Arrancando backend (puerto ${PHOENIXTASK_BACKEND_PORT})..."
(
  cd "${ROOT_DIR}/backend"
  nohup env \
    SPRING_PROFILES_ACTIVE=local \
    SERVER_PORT="${PHOENIXTASK_BACKEND_PORT}" \
    PHOENIXTASK_DB_PORT="${PHOENIXTASK_DB_PORT}" \
    PHOENIXTASK_WS_DB_PORT="${PHOENIXTASK_WS_DB_PORT}" \
    ./mvnw spring-boot:run > "${LOG_DIR}/backend.log" 2>&1 &
  echo $! > "${PID_DIR}/backend.pid"
)

echo "🚀 Arrancando frontend (puerto ${PHOENIXTASK_FRONTEND_PORT})..."
(
  cd "${ROOT_DIR}/frontend"
  nohup env \
    NEXT_PUBLIC_BACKEND_URL="http://localhost:${PHOENIXTASK_BACKEND_PORT}" \
    npm run dev -- --port "${PHOENIXTASK_FRONTEND_PORT}" > "${LOG_DIR}/frontend.log" 2>&1 &
  echo $! > "${PID_DIR}/frontend.pid"
)

echo "⏳ Esperando backend..."
backend_ready=0
for _ in {1..30}; do
  if curl -fsS "http://localhost:${PHOENIXTASK_BACKEND_PORT}/api/system/status" >/dev/null 2>&1; then
    backend_ready=1
    break
  fi
  sleep 1
done
if [[ "${backend_ready}" -eq 0 ]]; then
  echo "❌ Backend no respondió en ${PHOENIXTASK_BACKEND_PORT}. Revisa logs/backend.log"
  exit 1
fi

echo "⏳ Esperando frontend..."
frontend_ready=0
for _ in {1..30}; do
  if curl -fsS "http://localhost:${PHOENIXTASK_FRONTEND_PORT}" >/dev/null 2>&1; then
    frontend_ready=1
    break
  fi
  sleep 1
done
if [[ "${frontend_ready}" -eq 0 ]]; then
  echo "❌ Frontend no respondió en ${PHOENIXTASK_FRONTEND_PORT}. Revisa logs/frontend.log"
  exit 1
fi

echo "✅ Entorno levantado:"
echo "- Backend: http://localhost:${PHOENIXTASK_BACKEND_PORT}"
echo "- Frontend: http://localhost:${PHOENIXTASK_FRONTEND_PORT}"
echo "- Postgres: localhost:${PHOENIXTASK_PG_PORT}"
