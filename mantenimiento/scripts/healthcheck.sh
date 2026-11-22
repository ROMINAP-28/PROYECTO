#!/bin/bash
# ==============================================================================
# TRAVELINK - Healthcheck Actuator CLI
# ==============================================================================
set -euo pipefail

TARGET_URL="${APP_HEALTH_URL:-http://127.0.0.1:8080/actuator/health}"
TIMEOUT="${TIMEOUT_SEC:-5}"

HTTP_CODE=$(curl -s -o /tmp/health_response.json -w "%{http_code}" --max-time "$TIMEOUT" "$TARGET_URL" 2>/dev/null || echo "000")

if [ "$HTTP_CODE" -eq 200 ] && grep -q '"status":"UP"' /tmp/health_response.json 2>/dev/null; then
    echo "[OK] Healthcheck exitoso: $TARGET_URL -> HTTP $HTTP_CODE (UP)"
    rm -f /tmp/health_response.json
    exit 0
else
    echo "[CRÍTICO] Falló el healthcheck: $TARGET_URL -> HTTP $HTTP_CODE"
    cat /tmp/health_response.json 2>/dev/null || true
    echo ""
    rm -f /tmp/health_response.json
    exit 1
fi
