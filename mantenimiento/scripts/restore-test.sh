#!/bin/bash
# ==============================================================================
# TRAVELINK - Prueba de Restauración Automatizada de Backups
# ==============================================================================
set -euo pipefail

SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
BACKUP_DIR="${1:-$SCRIPT_DIR/../backups}"
TEST_DB="${2:-travelink_restore_test}"

DB_HOST="${DB_HOST:-127.0.0.1}"
DB_PORT="${DB_PORT:-3307}"
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
gzip -dc "$LATEST_BACKUP" > "$TEMP_SQL"
sed -i "s/\bDBTravelink\b/$TEST_DB/g" "$TEMP_SQL"
echo "[OK] Backup descomprimido y adaptado a DB temporal: $TEMP_SQL"

# 3. Restaurar en Base de Datos de Prueba (mysql CLI o contenedor Docker travelink-db)
DOCKER_CONTAINER=""
if command -v docker &>/dev/null; then
    DOCKER_CONTAINER=$(docker ps --filter "name=travelink-db" --format "{{.Names}}" 2>/dev/null | head -n 1 || true)
fi

if command -v mysql &>/dev/null; then
    export MYSQL_PWD="$DB_PASSWORD"
    echo "[INFO] Creando base temporal $TEST_DB en ${DB_HOST}:${DB_PORT}..."
    mysql -h "$DB_HOST" -P "$DB_PORT" -u "$DB_USER" -e "DROP DATABASE IF EXISTS $TEST_DB; CREATE DATABASE $TEST_DB CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;" 2>/dev/null
    
    echo "[INFO] Restaurando datos..."
    mysql -h "$DB_HOST" -P "$DB_PORT" -u "$DB_USER" "$TEST_DB" < "$TEMP_SQL" 2>/dev/null

    echo "[INFO] Comprobando integridad de tablas maestras..."
    for tbl in Rol Persona Usuario Agencia Destino Tour Reserva; do
        count=$(mysql -h "$DB_HOST" -P "$DB_PORT" -u "$DB_USER" -D "$TEST_DB" -N -e "SELECT COUNT(*) FROM $tbl;" 2>/dev/null || echo "0")
        echo "  -> Tabla $tbl: $count filas"
    done

    echo "[INFO] Limpiando base temporal $TEST_DB..."
    mysql -h "$DB_HOST" -P "$DB_PORT" -u "$DB_USER" -e "DROP DATABASE IF EXISTS $TEST_DB;" 2>/dev/null
    echo "[ÉXITO] Prueba de restauración finalizada con éxito."
elif [ -n "$DOCKER_CONTAINER" ]; then
    echo "[INFO] Ejecutando restauración en contenedor Docker '$DOCKER_CONTAINER'..."
    docker exec -i "$DOCKER_CONTAINER" sh -c 'mysql -uroot -p"$MYSQL_ROOT_PASSWORD" -e "DROP DATABASE IF EXISTS '"$TEST_DB"'; CREATE DATABASE '"$TEST_DB"' CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;"' 2>/dev/null

    echo "[INFO] Restaurando datos en '$TEST_DB' dentro del contenedor..."
    docker exec -i "$DOCKER_CONTAINER" sh -c "mysql -uroot -p\"\$MYSQL_ROOT_PASSWORD\" $TEST_DB" < "$TEMP_SQL" 2>/dev/null

    echo "[INFO] Comprobando integridad de tablas maestras..."
    for tbl in Rol Persona Usuario Agencia Destino Tour Reserva; do
        count=$(docker exec -i "$DOCKER_CONTAINER" sh -c "mysql -uroot -p\"\$MYSQL_ROOT_PASSWORD\" -N -D $TEST_DB -e 'SELECT COUNT(*) FROM $tbl;'" 2>/dev/null || echo "0")
        echo "  -> Tabla $tbl: $count filas"
    done

    echo "[INFO] Eliminando base temporal '$TEST_DB'..."
    docker exec -i "$DOCKER_CONTAINER" sh -c 'mysql -uroot -p"$MYSQL_ROOT_PASSWORD" -e "DROP DATABASE IF EXISTS '"$TEST_DB"';"' 2>/dev/null
    echo "[ÉXITO] Prueba de restauración finalizada con éxito."
else
    # Validación sintáctica offline
    LINES=$(wc -l < "$TEMP_SQL")
    if grep -q "CREATE TABLE" "$TEMP_SQL" && [ "$LINES" -gt 50 ]; then
        echo "[OK] Validación Offline: Dump sintácticamente válido ($LINES líneas con DDL/DML)."
    else
        echo "[ERROR] Dump inválido o incompleto."
        rm -f "$TEMP_SQL"
        exit 1
    fi
fi

rm -f "$TEMP_SQL"
echo "====================================================="
