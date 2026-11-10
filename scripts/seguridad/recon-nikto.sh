#!/usr/bin/env bash
# ==========================================
# Reconocimiento Nikto - TRAVELINK
# ==========================================
set -euo pipefail

TARGET_HOST="${1:-127.0.0.1}"
PORT="${2:-8080}"

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
OUT_DIR="$PROJECT_ROOT/docs/seguridad/evidencias/nikto"
mkdir -p "$OUT_DIR"
OUT_FILE="$OUT_DIR/nikto-output.txt"
TARGET_URL="http://${TARGET_HOST}:${PORT}"

echo "=========================================================="
echo " [RECON] Escaneo Nikto - TRAVELINK ($TARGET_URL)"
echo "=========================================================="

if command -v nikto &>/dev/null; then
    nikto -h "$TARGET_URL" -o "$OUT_FILE"
    echo "[ÉXITO] Resultados guardados en $OUT_FILE"
elif command -v docker &>/dev/null; then
    echo "[INFO] Ejecutando mediante Docker sullo/nikto..."
    docker run --rm sullo/nikto -h "http://host.docker.internal:$PORT" | tee "$OUT_FILE"
else
    echo "[AVISO] Nikto no está instalado."
    echo "[PENDIENTE] Instalar Nikto con: sudo apt install -y nikto o docker run sullo/nikto" > "$OUT_FILE"
fi
