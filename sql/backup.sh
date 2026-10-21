#!/usr/bin/env bash
# ===================================================
#   TRAVELINK - Respaldo de Base de Datos (MySQL)
# ===================================================
set -euo pipefail

SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
ROOT_DIR="$(cd "${SCRIPT_DIR}/.." && pwd)"

# 1. Cargar variables desde .env si existe
if [ -f "${ROOT_DIR}/.env" ]; then
    echo "[INFO] Cargando variables de entorno desde ${ROOT_DIR}/.env..."
    # Cargar respetando comentarios y comillas
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

# 2. Validar variables minimas
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
    # Formato: jdbc:mysql://host:port/database?params
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

# 4. Carpeta de destino
BACKUP_DIR="${SCRIPT_DIR}/backups"
mkdir -p "${BACKUP_DIR}"

TIMESTAMP="$(date +'%Y%m%d_%H%M%S')"
BACKUP_FILE="${BACKUP_DIR}/travelink_backup_${TIMESTAMP}.sql"

echo "[INFO] Host:     ${DB_HOST}:${DB_PORT}"
echo "[INFO] Base:     ${DB_NAME}"
echo "[INFO] Usuario:  ${DB_USERNAME}"
echo "[INFO] Destino:  ${BACKUP_FILE}"
echo "[INFO] Ejecutando mysqldump..."

export MYSQL_PWD="${DB_PASSWORD}"
mysqldump \
    --host="${DB_HOST}" \
    --port="${DB_PORT}" \
    --user="${DB_USERNAME}" \
    --single-transaction \
    --routines \
    --triggers \
    --default-character-set=utf8mb4 \
    "${DB_NAME}" > "${BACKUP_FILE}"
STATUS=$?
unset MYSQL_PWD

if [ $STATUS -eq 0 ]; then
    echo "[ÉXITO] Respaldo generado correctamente en:"
    echo "        ${BACKUP_FILE}"
else
    echo "[ERROR] Falló al ejecutar mysqldump (Código: ${STATUS})." >&2
    rm -f "${BACKUP_FILE}"
    exit $STATUS
fi
