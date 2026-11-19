#!/usr/bin/env bash
# ==============================================================================
# Script de Construcción y Empaquetado Maven de Producción - TRAVELINK
# ==============================================================================
set -euo pipefail

SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
PROJECT_ROOT="$(cd "${SCRIPT_DIR}/../.." && pwd)"

echo "=== [TRAVELINK] Compilación de Artefacto de Producción (JAR) ==="
cd "${PROJECT_ROOT}"

# Ejecución limpia del empaquetado omitiendo pruebas (ya validadas en CI)
./mvnw clean package -DskipTests -B

JAR_FILE=$(find target -maxdepth 1 -name "*.jar" ! -name "*.original" | head -n 1)

if [ -f "${JAR_FILE}" ]; then
    echo "[OK] Artefacto generado exitosamente: ${JAR_FILE}"
    echo "--> Calculando checksum SHA-256..."
    sha256sum "${JAR_FILE}"
else
    echo "[ERROR] No se pudo localizar el archivo JAR en target/" >&2
    exit 1
fi
