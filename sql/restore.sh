#!/usr/bin/env bash
# ===================================================
#   TRAVELINK - Restauración de Base de Datos (MySQL)
# ===================================================
set -euo pipefail

SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
ROOT_DIR="$(cd "${SCRIPT_DIR}/.." && pwd)"

# 1. Cargar variables desde .env si existe
if [ -f "${ROOT_DIR}/.env" ]; then
    echo "[INFO] Cargando variables de entorno desde ${ROOT_DIR}/.env..."
    set -a
    # shellcheck disable=SC1091
    source <(grep -v '^[[:space:]]*#' "${ROOT_DIR}/.env" | grep -v '^[[:space:]]*$' | sed -e 's/\r$//')
    set +a
elif [ -f "${SCRIPT_DIR}/.env" ]; then
    echo "[INFO] Cargando variables de entorno desde ${SCRIPT_DIR}/.env..."
    set -a
    # shellcheck disable=SC1091
    source <(grep -v '^[[:space:]]*#' "${SCRIPT_DIR}/.env" | grep -v '^[[:space:]]*$' | sed -e 's/\r$//')
    set +a
fi

# 2. Validar variables mínimas
if [ -z "${DB_USERNAME:-}" ]; then
    echo "[ERROR] Variable DB_USERNAME no definida." >&2
    exit 1
fi
if [ -z "${DB_PASSWORD:-}" ]; then
    echo "[ERROR] Variable DB_PASSWORD no definida." >&2
    exit 1
fi

# 3. Parsear DB_URL si es necesario
DB_HOST="${DB_HOST:-localhost}"
DB_PORT="${DB_PORT:-3306}"
DB_NAME="${DB_NAME:-DBTravelink}"

if [ -n "${DB_URL:-}" ]; then
    CLEAN_URL="${DB_URL#jdbc:mysql://}"
    HOST_PORT="${CLEAN_URL%%/*}"
    DB_WITH_PARAMS="${CLEAN_URL#*/}"
    PARSED_DB="${DB_WITH_PARAMS%%\?*}"

    if [[ "$HOST_PORT" =~ ^([^:]+):([0-9]+)$ ]]; then
        DB_HOST="${BASH_REMATCH[1]}"
        DB_PORT="${BASH_REMATCH[2]}"
    elif [ -n "$HOST_PORT" ]; then
        DB_HOST="$HOST_PORT"
    fi

    if [ -n "$PARSED_DB" ]; then
        DB_NAME="$PARSED_DB"
    fi
fi

# 4. Determinar archivo a restaurar
BACKUP_DIR="${SCRIPT_DIR}/backups"
BACKUP_FILE="${1:-}"

if [ -z "${BACKUP_FILE}" ]; then
    echo "[INFO] No se especificó archivo. Buscando el más reciente en ${BACKUP_DIR}..."
    if [ -d "${BACKUP_DIR}" ]; then
        BACKUP_FILE="$(ls -t "${BACKUP_DIR}"/*.sql 2>/dev/null | head -n 1 || true)"
    fi
fi

if [ -z "${BACKUP_FILE}" ] || [ ! -f "${BACKUP_FILE}" ]; then
    echo "[ERROR] No se encontró ningún archivo de respaldo para restaurar." >&2
    echo "Uso: ./restore.sh [ruta_al_archivo_sql]" >&2
    exit 1
fi

echo "[ADVERTENCIA] Se restaurará: ${BACKUP_FILE}"
echo "[ADVERTENCIA] Sobre la base:  ${DB_NAME} en ${DB_HOST}:${DB_PORT}"
echo "[INFO] Ejecutando restauración con mysql..."

export MYSQL_PWD="${DB_PASSWORD}"
mysql \
    --host="${DB_HOST}" \
    --port="${DB_PORT}" \
    --user="${DB_USERNAME}" \
    --default-character-set=utf8mb4 \
    "${DB_NAME}" < "${BACKUP_FILE}"
STATUS=$?
unset MYSQL_PWD

if [ $STATUS -eq 0 ]; then
    echo "[ÉXITO] Base de datos restaurada correctamente desde:"
    echo "        ${BACKUP_FILE}"
else
    echo "[ERROR] Falló la restauración (Código: ${STATUS})." >&2
    exit $STATUS
fi
