# Plan Maestro de Monitoreo y Observabilidad — TRAVELINK 📊

**Sistema:** TRAVELINK — Plataforma de Gestión Turística  
**Entorno Local:** Docker Compose Monitoring Stack (Prometheus, Grafana, Loki, Promtail, Node Exporter, cAdvisor, Zipkin)  
**Entorno Cloud:** Render Web Service (Free Tier) + Monitor Externo UptimeRobot  
**Estado:** `VALIDADO` (Ejecutado localmente) | `DOCUMENTADO` (Operación en Render Cloud)  

---

## 1. Objetivos del Sistema de Monitoreo

1. **Visibilidad en Tiempo Real (Observabilidad 360°):** Centralizar los tres pilares de observabilidad (**Métricas**, **Logs** y **Trazas**) para auditar la salud técnica y operacional de TRAVELINK.
2. **Detección Proactiva de Anomalías:** Configurar umbrales de alerta temprana para prevenir caídas de servicio, saturación de memoria Heap de la JVM y degradación de tiempos de respuesta en la reserva de tours.
3. **Seguridad y Auditoría:** Supervisar intentos fallidos de autenticación (fuerza bruta) y correlacionar errores 5xx con identificadores únicos de traza (`traceId`).
4. **Respeto de Restricciones de Producción:** Garantizar que los endpoints sensibles de métricas (`/actuator/prometheus`, `/actuator/metrics`) nunca queden expuestos públicamente en internet ni a través del proxy inverso Nginx ni en el despliegue de Render.

---

## 2. Los Tres Pilares de Observabilidad

### 2.1. Gestión de Logs (Ingesta, Almacenamiento y Búsqueda)
- **Ingesta:** La aplicación genera eventos mediante Logback en dos canales simultáneos:
  - Formato texto estándar con trazabilidad: `%d{yyyy-MM-dd HH:mm:ss.SSS} [%thread] [traceId=%X{traceId:-} spanId=%X{spanId:-}] %-5level %logger{36} - %msg%n`
  - Formato JSON estructurado nativo en `logs/travelink-json.log` para indexación de alto rendimiento.
- **Transporte y Parsing (Promtail):** Agente liviano montado sobre el volumen de logs. Extrae mediante expresiones regulares o deserialización JSON las etiquetas clave: `level`, `logger`, `traceId` y `spanId`.
- **Almacenamiento (Grafana Loki):** Motor de indexación optimizado que indexa exclusivamente metadatos y etiquetas, comprimiendo los chunks de texto en disco.
- **Búsqueda (LogQL):** Consultas en tiempo real desde Grafana, por ejemplo:
  ```logql
  {job="travelink"} |= "ERROR"
  {job="travelink"} |= "Bloqueo de seguridad"
  ```

### 2.2. Rendimiento (Métricas y Trazabilidad Distribuida)
- **Métricas Técnicas (Micrometer + Prometheus):**
  - Consumo de memoria JVM (Heap used, committed, max).
  - Actividad de recolección de basura (G1GC pauses y tiempos de ciclo).
  - Hilos de ejecución vivos y conexiones activas del pool HikariCP.
  - Throughput de peticiones HTTP por segundo y tasa de errores 4xx/5xx.
  - Latencias percentiles (p50, p95, p99) calculadas con histogramas de Micrometer.
- **Métricas de Negocio Personalizadas:**
  - `travelink.reservas.creadas.total`: Contador incremental cada vez que un turista confirma una reserva.
  - `travelink.reservas.canceladas.total`: Contador de cancelaciones de reservas.
  - `travelink.auth.logins.fallidos.total`: Registro de credenciales erróneas para auditoría defensiva.
  - `travelink.auth.logins.exitosos.total`: Flujo de accesos legítimos de usuarios.
- **Trazabilidad Distribuida (Micrometer Tracing + OpenTelemetry + Zipkin):**
  - Cada solicitud HTTP entrante recibe o propaga una cabecera W3C TraceContext (`traceparent`).
  - La traza inyecta automáticamente `traceId` y `spanId` en el contexto MDC de Logback.
  - Los spans se reportan al colector de Zipkin (`http://zipkin:9411/api/v2/spans`), permitiendo reconstruir la latencia interna de cada llamada a base de datos.

### 2.3. Salud del Sistema Operativo y Contenedores (OS Health)
- **Node Exporter:** Monitorea la máquina anfitriona (CPU idle, memoria libre, I/O de disco y tráfico de red).
  > **Nota de Arquitectura sobre Docker Desktop:** En entornos Windows con Docker Desktop, `node-exporter` se ejecuta dentro de un contenedor Linux y, por tanto, **mide las métricas de la máquina virtual subyacente de WSL2** asignada a Docker, reflejando el consumo real de recursos del motor.
- **cAdvisor:** Analiza en tiempo real el uso de CPU, memoria y throttling individual de cada contenedor Docker (`travelink-app-prod`, `travelink-db-prod`, `travelink-nginx-prod`).

---

## 3. Catálogo de Alertas Automatizadas

| Alerta | Expresión PromQL | Umbral / Duración | Severidad | Acción de Mitigación |
|---|---|---|---|---|
| **TravelinkAppDown** | `up{job="travelink-app"} == 0` | 15 segundos | **Critical** | Reiniciar contenedor o inspeccionar logs de fallo en arranque. |
| **JvmHeapUsageHigh** | `(jvm_memory_used_bytes{area="heap"} / jvm_memory_max_bytes{area="heap"}) * 100 > 85` | 30 segundos | **Warning** | Escalar memoria máxima (`-Xmx`) o depurar fuga de memoria en heap dump. |
| **Http5xxErrorRateHigh** | `sum(rate(http_server_requests_seconds_count{status=~"5.."}[1m])) / sum(rate(http_server_requests_seconds_count[1m])) * 100 > 5` | 30 segundos | **Critical** | Revisar excepciones en Loki con `{job="travelink"} \|= "ERROR"`. |
| **HttpLatencyP95High** | `histogram_quantile(0.95, sum(rate(http_server_requests_seconds_bucket[1m])) by (le)) > 1.5` | 30 segundos | **Warning** | Optimizar consultas SQL con índices en tablas `Tour` y `TourFecha`. |
| **HighCpuUsage** | `100 - (avg by (instance) (rate(node_cpu_seconds_total{mode="idle"}[1m])) * 100) > 85` | 1 minuto | **Warning** | Analizar concurrencia o hilos bloqueados en Tomcat. |
| **DiskSpaceLow** | `(node_filesystem_free_bytes{mountpoint="/"} / node_filesystem_size_bytes{mountpoint="/"}) * 100 < 15` | 1 minuto | **Critical** | Purgar logs antiguos o rotar imágenes no utilizadas de Docker. |

---

## 4. Comparativa de Implementación: Local (VALIDADO) vs Cloud Render (DOCUMENTADO)

| Característica | Entorno Local (`VALIDADO`) | Entorno Cloud Render (`DOCUMENTADO`) |
|---|---|---|
| **Pila de Monitoreo** | Prometheus + Grafana + Loki + Promtail + cAdvisor + Zipkin | Panel nativo de Render (Logs & Metrics) + UptimeRobot externo |
| **Exposición Actuator** | `/actuator/health`, `/actuator/info` y `/actuator/prometheus` (red interna) | Únicamente `/actuator/health` e `/actuator/info` (`never` details) |
| **Métricas del Sistema** | cAdvisor y Node Exporter midiendo VM WSL2 | Gráficas integradas de CPU y RAM del dashboard de Render |
| **Trazabilidad** | Colector Zipkin en puerto 9411 con sampling 100% | Desactivada por defecto (`sampling.probability=0.0`) para ahorrar recursos |
| **Limitaciones Free Tier** | Sin limitaciones (recursos locales asignados) | Sleep tras 15 min de inactividad, 512MB RAM, sin almacenamiento persistente de logs |
| **Healthcheck Externo** | Prometheus scrape cada 3s | UptimeRobot sondeando `https://proyecto-6x4t.onrender.com/actuator/health` cada 5 min (evita sleep) |

---

## 5. Resumen de Riesgos Técnicos y Mitigaciones

1. **Riesgo: Exposición de información sensible en Actuator.**  
   *Mitigación:* Se configuró `management.endpoint.health.show-details=never` en `application.properties` y `application-prod.properties`. Nginx bloquea con HTTP 403 cualquier ruta que empiece con `/actuator/` salvo `/actuator/health`.
2. **Riesgo: Sobrecarga del contenedor por tracing y recolección de métricas.**  
   *Mitigación:* Se fijaron límites estrictos de CPU y memoria en `docker-compose.monitoring.yml`. La recolección de Zipkin tiene sampling configurable y Prometheus scrapea cada 3 a 5 segundos con bajo impacto (< 1.5% de CPU).
3. **Riesgo: Desbordamiento de almacenamiento por logs.**  
   *Mitigación:* Logback aplica compresión gzip diaria (`.log.gz`) con tope acumulado de 1GB (`totalSizeCap`) y retención máxima de 30 días.
