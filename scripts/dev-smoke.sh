#!/usr/bin/env bash
set -euo pipefail

ROOT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")/.." && pwd)"
# shellcheck source=/dev/null
source "${ROOT_DIR}/scripts/_env.sh"

echo "🔎 Smoke check PhoenixTask®"

echo "1) Postgres..."
docker exec "${PHOENIXTASK_POSTGRES_CONTAINER}" pg_isready -U "${POSTGRES_USER}" -d "${POSTGRES_DB}" >/dev/null
echo "   ✅ OK"

echo "2) Backend..."
curl -fsS "http://localhost:${PHOENIXTASK_BACKEND_PORT}/api/system/status" >/dev/null
echo "   ✅ OK"

echo "3) Frontend..."
curl -fsS "http://localhost:${PHOENIXTASK_FRONTEND_PORT}" >/dev/null
echo "   ✅ OK"

echo "4) Tenant demo..."
tenant_status=""
tenant_err=""
for _ in {1..30}; do
  tenant_raw=$(PGPASSWORD="${PHOENIXTASK_DB_PASSWORD}" psql \
    -h "${PHOENIXTASK_DB_HOST}" \
    -p "${PHOENIXTASK_DB_PORT}" \
    -U "${PHOENIXTASK_DB_USER}" \
    -d "${PHOENIXTASK_CONTROL_DB}" \
    -tAc "SELECT status FROM tenant_registry WHERE code = 'demo'" 2>&1) || true

  if [[ -n "${tenant_raw}" && "${tenant_raw}" != *"does not exist"* ]]; then
    tenant_status=$(echo "${tenant_raw}" | tr -d '[:space:]')
    if [[ -n "${tenant_status}" ]]; then
      break
    fi
  else
    tenant_err="${tenant_raw}"
  fi
  sleep 1
done

if [[ -z "${tenant_status}" ]]; then
  echo "   ❌ No pude leer tenant_registry (demo)."
  if [[ -n "${tenant_err}" ]]; then
    echo "   ❌ Error: ${tenant_err}"
  fi
  exit 1
fi

if [[ "${tenant_status}" != "ACTIVE" ]]; then
  echo "   ❌ Tenant demo no está ACTIVE (status=${tenant_status:-null})"
  exit 1
fi
echo "   ✅ OK (ACTIVE)"

echo "5) Login demo..."
login_code=$(curl -s -o /tmp/phoenixtask-login.json -w "%{http_code}" \
  -X POST "http://localhost:${PHOENIXTASK_BACKEND_PORT}/api/auth/login" \
  -H "Content-Type: application/json" \
  -d "{\"email\":\"${PHOENIXTASK_DEMO_EMAIL}\",\"password\":\"${PHOENIXTASK_DEMO_PASSWORD}\",\"tenantCode\":\"${PHOENIXTASK_DEMO_TENANT}\"}")

if [[ "${login_code}" != "200" ]]; then
  echo "   ❌ Login demo falló (HTTP ${login_code})"
  cat /tmp/phoenixtask-login.json || true
  exit 1
fi
echo "   ✅ OK"

echo "✅ Smoke check completado."
