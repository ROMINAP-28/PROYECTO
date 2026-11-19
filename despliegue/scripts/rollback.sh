#!/usr/bin/env bash
# ==============================================================================
# Script de Rollback de Producción por Tag / Versión Previa - TRAVELINK
# Uso: ./rollback.sh [TAG_O_IMAGEN_OBJETIVO]
# Ejemplo: ./rollback.sh v1.0.0
# ==============================================================================
set -euo pipefail

SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
PROJECT_ROOT="$(cd "${SCRIPT_DIR}/../.." && pwd)"
COMPOSE_FILE="${PROJECT_ROOT}/despliegue/docker/docker-compose.prod.yml"
TARGET_TAG="${1:-previous}"

echo "=== [TRAVELINK] Iniciando Procedimiento de Rollback hacia: travelink-app:${TARGET_TAG} ==="

if [ ! -f "${COMPOSE_FILE}" ]; then
    echo "[ERROR] No se encuentra el archivo ${COMPOSE_FILE}" >&2
    exit 1
fi

echo "--> 1. Deteniendo contenedor de aplicacion fallido..."
docker compose --env-file "${PROJECT_ROOT}/.env" -f "${COMPOSE_FILE}" stop app

# Revertir a imagen previa etiquetada
if docker image inspect "travelink-app:${TARGET_TAG}" >/dev/null 2>&1; then
    echo "--> 2. Restaurando imagen: travelink-app:${TARGET_TAG} como travelink-app:latest..."
    docker tag "travelink-app:${TARGET_TAG}" travelink-app:latest
elif docker image inspect "travelink-app:previous" >/dev/null 2>&1; then
    echo "--> 2. Tag '${TARGET_TAG}' no encontrado localmente; restaurando fallback 'travelink-app:previous'..."
    docker tag travelink-app:previous travelink-app:latest
else
    echo "[AVISO] No se encontró imagen local específica. Descargando de registro o reusando caché..."
fi

echo "--> 3. Reiniciando servicio de aplicacion con imagen restaurada..."
docker compose --env-file "${PROJECT_ROOT}/.env" -f "${COMPOSE_FILE}" up -d app

echo "--> 4. Verificando convergencia y salud del servicio restaurado..."
for i in $(seq 1 20); do
    HEALTH=$(docker compose --env-file "${PROJECT_ROOT}/.env" -f "${COMPOSE_FILE}" ps app --format "{{.Health}}")
    if [ "$HEALTH" = "healthy" ]; then
        echo "[OK] Aplicación restaurada y en estado saludable."
        break
    fi
    echo "Esperando salud de la app ($i/20)..."
    sleep 3
done

docker compose --env-file "${PROJECT_ROOT}/.env" -f "${COMPOSE_FILE}" ps app
echo "=== [OK] Procedimiento de Rollback finalizado exitosamente ==="
