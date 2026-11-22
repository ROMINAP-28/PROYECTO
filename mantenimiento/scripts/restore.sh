#!/usr/bin/env bash
# ==============================================================================
# TRAVELINK - Restauración de Base de Datos MySQL (Linux / Bash)
# ==============================================================================
set -euo pipefail

SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
PROJECT_ROOT="$(cd "${SCRIPT_DIR}/../.." && pwd)"

if [ -f "${PROJECT_ROOT}/.env" ]; then
    set -a
    # shellcheck disable=SC1091
    source <(grep -v '^[[:space:]]*#' "${PROJECT_ROOT}/.env" | grep -v '^[[:space:]]*$' | sed -e 's/\r$//')
    set +a
fi

DB_HOST="${DB_HOST:-127.0.0.1}"
DB_PORT="${DB_PORT:-3306}"
DB_NAME="${1:-${DB_NAME:-${MYSQL_DATABASE:-DBTravelink}}}"
DB_USER="${DB_USER:-${DB_USERNAME:-root}}"
DB_PASS="${DB_PASSWORD:-${MYSQL_ROOT_PASSWORD:-}}"

BACKUP_FILE="${2:-}"

if [ -z "${BACKUP_FILE}" ]; then
    BACKUP_DIR="${PROJECT_ROOT}/mantenimiento/backups"
    BACKUP_FILE=$(find "${BACKUP_DIR}" -name "travelink_backup_*.sql.gz" -type f | sort -r | head -n 1)
    if [ -z "${BACKUP_FILE}" ]; then
        echo "[ERROR] No se encontraron archivos de respaldo en ${BACKUP_DIR}." >&2
        exit 1
    fi
fi

echo "=========================================================="
echo " TRAVELINK - Restauración de Base de Datos"
echo "=========================================================="
echo "[INFO] Archivo de Respaldo: ${BACKUP_FILE}"
echo "[INFO] Base Destino:        ${DB_NAME}"
echo "[INFO] Host:                ${DB_HOST}:${DB_PORT}"

# Verificar Checksum si existe
if [ -f "${BACKUP_FILE}.sha256" ]; then
    echo "[INFO] Verificando integridad SHA-256..."
    cd "$(dirname "${BACKUP_FILE}")"
    sha256sum -c "$(basename "${BACKUP_FILE}").sha256"
    cd "${PROJECT_ROOT}"
fi

export MYSQL_PWD="${DB_PASS}"

echo "[INFO] Restaurando datos en ${DB_NAME}..."
if command -v mysql >/dev/null 2>&1; then
    gunzip -c "${BACKUP_FILE}" | mysql --host="${DB_HOST}" --port="${DB_PORT}" --user="${DB_USER}" --default-character-set=utf8mb4 "${DB_NAME}"
else
    CONTAINER_NAME="${CONTAINER_DB_NAME:-travelink-db-prod}"
    gunzip -c "${BACKUP_FILE}" | docker exec -i "${CONTAINER_NAME}" mysql -u"${DB_USER}" -p"${DB_PASS}" --default-character-set=utf8mb4 "${DB_NAME}"
fi

unset MYSQL_PWD

echo "=========================================================="
echo " RESTAURACIÓN COMPLETADA EXITOSAMENTE EN ${DB_NAME}"
echo "=========================================================="
