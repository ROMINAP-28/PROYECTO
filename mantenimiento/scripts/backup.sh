#!/usr/bin/env bash
# ==============================================================================
# TRAVELINK - Respaldo de Base de Datos MySQL (Linux / Bash)
# mysqldump con --single-transaction, compresión gzip, SHA-256 y retención
# ==============================================================================
set -euo pipefail

SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
PROJECT_ROOT="$(cd "${SCRIPT_DIR}/../.." && pwd)"

# 1. Cargar variables de entorno desde .env si existe
if [ -f "${PROJECT_ROOT}/.env" ]; then
    set -a
    # shellcheck disable=SC1091
    source <(grep -v '^[[:space:]]*#' "${PROJECT_ROOT}/.env" | grep -v '^[[:space:]]*$' | sed -e 's/\r$//')
    set +a
fi

DB_HOST="${DB_HOST:-127.0.0.1}"
DB_PORT="${DB_PORT:-3306}"
DB_NAME="${DB_NAME:-${MYSQL_DATABASE:-DBTravelink}}"
DB_USER="${DB_USER:-${DB_USERNAME:-root}}"
DB_PASS="${DB_PASSWORD:-${MYSQL_ROOT_PASSWORD:-}}"

if [ -z "${DB_PASS}" ]; then
    echo "[ERROR] Variable DB_PASSWORD o MYSQL_ROOT_PASSWORD no definida." >&2
    exit 1
fi

RETENCION_DIARIA="${RETENCION_DIARIA:-7}"
RETENCION_SEMANAL="${RETENCION_SEMANAL:-4}"

BACKUP_DIR="${PROJECT_ROOT}/mantenimiento/backups"
mkdir -p "${BACKUP_DIR}"

TIMESTAMP="$(date +'%Y%m%d_%H%M%S')"
BACKUP_BASE="travelink_backup_${TIMESTAMP}"
BACKUP_GZ="${BACKUP_DIR}/${BACKUP_BASE}.sql.gz"
BACKUP_SHA="${BACKUP_GZ}.sha256"

echo "=========================================================="
echo " TRAVELINK - Respaldo Automatizado de Base de Datos"
echo "=========================================================="
echo "[INFO] Host:     ${DB_HOST}:${DB_PORT}"
echo "[INFO] Base:     ${DB_NAME}"
echo "[INFO] Usuario:  ${DB_USER}"
echo "[INFO] Archivo:  ${BACKUP_GZ}"

export MYSQL_PWD="${DB_PASS}"

if command -v mysqldump >/dev/null 2>&1; then
    echo "[INFO] Ejecutando mysqldump nativo con --single-transaction..."
    mysqldump \
        --host="${DB_HOST}" \
        --port="${DB_PORT}" \
        --user="${DB_USER}" \
        --single-transaction \
        --quick \
        --routines \
        --triggers \
        --default-character-set=utf8mb4 \
        "${DB_NAME}" | gzip -c > "${BACKUP_GZ}"
else
    echo "[INFO] mysqldump nativo no encontrado; usando contenedor Docker..."
    CONTAINER_NAME="${CONTAINER_DB_NAME:-travelink-db-prod}"
    docker exec "${CONTAINER_NAME}" mysqldump \
        -u"${DB_USER}" -p"${DB_PASS}" \
        --single-transaction --quick --routines --triggers \
        --default-character-set=utf8mb4 "${DB_NAME}" | gzip -c > "${BACKUP_GZ}"
fi

unset MYSQL_PWD

if [ ! -s "${BACKUP_GZ}" ]; then
    echo "[ERROR] El volcado SQL falló o el archivo comprimido está vacío." >&2
    rm -f "${BACKUP_GZ}"
    exit 1
fi

# Generar Checksum SHA-256
cd "${BACKUP_DIR}"
sha256sum "$(basename "${BACKUP_GZ}")" > "${BACKUP_SHA}"
cd "${PROJECT_ROOT}"

echo "[ÉXITO] Respaldo comprimido y verificado: $(basename "${BACKUP_GZ}")"
echo "[ÉXITO] Checksum: $(cat "${BACKUP_SHA}")"

# Política de Retención
echo "[INFO] Aplicando retención (Diarios: ${RETENCION_DIARIA}, Semanales: ${RETENCION_SEMANAL})..."
find "${BACKUP_DIR}" -name "travelink_backup_*.sql.gz" -type f -mtime +"${RETENCION_DIARIA}" | while read -r old_file; do
    dia_semana=$(date -r "${old_file}" +%u) # 7 = Domingo
    if [ "${dia_semana}" -eq 7 ] && [ "${RETENCION_SEMANAL}" -gt 0 ]; then
        echo "  -> Conservando respaldo semanal dominical: $(basename "${old_file}")"
        RETENCION_SEMANAL=$((RETENCION_SEMANAL - 1))
    else
        echo "  -> Purgando respaldo antiguo: $(basename "${old_file}")"
        rm -f "${old_file}" "${old_file}.sha256"
    fi
done

echo "=========================================================="
echo " RESPALDO COMPLETADO EXITOSAMENTE"
echo "=========================================================="
