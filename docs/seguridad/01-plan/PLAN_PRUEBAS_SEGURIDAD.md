# Plan Maestro de Pruebas de Seguridad — TRAVELINK

**Proyecto:** TRAVELINK — Plataforma de Gestión y Reservas Turísticas  
**Asignatura:** Integrador I Sistemas — Sesión 27: Pruebas de Seguridad (Rúbrica de Evaluación de 100 Puntos)  
**Fecha:** Noviembre 2026  
**Versión:** 1.0 — Final  
**Entorno de Ejecución:** Dockerizado Local Aislado (`127.0.0.1`, `travelink-net`)  

---

## 1. Objetivos del Plan de Pruebas

El presente documento define la estrategia, alcance, herramientas y metodología para evaluar integralmente la postura de seguridad de la aplicación **TRAVELINK**, verificando vulnerabilidades en dependencias, configuraciones de servidor, lógica de negocio y control de acceso, en estricto cumplimiento de los **8 criterios de la rúbrica de evaluación académica (100 puntos)**.

### Objetivos Específicos:
1. **Aislamiento y Reproducibilidad:** Garantizar un entorno de pruebas 100% reproducible y seguro mediante contenedores Docker vinculados a loopback (`127.0.0.1`), protegiendo cualquier entorno externo o de nube (prohibición estricta de conexión a Aiven).
2. **Análisis de Dependencias (SCA):** Detectar componentes obsoletos y CVEs conocidos en la cadena de suministro de software (Maven/Java 21/Spring Boot; línea base inicial 3.3.4, actualizada a 3.3.13 en fase de remediación).
3. **Escaneo Dinámico de Seguridad (DAST):** Auditar los endpoints expuestos mediante análisis dinámico automatizado (OWASP ZAP) y fuzzing de rutas (FFUF).
4. **Verificación de Resiliencia y Carga:** Medir la resistencia y estabilidad del backend frente a concurrencia masiva (100, 300 y 500 usuarios) con Apache JMeter.
5. **Auditoría de Inyecciones e Integridad:** Validar la resistencia contra inyecciones SQL controladas (SQLMap) y fallos de autorización (IDOR / BOLA / XSS / Backdoors).
6. **Automatización en CI/CD:** Integrar puertas de enlace de seguridad (*security gates*) automáticas en GitHub Actions.

---

## 2. Alcance y Arquitectura del Entorno de Pruebas

### 2.1. Reglas de Contención y Fronteras de Seguridad
- **Prohibición de Red Externa:** Todos los scripts y herramientas poseen guards de seguridad que abortan inmediatamente si detectan objetivos no locales o referencias a `aivencloud`.
- **Publicación Restringida a Loopback:**
  - Aplicación Web: `127.0.0.1:8080:8080` (contenedor `travelink-app`).
  - Base de Datos MySQL: `127.0.0.1:3307:3306` (contenedor `travelink-db`).
  - Red Interna: `travelink-net` (puente Docker no expuesto a la red física).
- **Prohibición de Prune Global:** Queda vetado el uso de `docker system prune` o `docker volume prune`; el reinicio se gestiona de forma limpia mediante `scripts/reset-db.ps1` o `docker compose down -v`.

### 2.2. Datos Sintéticos de Prueba (Seed Controlado)
Para evitar el uso de credenciales reales o datos personales, las pruebas se sustentan en `sql/seed_pruebas.sql`:
- **Usuarios de Prueba (BCrypt real):**
  - Turista A: `turista_a@test.local` / `TestTurista#2026` (ID: 100, Reserva #500)
  - Turista B: `turista_b@test.local` / `TestTurista#2027` (ID: 101, Reserva #501)
  - Agencia Demo: `agencia_demo@test.local` / `TestAgencia#2026` (ID: 10, Agencia #10)
  - Administrador Seguro: `admin_seguro@test.local` / `TestAdmin#2026` (ID: 1)
- **Recursos Evaluados:**
  - Tours: IDs 100 y 101.
  - Destinos: IDs 8 y 15.
  - Reservas: IDs 500 y 501.

### 2.3. Pruebas Unitarias vs Pruebas de Integración con Testcontainers
La suite de pruebas automatizadas en `Springboot/test/` comprende 23 pruebas:
- **20 Pruebas Unitarias:** Ejecutan sobre controladores y capas de servicio mediante mocks aislados (`AuthApiControllerTest`, `CalificacionServicioTest`, `ComisionServicioTest`, `ReservaCancelacionTest`, `ReservaServicioTest`), aprobadas al 100% (0 fallos, 0 errores).
- **3 Pruebas de Integración (`ReservaPersistenciaIntegracionTest`):** Diseñadas para validar la persistencia contra un contenedor MySQL real gestionado mediante **Testcontainers**. La clase cuenta con la anotación `@Testcontainers(disabledWithoutDocker = true)`. Al ejecutarse con Docker Desktop activo en Windows, la estrategia `NpipeSocketClientProviderStrategy` de Testcontainers 1.19.8 experimenta un fallo de negociación de protocolo contra el motor moderno Docker Engine 29.9.3 (`BadRequestException: Status 400`), por lo que la anotación omite de forma controlada y segura las 3 pruebas (`Skipped: 3`), culminando en `BUILD SUCCESS` sin quebrar el pipeline. La salida técnica y el log de excepción se encuentran documentados en [`pruebas/evidencias/integracion-testcontainers.txt`](../../pruebas/evidencias/integracion-testcontainers.txt).

---

## 3. Matriz de Herramientas y Técnicas de Evaluación

| Categoría | Herramienta / Método | Objetivo Específico | Entorno de Ejecución |
|---|---|---|---|
| **SAST** | Inspección Manual & Flujo de Datos | Identificación de backdoors, IDOR, bypass RBAC y XSS | Código fuente Java (`Springboot/src`) |
| **SCA** | OWASP Dependency-Check (v10.0.4) | Detección de CVEs en dependencias Maven | Maven CLI / Contenedor CI |
| **Reconocimiento** | Nmap (v7.9x) / Nikto (v2.5x) | Escaneo de puertos y fingerprinting HTTP | Host / Fallback documentado |
| **DAST Baseline** | OWASP ZAP (zap-stable) | Escaneo pasivo/activo de cabeceras, cookies y CSP | Docker (`travelink-net`) |
| **Fuzzing API** | FFUF (v2.1) / Wordlist API | Descubrimiento de endpoints ocultos y códigos HTTP | Loopback `127.0.0.1:8080` |
| **Pruebas de Carga** | Apache JMeter (v5.5 / 5.6) | Concurrencia de 100, 300 y 500 hilos (p95, p99, throughput) | Docker (`travelink-net`) |
| **SQLi Testing** | SQLMap (v1.10.11) | Inyección SQL controlada (`--level=1 --risk=1 --batch`) | Docker (`travelink-net`) |
| **CI/CD** | GitHub Actions Pipeline | Puerta de seguridad automática (falla con CVSS >= 9) | `.github/workflows/ci.yml` |

---

## 4. Mapeo del Plan contra los 8 Criterios de la Rúbrica Oficial (100 Puntos + 5 Bonus)

| # | Criterio de Evaluación (Rúbrica Oficial) | Puntos | Fase y Artefactos de Evidencia |
|:---:|---|:---:|---|
| **1** | **Preparación y Entorno Reproducible** | 10 pts | `Dockerfile`, `docker-compose.yml`, `pruebas/entorno/reset-db.*`, `sql/seed_pruebas.sql`, `CUENTAS_PRUEBA.md` |
| **2** | **Reconocimiento y Análisis de Dependencias (SCA)** | 15 pts | `pruebas/seguridad/scripts/recon-nmap.*`, `recon-nikto.*`, `sca-dependency-check.*`, `tabla-dependencias-cve.md` |
| **3** | **Escaneo Automático de Vulnerabilidades y Validación** | 15 pts | `pruebas/seguridad/scripts/zap-scan.*`, `zap-parsed.json`, `mapa-endpoints.md`, `VALIDACION_FALSOS_POSITIVOS.md` |
| **4** | **Explotación y Evidencia Técnica (PoCs)** | 20 pts | 9 Guías PoC (`docs/seguridad/04-poc/PoC-01` a `PoC-09`), Hallazgos H-01 a H-09, `sqlmap-results.txt` |
| **5** | **Fuzzing y Resiliencia de la API** | 10 pts | `pruebas/seguridad/scripts/fuzz-ffuf.*`, `wordlists/api-endpoints.txt`, `ffuf-result.json`, análisis de errores 500 |
| **6** | **Pruebas de Carga y Rendimiento** | 10 pts | `pruebas/rendimiento/travelink.jmx`, `jmeter-load.*`, `resumen-rendimiento.txt`, métricas p95/p99 |
| **7** | **Integración Continua (CI/CD) y Automatización** | 10 pts | `.github/workflows/ci.yml` (Job security, Security Gates automatizados con jq y fail en CVSS >= 9) |
| **8** | **Calidad del Reporte y Presentación Ejecutiva** | 10 pts | `INFORME_FINAL.md`, `PRESENTACION_BREVE.md`, `CHECKLIST_RUBRICA.md`, `INVENTARIO_DOCUMENTACION.md` |
| **BONUS** | **Pruebas de Carga Avanzadas con Apache JMeter** | +5 pts | 3 escalones de concurrencia (100, 300, 500 hilos), análisis de JVM warm-up y pool HikariCP |
| **TOTAL** | **EVALUACIÓN INTEGRAL CON BONIFICACIÓN** | **105 pts** | **100 Pts Base + 5 Pts Bonus (Cumplimiento 105%)** |

---

## 5. Cronograma de Ejecución por Sesiones (S1 a S6)

El plan se estructura en 6 sesiones operativas secuenciales diseñadas para cubrir metódicamente la auditoría y asegurar la reproducibilidad:

| Sesión | Denominación y Foco Técnico | Objetivos y Actividades Principales | Entregables y Evidencias Asociadas |
|:---:|---|---|---|
| **S1** | **Aprovisionamiento y Análisis Estático (SAST)** | • Configuración del entorno dockerizado local aislado (`travelink-net`).<br>• Carga de esquema y seed sintético (`seed_pruebas.sql`).<br>• Revisión estática del código Java (`Springboot/src`) para identificar patrones inseguros (backdoors, autorizaciones faltantes). | • `docker-compose.yml`<br>• `pruebas/entorno/reset-db.ps1` / `.sh`<br>• [`docs/seguridad/02-analisis/ANALISIS_ESTATICO.md`](../02-analisis/ANALISIS_ESTATICO.md)<br>• [`CUENTAS_PRUEBA.md`](../05-informe/CUENTAS_PRUEBA.md) |
| **S2** | **Reconocimiento y Análisis de Dependencias (SCA)** | • Fingerprinting de servicios y puertos locales con Nmap.<br>• Escaneo de cabeceras de servidor y configuraciones con Nikto.<br>• Auditoría SCA con OWASP Dependency-Check y Trivy sobre `pom.xml`. | • `pruebas/evidencias/nmap/nmap-local.txt`<br>• `pruebas/evidencias/nikto/nikto-local.txt`<br>• [`docs/seguridad/02-analisis/tabla-dependencias-cve.md`](../02-analisis/tabla-dependencias-cve.md) |
| **S3** | **Escaneo Dinámico Automatizado (DAST) y Triaje** | • Ejecución de OWASP ZAP Baseline Scan contra `http://app:8080` (resolución interna en `travelink-net`; en host se accede vía `http://localhost:8080`).<br>• Análisis de alertas generadas en `zap-report.json`.<br>• Triaje de falsos positivos y confirmación manual con peticiones HTTP reales. | • `pruebas/evidencias/zap/zap-report.json` / `zap-report.html`<br>• `pruebas/evidencias/zap/zap-parsed.json`<br>• [`docs/seguridad/02-analisis/VALIDACION_FALSOS_POSITIVOS.md`](../02-analisis/VALIDACION_FALSOS_POSITIVOS.md) |
| **S4** | **Fuzzing de API y Explotación Técnica (PoCs)** | • Fuzzing de endpoints con FFUF y wordlist especializada.<br>• Pruebas de inyección SQL con SQLMap (`sqlmap-test`).<br>• Desarrollo y documentación paso a paso de las 9 PoCs funcionales (H-01 a H-09). | • `pruebas/evidencias/ffuf/ffuf-result.json`<br>• `pruebas/evidencias/sqlmap/sqlmap-results.txt`<br>• [`docs/seguridad/02-analisis/INFORME_FUZZING_RESILIENCIA.md`](../02-analisis/INFORME_FUZZING_RESILIENCIA.md)<br>• [`docs/seguridad/04-poc/PoC-01`](../04-poc/PoC-01-IDOR.md) a [`PoC-09`](../04-poc/PoC-09-Escalamiento-Rutas-Duales.md) |
| **S5** | **Pruebas de Carga y Resiliencia con Apache JMeter** | • Calibración de planes de prueba `.jmx` (100, 300 y 500 usuarios virtuales).<br>• Ejecución no-GUI en Docker y recolección de métricas p95/p99 y throughput.<br>• Análisis de estabilidad del pool HikariCP y comportamiento ante degradación. | • `pruebas/rendimiento/travelink.jmx`<br>• `pruebas/evidencias/jmeter/resumen-rendimiento.txt`<br>• `pruebas/evidencias/jmeter/statistics.json`<br>• Sección 4 de [`docs/seguridad/05-informe/INFORME_FINAL.md`](../05-informe/INFORME_FINAL.md) |
| **S6** | **Automatización en CI/CD, Consolidación y Cierre** | • Configuración de GitHub Actions con gates para SCA y DAST.<br>• Consolidación del informe final de auditoría y checklist contra la rúbrica oficial.<br>• Preparación del guion ejecutivo para sustentación. | • `.github/workflows/ci.yml`<br>• [`.trivyignore`](../../../.trivyignore)<br>• [`docs/seguridad/05-informe/INFORME_FINAL.md`](../05-informe/INFORME_FINAL.md)<br>• [`docs/seguridad/05-informe/CHECKLIST_RUBRICA.md`](../05-informe/CHECKLIST_RUBRICA.md)<br>• [`docs/seguridad/05-informe/PRESENTACION_BREVE.md`](../05-informe/PRESENTACION_BREVE.md) |

---

## 6. Matriz de Roles y Responsabilidades (RACI)

Para una gobernanza clara de las pruebas y la trazabilidad del aseguramiento de calidad, se define la siguiente asignación de responsabilidades:

### 6.1. Definición de Roles del Equipo
- **Líder de Seguridad y Calidad (QA/Sec Lead):** Responsable de la estrategia global, validación de la rúbrica académica, triaje de vulnerabilidades y redacción del informe final.
- **Ingeniero DevSecOps:** Responsable del pipeline de CI/CD, contenedorización, escaneo SCA (Dependency-Check, Trivy) y automatización de gates de seguridad.
- **Auditor de Seguridad Ofensiva (Pentester / DAST):** Responsable de la ejecución de ZAP, FFUF, SQLMap y la elaboración de las 9 Guías PoC con evidencias reproducibles.
- **Ingeniero de Rendimiento (Performance QA):** Responsable del diseño y ejecución de los planes JMeter, análisis de percentiles (p95/p99) y monitoreo de recursos del servidor.

### 6.2. Matriz RACI por Actividad

> **Convención RACI:**  
> **R** = Responsable (*Responsible* — quien realiza la tarea)  
> **A** = Aprobador (*Accountable* — quien responde por el resultado final)  
> **C** = Consultado (*Consulted* — a quien se solicita retroalimentación o criterio)  
> **I** = Informado (*Informed* — a quien se mantiene notificado del avance)

| Actividad / Hito de Seguridad | Líder QA / Sec | Ingeniero DevSecOps | Auditor DAST / Pentester | Ingeniero de Rendimiento |
|---|:---:|:---:|:---:|:---:|
| Aislamiento de entorno Docker y datos de seed | **A** | **R** | **C** | **I** |
| Análisis de dependencias (SCA) y triaje CVE | **A** | **R** | **C** | **I** |
| Escaneo dinámico DAST (OWASP ZAP) | **A** | **C** | **R** | **I** |
| Triaje y confirmación manual de falsos positivos | **A** | **I** | **R** | **I** |
| Fuzzing de endpoints y evaluación de errores 500 | **A** | **I** | **R** | **C** |
| Inyección SQL y pruebas de integridad (SQLMap) | **A** | **I** | **R** | **I** |
| Ejecución y documentación técnica de PoCs (01-09) | **A** | **C** | **R** | **I** |
| Pruebas de carga concurrente JMeter (100-500 hilos) | **A** | **C** | **I** | **R** |
| Integración de Security Gates en GitHub Actions | **A** | **R** | **C** | **I** |
| Elaboración de Informe Final y Checklist de Rúbrica | **R / A** | **C** | **C** | **C** |
| Sustentación técnica ante evaluador | **R / A** | **R** | **R** | **R** |
