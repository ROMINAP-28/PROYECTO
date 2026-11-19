# Módulo de Pruebas y Evidencias — TRAVELINK 🛡️

Este directorio consolida todos los scripts de prueba, configuraciones de herramientas de seguridad, planes de carga, datos sintéticos y evidencias generadas para el sistema **TRAVELINK** (Spring Boot 3.3.13 / base auditada 3.3.4 + Java 21 + MySQL 8.0).

---

## 1. Estructura del Directorio `pruebas/`

```text
pruebas/
├── README.md                          # Este manual de pruebas
├── entorno/                           # Control del ciclo de vida del ambiente de pruebas
│   ├── reset-db.ps1 / .sh             # Reinicio de contenedores y volúmenes Docker
│   └── seed.ps1 / .sh                 # Carga de datos sintéticos (sql/seed_pruebas.sql)
├── seguridad/                         # Pruebas de seguridad automatizadas y estáticas
│   ├── scripts/                       # Scripts de escaneo (versiones PowerShell y Bash)
│   │   ├── sca-dependency-check.ps1 / .sh # Análisis de dependencias (OWASP Dependency-Check)
│   │   ├── recon-nmap.ps1 / .sh       # Reconocimiento de puertos y servicios
│   │   ├── recon-nikto.ps1 / .sh      # Escaneo de vulnerabilidades y cabeceras web
│   │   ├── zap-scan.ps1 / .sh         # DAST automatizado con OWASP ZAP Baseline
│   │   ├── fuzz-ffuf.ps1 / .sh        # Fuzzing de endpoints REST
│   │   └── sqlmap-test.ps1 / .sh      # Pruebas controladas de inyección SQL
│   ├── wordlists/
│   │   └── api-endpoints.txt          # Diccionario de 56 rutas REST evaluadas
│   ├── config/
│   │   └── zap.yaml                   # Reglas y configuración del escaneo ZAP
│   └── payloads/
│       └── calificar.json             # Payload de prueba para PoC-03 (CSRF)
├── rendimiento/                       # Pruebas de carga y estrés
│   ├── travelink.jmx                  # Plan de pruebas JMeter (100, 300 y 500 usuarios)
│   └── scripts/
│       └── jmeter-load.ps1 / .sh      # Ejecución non-GUI de JMeter en host o Docker
└── evidencias/                        # Artefactos y reportes generados
    ├── dependency-check/              # Reportes HTML, JSON, CSV y árbol de dependencias
    ├── ffuf/                          # Resultados de fuzzing (ffuf-result.json)
    ├── jmeter/                        # Resultados .jtl, resumen de métricas y reportes HTML
    ├── nikto/                         # Salida de escaneo HTTP (nikto-output.txt)
    ├── nmap/                          # Mapeo de puertos (nmap-recon.txt)
    ├── sqlmap/                        # Reporte de no-inyectabilidad (sqlmap-results.txt)
    └── zap/                           # Reportes ZAP Baseline (HTML, JSON estructurado)
```

---

## 2. Pruebas Unitarias y de Integración (`Springboot/test/`)

Las pruebas unitarias y de integración de la aplicación residen en `Springboot/test/` y se ejecutan mediante Maven Wrapper:

```bash
# Ejecutar todas las pruebas unitarias
./mvnw -B clean test

# En Windows PowerShell
.\mvnw.cmd -B clean test
```

> **Nota:** La suite de pruebas de integración con base de datos real requiere que el servicio `travelink-db` esté levantado en el puerto 3307 o se excluye temporalmente con `-Dtest='!ReservaPersistenciaIntegracionTest'`.

---

## 3. Requisitos Previos para Pruebas Dinámicas

1. **Docker y Docker Compose:** Motor local activo (`docker compose version` >= 2.0).
2. **Entorno local en ejecución:**
   ```bash
   docker compose up -d db app
   ```
3. **Herramientas opcionales en el host:**
   - Si Nmap, Nikto, ffuf o JMeter no están instalados en el PATH, los scripts ejecutan sus alternativas basadas en Docker o generan un archivo de diagnóstico controlado.

---

## 4. Cómo acceder según desde dónde ejecutes (app vs localhost)

> [!IMPORTANT]
> **Diferencia crítica entre `http://app:8080` y `http://localhost:8080`:**
> - **Desde contenedores Docker (ZAP, SQLMap, JMeter en Docker, GitHub Actions runner):** Se utiliza `http://app:8080`. El nombre `app` es el hostname del servicio dentro de la red puente Docker `travelink-net`, resoluble **únicamente** por los contenedores conectados a dicha red. Si intentas ingresar a `http://app:8080` desde el navegador web de Windows (Chrome, Edge), el DNS del sistema operativo fallará con el error `DNS_PROBE_FINISHED_NXDOMAIN`.
> - **Desde el navegador web o terminal (PowerShell / Bash) del equipo anfitrión:** Se utiliza **`http://localhost:8080`** (o `http://127.0.0.1:8080`). `docker-compose.yml` mapea y publica el puerto en la interfaz loopback (`127.0.0.1:8080:8080`) protegiendo el servicio de la red externa.
> - **Protocolo HTTP vs HTTPS:** La aplicación sirve **HTTP plano** en el puerto 8080. Si colocas `https://` en la barra de direcciones o en `curl`, la conexión fallará por rechazo de protocolo hasta que se implemente un reverse proxy Nginx con terminación TLS.

---

## 5. Orden Recomendado de Ejecución

| Orden | Fase de Prueba | Script (PowerShell / Bash) | Salida Generada |
|:---:|---|---|---|
| **0** | Inicialización | `pruebas/entorno/reset-db.ps1`<br>`pruebas/entorno/seed.ps1` | Base de datos limpia con seed sintético en `http://127.0.0.1:8080` |
| **1** | SCA (Dependencias) | `pruebas/seguridad/scripts/sca-dependency-check.ps1` | `pruebas/evidencias/dependency-check/` |
| **2** | Reconocimiento | `pruebas/seguridad/scripts/recon-nmap.ps1`<br>`pruebas/seguridad/scripts/recon-nikto.ps1` | `pruebas/evidencias/nmap/nmap-recon.txt`<br>`pruebas/evidencias/nikto/nikto-output.txt` |
| **3** | Fuzzing de API | `pruebas/seguridad/scripts/fuzz-ffuf.ps1` | `pruebas/evidencias/ffuf/ffuf-result.json` |
| **4** | DAST (ZAP) | `pruebas/seguridad/scripts/zap-scan.ps1` | `pruebas/evidencias/zap/zap-report.html` y `.json` |
| **5** | SQL Injection | `pruebas/seguridad/scripts/sqlmap-test.ps1` | `pruebas/evidencias/sqlmap/sqlmap-results.txt` |
| **6** | Rendimiento | `pruebas/rendimiento/scripts/jmeter-load.ps1` | `pruebas/evidencias/jmeter/resumen-rendimiento.txt` y carpetas `tier_*_users/` |

---

## 5. Gestión de Políticas de Versionado de Evidencias

- **Versionadas en Git:** Evidencias ligeras textuales y métricas condensadas (`nmap-recon.txt`, `nikto-output.txt`, `sqlmap-results.txt`, `ffuf-result.json`, `zap-parsed.json`, `resumen-rendimiento.txt`, `statistics.json`, `dependency-tree.txt`, `parsed-deps.json`).
- **Excluidas por `.gitignore`:** Archivos pesados binarios o generados en masa (`*.jtl`, carpetas `html_report/` completas excepto `statistics.json`, reportes HTML masivos de dependency-check y cookies temporales).

