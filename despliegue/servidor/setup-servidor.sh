#!/usr/bin/env bash
# ==============================================================================
# SCRIPT DE APROVISIONAMIENTO Y HARDENING DEL SERVIDOR LINUX — TRAVELINK
# Objetivo: Preparar servidor Ubuntu 22.04 / 24.04 LTS para producción standalone
# ==============================================================================
set -euo pipefail

echo "[1/7] Actualizando repositorios del sistema..."
apt-get update && apt-get upgrade -y

echo "[2/7] Instalando dependencias base (Curl, OpenSSL, UFW, Nginx)..."
apt-get install -y curl wget gnupg2 software-properties-common ufw nginx openssl

echo "[3/7] Instalando Eclipse Temurin OpenJDK 21 LTS..."
if ! java -version 2>&1 | grep -q "21"; then
    mkdir -p /etc/apt/keyrings
    wget -O - https://packages.adoptium.net/artifactory/api/gpg/key/public | tee /etc/apt/keyrings/adoptium.asc
    echo "deb [signed-by=/etc/apt/keyrings/adoptium.asc] https://packages.adoptium.net/artifactory/deb $(lsb_release -cs) main" | tee /etc/apt/sources.list.d/adoptium.list
    apt-get update
    apt-get install -y temurin-21-jdk
fi

echo "[4/7] Creando usuario y directorios dedicados de TRAVELINK..."
if ! id "travelink" &>/dev/null; then
    groupadd -r travelink
    useradd -r -g travelink -d /opt/travelink -s /sbin/nologin -c "TRAVELINK Service User" travelink
fi

mkdir -p /opt/travelink /var/log/travelink /etc/travelink
chown -R travelink:travelink /opt/travelink /var/log/travelink
chmod 750 /opt/travelink /var/log/travelink
chmod 700 /etc/travelink

echo "[5/7] Configurando cortafuegos perimetral (UFW)..."
ufw default deny incoming
ufw default allow outgoing
ufw allow 22/tcp comment 'SSH'
ufw allow 80/tcp comment 'HTTP Nginx'
ufw allow 443/tcp comment 'HTTPS Nginx'
# Bloqueo explícito de accesos externos a Spring Boot y MySQL
ufw deny 8080/tcp comment 'Bloquear acceso directo a Tomcat'
ufw deny 3306/tcp comment 'Bloquear acceso directo a MySQL'
echo "y" | ufw enable || true

echo "[6/7] Instalando archivo de unidad systemd..."
cp ./despliegue/servidor/travelink.service /etc/systemd/system/travelink.service
systemctl daemon-reload
systemctl enable travelink.service

echo "[7/7] Verificación de instalación completada."
java -version
ufw status verbose
echo "Aprovisionamiento exitoso. Copie app.jar a /opt/travelink/ y active el servicio con: systemctl start travelink"
