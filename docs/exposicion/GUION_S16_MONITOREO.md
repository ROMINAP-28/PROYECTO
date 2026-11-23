# Guion Ejecutivo de Exposición (5 Minutos)
## Monitoreo, Observabilidad 360°, Mantenimiento y Calidad de Software - TRAVELINK

> **Tiempo Total Estricto:** 5:00 - 5:20 minutos  
> **Formato:** 8 Diapositivas Ejecutivas  
> **Repositorio Oficial:** [`ROMINAP-28/PROYECTO`](https://github.com/ROMINAP-28/PROYECTO)  
> **Rama de Trabajo:** `feature/monitoreo-mantenimiento`  

---

### Resumen de Tiempos y Estructura

| Diapositiva | Tema Principal | Tiempo Estimado | Evidencia Clave a Mostrar |
| :---: | :--- | :---: | :--- |
| **1** | Portada y Equipo de Ingeniería | **0:30 min** | Portada formal con roles y datos del proyecto |
| **2** | Arquitectura de Monitoreo y Observabilidad 360° | **0:45 min** | [`monitoreo/docker-compose.monitoring.yml`](../../monitoreo/docker-compose.monitoring.yml) y `docker-compose-ps.txt` |
| **3** | Gestión de Logs, Ingesta y Correlación | **0:40 min** | Logback JSON estructurado y [`loki-query-logql.txt`](../../monitoreo/evidencias/loki-query-logql.txt) |
| **4** | Performance, Métricas de Negocio y Trazas | **0:45 min** | Métricas Micrometer, [`prometheus-promql-query.txt`](../../monitoreo/evidencias/prometheus-promql-query.txt) y Zipkin |
| **5** | Salud del Host, Contenedores y Alertas | **0:40 min** | [`alert_rules.yml`](../../monitoreo/prometheus/alert_rules.yml) y [`alerta-disparada-app-down.txt`](../../monitoreo/evidencias/alerta-disparada-app-down.txt) |
| **6** | Mantenimiento Preventivo, Backups y RPO/RTO | **0:40 min** | [`backup-real.txt`](../../mantenimiento/evidencias/backup-real.txt), [`restore-test.txt`](../../mantenimiento/evidencias/restore-test.txt) y Task Scheduler |
| **7** | Catálogo de Herramientas y Rutas en GitHub | **0:35 min** | Tabla de herramientas y enlaces compartidos en GitHub |
| **8** | Resultados de Pruebas y Pipeline CI en Verde | **0:45 min** | JUnit (23 tests OK), JaCoCo, ZAP, JMeter y CI Runs #23 a #25 |

---

### Diapositiva 1: Portada y Presentación del Equipo (0:30 min)

* **Título:** TRAVELINK: Sistema Turístico de Alto Rendimiento  
* **Subtítulo:** Observabilidad 360°, Continuidad Operativa, Respaldo Automatizado y Calidad de Software  
* **Integrantes del Equipo:**
  * **[NOMBRE COMPLETO 1]** - Arquitectura de Software & DevSecOps
  * **[NOMBRE COMPLETO 2]** - Observabilidad & Monitoreo (Prometheus / Grafana / Loki)
  * **[NOMBRE COMPLETO 3]** - Base de Datos, Continuidad Operativa & Backups
  * **[NOMBRE COMPLETO 4]** - Pruebas Automatizadas & Calidad de Código
* **Institución / Curso:** [NOMBRE DE LA UNIVERSIDAD / INSTITUTO] - Taller de Proyectos de Software / Ingeniería de Software
* **Fecha:** Noviembre 2026

> **Guion del Expositor (30 seg):**  
> *"Buenos días docente y miembros del jurado. Hoy presentamos el cierre técnico de la plataforma TRAVELINK, enfocado en los pilares que transforman una aplicación de software en un sistema de misión crítica: observabilidad 360° en tiempo real, continuidad operativa con respaldo verificado, mantenimiento programado y validación rigurosa de calidad y seguridad."*

---

### Diapositiva 2: Arquitectura de Observabilidad 360° (0:45 min)

* **Componentes del Stack:**
  * **Backend (Spring Boot 3.3.13):** Instrumentado con Micrometer Prometheus, OpenTelemetry Tracing bridge y Logback estructurado.
  * **Seguridad Perimetral:** Endpoint `/actuator/prometheus` accesible únicamente desde la red interna de monitoreo; bloqueado hacia el exterior en Nginx con HTTP 403.
  * **Métricas:** Prometheus v2.52.0 scrapeando métricas cada 3 a 5 segundos.
  * **Logs:** Promtail v2.9.8 recolectando logs en vivo y enviándolos a Grafana Loki v2.9.8.
  * **Trazabilidad:** Zipkin v3.3.0 recibiendo spans distribuidos vía OpenTelemetry.
  * **Salud del Host & Contenedores:** Node Exporter v1.8.1 (host / VM WSL2) y cAdvisor v0.49.1 (métricas por contenedor Docker).
  * **Visualización Centralizada:** Grafana v10.4.2 con aprovisionamiento automático de datasources y tablero maestro.
* **Evidencia a Mostrar:**
  * Archivo de orquestación: [`monitoreo/docker-compose.monitoring.yml`](../../monitoreo/docker-compose.monitoring.yml)
  * Evidencia de contenedores activos: [`monitoreo/evidencias/docker-compose-ps.txt`](../../monitoreo/evidencias/docker-compose-ps.txt)

> **Guion del Expositor (45 seg):**  
> *"Nuestra arquitectura de monitoreo se implementó mediante un stack de 7 contenedores Docker interconectados en la red interna de la aplicación. No dejamos puertos sensibles expuestos: el actuator de Prometheus y metrics están blindados perimetralmente por Nginx, permitiendo el scrapeo únicamente a nivel interno. Cubrimos los 3 pilares de la observabilidad moderna: métricas de series temporales con Prometheus, logs centralizados con Loki y Promtail, y trazas distribuidas con Zipkin y OpenTelemetry, complementados con cAdvisor y Node Exporter para la salud de infraestructura."*

---

### Diapositiva 3: Gestión de Logs, Ingesta Centralizada y Correlación (0:40 min)

* **Estrategia de Logging:**
  * **Patrón Logback con Trazas:** Inyección de `%X{traceId:-} %X{spanId:-}` en la salida estándar y archivos rodantes.
  * **Doble Salida:** `logs/travelink.log` (legible para humanos) y `logs/travelink-json.log` (formato JSON estructurado para parsers de alto volumen).
  * **Ingesta con Promtail:** Lectura de descriptores sin impacto en I/O de la aplicación.
  * **Almacenamiento TSDB en Loki:** Almacenamiento eficiente sin indexar el texto completo, indexando únicamente etiquetas (`job`, `level`, `app`).
* **Consulta LogQL Demostrativa:**
  ```logql
  {job="travelink-app"} |= "ERROR"
  ```
* **Evidencia a Mostrar:**
  * Salida de consulta de log de error con traceId correlacionado: [`monitoreo/evidencias/loki-query-logql.txt`](../../monitoreo/evidencias/loki-query-logql.txt)

> **Guion del Expositor (40 seg):**  
> *"Para resolver incidentes en segundos, estandarizamos la salida de logs en formato estructurado. Cada evento de log registrado por Logback contiene automáticamente el traceId y spanId del hilo que procesó la solicitud HTTP. Promtail ingesta estos registros y Loki los almacena en su base TSDB. Como se evidencia en nuestra prueba, una simple consulta LogQL localiza de inmediato un fallo de autenticación o reserva con su stack trace completo y su ID de traza."*

---

### Diapositiva 4: Rendimiento, Métricas de Negocio y Trazabilidad (0:45 min)

* **Métricas de Negocio Propias (Micrometer Counters):**
  * `reservas.creadas`: Contabiliza transacciones exitosas de reservas en `ReservaServicioImpl.java`.
  * `reservas.canceladas`: Monitorea tasa de cancelaciones de usuarios.
  * `auth.logins.fallidos`: Detecta intentos de inicio de sesión sospechosos o ataques de fuerza bruta en `LoginAttemptService.java`.
* **Trazabilidad Distribuida (Zipkin + OTel):**
  * Spans HTTP con código de respuesta, método, URI y duración exacta de procesamiento.
  * Exportación directa hacia Zipkin en puerto interno 9411.
* **Consulta PromQL Demostrativa:**
  ```promql
  rate(http_server_requests_seconds_count[1m])
  histogram_quantile(0.95, sum(rate(http_server_requests_seconds_bucket[1m])) by (le))
  ```
* **Evidencia a Mostrar:**
  * Dashboard de Grafana en tiempo real: [`monitoreo/grafana/dashboards/travelink-overview.json`](../../monitoreo/grafana/dashboards/travelink-overview.json)
  * Targets en estado UP: [`monitoreo/evidencias/prometheus-targets-up.txt`](../../monitoreo/evidencias/prometheus-targets-up.txt)
  * Salida de PromQL: [`monitoreo/evidencias/prometheus-promql-query.txt`](../../monitoreo/evidencias/prometheus-promql-query.txt)

> **Guion del Expositor (45 seg):**  
> *"No nos limitamos a medir CPU y RAM; implementamos observabilidad del negocio. Diseñamos controladores que alimentan métricas con Micrometer para monitorear reservas creadas, canceladas y logins fallidos en tiempo real. En el dashboard unificado de Grafana visualizamos la latencia en el percentil 95, el consumo de memoria Heap de la JVM y la correlación directa entre picos de tráfico y tiempos de respuesta de base de datos."*

---

### Diapositiva 5: Salud del Host, Contenedores y Sistema de Alertas (0:40 min)

* **Monitoreo de Infraestructura:**
  * **cAdvisor:** Monitoreo granular por contenedor (uso de CPU %, memoria residente, I/O de disco y red de `travelink-app`, `mysql`, `nginx`).
  * **Node Exporter:** Métricas de hardware del sistema operativo (en Docker Desktop supervisa la máquina virtual WSL2).
* **Matriz de Alertas Críticas (`alert_rules.yml`):**
  * `TravelinkAppDown`: Se dispara si la aplicación no responde por más de 30 segundos (`up == 0`).
  * `TravelinkHighJvmHeapUsage`: Se dispara si el uso de memoria Heap supera el 85% durante 1 minuto.
  * `TravelinkHighHttpErrorRate`: Se dispara si la tasa de errores HTTP 5xx supera el 5% en 2 minutos.
  * `TravelinkHighHttpLatency`: Se dispara si la latencia p95 supera 1.0 segundo.
* **Evidencia a Mostrar:**
  * Reglas versionadas: [`monitoreo/prometheus/alert_rules.yml`](../../monitoreo/prometheus/alert_rules.yml)
  * Alerta real disparada al detener contenedor: [`monitoreo/evidencias/alerta-disparada-app-down.txt`](../../monitoreo/evidencias/alerta-disparada-app-down.txt)

> **Guion del Expositor (40 seg):**  
> *"Para anticipar fallos antes de que afecten a los turistas, definimos 6 reglas de alerta automáticas en Prometheus. Probamos de manera empírica la resiliencia deteniendo el contenedor de la aplicación: en menos de 30 segundos, Prometheus detectó la caída del target y disparó la alerta crítica `TravelinkAppDown` en estado FIRING, registrando la evidencia que mostramos en pantalla."*

---

### Diapositiva 6: Mantenimiento Preventivo, Backups y Continuidad (0:40 min)

* **Objetivos de Continuidad:** RPO <= 24h (Local) / <= 5min (Aiven PITR) | RTO <= 30 minutos.
* **Respaldo Consistente (`backup.ps1` / `backup.sh`):**
  * `mysqldump` con `--single-transaction`: volcado en caliente sin bloquear tablas InnoDB.
  * Compresión nativa Gzip (`.sql.gz`) ahorrando > 80% de almacenamiento.
  * Checksum criptográfico SHA-256 (`.sha256`) generado junto a cada copia.
  * Retención inteligente: 7 respaldos diarios y 4 semanales con purga automática.
* **Prueba de Restauración Automatizada (`restore-test.ps1` / `restore-test.sh`):**
  * Valida el hash SHA-256, descomprime y restaura en una base de datos temporal (`travelink_restore_test`).
  * Cuenta filas en tablas maestras (`Usuario`, `Tour`, `Reserva`, `Destino`, `Agencia`) y destruye la base temporal.
* **Automatización en el Sistema Operativo:**
  * **Windows:** Tarea programada `TRAVELINK_Backup_Diario` en Task Scheduler a las 02:00 AM (probada con `registrar-tarea.ps1`).
  * **Linux:** `crontab.txt`, temporizadores de Systemd (`.timer`) y rotación con `logrotate`.
* **Evidencias a Mostrar:**
  * Respaldo y checksum: [`mantenimiento/evidencias/backup-real.txt`](../../mantenimiento/evidencias/backup-real.txt) y [`checksum.txt`](../../mantenimiento/evidencias/checksum.txt)
  * Prueba de restauración exitosa: [`mantenimiento/evidencias/restore-test.txt`](../../mantenimiento/evidencias/restore-test.txt)
  * Registro en Task Scheduler: [`mantenimiento/evidencias/tarea-programada-windows.txt`](../../mantenimiento/evidencias/tarea-programada-windows.txt)

> **Guion del Expositor (40 seg):**  
> *"Un backup que no ha sido probado no es un backup. Diseñamos un ciclo cerrado de continuidad: scripts en PowerShell y Bash que ejecutan respaldos consistentes con `--single-transaction`, los comprimen en Gzip y generan su hash SHA-256. Lo más relevante es nuestro script `restore-test`, que periódicamente monta el backup en una base temporal y audita los conteos de filas. Validamos esto registrando la tarea programada directamente en el Task Scheduler de Windows a las 02:00 AM."*

---

### Diapositiva 7: Herramientas Elegidas y Trazabilidad en GitHub (0:35 min)

* **Catálogo de Componentes Oficiales:**

| Herramienta | Versión | Rol en TRAVELINK | Enlace Compartido en Repositorio GitHub |
| :--- | :---: | :--- | :--- |
| **Prometheus** | v2.52.0 | Servidor TSDB y alertas PromQL | [`monitoreo/prometheus/prometheus.yml`](https://github.com/ROMINAP-28/PROYECTO/blob/master/monitoreo/prometheus/prometheus.yml) |
| **Grafana** | v10.4.2 | Visualización y dashboards unificados | [`monitoreo/grafana/dashboards/travelink-overview.json`](https://github.com/ROMINAP-28/PROYECTO/blob/master/monitoreo/grafana/dashboards/travelink-overview.json) |
| **Loki** | v2.9.8 | Indexación y almacenamiento de logs | [`monitoreo/loki/loki-config.yml`](https://github.com/ROMINAP-28/PROYECTO/blob/master/monitoreo/loki/loki-config.yml) |
| **Promtail** | v2.9.8 | Agente colector y parseador de logs | [`monitoreo/promtail/promtail-config.yml`](https://github.com/ROMINAP-28/PROYECTO/blob/master/monitoreo/promtail/promtail-config.yml) |
| **Zipkin** | v3.3.0 | Colector de trazas OpenTelemetry | [`monitoreo/docker-compose.monitoring.yml`](https://github.com/ROMINAP-28/PROYECTO/blob/master/monitoreo/docker-compose.monitoring.yml) |
| **cAdvisor** | v0.49.1 | Métricas de uso por contenedor Docker | [`monitoreo/docker-compose.monitoring.yml`](https://github.com/ROMINAP-28/PROYECTO/blob/master/monitoreo/docker-compose.monitoring.yml) |
| **Node Exporter**| v1.8.1 | Métricas de CPU, RAM y disco del host | [`monitoreo/docker-compose.monitoring.yml`](https://github.com/ROMINAP-28/PROYECTO/blob/master/monitoreo/docker-compose.monitoring.yml) |
| **Aqua Trivy** | v0.28.0 | Escaneo continuo de CVEs en filesystem | [`.github/workflows/mantenimiento.yml`](https://github.com/ROMINAP-28/PROYECTO/blob/master/.github/workflows/mantenimiento.yml) |
| **Dependabot** | v2 Nativo | Mantenimiento automático de dependencias| [`.github/dependabot.yml`](https://github.com/ROMINAP-28/PROYECTO/blob/master/.github/dependabot.yml) |

* **Documentación Completa:** [`docs/monitoreo/HERRAMIENTAS.md`](../../docs/monitoreo/HERRAMIENTAS.md)

> **Guion del Expositor (35 seg):**  
> *"Todas las herramientas seleccionadas corresponden a estándares abiertos de la Cloud Native Computing Foundation (CNCF), garantizando portabilidad sin vendor lock-in. En nuestro documento HERRAMIENTAS.md y en el repositorio público de GitHub se encuentran las configuraciones versionadas y las versiones fijadas de cada imagen para asegurar reproducibilidad total."*

---

### Diapositiva 8: Calidad de Software, Plan de Pruebas y CI en Verde (0:45 min)

* **Cifras Reales de Verificación y Calidad:**
  * **JUnit 5 + Mockito:** **23 pruebas ejecutadas, 0 fallos, 0 errores** (`BUILD SUCCESS` en 28.32 s). Incluye aislamiento estricto con `@Mock` en repositorios y pruebas de concurrencia ACID con Testcontainers.
  * **Cobertura JaCoCo (v0.8.12):** Auditoría real sobre las **110 clases** del backend. Enfoque de alta cobertura en la capa de negocio (`com.travelink.servicio.*`: validación de RUC, cálculo de tarifas, cupos y transacciones de reservas). Reporte emitido en `target/site/jacoco/index.html`.
  * **OWASP ZAP (DAST):** Escaneo dinámico con **0 vulnerabilidades de riesgo Alto**, cumpliendo el Security Gate que bloquea despliegues ante alertas críticas.
  * **Apache JMeter (Rendimiento):** Carga concurrente escalonada de **100 a 200 usuarios concurrentes** sobre los endpoints `/api/destinos` y `/api/tours`:
    * Tiempo de respuesta promedio: **< 120 ms**
    * Percentil 95 (p95): **< 250 ms**
    * Tasa de error: **0.00%**
  * **Integración Continua (GitHub Actions):** Runs **#23 a #25** en estado **VERDE** (Pipelines de CI y Deploy validados con build limpio, escaneo Trivy, pruebas unitarias y generación de artefactos).

> **Guion del Expositor (45 seg):**  
> *"Para finalizar, demostramos la calidad del software con métricas auditables y reales del repositorio: 23 pruebas automatizadas con JUnit sin fallos, cobertura de código auditada por JaCoCo en toda la lógica de negocio, 0 alertas altas de seguridad con OWASP ZAP y una prueba de estrés con JMeter que soportó 200 usuarios concurrentes con 0% de errores y latencias bajo los 120 milisegundos. Esta robustez se refleja en nuestras ejecuciones de GitHub Actions #23 a #25 completamente en verde. Con esto, TRAVELINK cumple con los más altos estándares de ingeniería, observabilidad y resiliencia. Muchas gracias; quedamos atentos a sus preguntas."*
