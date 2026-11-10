#!/usr/bin/env bash
# ==========================================
# Fuzzing de Endpoints ffuf - TRAVELINK
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
OUT_DIR="$PROJECT_ROOT/docs/seguridad/evidencias/ffuf"
mkdir -p "$OUT_DIR"
OUT_FILE="$OUT_DIR/ffuf-result.json"
WORDLIST="$SCRIPT_DIR/wordlists/api-endpoints.txt"
TARGET_URL="http://${TARGET_HOST}:${PORT}/api/FUZZ"

echo "=========================================================="
echo " [FUZZING] Escaneo con ffuf - TRAVELINK ($TARGET_URL)"
echo "=========================================================="

if command -v ffuf &>/dev/null; then
    ffuf -u "$TARGET_URL" -w "$WORDLIST" -mc 200,401,403,500 -o "$OUT_FILE" -of json
    echo "[ÉXITO] Resultados guardados en $OUT_FILE"
else
    echo "[AVISO] ffuf no está instalado."
    echo "Instalar con: sudo apt install -y ffuf"
    echo "[PENDIENTE] ffuf no instalado. Ejecutar: ffuf -u $TARGET_URL -w $WORDLIST -mc 200,401,403,500 -o $OUT_FILE -of json" > "$OUT_FILE"
fi
