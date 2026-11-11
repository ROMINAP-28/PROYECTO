# Inventario de Documentación y Artefactos de Seguridad — TRAVELINK

Este documento registra el catálogo completo de activos, scripts, guías de prueba de concepto, reportes de escaneo y archivos de configuración creados durante el proceso de auditoría y pruebas de seguridad del proyecto **TRAVELINK**.

---

## 1. Infraestructura y Entorno Docker Reproducible

| Archivo / Ruta | Tipo | Descripción | Estado |
|---|---|---|:---:|
| [`Dockerfile`](file:///d:/ProyectosU/PROYECTO/Dockerfile) | Configuración | Build multi-stage (Maven Temurin 21 + JRE Eclipse Temurin 21) | Implementado / Verificado |
| [`docker-compose.yml`](file:///d:/ProyectosU/PROYECTO/docker-compose.yml) | Orquestación | Servicios `travelink-db` (MySQL 8.0) y `travelink-app` vinculados a loopback | Implementado / Verificado |
| [`.dockerignore`](file:///d:/ProyectosU/PROYECTO/.dockerignore) | Configuración | Exclusión de `.env`, `.git`, `docs/`, `target/`, preservando `./mvnw` y `.mvn/` | Implementado / Verificado |
| [`.gitignore`](file:///d:/ProyectosU/PROYECTO/.gitignore) | Git Hardening | Bloqueo estricto de secretos (`.env*`), volcados de BD (`*.dump`) y evidencias crudas | Implementado / Verificado |
| [`scripts/reset-db.ps1`](file:///d:/ProyectosU/PROYECTO/scripts/reset-db.ps1) | Script PowerShell | Limpieza y reinicio controlado de volúmenes con guard de seguridad | Implementado / Verificado |
| [`scripts/reset-db.sh`](file:///d:/ProyectosU/PROYECTO/scripts/reset-db.sh) | Script Bash | Equivalente en Bash para reinicio del entorno Docker | Implementado / Verificado |
| [`scripts/seed.ps1`](file:///d:/ProyectosU/PROYECTO/scripts/seed.ps1) | Script PowerShell | Re-ejecución del script sintético de pruebas sobre la BD local | Implementado / Verificado |
| [`scripts/seed.sh`](file:///d:/ProyectosU/PROYECTO/scripts/seed.sh) | Script Bash | Equivalente en Bash para inyección de datos de prueba | Implementado / Verificado |

---

## 2. Bases de Datos y Datos Sintéticos de Prueba

| Archivo / Ruta | Tipo | Contenido y Propósito | Estado |
|---|---|---|:---:|
| [`sql/travelink_schema.sql`](file:///d:/ProyectosU/PROYECTO/sql/travelink_schema.sql) | DDL | Definición de las 18 tablas relacionales del proyecto TRAVELINK | Producción / Verificado |
| [`sql/datos_minimos.sql`](file:///d:/ProyectosU/PROYECTO/sql/datos_minimos.sql) | DML Inicial | 3 roles del sistema y usuario admin inicial | Producción / Verificado |
| [`sql/seed_pruebas.sql`](file:///d:/ProyectosU/PROYECTO/sql/seed_pruebas.sql) | DML Sintético | 4 usuarios de prueba (hashes BCrypt reales), tours 100/101 y reservas 500/501 | Implementado / Verificado |
| [`docs/seguridad/CUENTAS_PRUEBA.md`](file:///d:/ProyectosU/PROYECTO/docs/seguridad/CUENTAS_PRUEBA.md) | Documentación | Credenciales sintéticas de prueba, roles asignados y hashes generados | Implementado / Verificado |

---

## 3. Scripts de Seguridad Dinámicos y Automatización

| Archivo / Script | Tipo | Parámetros y Funcionalidad | Estado |
|---|---|---|:---:|
| [`scripts/seguridad/sca-dependency-check.ps1`](file:///d:/ProyectosU/PROYECTO/scripts/seguridad/sca-dependency-check.ps1) | PowerShell | Análisis SCA vía Maven OWASP Dependency-Check (v10.0.4) | Ejecutado (1.25 MB HTML, 753 KB JSON) |
| [`scripts/seguridad/sca-dependency-check.sh`](file:///d:/ProyectosU/PROYECTO/scripts/seguridad/sca-dependency-check.sh) | Bash | Script SCA para entornos Linux / CI | Implementado |
| [`scripts/seguridad/recon-nmap.ps1`](file:///d:/ProyectosU/PROYECTO/scripts/seguridad/recon-nmap.ps1) | PowerShell | Escaneo de servicios en puertos 8080 y 3307 con Nmap | Documentado / Fallback activo |
| [`scripts/seguridad/recon-nmap.sh`](file:///d:/ProyectosU/PROYECTO/scripts/seguridad/recon-nmap.sh) | Bash | Escaneo Nmap para entornos Linux / WSL | Documentado / Fallback activo |
| [`scripts/seguridad/recon-nikto.ps1`](file:///d:/ProyectosU/PROYECTO/scripts/seguridad/recon-nikto.ps1) | PowerShell | Fingerprinting web HTTP con Nikto | Documentado / Fallback activo |
| [`scripts/seguridad/recon-nikto.sh`](file:///d:/ProyectosU/PROYECTO/scripts/seguridad/recon-nikto.sh) | Bash | Escaneo Nikto para entornos Linux | Documentado / Fallback activo |
| [`scripts/seguridad/zap-scan.ps1`](file:///d:/ProyectosU/PROYECTO/scripts/seguridad/zap-scan.ps1) | PowerShell | Ejecución DAST automatizada con ZAP Baseline en Docker | Ejecutado (16 alertas analizadas) |
| [`scripts/seguridad/zap-scan.sh`](file:///d:/ProyectosU/PROYECTO/scripts/seguridad/zap-scan.sh) | Bash | Escaneo ZAP Baseline para Linux / CI | Implementado |
| [`scripts/seguridad/fuzz-ffuf.ps1`](file:///d:/ProyectosU/PROYECTO/scripts/seguridad/fuzz-ffuf.ps1) | PowerShell | Fuzzing de 56 endpoints de API sobre diccionario | Ejecutado (56 rutas analizadas) |
| [`scripts/seguridad/fuzz-ffuf.sh`](file:///d:/ProyectosU/PROYECTO/scripts/seguridad/fuzz-ffuf.sh) | Bash | Fuzzing con FFUF para entornos Linux | Implementado |
| [`scripts/seguridad/jmeter-load.ps1`](file:///d:/ProyectosU/PROYECTO/scripts/seguridad/jmeter-load.ps1) | PowerShell | Pruebas de carga concurrente (100, 300, 500 hilos) con Docker | Ejecutado (7,200 peticiones, 0% fallos) |
| [`scripts/seguridad/jmeter-load.sh`](file:///d:/ProyectosU/PROYECTO/scripts/seguridad/jmeter-load.sh) | Bash | Pruebas de carga JMeter para Linux | Implementado |
| [`scripts/seguridad/sqlmap-test.ps1`](file:///d:/ProyectosU/PROYECTO/scripts/seguridad/sqlmap-test.ps1) | PowerShell | Auditoría de inyección SQL controlada (--level=1 --risk=1 --batch) | Ejecutado (6 recursos reales probados) |
| [`scripts/seguridad/sqlmap-test.sh`](file:///d:/ProyectosU/PROYECTO/scripts/seguridad/sqlmap-test.sh) | Bash | Pruebas SQLMap para Linux | Implementado |
| [`scripts/seguridad/wordlists/api-endpoints.txt`](file:///d:/ProyectosU/PROYECTO/scripts/seguridad/wordlists/api-endpoints.txt) | Wordlist | Diccionario de 56 rutas y recursos REST evaluados en TRAVELINK | Implementado / Verificado |

---

## 4. Guías de Pruebas de Concepto (PoC)

| Guía PoC | Vulnerabilidad Evaluada | Recurso / Vector | Estado |
|---|---|---|:---:|
| [`PoC-01-IDOR.md`](file:///d:/ProyectosU/PROYECTO/docs/seguridad/poc/PoC-01-IDOR.md) | Insecure Direct Object References | `/api/obtener_reservas?idUsuario=100` (Reserva #500) | Validada |
| [`PoC-02-XSS.md`](file:///d:/ProyectosU/PROYECTO/docs/seguridad/poc/PoC-02-XSS.md) | Stored Cross-Site Scripting | Inyección en comentario de tour #100 y renderizado | Validada |
| [`PoC-03-Privilege-Escalation.md`](file:///d:/ProyectosU/PROYECTO/docs/seguridad/poc/PoC-03-Privilege-Escalation.md) | Bypass de Autorización RBAC | Invocación de `/api/usuarios` y `/api/comisiones` con rol Turista | Validada |
| [`PoC-04-Backdoor-Login.md`](file:///d:/ProyectosU/PROYECTO/docs/seguridad/poc/PoC-04-Backdoor-Login.md) | Puerta Trasera en Autenticación | Bypass con cuentas estáticas en `UsuarioServicioImpl` | Validada |
| [`PoC-05-Admin-Plaintext.md`](file:///d:/ProyectosU/PROYECTO/docs/seguridad/poc/PoC-05-Admin-Plaintext.md) | Credencial en Texto Plano | Login administrativo con clave predeterminada del seed | Validada |
| [`PoC-06-CSRF-Exemption.md`](file:///d:/ProyectosU/PROYECTO/docs/seguridad/poc/PoC-06-CSRF-Exemption.md) | Exención Masiva de CSRF | Mutación de estado en endpoints transaccionales sin token | Validada |
| [`PoC-07-CORS-Exploitation.md`](file:///d:/ProyectosU/PROYECTO/docs/seguridad/poc/PoC-07-CORS-Exploitation.md) | CORS Permisivo con Credenciales | Petición cross-origin con credenciales desde dominio externo | Validada |
| [`PoC-08-Security-Headers.md`](file:///d:/ProyectosU/PROYECTO/docs/seguridad/poc/PoC-08-Security-Headers.md) | Ausencia de Cabeceras Defensivas | Comprobación de cabeceras HTTP faltantes (`curl -I`) | Validada |
| [`PoC-09-IDOR-Cancelacion.md`](file:///d:/ProyectosU/PROYECTO/docs/seguridad/poc/PoC-09-IDOR-Cancelacion.md) | IDOR en Cancelación de Reserva | Cancelación no autorizada de reserva ajena #500 | Validada |

---

## 5. Fichas Técnicas de Hallazgos Individuales

| ID | Título del Hallazgo | Severidad | Archivo de Especificación |
|---|---|:---:|---|
| **H-01** | Puerta Trasera (Backdoor) en Verificación de Contraseñas | **CRÍTICO** | [`docs/seguridad/hallazgos/H-01.md`](file:///d:/ProyectosU/PROYECTO/docs/seguridad/hallazgos/H-01.md) |
| **H-02** | Credencial de Administrador en Texto Plano en Seed Inicial | **ALTO** | [`docs/seguridad/hallazgos/H-02.md`](file:///d:/ProyectosU/PROYECTO/docs/seguridad/hallazgos/H-02.md) |
| **H-03** | Fuga Masiva de Datos e IDOR / BOLA en Consulta y Cancelación de Reservas | **CRÍTICO** | [`docs/seguridad/hallazgos/H-03.md`](file:///d:/ProyectosU/PROYECTO/docs/seguridad/hallazgos/H-03.md) |
| **H-04** | Configuración CORS Excesivamente Permisiva con Credenciales Habilitadas | **ALTO** | [`docs/seguridad/hallazgos/H-04.md`](file:///d:/ProyectosU/PROYECTO/docs/seguridad/hallazgos/H-04.md) |
| **H-05** | Desactivación Masiva de Protección CSRF en Endpoints Transaccionales | **MEDIO** | [`docs/seguridad/hallazgos/H-05.md`](file:///d:/ProyectosU/PROYECTO/docs/seguridad/hallazgos/H-05.md) |
| **H-06** | Cross-Site Scripting Almacenado (Stored XSS) en Reseñas | **ALTO** | [`docs/seguridad/hallazgos/H-06.md`](file:///d:/ProyectosU/PROYECTO/docs/seguridad/hallazgos/H-06.md) |
| **H-07** | Ausencia de Cabeceras Defensivas HTTP y Cache-Control Desactivado | **MEDIO** | [`docs/seguridad/hallazgos/H-07.md`](file:///d:/ProyectosU/PROYECTO/docs/seguridad/hallazgos/H-07.md) |
| **H-08** | Escalamiento de Privilegios por Rutas Duales sin Prefijo `/api/admin/` | **CRÍTICO** | [`docs/seguridad/hallazgos/H-08.md`](file:///d:/ProyectosU/PROYECTO/docs/seguridad/hallazgos/H-08.md) |
| **H-09** | Broken Object Level Authorization (BOLA) en Reseñas | **ALTO** | [`docs/seguridad/hallazgos/H-09.md`](file:///d:/ProyectosU/PROYECTO/docs/seguridad/hallazgos/H-09.md) |

---

## 6. Documentos Maestros de Seguridad y Rúbrica

| Documento | Descripción | Estado |
|---|---|:---:|
| [`PLAN_PRUEBAS_SEGURIDAD.md`](file:///d:/ProyectosU/PROYECTO/docs/seguridad/PLAN_PRUEBAS_SEGURIDAD.md) | Estrategia, objetivos y mapeo contra los 8 criterios de evaluación | Completado |
| [`ANALISIS_ESTATICO.md`](file:///d:/ProyectosU/PROYECTO/docs/seguridad/ANALISIS_ESTATICO.md) | Informe SAST detallado de código fuente Java y configuraciones Spring | Completado |
| [`mapa-endpoints.md`](file:///d:/ProyectosU/PROYECTO/docs/seguridad/mapa-endpoints.md) | Mapeo integral de 56 endpoints, métodos HTTP, handlers y niveles RBAC | Completado |
| [`tabla-dependencias-cve.md`](file:///d:/ProyectosU/PROYECTO/docs/seguridad/tabla-dependencias-cve.md) | Tabla exhaustiva de 15 dependencias vulnerables y 179 CVEs | Completado |
| [`INFORME_FINAL.md`](file:///d:/ProyectosU/PROYECTO/docs/seguridad/INFORME_FINAL.md) | Informe ejecutivo y técnico consolidado para entrega académica | En Elaboración |
| [`CHECKLIST_RUBRICA.md`](file:///d:/ProyectosU/PROYECTO/docs/seguridad/CHECKLIST_RUBRICA.md) | Matriz de cumplimiento exhaustivo punto por punto (100 pts) | En Elaboración |
