#!/usr/bin/env bash
set -euo pipefail

ROOT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")/.." && pwd)"
# shellcheck source=/dev/null
source "${ROOT_DIR}/scripts/_env.sh"

FORCE=0
if [[ "${1:-}" == "--force" ]]; then
  FORCE=1
fi

mkdir -p "${PID_DIR}"

stop_pid() {
  local name="$1"
  local pid_file="${PID_DIR}/${name}.pid"
  if [[ -f "${pid_file}" ]]; then
    local pid
    pid="$(cat "${pid_file}")"
    if [[ -n "${pid}" ]] && kill -0 "${pid}" 2>/dev/null; then
      kill "${pid}" || true
      for _ in {1..10}; do
        if kill -0 "${pid}" 2>/dev/null; then
          sleep 0.3
        else
          break
        fi
      done
      if kill -0 "${pid}" 2>/dev/null; then
        kill -9 "${pid}" || true
      fi
    fi
    rm -f "${pid_file}"
  fi
}

stop_pid "backend"
stop_pid "frontend"

pids_by_port() {
  local port="$1"
  if command -v lsof >/dev/null 2>&1; then
    lsof -t -iTCP:"${port}" -sTCP:LISTEN -n -P 2>/dev/null || true
  else
    ss -lntp | grep ":${port} " | sed -n 's/.*pid=\\([0-9]*\\).*/\\1/p' | sort -u || true
  fi
}

if [[ "${FORCE}" -eq 1 ]]; then
  for port in 3000 8080 8081; do
    pids=$(pids_by_port "${port}")
    for pid in ${pids}; do
      if kill -0 "${pid}" 2>/dev/null; then
        kill "${pid}" || true
        for _ in {1..10}; do
          if kill -0 "${pid}" 2>/dev/null; then
            sleep 0.3
          else
            break
          fi
        done
        if kill -0 "${pid}" 2>/dev/null; then
          kill -9 "${pid}" || true
        fi
      fi
    done
  done
fi

docker compose stop postgres >/dev/null 2>&1 || true

echo "✅ Backend y frontend detenidos. Puertos liberados (si --force)."
