#!/usr/bin/env bash
# ==========================================
# SCA Dependency-Check - TRAVELINK (Linux/macOS/WSL)
# ==========================================
set -euo pipefail

TARGET_HOST="${1:-127.0.0.1}"

# Guard de seguridad
ALLOWED_HOSTS=("localhost" "127.0.0.1" "host.docker.internal" "app" "db")
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

OUT_DIR="$PROJECT_ROOT/docs/seguridad/evidencias/dependency-check"
mkdir -p "$OUT_DIR"

echo "=========================================================="
echo " [SCA] Análisis de Dependencias - TRAVELINK"
echo "=========================================================="

echo "[1/2] Generando árbol de dependencias..."
MVN_CMD="./mvnw"
if [ ! -f "$MVN_CMD" ]; then MVN_CMD="mvn"; fi

$MVN_CMD dependency:tree "-DoutputFile=$OUT_DIR/dependency-tree.txt"

echo "[2/2] Ejecutando OWASP Dependency-Check..."
EXTRA_ARGS=()
if [ -n "${NVD_API_KEY:-}" ]; then
    echo "[INFO] Usando NVD_API_KEY del entorno."
    EXTRA_ARGS+=("-DnvdApiKey=$NVD_API_KEY")
else
    echo "[ADVERTENCIA] NVD_API_KEY no definida. La descarga puede demorar."
fi

$MVN_CMD org.owasp:dependency-check-maven:10.0.4:check \
    "-Dformat=ALL" \
    "-DoutputDirectory=$OUT_DIR" \
    "-DfailBuildOnCVSS=11" \
    "-DskipTestCompile=true" \
    "-DossindexAnalyzerEnabled=false" \
    "-DautoUpdate=false" \
    "${EXTRA_ARGS[@]}"

echo "[ÉXITO] Reportes guardados en $OUT_DIR"

