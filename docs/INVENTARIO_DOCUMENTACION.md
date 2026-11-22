# Inventario Maestro de Documentación y Artefactos — TRAVELINK 📚

Este documento es el catálogo maestro y punto único de referencia de todos los activos de documentación, especificaciones de ingeniería, planes de prueba, análisis de seguridad, procedimientos de despliegue y evidencias técnicas del proyecto **TRAVELINK**, alineados con los **Ítems del Taller 1 al 12** y el **Módulo de Despliegue en Producción**.

---

## 1. Documentación Raíz e Infraestructura del Proyecto

| Activo / Ruta | Tipo | Descripción | Ítem (Taller / Fase) | Estado |
|---|---|---|:---:|:---:|
| [`README.md`](../README.md) | Manual | Presentación del proyecto, stack técnico, instalación, accesos y ejecución | Taller 7 / General | Implementado / Verificado |
| [`Dockerfile`](../Dockerfile) | Infraestructura | Construcción multi-stage (Maven Temurin 21 + JRE Eclipse Temurin 21) | Taller 7 / Entorno | Implementado / Verificado |
| [`docker-compose.yml`](../docker-compose.yml) | Orquestación | Servicios locales `travelink-db` y `travelink-app` en loopback | Taller 7 / Entorno | Implementado / Verificado |
| [`.env.example`](../.env.example) | Configuración | Plantilla de variables de entorno para base de datos y credenciales | Taller 7 / Entorno | Implementado / Verificado |
| [`.trivyignore`](../.trivyignore) | Seguridad SCA | 9 excepciones justificadas de CVEs críticos (vencimiento `exp:2027-06-30`) | Taller 8 / SCA | Implementado / Verificado |
| [`.github/workflows/ci.yml`](../.github/workflows/ci.yml) | DevSecOps CI | Pipeline CI: build, tests, SCA Trivy (v0.36.0) y DAST ZAP | Taller 12 / CI-CD | Implementado / Verificado |
| [`.github/workflows/deploy.yml`](../.github/workflows/deploy.yml) | CD Pipeline | Pipeline CD: build, tests, Docker GHCR, smoke test y compuerta prod | Despliegue / CD | Implementado / Verificado |
| [`render.yaml`](../render.yaml) | Blueprint Render | Especificación IaC para despliegue automático en contenedor Docker en Render | Despliegue / Cloud | Implementado / Verificado |

---

## 2. Base de Datos y Datos Sintéticos (`sql/`)

| Archivo / Ruta | Tipo | Contenido y Propósito | Ítem (Taller / Fase) | Estado |
|---|---|---|:---:|:---:|
| [`sql/travelink_schema.sql`](../sql/travelink_schema.sql) | DDL | Definición de las 18 tablas relacionales del sistema TRAVELINK | Taller 3 / Base de Datos | Producción / Verificado |
| [`sql/datos_minimos.sql`](../sql/datos_minimos.sql) | DML Inicial | Roles del sistema y cuenta administrativa inicial | Taller 3 / Base de Datos | Producción / Verificado |
| [`sql/seed_pruebas.sql`](../sql/seed_pruebas.sql) | DML Sintético | 4 usuarios de prueba (hashes BCrypt reales), tours 100/101 y reservas 500/501 | Taller 3 / Base de Datos | Implementado / Verificado |

---

## 3. Arquitectura del Sistema (`docs/arquitectura/`)

| Documento | Tipo | Descripción | Ítem (Taller / Fase) | Estado |
|---|---|---|:---:|:---:|
| [`docs/arquitectura/README.md`](arquitectura/README.md) | Índice | Índice de arquitectura y componentes del sistema | Taller 2 / Arquitectura | Implementado |
| [`docs/arquitectura/ARQUITECTURA.md`](arquitectura/ARQUITECTURA.md) | Arquitectura | Especificación MVC, capas, dualidad JPA/JDBC, catálogo `pom.xml` | Taller 2 / Arquitectura | Implementado |

---

## 4. Ingeniería de Software (`docs/ingenieria/`)

| Submódulo | Documento / Ruta | Descripción | Ítem (Taller / Fase) | Estado |
|---|---|---|:---:|:---:|
| **General** | [`docs/ingenieria/README.md`](ingenieria/README.md) | Índice consolidado de artefactos de ingeniería de software | Talleres 1 a 6 | Implementado |
| **Procesos** | [`docs/ingenieria/procesos/README.md`](ingenieria/procesos/README.md) | Índice de procesos de negocio de la plataforma | Taller 1 / Procesos | Implementado |
| **Procesos** | [`docs/ingenieria/procesos/DIAGRAMAS_PROCESOS.md`](ingenieria/procesos/DIAGRAMAS_PROCESOS.md) | Flujos funcionales de reservas, comisiones (15%), agencias y tours | Taller 1 / Procesos | Implementado |
| **Procesos** | [`docs/ingenieria/procesos/DIAGRAMAS_PROCESOS.md`](ingenieria/procesos/DIAGRAMAS_PROCESOS.md) | Diagramas de flujo y secuencia Mermaid de los procesos del código | Taller 1 / Procesos | Implementado |
| **Diseño** | [`docs/ingenieria/diseno/README.md`](ingenieria/diseno/README.md) | Índice de diseño estructural y de clases | Taller 2 / Diseño | Implementado |
| **Diseño** | [`docs/ingenieria/diseno/PATRONES.md`](ingenieria/diseno/PATRONES.md) | Patrones arquitecturales y capas del sistema | Taller 2 / Diseño | Implementado |
| **Diseño** | [`docs/ingenieria/diseno/PATRONES.md`](ingenieria/diseno/PATRONES.md) | Implementación de DTO, DAO vs Repository, MVC y principios SOLID | Taller 2 / Diseño | Implementado |
| **Diseño** | [`docs/ingenieria/diseno/DIAGRAMA_CLASES.md`](ingenieria/diseno/DIAGRAMA_CLASES.md) | Diagramas de clases orientados a objetos (entidades, DTOs, servicios, repos, controladores) | Taller 2 / Diseño | Implementado |
| **Base de Datos** | [`docs/ingenieria/base-datos/README.md`](ingenieria/base-datos/README.md) | Índice del modelo de datos e integridad referencial | Taller 3 / Base de Datos | Implementado |
| **Base de Datos** | [`docs/ingenieria/base-datos/MODELO_LOGICO_FISICO.md`](ingenieria/base-datos/MODELO_LOGICO_FISICO.md) | Esquema relacional de tablas, llaves foráneas y reglas de integridad | Taller 3 / Base de Datos | Implementado |
| **Base de Datos** | [`docs/ingenieria/base-datos/MODELO_LOGICO_FISICO.md`](ingenieria/base-datos/MODELO_LOGICO_FISICO.md) | Diagrama Entidad-Relación (erDiagram Mermaid), modelo lógico y físico con índices | Taller 3 / Base de Datos | Implementado |
| **Base de Datos** | [`docs/ingenieria/base-datos/DICCIONARIO_DATOS.md`](ingenieria/base-datos/DICCIONARIO_DATOS.md) | Diccionario de datos exhaustivo de las 18 tablas con tipos, PK/FK y nulos | Taller 3 / Base de Datos | Implementado |
| **UX / UI** | [`docs/ingenieria/ux-ui/README.md`](ingenieria/ux-ui/README.md) | Índice de interfaces de usuario y experiencia visual | Taller 4 / UX-UI | Implementado |
| **UX / UI** | [`docs/ingenieria/ux-ui/GUIA_ESTILOS_UX.md`](ingenieria/ux-ui/GUIA_ESTILOS_UX.md) | Arquitectura frontend desacoplada (HTML5/CSS3/Vanilla JS), accesibilidad | Taller 4 / UX-UI | Implementado |
| **UX / UI** | [`docs/ingenieria/ux-ui/PROTOTIPOS.md`](ingenieria/ux-ui/PROTOTIPOS.md) | Inventario de las 34 páginas HTML por rol, componentes clave y endpoints | Taller 4 / UX-UI | Implementado |
| **Reportes** | [`docs/ingenieria/reportes/README.md`](ingenieria/reportes/README.md) | Índice de especificación y arquitectura de reportes | Taller 5 / Reportes | Implementado |
| **Reportes** | [`docs/ingenieria/reportes/MODELO_REPORTES.md`](ingenieria/reportes/MODELO_REPORTES.md) | Generación PDF (OpenPDF) y carga masiva/exportación Excel (Apache POI) | Taller 5 / Reportes | Implementado |
| **Reportes** | [`docs/ingenieria/reportes/MODELO_REPORTES.md`](ingenieria/reportes/MODELO_REPORTES.md) | Columnas por reporte, streaming SXSSFWorkbook, nomenclatura y ficheros | Taller 5 / Reportes | Implementado |
| **Código Fuente** | [`docs/ingenieria/codigo-fuente/README.md`](ingenieria/codigo-fuente/README.md) | Índice de organización y modularización del código fuente | Taller 6 / Código | Implementado |
| **Código Fuente** | [`docs/ingenieria/codigo-fuente/RECURSOS_CODIGO.md`](ingenieria/codigo-fuente/RECURSOS_CODIGO.md) | Mapeo de paquetes `com.travelink.*`, controladores, entidades, servicios y DAOs | Taller 6 / Código | Implementado |
| **Código Fuente** | [`docs/ingenieria/codigo-fuente/RECURSOS_CODIGO.md`](ingenieria/codigo-fuente/RECURSOS_CODIGO.md) | Árbol de paquetes, justificación pom.xml, perfiles properties y logback | Taller 6 / Código | Implementado |

---

## 5. Auditoría y Seguridad del Software (`docs/seguridad/`)

| Submódulo | Documento / Ruta | Descripción | Ítem (Taller / Fase) | Estado |
|---|---|---|:---:|:---:|
| **Índice** | [`docs/seguridad/README.md`](seguridad/README.md) | Índice general del módulo de seguridad y auditoría | Talleres 7 a 11 | Implementado |
| **Plan Maestro** | [`docs/seguridad/01-plan/README.md`](seguridad/01-plan/README.md) | Índice del plan de auditoría de seguridad | Taller 7 / Plan | Implementado |
| **Plan Maestro** | [`docs/seguridad/01-plan/PLAN_PRUEBAS_SEGURIDAD.md`](seguridad/01-plan/PLAN_PRUEBAS_SEGURIDAD.md) | Estrategia, alcance, 8 criterios de rúbrica, cronograma S1-S6 y RACI | Taller 7 / Plan | Implementado |
| **Análisis** | [`docs/seguridad/02-analisis/README.md`](seguridad/02-analisis/README.md) | Índice de análisis estático, dependencias y triaje | Talleres 8 y 9 | Implementado |
| **SAST** | [`docs/seguridad/02-analisis/ANALISIS_ESTATICO.md`](seguridad/02-analisis/ANALISIS_ESTATICO.md) | Inspección estática de código fuente y configuraciones de Spring Security | Taller 8 / SAST | Implementado |
| **Seguridad Java** | [`docs/seguridad/02-analisis/CONSIDERACIONES_SEGURIDAD_JAVA.md`](seguridad/02-analisis/CONSIDERACIONES_SEGURIDAD_JAVA.md) | Resumen SecurityConfig (BCrypt, CSRF, CORS, RBAC) vinculado a H-01..H-09 | Taller 8 / Seguridad Java | Implementado |
| **Superficie** | [`docs/seguridad/02-analisis/mapa-endpoints.md`](seguridad/02-analisis/mapa-endpoints.md) | Inventario de 56 endpoints, métodos HTTP, handlers y roles requeridos | Taller 8 / Superficie | Implementado |
| **SCA** | [`docs/seguridad/02-analisis/tabla-dependencias-cve.md`](seguridad/02-analisis/tabla-dependencias-cve.md) | Catálogo de 179 CVEs y estado de remediación tras Spring Boot 3.3.13 | Taller 8 / SCA | Implementado |
| **ZAP Triaje** | [`docs/seguridad/02-analisis/VALIDACION_FALSOS_POSITIVOS.md`](seguridad/02-analisis/VALIDACION_FALSOS_POSITIVOS.md) | Triaje de las 16 alertas ZAP (TP/FP/Info) con validaciones HTTP manuales | Taller 9 / DAST | Implementado |
| **Fuzzing** | [`docs/seguridad/02-analisis/INFORME_FUZZING_RESILIENCIA.md`](seguridad/02-analisis/INFORME_FUZZING_RESILIENCIA.md) | Análisis de resiliencia FFUF: 5 respuestas HTTP 500 y status 0 | Taller 11 / Fuzzing | Implementado |
| **Hallazgos** | [`docs/seguridad/03-hallazgos/README.md`](seguridad/03-hallazgos/README.md) | Índice de las 9 vulnerabilidades identificadas (H-01 a H-09) | Taller 10 / Hallazgos | Implementado |
| **Hallazgos** | [`docs/seguridad/03-hallazgos/H-01.md`](seguridad/03-hallazgos/H-01.md) a `H-09.md` | Fichas técnicas individuales H-01 a H-09 con severidades CVSS | Taller 10 / Hallazgos | Implementado |
| **PoCs** | [`docs/seguridad/04-poc/README.md`](seguridad/04-poc/README.md) | Índice de pruebas de concepto reproducibles | Taller 10 / PoCs | Implementado |
| **PoCs** | [`docs/seguridad/04-poc/PoC-01-IDOR.md`](seguridad/04-poc/PoC-01-IDOR.md) a `PoC-09` | 9 guías de explotación técnica reproducibles con evidencias reales | Taller 10 / PoCs | Implementado |
| **Informe** | [`docs/seguridad/05-informe/README.md`](seguridad/05-informe/README.md) | Índice de informes ejecutivos y entregables de cierre | Taller 12 / Informe | Implementado |
| **Informe** | [`docs/seguridad/05-informe/INFORME_FINAL.md`](seguridad/05-informe/INFORME_FINAL.md) | Reporte ejecutivo consolidado, carga JMeter y plan de remediación | Taller 12 / Informe | Implementado |
| **Rúbrica** | [`docs/seguridad/05-informe/CHECKLIST_RUBRICA.md`](seguridad/05-informe/CHECKLIST_RUBRICA.md) | Matriz oficial contra los 8 criterios de evaluación académica (105/100 pts) | Taller 12 / Rúbrica | Implementado |
| **Cuentas** | [`docs/seguridad/05-informe/CUENTAS_PRUEBA.md`](seguridad/05-informe/CUENTAS_PRUEBA.md) | Credenciales sintéticas y roles del seed local de pruebas | Taller 7 / Cuentas | Implementado |
| **Sustentación** | [`docs/seguridad/05-informe/PRESENTACION_BREVE.md`](seguridad/05-informe/PRESENTACION_BREVE.md) | Guion de defensa técnica de 10 diapositivas para exposición rápida | Taller 12 / Sustentación | Implementado |

---

## 6. Pruebas de Software y Aseguramiento de Calidad (`docs/pruebas-software/` y `pruebas/`)

| Activo / Ruta | Tipo | Descripción | Ítem (Taller / Fase) | Estado |
|---|---|---|:---:|:---:|
| [`docs/pruebas-software/README.md`](pruebas-software/README.md) | Índice | Índice del módulo de aseguramiento de calidad y pruebas | Taller 12 / Pruebas | Implementado |
| [`docs/pruebas-software/PLAN_PRUEBAS_SOFTWARE.md`](pruebas-software/PLAN_PRUEBAS_SOFTWARE.md) | Plan Maestro | Plan de pruebas, 20 unitarias, Testcontainers, JMeter, ZAP y JaCoCo | Taller 12 / Pruebas | Implementado |
| [`docs/pruebas-software/PLAN_PRUEBAS_SOFTWARE.md`](pruebas-software/PLAN_PRUEBAS_SOFTWARE.md) | Estrategia | Pirámide de pruebas, suite de 20 casos unitarios y consideraciones CI | Taller 12 / Pruebas | Implementado |
| [`pruebas/README.md`](../pruebas/README.md) | Manual | Manual operativo de pruebas, acceso `app` vs `localhost` y orden de ejecución | Taller 7 / Manual Pruebas | Implementado |
| [`pruebas/entorno/reset-db.ps1`](../pruebas/entorno/reset-db.ps1) / [`.sh`](../pruebas/entorno/reset-db.sh) | Entorno | Reinicio controlado de contenedores y volúmenes Docker con guard de seguridad | Taller 7 / Entorno | Implementado |
| [`pruebas/entorno/seed.ps1`](../pruebas/entorno/seed.ps1) / [`.sh`](../pruebas/entorno/seed.sh) | Entorno | Carga de datos sintéticos con hashes BCrypt reales | Taller 7 / Entorno | Implementado |
| [`pruebas/seguridad/scripts/`](../pruebas/seguridad/scripts/) | Scripts | Scripts de escaneo (SCA, Nmap, Nikto, ZAP, FFUF, SQLMap) | Talleres 8 a 10 | Implementado |
| [`pruebas/rendimiento/scripts/`](../pruebas/rendimiento/scripts/) | Scripts | Automatización de pruebas de carga con Apache JMeter (100, 300, 500 hilos) | Taller 11 / Rendimiento | Implementado |
| [`pruebas/evidencias/`](../pruebas/evidencias/) | Evidencias | Reportes generados (Dependency-Check, FFUF, JMeter, Nmap, Nikto, SQLMap, ZAP) | Talleres 8 a 11 | Implementado |
| [`pruebas/evidencias/integracion-testcontainers.txt`](../pruebas/evidencias/integracion-testcontainers.txt) | Evidencia | Salida de ejecución JUnit 5 de `ReservaPersistenciaIntegracionTest` | Taller 12 / Integración | Implementado |

---

## 7. Control de Versiones y Gestión en Git (`docs/control-versiones/`)

| Documento | Tipo | Descripción | Ítem (Taller / Fase) | Estado |
|---|---|---|:---:|:---:|
| [`docs/control-versiones/README.md`](control-versiones/README.md) | Índice | Índice de políticas de control de versiones y trazabilidad | Taller 12 / Git | Implementado |
| [`docs/control-versiones/CONTROL_VERSIONES.md`](control-versiones/CONTROL_VERSIONES.md) | Especificación | Remoto, ramas, PRs (#1, #3, #4, #6), grafo de 40 commits y rollback real | Taller 12 / Git | Implementado |
| [`docs/control-versiones/CONTROL_VERSIONES.md`](control-versiones/CONTROL_VERSIONES.md) | Flujo | Estrategia de ramas (GitHub Flow), commits semánticos y reglas de protección | Taller 12 / Git | Implementado |

---

## 8. Despliegue y Operaciones (`docs/despliegue/` y `despliegue/`)

| Activo / Ruta | Tipo | Descripción | Ítem (Taller / Fase) | Estado |
|---|---|---|:---:|:---:|
| [`docs/despliegue/README.md`](despliegue/README.md) | Índice | Índice de planes y guías operativas de despliegue | Despliegue / Índice | Implementado |
| [`docs/despliegue/PLAN_DESPLIEGUE.md`](despliegue/PLAN_DESPLIEGUE.md) | Plan Maestro | Plan según Sesión 15: fundamentos, JAR/WAR, Nginx TLS, Docker prod, JVM, CI/CD | Despliegue / Plan Maestro | Implementado |
| [`docs/despliegue/GUIA_DESPLIEGUE.md`](despliegue/GUIA_DESPLIEGUE.md) | Guía Operativa | Procedimiento de despliegue continuo, docker-compose prod, systemd y rollback | Despliegue / Guía | Implementado |
| [`despliegue/README.md`](../despliegue/README.md) | Infraestructura | Especificación de la carpeta ejecutable de despliegue e infraestructura | Despliegue / Infraestructura | Implementado |
| [`despliegue/docker/docker-compose.prod.yml`](../despliegue/docker/docker-compose.prod.yml) | Orquestación | Configuración multicontenedor de producción con Nginx perimetral | Despliegue / Docker | Implementado |
| [`despliegue/servidor/nginx/travelink.conf`](../despliegue/servidor/nginx/travelink.conf) | Servidor Web | Proxy inverso HTTPS con terminación TLS, HSTS, CSP y hardening OWASP | Despliegue / Nginx TLS | Implementado |
| [`despliegue/servidor/travelink.service`](../despliegue/servidor/travelink.service) | Servicio OS | Archivo de unidad systemd para ejecución standalone del JAR con sandboxing | Despliegue / Systemd | Implementado |
| [`despliegue/servidor/setup-servidor.sh`](../despliegue/servidor/setup-servidor.sh) | Scripts | Aprovisionamiento automatizado de servidor Linux (Java 21, UFW, Nginx) | Despliegue / Servidor | Implementado |
| [`despliegue/scripts/deploy.sh`](../despliegue/scripts/deploy.sh) / [`.ps1`](../despliegue/scripts/deploy.ps1) | Scripts | Automatización de despliegue, healthcheck y smoke test HTTP | Despliegue / Scripts | Implementado |
| [`despliegue/scripts/rollback.sh`](../despliegue/scripts/rollback.sh) / [`.ps1`](../despliegue/scripts/rollback.ps1) | Scripts | Procedimiento automatizado de reversión a versión anterior estable o tag | Despliegue / Scripts | Implementado |
| [`despliegue/maven/build.sh`](../despliegue/maven/build.sh) / [`.ps1`](../despliegue/maven/build.ps1) | Scripts | Empaquetado de producción y verificación de checksum SHA-256 | Despliegue / Maven | Implementado |
| [`despliegue/maven/settings.xml.example`](../despliegue/maven/settings.xml.example) | Configuración | Plantilla corporativa de repositorios Maven y credenciales desacopladas | Despliegue / Maven | Implementado |
| [`despliegue/evidencias/antes/despliegue-local-3.3.13.txt`](../despliegue/evidencias/antes/despliegue-local-3.3.13.txt) | Evidencia | Evidencia histórica de despliegue local inicial con Spring Boot 3.3.13 | Despliegue / Evidencia | Implementado |
| [`despliegue/evidencias/cabeceras-despues-tls.txt`](../despliegue/evidencias/cabeceras-despues-tls.txt) | Evidencia | Cabeceras HTTP reales emitidas por Nginx con TLS, HSTS y CSP | Despliegue / Evidencia | Implementado |
| [`despliegue/evidencias/comparativa-cabeceras-antes-despues.txt`](../despliegue/evidencias/comparativa-cabeceras-antes-despues.txt) | Evidencia | Matriz comparativa de seguridad perimetral antes vs después del despliegue | Despliegue / Evidencia | Implementado |
| [`despliegue/evidencias/prueba-rollback.txt`](../despliegue/evidencias/prueba-rollback.txt) | Evidencia | Ejecución real y verificación de convergencia del script de rollback | Despliegue / Evidencia | Implementado |
| [`despliegue/evidencias/demostracion-rollback-git.txt`](../despliegue/evidencias/demostracion-rollback-git.txt) | Evidencia | Salida de rollback práctico en Git (revert, restore, checkout) | Taller 12 / Despliegue | Implementado |
| [`despliegue/evidencias/grafo-git.txt`](../despliegue/evidencias/grafo-git.txt) | Evidencia | Grafo de 40 commits del repositorio (`git log --graph --oneline --all`) | Taller 12 / Despliegue | Implementado |

---

## 9. Exposición y Sustentación Técnica (`docs/exposicion/`)

| Documento | Tipo | Descripción | Ítem (Taller / Fase) | Estado |
|---|---|---|:---:|:---:|
| [`docs/exposicion/README.md`](exposicion/README.md) | Índice | Índice de materiales de presentación académica | Taller 12 / Exposición | Implementado |
| [`docs/exposicion/GUION_EXPOSICION.md`](exposicion/GUION_EXPOSICION.md) | Guion Maestro | Guion de 14 diapositivas ejecutivas y guion de demo en vivo vinculando código | Taller 12 / Exposición | Implementado |
| [`docs/exposicion/GUION_5MIN_MONITOREO.md`](exposicion/GUION_5MIN_MONITOREO.md) | Guion 5 Min | Guion de 8 diapositivas (5 min) enfocado en Monitoreo, Observabilidad, Mantenimiento y Pruebas | Taller 12 / Exposición | Implementado / Validado |

---

## 10. Monitoreo y Observabilidad (`docs/monitoreo/` y `monitoreo/`)

| Activo / Ruta | Tipo | Descripción | Ítem (Taller / Fase) | Estado |
|---|---|---|:---:|:---:|
| [`docs/monitoreo/PLAN_MONITOREO.md`](monitoreo/PLAN_MONITOREO.md) | Plan Maestro | Plan de observabilidad 360°: logs (Loki/Promtail), métricas (Prometheus), trazas (Zipkin), alertas y salud OS | Monitoreo / Plan | VALIDADO (Local) / DOCUMENTADO (Render) |
| [`docs/monitoreo/HERRAMIENTAS.md`](monitoreo/HERRAMIENTAS.md) | Catálogo | Fichas técnicas, versiones, URLs locales, archivos de configuración y enlaces compartidos en GitHub | Monitoreo / Catálogo | VALIDADO |
| [`monitoreo/docker-compose.monitoring.yml`](../monitoreo/docker-compose.monitoring.yml) | Orquestación | Stack Docker de 7 contenedores (Prometheus, Grafana, Loki, Promtail, Node Exporter, cAdvisor, Zipkin) | Monitoreo / Stack | VALIDADO |
| [`monitoreo/prometheus/prometheus.yml`](../monitoreo/prometheus/prometheus.yml) | Configuración | Reglas de scrape (app, prometheus, node-exporter, cadvisor) cada 3s - 5s | Monitoreo / Prometheus | VALIDADO |
| [`monitoreo/prometheus/alert_rules.yml`](../monitoreo/prometheus/alert_rules.yml) | Reglas de Alerta | 6 alertas críticas (App Down, Heap >85%, tasa de errores 5xx, latencia p95, CPU, disco) | Monitoreo / Alertas | VALIDADO |
| [`monitoreo/grafana/dashboards/travelink-overview.json`](../monitoreo/grafana/dashboards/travelink-overview.json) | Dashboard | Tablero unificado en Grafana: KPIs de negocio, throughput HTTP, latencias, JVM Heap, OS y logs Loki | Monitoreo / Grafana | VALIDADO |
| [`monitoreo/loki/loki-config.yml`](../monitoreo/loki/loki-config.yml) | Configuración | Configuración de almacenamiento en TSDB, esquemas y retención de logs | Monitoreo / Loki | VALIDADO |
| [`monitoreo/promtail/promtail-config.yml`](../monitoreo/promtail/promtail-config.yml) | Configuración | Ingesta y parsing estructurado de logs y trazas (traceId, spanId, level) | Monitoreo / Promtail | VALIDADO |
| [`monitoreo/evidencias/`](../monitoreo/evidencias/) | Evidencias | 7 evidencias reales de ejecución (compose ps, targets UP, PromQL, LogQL, dashboard, alertas, JMeter) | Monitoreo / Evidencias | VALIDADO |

---

## 11. Mantenimiento y Continuidad Operativa (`docs/mantenimiento/` y `mantenimiento/`)

| Activo / Ruta | Tipo | Descripción | Ítem (Taller / Fase) | Estado |
|---|---|---|:---:|:---:|
| [`docs/mantenimiento/PLAN_MANTENIMIENTO.md`](mantenimiento/PLAN_MANTENIMIENTO.md) | Plan Maestro | Plan de continuidad operativa: matriz de tareas, política de backups (RPO/RTO), dependencias y Aiven PITR | Mantenimiento / Plan | VALIDADO (Local) / DOCUMENTADO (Cloud) |
| [`mantenimiento/scripts/backup.ps1`](../mantenimiento/scripts/backup.ps1) | Script PowerShell | Respaldo consistente con mysqldump --single-transaction, compresión Gzip, checksum SHA-256 y retención | Mantenimiento / Scripts | VALIDADO |
| [`mantenimiento/scripts/backup.sh`](../mantenimiento/scripts/backup.sh) | Script Bash | Versión Linux/Bash del script de respaldo transaccional, compresión Gzip y hash SHA-256 | Mantenimiento / Scripts | VALIDADO |
| [`mantenimiento/scripts/restore.ps1`](../mantenimiento/scripts/restore.ps1) | Script PowerShell | Restauración automatizada con verificación obligatoria de checksum SHA-256 | Mantenimiento / Scripts | VALIDADO |
| [`mantenimiento/scripts/restore.sh`](../mantenimiento/scripts/restore.sh) | Script Bash | Versión Linux/Bash de restauración automatizada con validación criptográfica SHA-256 | Mantenimiento / Scripts | VALIDADO |
| [`mantenimiento/scripts/restore-test.ps1`](../mantenimiento/scripts/restore-test.ps1) | Script PowerShell | Prueba de restauración en base temporal (travelink_restore_test) y conteo de tablas maestras | Mantenimiento / Scripts | VALIDADO |
| [`mantenimiento/scripts/restore-test.sh`](../mantenimiento/scripts/restore-test.sh) | Script Bash | Versión Linux/Bash de prueba de restauración automatizada en base temporal | Mantenimiento / Scripts | VALIDADO |
| [`mantenimiento/scripts/limpiar-logs.ps1`](../mantenimiento/scripts/limpiar-logs.ps1) | Script PowerShell | Purga de archivos .log.gz y temporales que superen los 14 días de retención | Mantenimiento / Scripts | VALIDADO |
| [`mantenimiento/scripts/limpiar-logs.sh`](../mantenimiento/scripts/limpiar-logs.sh) | Script Bash | Versión Linux/Bash de purga y limpieza periódica de logs históricos | Mantenimiento / Scripts | VALIDADO |
| [`mantenimiento/scripts/healthcheck.ps1`](../mantenimiento/scripts/healthcheck.ps1) | Script PowerShell | Sondeo Actuator (/actuator/health), verificación de status UP y código de salida (0 OK, 1 Alerta) | Mantenimiento / Scripts | VALIDADO |
| [`mantenimiento/scripts/healthcheck.sh`](../mantenimiento/scripts/healthcheck.sh) | Script Bash | Versión Linux/Bash de healthcheck con curl y validación JSON de status UP | Mantenimiento / Scripts | VALIDADO |
| [`mantenimiento/scripts/reset-datos-prueba.ps1`](../mantenimiento/scripts/reset-datos-prueba.ps1) | Script PowerShell | Reinicio de base de datos con seed_pruebas.sql con bloqueo automático para entornos productivos/Aiven | Mantenimiento / Scripts | VALIDADO |
| [`mantenimiento/scripts/reset-datos-prueba.sh`](../mantenimiento/scripts/reset-datos-prueba.sh) | Script Bash | Versión Linux/Bash de reinicio controlado de seed de prueba | Mantenimiento / Scripts | VALIDADO |
| [`mantenimiento/scripts/imagenes-en-uso.sql`](../mantenimiento/scripts/imagenes-en-uso.sql) | Script SQL | Consulta para auditar rutas de imágenes activas en base de datos (TourImagen, Agencia, Pago) | Mantenimiento / Scripts | VALIDADO |
| [`mantenimiento/cron/crontab.txt`](../mantenimiento/cron/crontab.txt) | Automatización | Cron maestro: backup diario 02:00, restore-test semanal dom 02:30, purga dom 03:00, healthcheck c/5min | Mantenimiento / Cron | VALIDADO |
| [`mantenimiento/systemd/`](../mantenimiento/systemd/) | Orquestación OS | Unidades .service y .timer para backup diario, purga semanal y healthcheck continuo en Linux | Mantenimiento / Systemd | VALIDADO |
| [`mantenimiento/logrotate/travelink`](../mantenimiento/logrotate/travelink) | Configuración OS | Rotación diaria de logs de aplicación con compresión retardada y retención de 14 días | Mantenimiento / Logrotate | VALIDADO |
| [`mantenimiento/windows/registrar-tarea.ps1`](../mantenimiento/windows/registrar-tarea.ps1) | Automatización OS | Registro y consulta de tarea programada TRAVELINK_Backup_Diario en Windows Task Scheduler | Mantenimiento / Windows | VALIDADO |
| [`.github/workflows/mantenimiento.yml`](../.github/workflows/mantenimiento.yml) | Pipeline CI/CD | Workflow semanal: escaneo de vulnerabilidades SCA con Trivy y reporte de actualizaciones Maven | Mantenimiento / CI-CD | VALIDADO |
| [`.github/dependabot.yml`](../.github/dependabot.yml) | Automatización | Configuración de Dependabot para actualizaciones automáticas de Maven, Docker y GitHub Actions | Mantenimiento / Dependabot | VALIDADO |
| [`mantenimiento/evidencias/`](../mantenimiento/evidencias/) | Evidencias | 6 evidencias reales de ejecución (backup, checksum, restore-test, rotación, tarea Windows, workflow) | Mantenimiento / Evidencias | VALIDADO |

