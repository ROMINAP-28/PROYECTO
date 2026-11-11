#!/usr/bin/env bash
# ==============================================================================
# Script: sqlmap-test.sh
# Descripción: Pruebas de inyección SQL controladas (--level=1 --risk=1 --batch).
# ==============================================================================
set -euo pipefail

TARGET_HOST="${1:-127.0.0.1}"
PORT="${2:-8080}"
DOCKER_NETWORK="${3:-travelink-net}"

# Guard estricto
ALLOWED_HOSTS=("localhost" "127.0.0.1" "host.docker.internal" "app")
TARGET_ALLOWED=false
for h in "${ALLOWED_HOSTS[@]}"; do
    if [ "$TARGET_HOST" = "$h" ]; then
        TARGET_ALLOWED=true
        break
    fi
done

if [ "$TARGET_ALLOWED" = false ] || [[ "$TARGET_HOST" == *"aivencloud"* ]]; then
    echo "[ALERTA DE SEGURIDAD] Operación abortada: objetivo no local o remoto: '$TARGET_HOST'." >&2
    exit 1
fi

SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
PROJECT_ROOT="$(cd "$SCRIPT_DIR/../.." && pwd)"
OUT_DIR="$PROJECT_ROOT/docs/seguridad/evidencias/sqlmap"
mkdir -p "$OUT_DIR"
LOG_FILE="$OUT_DIR/sqlmap-results.txt"

{
    echo "=========================================================="
    echo " PRUEBAS CONTROLADAS DE INYECCIÓN SQL (SQLMAP) - TRAVELINK"
    echo " Fecha: $(date '+%Y-%m-%d %H:%M:%S')"
    echo " Parámetros: --level=1 --risk=1 --batch"
    echo "=========================================================="
} > "$LOG_FILE"

ENDPOINTS=(
    "/api/tours?idTour=100"
    "/api/tours?idTour=101"
    "/api/destinos?idDestino=8"
    "/api/destinos?idDestino=15"
    "/api/obtener_reservas?idUsuario=100"
    "/api/obtener_reservas?idUsuario=101"
)

if command -v sqlmap &>/dev/null; then
    for ep in "${ENDPOINTS[@]}"; do
        url="http://${TARGET_HOST}:${PORT}${ep}"
        echo "[INFO] Analizando $url..."
        sqlmap -u "$url" --level=1 --risk=1 --batch --banner >> "$LOG_FILE" 2>&1 || true
    done
elif command -v docker &>/dev/null; then
    echo "[INFO] Ejecutando sqlmap vía Docker travelink-sqlmap..."
    for ep in "${ENDPOINTS[@]}"; do
        docker_url="http://app:8080${ep}"
        docker run --rm --network "$DOCKER_NETWORK" travelink-sqlmap -u "$docker_url" --level=1 --risk=1 --batch --banner >> "$LOG_FILE" 2>&1 || true
    done
else
    echo "[AVISO] sqlmap no instalado."
    echo "[PENDIENTE] sqlmap no ejecutado." >> "$LOG_FILE"
fi

echo "[COMPLETADO] Registro guardado en $LOG_FILE"
