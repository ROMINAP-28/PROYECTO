#!/usr/bin/env bash
# ==========================================
# OWASP ZAP Baseline Scan - TRAVELINK
# ==========================================
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
PROJECT_ROOT="$(cd "$SCRIPT_DIR/../.." && pwd)"
OUT_DIR="$PROJECT_ROOT/docs/seguridad/evidencias/zap"
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

