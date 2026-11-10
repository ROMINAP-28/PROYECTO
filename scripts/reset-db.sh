#!/usr/bin/env bash
# ==========================================================
# TRAVELINK - SCRIPT DE REINICIO DE BASE DE DATOS LOCAL
# Entorno: Local exclusivamente (Docker / Linux / WSL / macOS)
# ==========================================================

set -euo pipefail

# GUARD 1: Verificar que se ejecute desde la raíz del proyecto TRAVELINK
if [[ ! -f "pom.xml" || ! -f "sql/travelink_schema.sql" || ! -f "sql/seed_pruebas.sql" ]]; then
    echo "[BLOQUEO DE SEGURIDAD] Este script debe ejecutarse desde el directorio raíz del proyecto TRAVELINK." >&2
    exit 1
fi

# GUARD 2: Verificar que el host de DB sea estrictamente local
PERMITIDOS=("localhost" "127.0.0.1" "host.docker.internal" "app" "db")
DB_URL_VAL="${DB_URL:-}"

if [[ -n "$DB_URL_VAL" ]]; then
    if [[ "$DB_URL_VAL" =~ aivencloud ]]; then
        echo "[BLOQUEO DE SEGURIDAD] Se detectó 'aivencloud' en DB_URL. Abortando operación destructiva." >&2
        exit 1
    fi
    ES_SEGURO=false
    for TARGET in "${PERMITIDOS[@]}"; do
        if [[ "$DB_URL_VAL" =~ $TARGET ]]; then
            ES_SEGURO=true
            break
        fi
    done
    if [[ "$ES_SEGURO" = false ]]; then
        echo "[BLOQUEO DE SEGURIDAD] El host en DB_URL no coincide con objetivos locales permitidos. Abortando." >&2
        exit 1
    fi
fi

echo "=========================================================="
echo "  TRAVELINK - REINICIO DEL ENTORNO LOCAL DE BASE DE DATOS"
echo "=========================================================="
echo "ADVERTENCIA: Esta acción eliminará el contenedor y volumen local 'travelink_mysql_data'"
echo "y reconstruirá la base de datos con los datos sintéticos de prueba."

if [[ "${1:-}" != "-f" && "${1:-}" != "--force" ]]; then
    read -p "¿Desea continuar con el reinicio local? (s/N): " CONFIRMACION
    if [[ "$CONFIRMACION" != "s" && "$CONFIRMACION" != "S" ]]; then
        echo "Operación cancelada por el usuario."
        exit 0
    fi
fi

echo "[1/3] Deteniendo y eliminando contenedores y volumen local..."
docker compose down -v

echo "[2/3] Levantando servicio db con inicialización automática..."
docker compose up -d db

echo "[3/3] Esperando que la base de datos esté saludable..."
SALUDABLE=false
for i in {1..30}; do
    STATUS=$(docker inspect --format="{{.State.Health.Status}}" travelink-db 2>/dev/null || echo "starting")
    if [[ "$STATUS" == "healthy" ]]; then
        SALUDABLE=true
        echo "Base de datos local inicializada exitosamente y lista para pruebas."
        break
    fi
    sleep 2
    echo "Esperando inicialización... ($i/30)"
done

if [[ "$SALUDABLE" = false ]]; then
    echo "[ERROR CRÍTICO] La base de datos no alcanzó el estado 'healthy' tras 30 intentos. ABORTANDO y NO se levantará la aplicación." >&2
    exit 1
fi

echo "Reiniciando la aplicación web..."
docker compose up -d app
echo "Entorno local restablecido en http://127.0.0.1:8080"
