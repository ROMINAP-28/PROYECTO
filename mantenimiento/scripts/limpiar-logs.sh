#!/bin/bash
# ==============================================================================
# TRAVELINK - Limpieza y Purga de Logs Antiguos
# ==============================================================================
set -euo pipefail

SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
LOGS_DIR="${1:-$SCRIPT_DIR/../../logs}"
RETENTION_DAYS="${2:-14}"

echo "====================================================="
echo "   TRAVELINK - PURGA Y LIMPIEZA DE LOGS HISTÓRICOS"
echo "====================================================="
echo "Directorio: $LOGS_DIR | Días de retención: $RETENTION_DAYS"

if [ ! -d "$LOGS_DIR" ]; then
    echo "[INFO] Directorio $LOGS_DIR no existe. Nada que purgar."
    exit 0
fi

# Eliminar logs rotados comprimidos que superen los días de retención
# No tocar los archivos activos travelink.log ni travelink-json.log
COUNT=$(find "$LOGS_DIR" -type f \( -name "*.log.gz" -o -name "*.log.*" -o -name "*.tmp" \) ! -name "travelink.log" ! -name "travelink-json.log" -mtime +"$RETENTION_DAYS" | wc -l)

find "$LOGS_DIR" -type f \( -name "*.log.gz" -o -name "*.log.*" -o -name "*.tmp" \) ! -name "travelink.log" ! -name "travelink-json.log" -mtime +"$RETENTION_DAYS" -delete

echo "[OK] Purga finalizada: $COUNT archivos de log eliminados."
echo "====================================================="
