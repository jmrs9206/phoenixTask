#!/usr/bin/env bash
set -euo pipefail

psql -v ON_ERROR_STOP=1 --username "$POSTGRES_USER" --dbname "$POSTGRES_DB" <<-EOSQL
CREATE DATABASE phoenixtask_local OWNER "$POSTGRES_USER";
CREATE DATABASE phoenixtask_demo OWNER "$POSTGRES_USER";
EOSQL
