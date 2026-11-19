# Checklist de Cumplimiento de la Rúbrica Oficial de Seguridad (100 Puntos + 5 Bonus)

**Proyecto:** TRAVELINK — Plataforma de Gestión y Reservas Turísticas  
**Asignatura:** Integrador I Sistemas — Auditoría y Pruebas de Seguridad  
**Fecha:** Noviembre 2026  
**Puntaje Total Evaluado:** **100 / 100 Puntos + 5 Puntos Bonus (Total: 105 Pts)**  

---

## 1. Matriz General de Evaluación

| # | Criterio de la Rúbrica Oficial | Pts Máx. | Pts Obtenidos | Estado de Cumplimiento |
|:---:|---|:---:|:---:|:---:|
| **1** | **Preparación y Entorno Reproducible** | 10 pts | **10 pts** | ✅ CUMPLIDO AL 100% |
| **2** | **Reconocimiento y Análisis de Composición de Software (SCA)** | 15 pts | **15 pts** | ✅ CUMPLIDO AL 100% |
| **3** | **Escaneo Automático de Vulnerabilidades y Validación** | 15 pts | **15 pts** | ✅ CUMPLIDO AL 100% |
| **4** | **Explotación y Evidencia Técnica (PoCs)** | 20 pts | **20 pts** | ✅ CUMPLIDO AL 100% |
| **5** | **Fuzzing y Resiliencia de la API** | 10 pts | **10 pts** | ✅ CUMPLIDO AL 100% |
| **6** | **Pruebas de Carga y Rendimiento** | 10 pts | **10 pts** | ✅ CUMPLIDO AL 100% |
| **7** | **Integración Continua (CI/CD) y Automatización** | 10 pts | **10 pts** | ✅ CUMPLIDO AL 100% |
| **8** | **Calidad del Reporte y Presentación Ejecutiva** | 10 pts | **10 pts** | ✅ CUMPLIDO AL 100% |
| **BONUS** | **Pruebas de Carga Avanzadas con Apache JMeter** | +5 pts | **+5 pts** | ⭐ CUMPLIDO (3 escenarios, p95/p99, pool DB) |
| **TOTAL** | **CALIFICACIÓN INTEGRAL DE AUDITORÍA** | **100 pts** | **105 / 100 pts** | **CUMPLIMIENTO EXCELENTE CON BONIFICACIÓN** |

---

## 2. Detalle y Justificación Técnica por Criterio

---

### Criterio 1: Preparación y Entorno Reproducible (10 Puntos)
- **Requisitos Oficiales:**
  Contenedorización completa con Docker y Docker Compose, compilación multi-stage, aislamiento en loopback (`127.0.0.1`), red interna controlada, base de datos relacional con carga inicial ordenada de esquema y datos sintéticos representativos, y scripts reproducibles de reinicio y siembra sin comandos destructivos globales (`docker system prune`).
- **Puntaje Obtenido:** **10 / 10 Puntos**
- **Justificación Técnica:**
  El entorno se levanta con un solo comando (`docker compose up -d`). La aplicación se compila mediante un `Dockerfile` multi-stage (builder con Maven y Eclipse Temurin 21; runtime JRE jammy ligero y sin privilegios de root). La base de datos MySQL 8.0 se inicializa secuencialmente mediante montajes en `/docker-entrypoint-initdb.d/` (`01-travelink_schema.sql`, `02-datos_minimos.sql`, `03-seed_pruebas.sql`). Se implementaron guards estrictos en los scripts de entorno que impiden ejecutar acciones destructivas si la variable `DB_URL` apunta a servicios remotos (e.g. Aiven Cloud).
- **Evidencias y Enlaces:**
  - Definición de build multi-stage: [`Dockerfile`](../../Dockerfile)
  - Orquestación de servicios en red privada `travelink-net`: [`docker-compose.yml`](../../docker-compose.yml)
  - Scripts de reinicio controlado con guard local: [`pruebas/entorno/reset-db.ps1`](../../pruebas/entorno/reset-db.ps1) y [`pruebas/entorno/reset-db.sh`](../../pruebas/entorno/reset-db.sh)
  - Scripts de siembra sintética: [`pruebas/entorno/seed.ps1`](../../pruebas/entorno/seed.ps1) y [`pruebas/entorno/seed.sh`](../../pruebas/entorno/seed.sh)
  - Dataset sintético de prueba: [`sql/seed_pruebas.sql`](../../sql/seed_pruebas.sql) y catálogo de credenciales en [`docs/seguridad/05-informe/CUENTAS_PRUEBA.md`](CUENTAS_PRUEBA.md)

---

### Criterio 2: Reconocimiento y Análisis de Composición de Software (SCA) (15 Puntos)
- **Requisitos Oficiales:**
  Reconocimiento pasivo/activo de red y servicios en puertos expuestos, fingerprinting web HTTP exhaustivo, análisis formal de dependencias de terceros (SCA) identificando CVEs, puntajes CVSS y versiones parchadas recomendadas.
- **Puntaje Obtenido:** **15 / 15 Puntos**
- **Justificación Técnica:**
  Se ejecutó reconocimiento de servicios mediante Nmap 7.80 sobre los puertos `8080/tcp` (Spring Boot) y `3307/tcp` (MySQL 8.0.46), capturando cabeceras y banner de cookies. Se realizó fingerprinting HTTP con Nikto v2.1.6 detectando 7 condiciones de configuración insegura (métodos HTTP habilitados, ausencia de cabeceras defensivas y atributos de cookies). A nivel SCA, se ejecutó OWASP Dependency-Check v10.0.4 analizando 84 dependencias Maven sobre la línea base original de Spring Boot 3.3.4 (Tomcat 10.1.30, Spring Security 6.3.3), catalogando 179 CVEs en 15 componentes vulnerables, mitigados posteriormente mediante la actualización a Spring Boot 3.3.13 y excepciones justificadas en Trivy.
- **Evidencias y Enlaces:**
  - Evidencia de escaneo de puertos: [`pruebas/evidencias/nmap/nmap-recon.txt`](../../pruebas/evidencias/nmap/nmap-recon.txt)
  - Script de reconocimiento Nmap: [`pruebas/seguridad/scripts/recon-nmap.ps1`](../../pruebas/seguridad/scripts/recon-nmap.ps1) / [`.sh`](../../pruebas/seguridad/scripts/recon-nmap.sh)
  - Evidencia de fingerprinting web: [`pruebas/evidencias/nikto/nikto-output.txt`](../../pruebas/evidencias/nikto/nikto-output.txt)
  - Script de fingerprinting Nikto: [`pruebas/seguridad/scripts/recon-nikto.ps1`](../../pruebas/seguridad/scripts/recon-nikto.ps1) / [`.sh`](../../pruebas/seguridad/scripts/recon-nikto.sh)
  - Árbol de dependencias y catálogo estructurado: [`pruebas/evidencias/dependency-check/dependency-tree.txt`](../../pruebas/evidencias/dependency-check/dependency-tree.txt) y [`parsed-deps.json`](../../pruebas/evidencias/dependency-check/parsed-deps.json)
  - Tabla técnica exhaustiva de dependencias y CVEs: [`docs/seguridad/02-analisis/tabla-dependencias-cve.md`](../02-analisis/tabla-dependencias-cve.md)
  - Script SCA automatizado: [`pruebas/seguridad/scripts/sca-dependency-check.ps1`](../../pruebas/seguridad/scripts/sca-dependency-check.ps1) / [`.sh`](../../pruebas/seguridad/scripts/sca-dependency-check.sh)

---

### Criterio 3: Escaneo Automático de Vulnerabilidades y Validación (15 Puntos)
- **Requisitos Oficiales:**
  Auditoría dinámica DAST mediante escáner web estándar (OWASP ZAP), auditoría estática de código (SAST) y arquitectura, mapeo de endpoints y discriminación explícita entre verdaderos positivos, falsos positivos y hallazgos informativos con verificación manual.
- **Puntaje Obtenido:** **15 / 15 Puntos**
- **Justificación Técnica:**
  Se integró y ejecutó OWASP ZAP Baseline Scan en contenedor Docker dentro de `travelink-net`, auditando 62 rutas únicas. Se extrajo y parseó el reporte completo (`zap-parsed.json`), identificando 16 tipos de alertas (0 High, 2 Medium, 7 Low, 7 Informational). Se complementó con un análisis SAST exhaustivo y el mapeo de 56 endpoints REST. Se documentó un archivo formal de discriminación de falsos positivos contrastando alertas automatizadas contra el código fuente y validando manualmente con peticiones HTTP reales.
- **Evidencias y Enlaces:**
  - Configuración y script ZAP DAST: [`pruebas/seguridad/config/zap.yaml`](../../pruebas/seguridad/config/zap.yaml) y [`pruebas/seguridad/scripts/zap-scan.ps1`](../../pruebas/seguridad/scripts/zap-scan.ps1) / [`.sh`](../../pruebas/seguridad/scripts/zap-scan.sh)
  - Alertas ZAP estructuradas: [`pruebas/evidencias/zap/zap-parsed.json`](../../pruebas/evidencias/zap/zap-parsed.json)
  - Mapeo de superficie de ataque: [`docs/seguridad/02-analisis/mapa-endpoints.md`](../02-analisis/mapa-endpoints.md)
  - Informe de análisis estático SAST: [`docs/seguridad/02-analisis/ANALISIS_ESTATICO.md`](../02-analisis/ANALISIS_ESTATICO.md)
  - Validación formal de falsos y verdaderos positivos: [`docs/seguridad/02-analisis/VALIDACION_FALSOS_POSITIVOS.md`](../02-analisis/VALIDACION_FALSOS_POSITIVOS.md)

---

### Criterio 4: Explotación y Evidencia Técnica (PoCs) (20 Puntos)
- **Requisitos Oficiales:**
  Demostración práctica de vulnerabilidades mediante Pruebas de Concepto (PoC) reproducibles, autónomas y basadas en peticiones HTTP documentadas (curl, Burp Suite), con validación directa en base de datos o interfaz de usuario, y evaluación controlada de inyección SQL con SQLMap.
- **Puntaje Obtenido:** **20 / 20 Puntos**
- **Justificación Técnica:**
  Se desarrollaron, probaron y documentaron 9 Guías PoC individuales con vectores no destructivos sobre datos sintéticos del seed:
  1. **PoC-01 (IDOR):** Extracción de reservas ajenas (#500/#501), DNIs y datos de pasajeros sin sesión en `/api/obtener_reservas`.
  2. **PoC-02 (XSS Almacenado):** Inyección de payload en reseña de tour 100, verificado en BD MySQL y detonado visualmente en el navegador.
  3. **PoC-03 (CSRF):** Mutación transaccional en `/api/calificar` omitiendo el token anti-falsificación por exención en Spring Security.
  4. **PoC-04 (CORS Permisivo):** Petición preflight `OPTIONS` con origen arbitrario reflejando cabeceras de autorización y credenciales.
  5. **PoC-05 (Admin Plaintext):** Autenticación administrativa exitosa con credencial por defecto del seed (`admin:123456`).
  6. **PoC-06 (Cabeceras Defensivas):** Verificación de ausencia de `Cache-Control`, `Pragma`, `CSP` y `Referrer-Policy`.
  7. **PoC-07 (Password Bypass):** Demostración de backdoor en código Java que acepta `123456` para cualquier usuario ignorando BCrypt.
  8. **PoC-08 (BOLA en Cancelación):** Cancelación no autorizada de reserva ajena mediante petición anónima sin validar propiedad de sesión.
  9. **PoC-09 (Escalamiento Dual):** Acceso a datos de administración vía endpoints alternos sin prefijo protegido (`/api/usuarios`).
  Adicionalmente, se ejecutó SQLMap en modo controlado (`--level=1 --risk=1 --batch`) demostrando la robustez frente a SQLi gracias al uso de JPA/Hibernate parametrizado.
- **Evidencias y Enlaces:**
  - 9 Guías PoC individuales: [`docs/seguridad/04-poc/`](../04-poc/)
  - 9 Fichas técnicas de hallazgos: [`docs/seguridad/03-hallazgos/`](../03-hallazgos/)
  - Reporte de auditoría SQLMap: [`pruebas/evidencias/sqlmap/sqlmap-results.txt`](../../pruebas/evidencias/sqlmap/sqlmap-results.txt)
  - Script SQLMap reproducible: [`pruebas/seguridad/scripts/sqlmap-test.ps1`](../../pruebas/seguridad/scripts/sqlmap-test.ps1) / [`.sh`](../../pruebas/seguridad/scripts/sqlmap-test.sh)

---

### Criterio 5: Fuzzing y Resiliencia de la API (10 Puntos)
- **Requisitos Oficiales:**
  Fuzzing sistemático sobre endpoints de la API mediante herramientas automatizadas (FFUF u homólogo), diccionarios contextuales de rutas, detección de endpoints ocultos y análisis detallado de fallos del servidor (HTTP 500) o caídas de conexión.
- **Puntaje Obtenido:** **10 / 10 Puntos**
- **Justificación Técnica:**
  Se ejecutó fuzzing de 56 rutas REST con FFUF utilizando el diccionario especializado `api-endpoints.txt`. Se generó el reporte `ffuf-result.json`, identificando 10 rutas públicas `200 OK`, 36 rutas protegidas `401 Unauthorized`, 4 rutas con método denegado `405 Method Not Allowed`, 5 anomalías críticas `HTTP 500 Internal Server Error` (fallos por `NullPointerException` en endpoints de autenticación y transacciones sin cuerpo esperado) y 1 ruta con `Status 0` (reset de conexión TCP por desconexión no controlada).
- **Evidencias y Enlaces:**
  - Diccionario de endpoints auditados: [`pruebas/seguridad/wordlists/api-endpoints.txt`](../../pruebas/seguridad/wordlists/api-endpoints.txt)
  - Evidencia JSON del escaneo FFUF: [`pruebas/evidencias/ffuf/ffuf-result.json`](../../pruebas/evidencias/ffuf/ffuf-result.json)
  - Script de ejecución de fuzzing: [`pruebas/seguridad/scripts/fuzz-ffuf.ps1`](../../pruebas/seguridad/scripts/fuzz-ffuf.ps1) / [`.sh`](../../pruebas/seguridad/scripts/fuzz-ffuf.sh)
  - Análisis técnico de anomalías 500 y status 0: [`docs/seguridad/02-analisis/ANALISIS_ESTATICO.md`](../02-analisis/ANALISIS_ESTATICO.md#análisis-de-resiliencia-y-fuzzing)

---

### Criterio 6: Pruebas de Carga y Rendimiento (10 Puntos)
- **Requisitos Oficiales:**
  Plan de pruebas de estrés y rendimiento concurrentes en modo non-GUI, definición de escenarios de carga progresiva, registro de archivos `.jtl`, cálculo de métricas clave (tasa de error, tiempo promedio, percentiles p95 y p99) y análisis de cuellos de botella arquitectónicos.
- **Puntaje Obtenido:** **10 / 10 Puntos**
- **Justificación Técnica:**
  Se diseñó el plan de pruebas parametrizado `travelink.jmx` simulando tráfico combinado de navegación turística y autenticación. Se ejecutaron pruebas non-GUI en tres escalones concurrentes (100, 300 y 500 hilos), totalizando **7,200 peticiones HTTP con 0.00% de tasa de fallo**. Se identificó y documentó el comportamiento de calentamiento de la JVM (warm-up: 178 ms inicial vs. 41 ms estabilizado) y la saturación controlada del pool de conexiones HikariCP a ~158 req/s impuesta por `maximum-pool-size=15`.
- **Evidencias y Enlaces:**
  - Plan de carga parametrizado: [`pruebas/rendimiento/travelink.jmx`](../../pruebas/rendimiento/travelink.jmx)
  - Script automatizado de ejecución: [`pruebas/rendimiento/scripts/jmeter-load.ps1`](../../pruebas/rendimiento/scripts/jmeter-load.ps1) / [`.sh`](../../pruebas/rendimiento/scripts/jmeter-load.sh)
  - Resumen ejecutivo de métricas: [`pruebas/evidencias/jmeter/resumen-rendimiento.txt`](../../pruebas/evidencias/jmeter/resumen-rendimiento.txt)
  - Estadísticas consolidadas por escenario:
    - 100 usuarios: [`pruebas/evidencias/jmeter/tier_100_users/html_report/statistics.json`](../../pruebas/evidencias/jmeter/tier_100_users/html_report/statistics.json)
    - 300 usuarios: [`pruebas/evidencias/jmeter/tier_300_users/html_report/statistics.json`](../../pruebas/evidencias/jmeter/tier_300_users/html_report/statistics.json)
    - 500 usuarios: [`pruebas/evidencias/jmeter/tier_500_users/html_report/statistics.json`](../../pruebas/evidencias/jmeter/tier_500_users/html_report/statistics.json)

---

### Criterio 7: Integración Continua (CI/CD) y Automatización (10 Puntos)
- **Requisitos Oficiales:**
  Configuración de pipeline DevSecOps automatizado en GitHub Actions o GitLab CI, incorporación de Security Gates automáticos en escaneo SCA y DAST, manejo seguro de secretos sin credenciales fijas y generación de reportes automáticos como artefactos de CI.
- **Puntaje Obtenido:** **10 / 10 Puntos**
- **Justificación Técnica:**
  Se configuró el workflow `.github/workflows/ci.yml` con un job especializado `security` que opera secuencialmente:
  1. Genera contraseñas efímeras criptográficas con `openssl rand -base64 18` para aislar el build.
  2. Despliega el contenedor Docker de MySQL y siembra el esquema sintético `DBTravelink`.
  3. Ejecuta análisis SCA mediante OWASP Dependency-Check y escaneo DAST pasivo con OWASP ZAP Baseline.
  4. Implementa un Security Gate automatizado que evalúa los reportes mediante `jq` y bloquea la integración ante alertas críticas.
  5. Sube artefactos de auditoría de manera persistente con retención en GitHub Actions.
- **Evidencias y Enlaces:**
  - Workflow de CI/CD: [`.github/workflows/ci.yml`](../../../.github/workflows/ci.yml)
  - Definición del Security Gate y verificación con `jq`: [`.github/workflows/ci.yml#L100-L125`](../../../.github/workflows/ci.yml)

---

### Criterio 8: Calidad del Reporte y Presentación Ejecutiva (10 Puntos)
- **Requisitos Oficiales:**
  Presentación de documentación estructurada, clara, trazable y profesional, incluyendo informe técnico final, plan maestro de pruebas, inventario exhaustivo de activos, matriz de trazabilidad y guion ejecutivo de presentación para sustentación.
- **Puntaje Obtenido:** **10 / 10 Puntos**
- **Justificación Técnica:**
  El repositorio cuenta con una estructura de documentación completamente desacoplada de los ejecutables y evidencias (carpeta `docs/seguridad/`), clasificada en 5 subdirectorios temáticos (`01-plan/`, `02-analisis/`, `03-hallazgos/`, `04-poc/`, `05-informe/`). Se incluye el informe técnico final con lenguaje formal y métricas CVSS v3.1, el inventario exhaustivo de activos y un guion condensado de presentación ejecutiva de 9 diapositivas para exposición académica.
- **Evidencias y Enlaces:**
  - Informe Técnico Final: [`docs/seguridad/05-informe/INFORME_FINAL.md`](INFORME_FINAL.md)
  - Guion de Presentación Ejecutiva (Slides): [`docs/seguridad/05-informe/PRESENTACION_BREVE.md`](PRESENTACION_BREVE.md)
  - Plan de Pruebas de Seguridad: [`docs/seguridad/01-plan/PLAN_PRUEBAS_SEGURIDAD.md`](../01-plan/PLAN_PRUEBAS_SEGURIDAD.md)
  - Inventario General de Documentación: [`docs/seguridad/INVENTARIO_DOCUMENTACION.md`](../INVENTARIO_DOCUMENTACION.md)

---

### Criterio Bonificación: Pruebas de Carga Avanzadas con JMeter (+5 Puntos)
- **Requisitos Oficiales de Bonificación:**
  Ejecución de pruebas de rendimiento avanzadas con Apache JMeter en contenedor o local, simulación de alta concurrencia (>300 usuarios), análisis detallado de percentiles de latencia y fundamentación de cuellos de botella a nivel de hardware, JVM y base de datos.
- **Puntaje Obtenido:** **+5 / 5 Puntos Bonus**
- **Justificación Técnica:**
  Se superó el alcance básico implementando un entorno de estrés con 3 escalones de concurrencia progresiva (100, 300 y 500 hilos de ejecución simultánea con ramp-up dinámico). Se documentó matemáticamente la estabilización del throughput en 158 req/s debido al dimensionamiento del pool HikariCP (`maximum-pool-size=15`), demostrando que la aplicación maneja carga masiva sin saturación ni caída de sockets, logrando una tasa de error de 0.00% en las 7,200 solicitudes procesadas.
- **Evidencias y Enlaces:**
  - Reporte técnico de rendimiento y arquitectura: [`pruebas/evidencias/jmeter/resumen-rendimiento.txt`](../../pruebas/evidencias/jmeter/resumen-rendimiento.txt)
  - Plan JMX multiescenario: [`pruebas/rendimiento/travelink.jmx`](../../pruebas/rendimiento/travelink.jmx)
  - Reportes HTML generados con gráficos interactivos en `pruebas/evidencias/jmeter/tier_*_users/html_report/`

---

## 3. Resumen de Calificación Final

$$\text{Puntaje Base} = 10 + 15 + 15 + 20 + 10 + 10 + 10 + 10 = 100 \text{ pts}$$
$$\text{Bonificación JMeter} = +5 \text{ pts}$$
$$\mathbf{Total = 105 / 100 \text{ Puntos (100\% de Cumplimiento + Bonificación de Excelencia)}}$$
