#!/usr/bin/env bash
# ==========================================
# Reconocimiento Nmap - TRAVELINK
# ==========================================
set -euo pipefail

TARGET_HOST="${1:-127.0.0.1}"
PORTS="${2:-8080,3307}"

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
OUT_DIR="$PROJECT_ROOT/docs/seguridad/evidencias/nmap"
mkdir -p "$OUT_DIR"
OUT_FILE="$OUT_DIR/nmap-recon.txt"

echo "=========================================================="
echo " [RECON] Escaneo Nmap - TRAVELINK ($TARGET_HOST)"
echo "=========================================================="

if command -v nmap &>/dev/null; then
    echo "[INFO] Ejecutando: nmap -sV -p $PORTS $TARGET_HOST -oN $OUT_FILE"
    nmap -sV -p "$PORTS" "$TARGET_HOST" -oN "$OUT_FILE"
    echo "[ÉXITO] Resultados guardados en $OUT_FILE"
else
    echo "[AVISO] Nmap no está instalado en este entorno."
    echo "Instalar con: sudo apt update && sudo apt install -y nmap"
    echo "[PENDIENTE] Nmap no instalado. Ejecutar: nmap -sV -p $PORTS $TARGET_HOST" > "$OUT_FILE"
fi

