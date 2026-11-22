# Pruebas de Despliegue y Verificación Operativa — TRAVELINK 🚀

Este documento registra los resultados consolidados de la batería de pruebas de ciclo de vida Maven, construcción de contenedores Docker, pruebas de humo y validación de procedimientos automatizados de despliegue (`deploy.ps1`) y reversión (`rollback.ps1`).

---

## 1. Tabla Resumen de Ejecución

| Paso | Comando | Resultado | Fecha | Notas / Evidencia |
|:---:|---|:---:|:---:|---|
| **1** | `git rm --cached ConexionDB.class` + Limpieza segura logging SLF4J | **OK** | 2026-11-19 09:37 | Código fuente limpio, imports depurados y registro SLF4J activo. |
| **2** | `./mvnw -B clean verify` | **OK** | 2026-11-19 09:38 | `BUILD SUCCESS` (28.32 s). 23 tests ejecutados (0 fallos, 0 errores). Cobertura JaCoCo generada. |
| **3** | `docker compose up -d --build` | **OK** | 2026-11-19 09:40 | Imagen `proyecto-app:latest` construida. Contenedores `travelink-db` y `travelink-app` en estado `healthy`. |
| **4** | `curl.exe http://localhost:8080/actuator/health` | **OK** | 2026-11-19 09:40 | HTTP 200 `{"status":"UP","components":{"db":{"status":"UP"},"diskSpace":{"status":"UP"}}}` |
| **5** | `./despliegue/scripts/deploy.ps1` | **OK** | 2026-11-19 10:25 | Despliegue de producción Nginx + App + DB levantado. Smoke test completado con éxito (`HTTP 200`). |
| **6** | `curl.exe -k https://localhost/actuator/health` | **OK** | 2026-11-19 10:26 | HTTP 200 `{"status":"UP"}` validado a través del reverse proxy Nginx con TLS y cabeceras de seguridad. |
| **7** | `./despliegue/scripts/rollback.ps1` | **OK** | 2026-11-19 10:27 | Reversión exitosa a tag previo (`travelink-app:previous`), servicio restaurado y verificado `healthy`. |
| **8** | `curl.exe -k https://localhost/actuator/health` (post-rollback) | **OK** | 2026-11-19 10:27 | HTTP 200 `{"status":"UP"}` confirmado con la versión restaurada post-rollback. |

---

## 2. Evidencias de Salud y Respuestas HTTP

### A. Healthcheck de la Aplicación (`/actuator/health`)
```json
{
  "status": "UP",
  "components": {
    "db": {
      "status": "UP",
      "details": {
        "database": "MySQL",
        "validationQuery": "isValid()"
      }
    },
    "diskSpace": {
      "status": "UP",
      "details": {
        "total": 1081101176832,
        "free": 1011608875008,
        "threshold": 10485760,
        "exists": true
      }
    },
    "livenessState": {
      "status": "UP"
    },
    "ping": {
      "status": "UP"
    },
    "readinessState": {
      "status": "UP"
    }
  },
  "groups": [
    "liveness",
    "readiness"
  ]
}
```

### B. Estado de Contenedores Docker (`docker compose ps`)
```text
NAME                   IMAGE                  COMMAND                  SERVICE   STATUS                    PORTS
travelink-app-prod     travelink-app:latest   "sh -c 'java $JAVA_O…"   app       Up (healthy)              8080/tcp
travelink-db-prod      mysql:8.0              "docker-entrypoint.s…"   db        Up (healthy)              0.0.0.0:3307->3306/tcp
travelink-nginx-prod   nginx:alpine           "/docker-entrypoint.…"   nginx     Up                        0.0.0.0:80->80/tcp, 0.0.0.0:443->443/tcp
```
