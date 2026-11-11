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
2. **Análisis de Dependencias (SCA):** Detectar componentes obsoletos y CVEs conocidos en la cadena de suministro de software (Maven/Java 21/Spring Boot 3.3.4).
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

## 4. Mapeo del Plan contra los 8 Criterios de la Rúbrica (100 Puntos)

| # | Criterio de Evaluación (Rúbrica) | Puntos | Fase y Artefactos de Evidencia |
|---|---|:---:|---|
| **1** | **Entorno de Pruebas Reproducible** | 10 pts | `Dockerfile` multi-stage, `docker-compose.yml`, `scripts/reset-db.*`, `sql/seed_pruebas.sql` |
| **2** | **Análisis Estático (SAST) de Código Fuente** | 15 pts | `docs/seguridad/ANALISIS_ESTATICO.md`, `docs/seguridad/mapa-endpoints.md`, Hallazgos H-01 a H-09 |
| **3** | **Análisis de Dependencias (SCA)** | 10 pts | `docs/seguridad/tabla-dependencias-cve.md`, `dependency-check-report.html`, `dependency-check-report.json` |
| **4** | **Reconocimiento y Escaneo de Superficie** | 10 pts | `scripts/seguridad/recon-nmap.*`, `scripts/seguridad/recon-nikto.*`, evidencias en `docs/seguridad/evidencias/` |
| **5** | **Escaneo Dinámico DAST y Fuzzing** | 15 pts | `zap-report.html`, `zap-report.json`, `ffuf-result.json`, `scripts/seguridad/fuzz-ffuf.*` |
| **6** | **Pruebas de Carga y Rendimiento** | 15 pts | `jmeter/travelink.jmx`, `results.jtl` (100/300/500 hilos), `resumen-rendimiento.txt`, reportes HTML |
| **7** | **Validación de Vulnerabilidades y PoCs** | 15 pts | 9 Guías PoC ejecutables (`docs/seguridad/poc/PoC-01` a `PoC-09`), `sqlmap-results.txt` |
| **8** | **Integración Continua y Reporte Final** | 10 pts | `.github/workflows/ci.yml` (Job security), `docs/seguridad/INFORME_FINAL.md`, `CHECKLIST_RUBRICA.md` |
| **TOTAL** | | **100 pts** | **Cumplimiento Integral 100%** |
