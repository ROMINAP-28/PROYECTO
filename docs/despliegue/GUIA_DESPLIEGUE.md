# Guía de Despliegue y Operaciones de Infraestructura

## 1. Modos de Despliegue

TRAVELINK admite dos modalidades principales de despliegue:

```text
┌─────────────────────────────────┐      ┌─────────────────────────────────┐
│     DESPLIEGUE LOCAL (DEV)      │      │     DESPLIEGUE PRODUCTIVO       │
│                                 │      │                                 │
│  - Docker Desktop               │      │  - Servidor Linux (Ubuntu LTS)  │
│  - docker-compose.yml           │      │  - despliegue/docker/           │
│  - Publicación 127.0.0.1:8080   │      │    docker-compose.prod.yml      │
│  - Base de datos MySQL local    │      │  - Nginx Reverse Proxy (TLS)    │
│  - Script rápido: deploy.ps1/sh │      │  - Systemd Service / CI Deploy  │
└─────────────────────────────────┘      └─────────────────────────────────┘
```

---

## 2. Requisitos Previos

- **Entorno Host**: Linux (Ubuntu 22.04 LTS o superior) o Windows 10/11 con Docker Desktop habilitado.
- **Docker Engine**: Versión 24.0+ con Docker Compose v2.
- **Java Development Kit**: Eclipse Temurin OpenJDK 17 (para compilaciones Maven fuera de contenedor).
- **Puertos Requeridos**:
  - `80/tcp` (HTTP) y `443/tcp` (HTTPS) para el proxy Nginx.
  - `8080/tcp` (solo interno en red Docker para Spring Boot).
  - `3306/tcp` (solo interno en red Docker para MySQL 8.0).

---

## 3. Despliegue Local con Docker Compose

Para levantar el entorno completo de desarrollo o validación:

```bash
# 1. Copiar y configurar variables de entorno
cp .env.example .env

# 2. Compilar y levantar contenedores en segundo plano
docker compose up -d --build

# 3. Verificar estado de salud de los servicios
docker compose ps

# 4. Probar conectividad al backend
curl -i http://localhost:8080/api/destinos
```

> [!NOTE]
> `http://localhost:8080` es accesible únicamente desde el anfitrión local. Dentro de la red interna Docker `travelink-net`, los servicios se comunican mediante `http://app:8080`.

---

## 4. Despliegue Productivo Automatizado

El directorio ejecutable `despliegue/` contiene los artefactos de infraestructura necesarios:

```text
despliegue/
├── docker/
│   └── docker-compose.prod.yml   # Orquestación con réplicas, límites de memoria y reinicio always
├── servidor/
│   ├── nginx.conf                # Proxy reverso con terminación SSL/TLS, compresión gzip y headers OWASP
│   └── travelink.service         # Unidad systemd para autoarranque del stack en el sistema operativo
├── scripts/
│   ├── deploy.sh / deploy.ps1    # Script idempotente de build, test, empaquetado y arranque
│   └── rollback.sh / rollback.ps1# Script de contingencia para restaurar la versión estable previa
└── maven/
    └── build.sh / build.ps1      # Compilación determinista del fat JAR
```

### 4.1 Ejecución del Despliegue
```bash
# En sistemas Linux/macOS
bash despliegue/scripts/deploy.sh

# En sistemas Windows PowerShell
pwsh despliegue/scripts/deploy.ps1
```

---

## 5. Procedimiento de Contingencia y Rollback

Si una nueva versión presenta fallos críticos en el healthcheck (`/actuator/health` o endpoint raíz):

```bash
# Ejecutar reversión inmediata a la imagen Docker previa
bash despliegue/scripts/rollback.sh
```

El script de rollback:
1. Detiene los contenedores de la versión con fallo.
2. Restaura el tagged image `travelink-app:previous`.
3. Reinicia la red Docker y verifica que el servicio responda HTTP 200 en menos de 60 segundos.
