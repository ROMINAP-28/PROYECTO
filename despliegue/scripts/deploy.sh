#!/usr/bin/env bash
# ==============================================================================
# Script de Despliegue de Producción - TRAVELINK
# ==============================================================================
set -euo pipefail

SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
PROJECT_ROOT="$(cd "${SCRIPT_DIR}/../.." && pwd)"
COMPOSE_FILE="${PROJECT_ROOT}/despliegue/docker/docker-compose.prod.yml"

echo "=== [TRAVELINK] Iniciando Despliegue de Producción ==="

# 1. Validar archivo compose
if [ ! -f "${COMPOSE_FILE}" ]; then
    echo "[ERROR] No se encuentra el archivo ${COMPOSE_FILE}" >&2
    exit 1
fi

# 2. Compilar / Reconstruir imágenes
echo "--> Reconstruyendo contenedores de producción..."
docker compose -f "${COMPOSE_FILE}" build app

# 3. Levantar servicios en segundo plano
echo "--> Iniciando servicios (DB, App, Nginx)..."
docker compose -f "${COMPOSE_FILE}" up -d

# 4. Esperar estado saludable (máximo 120s)
echo "--> Esperando que travelink-app-prod alcance estado saludable..."
ATTEMPTS=0
MAX_ATTEMPTS=24
HEALTHY=false

while [ ${ATTEMPTS} -lt ${MAX_ATTEMPTS} ]; do
    STATUS=$(docker inspect --format='{{json .State.Health.Status}}' travelink-app-prod 2>/dev/null || echo "\"starting\"")
    if [ "${STATUS}" == "\"healthy\"" ]; then
        HEALTHY=true
        break
    fi
    echo "    Estado actual: ${STATUS}... esperando 5s (${ATTEMPTS}/${MAX_ATTEMPTS})"
    sleep 5
    ATTEMPTS=$((ATTEMPTS + 1))
done

if [ "${HEALTHY}" = true ]; then
    echo "[OK] travelink-app-prod está saludable."
else
    echo "[ERROR] Timeout esperando que travelink-app-prod esté saludable." >&2
    docker compose -f "${COMPOSE_FILE}" logs app --tail 40
    exit 1
fi

# 5. Smoke test HTTP
echo "--> Ejecutando Smoke Test sobre Nginx (puerto 80)..."
HTTP_CODE=$(curl -s -o /dev/null -w "%{http_code}" http://localhost/api/destinos || echo "000")

if [ "${HTTP_CODE}" == "200" ]; then
    echo "=== [OK] Despliegue de Producción Completado Exitosamente (HTTP 200) ==="
    exit 0
else
    echo "[ERROR] Smoke Test falló con código HTTP: ${HTTP_CODE}" >&2
    exit 1
fi
