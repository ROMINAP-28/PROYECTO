# Lista de Capturas de Pantalla Requeridas - Monitoreo

Este directorio contiene las capturas de pantalla de la interfaz gráfica tomadas durante la validación del stack de observabilidad de **TRAVELINK**.

## Checklist de Capturas a Tomar

| # | Archivo Esperado | URL de Origen | Descripción de la Evidencia |
|---|------------------|---------------|------------------------------|
| 1 | `01-grafana-dashboard-overview.png` | `http://localhost:3000/d/travelink-prod-overview` | Dashboard maestro provisionado: estado UP (verde), tiempo activo, tasa de peticiones y métricas JVM. |
| 2 | `02-prometheus-targets-up.png` | `http://localhost:9090/targets` | Panel de targets de Prometheus confirmando `host.docker.internal:8080`, `cadvisor:8080`, `node-exporter:9100` y `localhost:9090` en estado **UP**. |
| 3 | `03-zipkin-distributed-traces.png` | `http://localhost:9411/zipkin/` | Búsqueda de trazas distribuidas con spans de HTTP GET `/` y `/destinos` mostrando latencia y jerarquía de llamadas. |
| 4 | `04-loki-promtail-logs.png` | `http://localhost:3000/explore` | Consulta LogQL en Grafana Explore con `{filename=~".*travelink.*"}` visualizando líneas de log estructurado. |
| 5 | `05-alerta-prometheus-firing.png` | `http://localhost:9090/alerts` | Reglas de alerta cargadas (`TravelinkAppDown`), mostrando umbral y estado de evaluación. |
| 6 | `06-tarea-programada-windows.png` | Programador de Tareas (`taskschd.msc`) | Tarea `TRAVELINK_Backup_Diario` en estado *Listo* con disparador diario a las 02:00 AM. |
