#!/usr/bin/env bash
# ==========================================
# Pruebas sqlmap - TRAVELINK
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
OUT_DIR="$PROJECT_ROOT/docs/seguridad/evidencias/sqlmap"
mkdir -p "$OUT_DIR"
LOG_FILE="$OUT_DIR/sqlmap-results.txt"

echo "=========================================================="
echo " [SQLi] Pruebas sqlmap - TRAVELINK"
echo " Objetivo: http://${TARGET_HOST}:${PORT}"
echo "=========================================================="

echo "PRUEBAS CONTROLADAS DE INYECCIÓN SQL (SQLMAP) - TRAVELINK" > "$LOG_FILE"
echo "Fecha: $(date)" >> "$LOG_FILE"

ENDPOINTS=(
    "http://${TARGET_HOST}:${PORT}/api/tours?idTour=1"
    "http://${TARGET_HOST}:${PORT}/api/destinos?idDestino=1"
    "http://${TARGET_HOST}:${PORT}/api/obtener_reservas?idUsuario=101"
)

if command -v sqlmap &>/dev/null; then
    for ep in "${ENDPOINTS[@]}"; do
        echo "Analizando: $ep"
        sqlmap -u "$ep" --level=1 --risk=1 --batch --banner >> "$LOG_FILE" 2>&1 || true
    done
    echo "[ÉXITO] Resultados guardados en $LOG_FILE"
else
    echo "[AVISO] sqlmap no instalado. Instalar con: sudo apt install -y sqlmap"
    echo "[PENDIENTE] sqlmap no instalado en este entorno." >> "$LOG_FILE"
fi
