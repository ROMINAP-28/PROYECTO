#!/usr/bin/env bash
# ==========================================================
# TRAVELINK - SCRIPT DE CARGA DE SEED SINTÉTICO LOCAL
# Entorno: Local exclusivamente (Docker / Linux / WSL / macOS)
# ==========================================================

set -euo pipefail

# GUARD 1: Verificar raíz del proyecto
if [[ ! -f "pom.xml" || ! -f "sql/seed_pruebas.sql" ]]; then
    echo "[BLOQUEO DE SEGURIDAD] Este script debe ejecutarse desde el directorio raíz del proyecto TRAVELINK." >&2
    exit 1
fi

# GUARD 2: Verificar que el contenedor de DB esté corriendo localmente
RUNNING=$(docker ps --filter "name=travelink-db" --filter "status=running" -q)
if [[ -z "$RUNNING" ]]; then
    echo "[ERROR] El contenedor 'travelink-db' no está en ejecución. Inicie con 'docker compose up -d db'." >&2
    exit 1
fi

echo "Cargando datos sintéticos desde sql/seed_pruebas.sql..."
cat sql/seed_pruebas.sql | docker exec -i travelink-db sh -c 'mysql -uroot -p"$MYSQL_ROOT_PASSWORD" DBTravelink'

echo "Seed sintético cargado exitosamente en travelink-db."
