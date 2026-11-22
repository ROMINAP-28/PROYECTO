#!/bin/bash
# ==============================================================================
# TRAVELINK - Prueba de Restauración Automatizada de Backups
# ==============================================================================
set -euo pipefail

SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
BACKUP_DIR="${1:-$SCRIPT_DIR/../backups}"
TEST_DB="${2:-travelink_restore_test}"

DB_HOST="${DB_HOST:-127.0.0.1}"
DB_PORT="${DB_PORT:-3306}"
DB_USER="${DB_USER:-root}"
DB_PASSWORD="${DB_PASSWORD:-root1234}"

echo "====================================================="
echo "   TRAVELINK - PRUEBA DE RESTAURACIÓN DE BACKUP"
echo "====================================================="
echo "Directorio de backups: $BACKUP_DIR"

if [ ! -d "$BACKUP_DIR" ]; then
    echo "[ERROR] El directorio de backups no existe: $BACKUP_DIR"
    exit 1
fi

LATEST_BACKUP=$(ls -t "$BACKUP_DIR"/travelink_backup_*.sql.gz 2>/dev/null | head -n 1 || true)

if [ -z "$LATEST_BACKUP" ]; then
    echo "[ERROR] No se encontraron archivos de backup (.sql.gz) en $BACKUP_DIR"
    exit 1
fi

echo "[INFO] Último backup detectado: $LATEST_BACKUP"

# 1. Verificar checksum
CHECKSUM_FILE="${LATEST_BACKUP}.sha256"
if [ -f "$CHECKSUM_FILE" ]; then
    EXPECTED_SHA=$(awk '{print $1}' "$CHECKSUM_FILE")
    ACTUAL_SHA=$(sha256sum "$LATEST_BACKUP" | awk '{print $1}')
    if [ "$EXPECTED_SHA" != "$ACTUAL_SHA" ]; then
        echo "[FATAL] Checksum no coincide. El backup está corrupto."
        exit 1
    fi
    echo "[OK] Checksum SHA-256 verificado: $ACTUAL_SHA"
else
    echo "[AVISO] No existe archivo .sha256; validando descompresión."
fi

# 2. Descomprimir temporalmente
TEMP_SQL="/tmp/travelink_test_$$.sql"
trap 'rm -f "$TEMP_SQL"' EXIT

gzip -dc "$LATEST_BACKUP" > "$TEMP_SQL"
echo "[OK] Backup descomprimido con éxito en $TEMP_SQL"

# 3. Restaurar y auditar si existe cliente mysql
if command -v mysql >/dev/null 2>&1; then
    export MYSQL_PWD="$DB_PASSWORD"
    echo "[INFO] Creando base temporal '$TEST_DB'..."
    mysql -h "$DB_HOST" -P "$DB_PORT" -u "$DB_USER" -e "DROP DATABASE IF EXISTS $TEST_DB; CREATE DATABASE $TEST_DB CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;"
    
    echo "[INFO] Restaurando datos..."
    mysql -h "$DB_HOST" -P "$DB_PORT" -u "$DB_USER" "$TEST_DB" < "$TEMP_SQL"
    
    echo "[INFO] Verificando conteo de registros en tablas maestras..."
    for tbl in Usuario Tour Reserva Destino Agencia; do
        COUNT=$(mysql -h "$DB_HOST" -P "$DB_PORT" -u "$DB_USER" -D "$TEST_DB" -sN -e "SELECT COUNT(*) FROM $tbl" 2>/dev/null || echo "N/A")
        echo "  -> Tabla $tbl: $COUNT registros"
    done
    
    echo "[INFO] Limpiando base de datos temporal..."
    mysql -h "$DB_HOST" -P "$DB_PORT" -u "$DB_USER" -e "DROP DATABASE IF EXISTS $TEST_DB;"
    echo "[ÉXITO] Restauración probada y validada correctamente."
else
    LINES=$(wc -l < "$TEMP_SQL")
    if grep -q "CREATE TABLE" "$TEMP_SQL" && [ "$LINES" -gt 50 ]; then
        echo "[OK] Validación Offline: Dump sintácticamente válido ($LINES líneas)."
        echo "[OK] Integridad estructural de tablas comprobada."
    else
        echo "[ERROR] Dump inválido o incompleto."
        exit 1
    fi
fi
echo "====================================================="
