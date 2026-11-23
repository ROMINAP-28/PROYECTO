#!/usr/bin/env bash
# ==============================================================================
# OWASP ZAP Baseline Scan - TRAVELINK
# NOTA DE RED (app vs localhost):
# - http://app:8080 es el nombre de servicio dentro de la red Docker travelink-net;
#   solo lo resuelven los contenedores (ZAP, SQLMap, JMeter, CI). En el navegador
#   anfitrión genera DNS_PROBE_FINISHED_NXDOMAIN.
# - Desde el navegador o curl del host anfitrión se usa http://localhost:8080.
# - La aplicación sirve HTTP plano en local (https:// requiere proxy TLS).
# ==============================================================================
set -euo pipefail

TARGET_HOST="${1:-app}"
PORT="${2:-8080}"
NETWORK="${3:-travelink-net}"

# Guard de seguridad
ALLOWED_HOSTS=("localhost" "127.0.0.1" "host.docker.internal" "app")
if [[ "$TARGET_HOST" == *"aivencloud"* ]]; then
    echo "[ALERTA DE SEGURIDAD] Objetivo remoto detectado ($TARGET_HOST). Abortando." >&2
    exit 1
fi
VALID_HOST=0
for h in "${ALLOWED_HOSTS[@]}"; do
    if [[ "$TARGET_HOST" == "$h" ]]; then VALID_HOST=1; break; fi
done
if [ $VALID_HOST -ne 1 ]; then
    echo "[ALERTA DE SEGURIDAD] Host '$TARGET_HOST' no autorizado. Abortando." >&2
    exit 1
fi

SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
PROJECT_ROOT="$(cd "$SCRIPT_DIR/../../.." && pwd)"
OUT_DIR="$PROJECT_ROOT/pruebas/evidencias/seguridad/zap"
mkdir -p "$OUT_DIR"

TARGET_URL="http://${TARGET_HOST}:${PORT}"
ZAP_IMAGE="zaproxy/zap-stable"

echo "=========================================================="
echo " [DAST] OWASP ZAP Baseline Scan - TRAVELINK ($TARGET_URL)"
echo " Imagen oficial: $ZAP_IMAGE"
echo " Red Docker: $NETWORK"
echo "=========================================================="

docker run --rm \
    --network "$NETWORK" \
    -v "$OUT_DIR:/zap/wrk:rw" \
    -t "$ZAP_IMAGE" \
    zap-baseline.py \
    -t "$TARGET_URL" \
    -r "zap-report.html" \
    -J "zap-report.json" \
    -I || true

echo "[ÉXITO] Reportes guardados en $OUT_DIR"

