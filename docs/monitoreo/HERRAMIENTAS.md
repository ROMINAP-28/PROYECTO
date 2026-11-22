# Catálogo de Herramientas de Monitoreo y Observabilidad — TRAVELINK 🛠️

Este documento consolida la ficha técnica de todas las herramientas integradas en la arquitectura de observabilidad de **TRAVELINK**, detallando su propósito, versión de despliegue, accesos locales, archivos de configuración versionados en el repositorio y enlaces oficiales.

---

## 1. Matriz Consolidada de Herramientas

| Herramienta | Propósito / Función | Versión | URL Local | Ruta de Configuración en el Repo | Enlace Oficial | Enlace al Archivo en GitHub |
|:---|:---|:---|:---|:---|:---|:---|
| **Spring Boot Actuator** | Expone endpoints de estado y diagnóstico interno de la aplicación. | `3.3.13` | [http://localhost:8080/actuator/health](http://localhost:8080/actuator/health) | [`Springboot/resources/application.properties`](../../Springboot/resources/application.properties) | [Docs Actuator](https://docs.spring.io/spring-boot/reference/actuator/index.html) | [application.properties](https://github.com/ROMINAP-28/PROYECTO/blob/feature/monitoreo-mantenimiento/Springboot/resources/application.properties) |
| **Micrometer Prometheus** | Formatea y recolecta métricas de JVM, HTTP y contadores de negocio para scraping. | `1.13.1` | [http://localhost:8080/actuator/prometheus](http://localhost:8080/actuator/prometheus) | [`pom.xml`](../../pom.xml) | [Micrometer Docs](https://micrometer.io/docs/registry/prometheus) | [pom.xml](https://github.com/ROMINAP-28/PROYECTO/blob/feature/monitoreo-mantenimiento/pom.xml) |
| **Prometheus** | Servidor de base de datos de series temporales, motor de scrape y evaluador de alertas. | `v2.51.0` | [http://localhost:9090](http://localhost:9090) | [`monitoreo/prometheus/prometheus.yml`](../../monitoreo/prometheus/prometheus.yml) | [Prometheus.io](https://prometheus.io/) | [prometheus.yml](https://github.com/ROMINAP-28/PROYECTO/blob/feature/monitoreo-mantenimiento/monitoreo/prometheus/prometheus.yml) |
| **Alertmanager / Reglas** | Definición y evaluación de umbrales críticos (App Down, Heap >85%, tasa 5xx, p95). | `v2.51.0` | [http://localhost:9090/alerts](http://localhost:9090/alerts) | [`monitoreo/prometheus/alert_rules.yml`](../../monitoreo/prometheus/alert_rules.yml) | [Prometheus Alerting](https://prometheus.io/docs/alerting/latest/overview/) | [alert_rules.yml](https://github.com/ROMINAP-28/PROYECTO/blob/feature/monitoreo-mantenimiento/monitoreo/prometheus/alert_rules.yml) |
| **Grafana** | Plataforma de visualización, tableros ejecutivos y análisis unificado de métricas y logs. | `10.4.1` | [http://localhost:3000](http://localhost:3000) | [`monitoreo/grafana/dashboards/travelink-overview.json`](../../monitoreo/grafana/dashboards/travelink-overview.json) | [Grafana.com](https://grafana.com/) | [travelink-overview.json](https://github.com/ROMINAP-28/PROYECTO/blob/feature/monitoreo-mantenimiento/monitoreo/grafana/dashboards/travelink-overview.json) |
| **Grafana Loki** | Sistema de agregación de registros (logs) horizontalmente escalable optimizado para LogQL. | `2.9.6` | [http://localhost:3100](http://localhost:3100) | [`monitoreo/loki/loki-config.yml`](../../monitoreo/loki/loki-config.yml) | [Grafana Loki](https://grafana.com/oss/loki/) | [loki-config.yml](https://github.com/ROMINAP-28/PROYECTO/blob/feature/monitoreo-mantenimiento/monitoreo/loki/loki-config.yml) |
| **Promtail** | Agente de transporte que extrae logs de Logback y los envía etiquetados a Loki. | `2.9.6` | [http://localhost:9080](http://localhost:9080) | [`monitoreo/promtail/promtail-config.yml`](../../monitoreo/promtail/promtail-config.yml) | [Promtail Docs](https://grafana.com/docs/loki/latest/clients/promtail/) | [promtail-config.yml](https://github.com/ROMINAP-28/PROYECTO/blob/feature/monitoreo-mantenimiento/monitoreo/promtail/promtail-config.yml) |
| **Zipkin** | Colector y visualizador de trazabilidad distribuida para inspección de spans de peticiones. | `3.1.1` | [http://localhost:9411](http://localhost:9411) | [`monitoreo/docker-compose.monitoring.yml`](../../monitoreo/docker-compose.monitoring.yml) | [OpenZipkin](https://zipkin.io/) | [docker-compose.monitoring.yml](https://github.com/ROMINAP-28/PROYECTO/blob/feature/monitoreo-mantenimiento/monitoreo/docker-compose.monitoring.yml) |
| **cAdvisor** | Analizador de uso de recursos (CPU, memoria, throttling) de los contenedores Docker en tiempo real. | `v0.49.1` | [http://localhost:8081](http://localhost:8081) | [`monitoreo/docker-compose.monitoring.yml`](../../monitoreo/docker-compose.monitoring.yml) | [cAdvisor GitHub](https://github.com/google/cadvisor) | [docker-compose.monitoring.yml](https://github.com/ROMINAP-28/PROYECTO/blob/feature/monitoreo-mantenimiento/monitoreo/docker-compose.monitoring.yml) |
| **Node Exporter** | Exportador de métricas del sistema operativo anfitrión (medición de la VM WSL2 en Docker Desktop). | `v1.7.0` | [http://localhost:9100](http://localhost:9100) | [`monitoreo/docker-compose.monitoring.yml`](../../monitoreo/docker-compose.monitoring.yml) | [Node Exporter](https://github.com/prometheus/node_exporter) | [docker-compose.monitoring.yml](https://github.com/ROMINAP-28/PROYECTO/blob/feature/monitoreo-mantenimiento/monitoreo/docker-compose.monitoring.yml) |
| **Logback** | Framework de registro de eventos con soporte para traceId/spanId y formato JSON nativo. | `1.5.18` | Consola / Archivos | [`Springboot/resources/logback.xml`](../../Springboot/resources/logback.xml) | [Logback Project](https://logback.qos.ch/) | [logback.xml](https://github.com/ROMINAP-28/PROYECTO/blob/feature/monitoreo-mantenimiento/Springboot/resources/logback.xml) |
| **Apache JMeter** | Generador de carga y estrés para validación de umbrales y comportamiento del dashboard. | `5.6.3` | Local CLI / GUI | [`pruebas/rendimiento/travelink.jmx`](../../pruebas/rendimiento/travelink.jmx) | [Apache JMeter](https://jmeter.apache.org/) | [travelink.jmx](https://github.com/ROMINAP-28/PROYECTO/blob/feature/monitoreo-mantenimiento/pruebas/rendimiento/travelink.jmx) |

---

## 2. Instrucciones Rápidas de Ejecución Local

Para levantar el ecosistema completo de monitoreo en local:

```powershell
# 1. Asegurar que la red de contenedores de TRAVELINK esté activa
docker compose -f despliegue/docker/docker-compose.prod.yml up -d

# 2. Iniciar el stack de monitoreo
docker compose -f monitoreo/docker-compose.monitoring.yml up -d

# 3. Acceder a los paneles:
# - Grafana:    http://localhost:3000 (Usuario: admin | Password: AdminTravelink2026!)
# - Prometheus: http://localhost:9090
# - Zipkin:     http://localhost:9411
# - cAdvisor:   http://localhost:8081
```
