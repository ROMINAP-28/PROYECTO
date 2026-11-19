#!/usr/bin/env bash
# Generar certificado autofirmado para pruebas locales TLS en Nginx
set -euo pipefail
DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
mkdir -p "$DIR/ssl"

openssl req -x509 -nodes -days 365 -newkey rsa:2048 \
  -keyout "$DIR/ssl/travelink.key" \
  -out "$DIR/ssl/travelink.crt" \
  -subj "/C=PE/ST=Lima/L=Lima/O=TRAVELINK/OU=DevOps/CN=localhost" \
  -addext "subjectAltName=DNS:localhost,IP:127.0.0.1"

echo "Certificados TLS generados exitosamente en $DIR/ssl/"
