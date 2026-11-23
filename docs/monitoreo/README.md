# Documentación de Monitoreo y Observabilidad

Directorio oficial con los planes, especificaciones técnicas y guías de herramientas para el stack integral de observabilidad de **TRAVELINK**.

## Contenido

- [PLAN_MONITOREO.md](PLAN_MONITOREO.md): Plan integral de monitoreo de la aplicación. Detalla la arquitectura de observabilidad basada en 4 pilares:
  1. **Logs**: Ingesta con Promtail, almacenamiento indexado con Grafana Loki y consultas LogQL.
  2. **Performance**: Métricas de negocio y JVM expuestas mediante Micrometer y Prometheus, y trazabilidad distribuida extremo a extremo con OpenTelemetry y Zipkin.
  3. **OS Health**: Métricas del host y contenedores recolectadas mediante Node Exporter y cAdvisor.
  4. **Alertas y Dashboards**: Panel maestro en Grafana con provisión automática y reglas de alerta proactivas en Prometheus.
- [HERRAMIENTAS.md](HERRAMIENTAS.md): Catálogo técnico de las herramientas seleccionadas, puertos de red asignados, archivos de configuración, endpoints de consulta y enlaces de trazabilidad al repositorio oficial en GitHub.
