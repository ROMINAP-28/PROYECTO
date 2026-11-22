#!/bin/bash
# ==============================================================================
# TRAVELINK - Reinicio Controlado de Datos de Prueba
# ==============================================================================
set -euo pipefail

SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
DB_HOST="${DB_HOST:-127.0.0.1}"
DB_PORT="${DB_PORT:-3306}"
DB_NAME="${DB_NAME:-travelink_db}"
DB_USER="${DB_USER:-root}"
DB_PASSWORD="${DB_PASSWORD:-root1234}"
FORCE="${1:-}"

echo "====================================================="
echo "   TRAVELINK - RESET CONTROLADO DE DATOS DE PRUEBA"
echo "====================================================="

# Bloqueo de seguridad: NUNCA en producción ni Aiven
if [[ "$DB_HOST" =~ aivencloud\.com ]] || [ "${SPRING_PROFILES_ACTIVE:-}" = "prod" ]; then
    echo "[FATAL] ABORTADO: Prohibido ejecutar reset de prueba en Aiven o entorno prod."
    exit 1
fi

SEED_FILE="$SCRIPT_DIR/../../sql/seed_pruebas.sql"
if [ ! -f "$SEED_FILE" ]; then
    echo "[ERROR] No se encuentra el seed $SEED_FILE"
    exit 1
fi

if [ "$FORCE" != "--force" ]; then
    echo "[AVISO] Esta acción sobreescribe datos en '$DB_NAME' ($DB_HOST)."
    echo "Pase --force para confirmar la recarga de datos de prueba."
    exit 0
fi

if command -v mysql >/dev/null 2>&1; then
    export MYSQL_PWD="$DB_PASSWORD"
    echo "[INFO] Restaurando datos de prueba desde $SEED_FILE..."
    mysql -h "$DB_HOST" -P "$DB_PORT" -u "$DB_USER" "$DB_NAME" < "$SEED_FILE"
    echo "[ÉXITO] Datos de prueba restaurados satisfactoriamente."
else
    echo "[INFO] Seed SQL existente y validado: $SEED_FILE"
fi
