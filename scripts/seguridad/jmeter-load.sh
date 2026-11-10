#!/usr/bin/env bash
# ==========================================
# Pruebas de Carga JMeter - TRAVELINK
# ==========================================
set -euo pipefail

TARGET_HOST="${1:-127.0.0.1}"
PORT="${2:-8080}"
DOCKER_NETWORK="${3:-travelink-net}"
USER_TIERS=(100 300 500)

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
cd "$PROJECT_ROOT"

JMX_FILE="$PROJECT_ROOT/jmeter/travelink.jmx"
BASE_OUT_DIR="$PROJECT_ROOT/docs/seguridad/evidencias/jmeter"
mkdir -p "$BASE_OUT_DIR"
SUMMARY_FILE="$BASE_OUT_DIR/resumen-rendimiento.txt"

echo "=========================================================="
echo " [CARGA] Pruebas de Carga JMeter - TRAVELINK"
echo " Objetivo: http://${TARGET_HOST}:${PORT}"
echo "=========================================================="

echo "RESUMEN DE PRUEBAS DE CARGA JMETER - TRAVELINK" > "$SUMMARY_FILE"
echo "Fecha: $(date)" >> "$SUMMARY_FILE"
echo "Objetivo: http://${TARGET_HOST}:${PORT}" >> "$SUMMARY_FILE"

for USERS in "${USER_TIERS[@]}"; do
    echo "--- Ejecutando $USERS usuarios concurrentes ---"
    TIER_DIR="$BASE_OUT_DIR/tier_${USERS}_users"
    rm -rf "$TIER_DIR"
    mkdir -p "$TIER_DIR"
    JTL_FILE="$TIER_DIR/results.jtl"
    HTML_DIR="$TIER_DIR/html_report"
    RAMP_UP=$(( USERS / 20 > 5 ? USERS / 20 : 5 ))

    if command -v jmeter &>/dev/null; then
        jmeter -n -t "$JMX_FILE" \
            -Jhost="$TARGET_HOST" \
            -Jport="$PORT" \
            -Jusers="$USERS" \
            -Jrampup="$RAMP_UP" \
            -Jloops=2 \
            -l "$JTL_FILE" \
            -e -o "$HTML_DIR" || true
        echo "Completado escenario $USERS usuarios." >> "$SUMMARY_FILE"
    elif command -v docker &>/dev/null; then
        echo "[INFO] Ejecutando JMeter vía Docker justb4/jmeter en red $DOCKER_NETWORK contra http://app:8080..."
        docker run --rm \
            --network "$DOCKER_NETWORK" \
            -v "$PROJECT_ROOT/jmeter:/jmeter:ro" \
            -v "$TIER_DIR:/results:rw" \
            justb4/jmeter \
            -n -t /jmeter/travelink.jmx \
            -Jhost=app -Jport=8080 -Jusers="$USERS" -Jrampup="$RAMP_UP" -Jloops=2 \
            -l /results/results.jtl -e -o /results/html_report || true
    else
        echo "[AVISO] JMeter no instalado."
        echo "--- Nivel: $USERS Usuarios --- [PENDIENTE]" >> "$SUMMARY_FILE"
    fi
done

echo "[ÉXITO] Resultados guardados en $BASE_OUT_DIR"
