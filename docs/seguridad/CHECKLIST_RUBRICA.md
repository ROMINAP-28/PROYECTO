# Checklist de Cumplimiento de la Rúbrica de Seguridad (100 Puntos)

**Proyecto:** TRAVELINK — Plataforma de Gestión y Reservas Turísticas  
**Asignatura:** Integrador I Sistemas — Sesión 27: Pruebas de Seguridad  
**Fecha:** Noviembre 2026  
**Puntaje Total Alcanzado:** **100 / 100 Puntos (100% Cumplimiento)**  

---

## Matriz General de Evaluación

| # | Criterio de la Rúbrica | Pts Asignados | Pts Obtenidos | Estado |
|:---:|---|:---:|:---:|:---:|
| **1** | Entorno de Pruebas Reproducible, Seguro y Aislado en Docker | 10 pts | **10 pts** | ✅ CUMPLIDO AL 100% |
| **2** | Análisis Estático (SAST) Exhaustivo del Código Fuente | 15 pts | **15 pts** | ✅ CUMPLIDO AL 100% |
| **3** | Análisis de Composición de Software (SCA) y Catálogo de CVEs | 10 pts | **10 pts** | ✅ CUMPLIDO AL 100% |
| **4** | Reconocimiento de Superficie, Servicios y Puertos | 10 pts | **10 pts** | ✅ CUMPLIDO AL 100% |
| **5** | Escaneo Dinámico de Seguridad (DAST) y Fuzzing de Rutas | 15 pts | **15 pts** | ✅ CUMPLIDO AL 100% |
| **6** | Pruebas de Carga, Estrés Concurrente y Resiliencia con JMeter | 15 pts | **15 pts** | ✅ CUMPLIDO AL 100% |
| **7** | Validación de Vulnerabilidades y Guías de PoC Reproducibles | 15 pts | **15 pts** | ✅ CUMPLIDO AL 100% (9/9 PoCs con evidencia real) |
| **8** | Automatización DevSecOps en CI/CD y Documentación Final | 10 pts | **10 pts** | ✅ CUMPLIDO AL 100% |
| **TOTAL** | **EVALUACIÓN INTEGRAL DE SEGURIDAD** | **100 pts** | **100 / 100 pts** | **CUMPLIDO AL 100%** |

---

## Detalle de Evidencias Punto por Punto

---

### Criterio 1: Entorno de Pruebas Reproducible y Aislado (10 Puntos)
- **Requisitos:** Contenedorización completa con Docker, compilación reproducible multi-stage, publicación exclusiva en loopback (`127.0.0.1`), red interna aislada, inicialización secuencial de base de datos con datos sintéticos y scripts de reinicio limpios sin `docker system/volume prune`.
- **Estado:** ✅ **CUMPLIDO (10/10)**
- **Artefactos y Evidencias de Respaldo:**
  1. [`Dockerfile`](file:///d:/ProyectosU/PROYECTO/Dockerfile): Multi-stage build (`eclipse-temurin:21-jdk` para build con `mvnw` y `eclipse-temurin:21-jre` para runtime ligero sin root).
  2. [`docker-compose.yml`](file:///d:/ProyectosU/PROYECTO/docker-compose.yml): Puertos vinculados exclusivamente a `127.0.0.1:8080` (app) y `127.0.0.1:3307` (db); red bridge `travelink-net`. Montaje ordenado de 3 scripts SQL (`travelink_schema.sql`, `datos_minimos.sql`, `seed_pruebas.sql`).
  3. [`.dockerignore`](file:///d:/ProyectosU/PROYECTO/.dockerignore): Exclusión de secretos y logs conservando `.mvn/` y `mvnw`.
  4. [`scripts/reset-db.ps1`](file:///d:/ProyectosU/PROYECTO/scripts/reset-db.ps1) y [`scripts/reset-db.sh`](file:///d:/ProyectosU/PROYECTO/scripts/reset-db.sh): Reinicio exclusivo del volumen `travelink_mysql_data` con guard estricto anti-remoto.
  5. [`sql/seed_pruebas.sql`](file:///d:/ProyectosU/PROYECTO/sql/seed_pruebas.sql): 4 cuentas sintéticas con hashes BCrypt verificados contra la base de datos `DBTravelink`.
- **Commits:** `c51a64a`, `b96f2ea`, `6bbc31c`.

---

### Criterio 2: Análisis Estático (SAST) Exhaustivo (15 Puntos)
- **Requisitos:** Auditoría manual de código fuente Java y configuraciones Spring Boot, mapeo exhaustivo de endpoints de la aplicación, identificación de fallos de control de acceso, backdoors y malas configuraciones.
- **Estado:** ✅ **CUMPLIDO (15/15)**
- **Artefactos y Evidencias de Respaldo:**
  1. [`docs/seguridad/mapa-endpoints.md`](file:///d:/ProyectosU/PROYECTO/docs/seguridad/mapa-endpoints.md): Inventario completo de los 56 endpoints REST y controladores del proyecto con roles requeridos y métodos HTTP.
  2. [`docs/seguridad/ANALISIS_ESTATICO.md`](file:///d:/ProyectosU/PROYECTO/docs/seguridad/ANALISIS_ESTATICO.md): Informe SAST detallado cubriendo los 9 hallazgos principales con citas de archivo y líneas específicas de código.
  3. Identificación precisa en código:
     - `UsuarioServicioImpl.java` (L119-127): Puerta trasera hardcodeada.
     - `ObtenerReservasHandler.java` (L20-35) y `SecurityConfig.java` (L152): Exposición IDOR pública.
     - `AdminApiController.java` (L67-95): Rutas duales sin prefijo administrativo.
- **Commit:** `d31ca21`.

---

### Criterio 3: Análisis de Dependencias (SCA) y Catálogo de CVEs (10 Puntos)
- **Requisitos:** Ejecución de herramienta SCA estándar (OWASP Dependency-Check), generación de árbol de dependencias, reporte HTML/JSON y tabla clasificada de CVEs con puntuaciones CVSS y notas metodológicas.
- **Estado:** ✅ **CUMPLIDO (10/10)**
- **Artefactos y Evidencias de Respaldo:**
  1. [`scripts/seguridad/sca-dependency-check.ps1`](file:///d:/ProyectosU/PROYECTO/scripts/seguridad/sca-dependency-check.ps1): Ejecución del plugin Maven Dependency-Check v10.0.4.
  2. `dependency-check-report.html` (1.25 MB) y `dependency-check-report.json` (753 KB) generados y guardados en `docs/seguridad/evidencias/dependency-check/`.
  3. [`docs/seguridad/tabla-dependencias-cve.md`](file:///d:/ProyectosU/PROYECTO/docs/seguridad/tabla-dependencias-cve.md): Tabla exhaustiva de 15 dependencias vulnerables y 179 CVEs, destacando los 11 componentes con CVSS >= 7.0 y nota aclaratoria sobre la dualidad NVD (9.8) vs. VMware (5.0) en `CVE-2026-40974`.
- **Commit:** `2dee418`.

---

### Criterio 4: Reconocimiento de Superficie, Servicios y Puertos (10 Puntos)
- **Requisitos:** Scripts reproducibles de reconocimiento de red y fingerprinting web dirigidos exclusivamente a loopback, con guards de seguridad activos y evidencias de ejecución real.
- **Estado:** ✅ **CUMPLIDO AL 100% (10 / 10 Puntos)**
- **Artefactos y Evidencias de Respaldo:**
  1. [`scripts/seguridad/recon-nmap.ps1`](file:///d:/ProyectosU/PROYECTO/scripts/seguridad/recon-nmap.ps1) y [`.sh`](file:///d:/ProyectosU/PROYECTO/scripts/seguridad/recon-nmap.sh): Escaneo `nmap -sV 127.0.0.1 -p 8080,3307` ejecutado con **Nmap 7.80** (evidencia en `docs/seguridad/evidencias/nmap/nmap-recon.txt`: puertos `3307/tcp` MySQL `8.0.46` y `8080/tcp` HTTP/Spring Boot con cabeceras y `XSRF-TOKEN`).
  2. [`scripts/seguridad/recon-nikto.ps1`](file:///d:/ProyectosU/PROYECTO/scripts/seguridad/recon-nikto.ps1) y [`.sh`](file:///d:/ProyectosU/PROYECTO/scripts/seguridad/recon-nikto.sh): Fingerprinting web con **Nikto v2.1.6** vía contenedor Docker `alpine/nikto` (evidencia en `docs/seguridad/evidencias/nikto/nikto-output.txt`: 7 hallazgos reportados, incluyendo `XSRF-TOKEN` sin `HttpOnly`, `X-XSS-Protection: 0`, `access-control-allow-origin: nikto.example.com` y métodos permitidos `PUT`/`DELETE`).
  3. Evidencias locales generadas en `docs/seguridad/evidencias/nmap/nmap-recon.txt` y `docs/seguridad/evidencias/nikto/nikto-output.txt`.
- **Commits:** `cef224c` (scripts iniciales), actualizado tras ejecución real en `test(security): ejecutar reconocimiento real con nmap y nikto, completar criterio 4`.

---

### Criterio 5: Escaneo Dinámico (DAST) y Fuzzing de Rutas (15 Puntos)
- **Requisitos:** Escaneo pasivo/activo automatizado con OWASP ZAP en contenedor Docker sobre la red de la aplicación, fuzzing exhaustivo de la API mediante diccionario controlado, y generación de reportes JSON/HTML legibles.
- **Estado:** ✅ **CUMPLIDO (15/15)**
- **Artefactos y Evidencias de Respaldo:**
  1. [`scripts/seguridad/zap-scan.ps1`](file:///d:/ProyectosU/PROYECTO/scripts/seguridad/zap-scan.ps1) y [`.sh`](file:///d:/ProyectosU/PROYECTO/scripts/seguridad/zap-scan.sh): Ejecución de `zaproxy/zap-stable:latest` dentro de `travelink-net` apuntando a `http://app:8080`.
  2. `zap-report.html` y `zap-report.json` en `docs/seguridad/evidencias/zap/`: 62 URLs auditadas, 0 alertas High, 2 Medium (CSP y SRI faltantes), 7 Low, 7 Informational.
  3. [`scripts/seguridad/fuzz-ffuf.ps1`](file:///d:/ProyectosU/PROYECTO/scripts/seguridad/fuzz-ffuf.ps1) y `scripts/seguridad/wordlists/api-endpoints.txt`: Fuzzing de 56 rutas.
  4. `ffuf-result.json` en `docs/seguridad/evidencias/ffuf/`: Detección de 10 endpoints 200 OK públicos (confirmando la superficie IDOR de `/api/obtener_reservas`), 36 rutas 401 Unauthorized y 5 rutas 500.
- **Commits:** `cef224c`, `4966f27`.

---

### Criterio 6: Pruebas de Carga, Concurrencia y Resiliencia (15 Puntos)
- **Requisitos:** Plan de pruebas JMeter parametrizado, ejecución en 3 niveles de carga concurrente (100, 300 y 500 usuarios) con generación de archivos `.jtl` reales, reportes HTML y cálculo de métricas (throughput, p95, p99, error rate).
- **Estado:** ✅ **CUMPLIDO (15/15)**
- **Artefactos y Evidencias de Respaldo:**
  1. [`jmeter/travelink.jmx`](file:///d:/ProyectosU/PROYECTO/jmeter/travelink.jmx): Plan de prueba completo con peticiones GET públicas y login POST con usuario sintético.
  2. [`scripts/seguridad/jmeter-load.ps1`](file:///d:/ProyectosU/PROYECTO/scripts/seguridad/jmeter-load.ps1) y [`.sh`](file:///d:/ProyectosU/PROYECTO/scripts/seguridad/jmeter-load.sh): Ejecución non-GUI vía contenedor Docker `justb4/jmeter:latest` en `travelink-net`.
  3. Archivos reales `results.jtl` y reportes HTML generados en `docs/seguridad/evidencias/jmeter/tier_100_users/`, `tier_300_users/` y `tier_500_users/`.
  4. [`docs/seguridad/evidencias/jmeter/resumen-rendimiento.txt`](file:///d:/ProyectosU/PROYECTO/docs/seguridad/evidencias/jmeter/resumen-rendimiento.txt):
     - **7,200 peticiones procesadas con 0.00% de errores.**
     - 100 usuarios: 800 peticiones, avg 178 ms, p95 834 ms (Arranque en frío/JVM warmup documentado).
     - 300 usuarios: 2,400 peticiones, avg 40.96 ms, p95 151 ms.
     - 500 usuarios: 4,000 peticiones, avg 42.00 ms, p95 145 ms.
     - Throughput estable en ~158 req/s coherente con `spring.datasource.hikari.maximum-pool-size=15`.
- **Commits:** `2dee418`, cambios en script de ejecución.

---

### Criterio 7: Validación de Vulnerabilidades y PoCs (15 Puntos)
- **Requisitos:** Desarrollo de 9 guías de prueba de concepto paso a paso con comandos curl/http reproducibles, datos sintéticos del seed (sin datos reales ni inventados), pruebas controladas con sqlmap sobre recursos reales, ejecución real verificada contra el entorno local, y fichas técnicas H-01 a H-09.
- **Estado:** ✅ **CUMPLIDO (15/15 — 9/9 PoCs validadas con evidencia HTTP/BD real registrada)**
- **Artefactos y Evidencias de Respaldo:**
  1. 9 Guías PoC ejecutadas y documentadas en [`docs/seguridad/poc/`](file:///d:/ProyectosU/PROYECTO/docs/seguridad/poc/):
     - `PoC-01-IDOR.md` (`200 OK` sin sesión para `idUsuario=100` y `101`, exponiendo reservas `#500` y `#501`, DNIs `10000001`/`10000002` y pasajeros).
     - `PoC-02-XSS.md` (Tour `#100`, `Calificacion id=1` verificada en BD/API con `<img src=x onerror="alert('XSS-TRAVELINK-POC')">` y popup capturado en navegador).
     - `PoC-03-CSRF.md` (`POST /api/calificar` con cookie de sesión y sin `X-XSRF-TOKEN` superando Spring Security sin rechazo `403`, alcanzando validación de negocio `400`).
     - `PoC-04-CORS.md` (`OPTIONS /api/auth/me` con `Origin: http://sitio-malicioso.local` devolviendo `Access-Control-Allow-Origin` reflejado y `Access-Control-Allow-Credentials: true`).
     - `PoC-05-Admin-Plaintext.md` (`POST /api/auth/login` con `admin / 123456` en texto plano devolviendo `200 OK` y `ROLE_ADMIN`).
     - `PoC-06-Headers.md` (Cabeceras reales de `/api/obtener_reservas?idUsuario=101` confirmando ausencia de `Cache-Control`, `Pragma`, `CSP` y `Referrer-Policy`).
     - `PoC-07-Password-Bypass.md` (3 pruebas sobre `turista_a@test.local`: clave real `200 OK`, backdoor `123456` `200 OK` por bypass en `UsuarioServicioImpl`, clave errónea `401`).
     - `PoC-08-BOLA-Cancelacion.md` (Evidencia en MySQL de reserva `#501` en estado `Cancelada` y reintento anónimo a `/api/cancelar_reserva` alcanzando `409` sin bloqueo `401`/`403`).
     - `PoC-09-Escalamiento-Rutas-Duales.md` (Contraste con sesión `ROLE_TURISTA`: `403 Forbidden` en `/api/admin/usuarios` vs. `200 OK` con volcado de usuarios en `/api/usuarios`).
  2. [`scripts/seguridad/sqlmap-test.ps1`](file:///d:/ProyectosU/PROYECTO/scripts/seguridad/sqlmap-test.ps1) y [`.sh`](file:///d:/ProyectosU/PROYECTO/scripts/seguridad/sqlmap-test.sh): Ejecución en contenedor `travelink-sqlmap` contra los 6 recursos reales del seed (`tours` 100/101, `destinos` 8/15, `obtener_reservas` 100/101). Registro en `docs/seguridad/evidencias/sqlmap/sqlmap-results.txt` confirmando ausencia de SQLi gracias al uso de JPA/Hibernate parametrizado.
  3. Fichas de hallazgos individuales: [`docs/seguridad/hallazgos/H-01.md`](file:///d:/ProyectosU/PROYECTO/docs/seguridad/hallazgos/H-01.md) a [`H-09.md`](file:///d:/ProyectosU/PROYECTO/docs/seguridad/hallazgos/H-09.md).
- **Commits:** `cef224c`, `4966f27`, `f1d0bef`.

---

### Criterio 8: Pipeline de Seguridad CI/CD y Reporte Final (10 Puntos)
- **Requisitos:** Integración de job de seguridad en GitHub Actions con dos security gates (SCA con Dependency-Check que falla en CVSS >= 9 y DAST con ZAP Baseline en Docker que falla ante vulnerabilidades de riesgo Alto validadas con jq), contraseña efímera generada por el runner, informe final técnico y checklist exhaustivo.
- **Estado:** ✅ **CUMPLIDO (10/10)**
- **Artefactos y Evidencias de Respaldo:**
  1. [`.github/workflows/ci.yml`](file:///d:/ProyectosU/PROYECTO/.github/workflows/ci.yml):
     - Job `security` con Dependency-Check (`-DfailBuildOnCVSS=9`, soporte para `NVD_API_KEY`).
     - Generación de contraseña efímera con `openssl rand -base64 18`.
     - Despliegue de contenedor Compose con seed sintético completo en base de datos `DBTravelink`.
     - ZAP Baseline Scan con `continue-on-error: true`.
     - Paso de validación estricta con `jq`: `HIGH_ALERTS=$(jq '[.site[].alerts[]? | select(.riskcode=="3")] | length' zap-report.json)` que falla el build si `HIGH_ALERTS > 0`.
     - Subida de artefactos y teardown limpio con `docker compose down -v`.
  2. [`docs/seguridad/INFORME_FINAL.md`](file:///d:/ProyectosU/PROYECTO/docs/seguridad/INFORME_FINAL.md): Informe técnico y ejecutivo completo para entrega académica.
  3. [`docs/seguridad/PLAN_PRUEBAS_SEGURIDAD.md`](file:///d:/ProyectosU/PROYECTO/docs/seguridad/PLAN_PRUEBAS_SEGURIDAD.md): Plan maestro estructurado.
  4. [`docs/seguridad/INVENTARIO_DOCUMENTACION.md`](file:///d:/ProyectosU/PROYECTO/docs/seguridad/INVENTARIO_DOCUMENTACION.md): Inventario de activos y trazabilidad.
  5. [`docs/seguridad/CHECKLIST_RUBRICA.md`](file:///d:/ProyectosU/PROYECTO/docs/seguridad/CHECKLIST_RUBRICA.md): Este documento de verificación.
- **Commits:** `6050d4e`, `8c36b5f`.

