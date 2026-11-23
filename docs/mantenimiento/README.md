# Documentación de Mantenimiento de Aplicación

Directorio oficial con los planes, políticas operativas y procedimientos de mantenimiento preventivo, correctivo y de seguridad para la plataforma **TRAVELINK**.

## Contenido

- [PLAN_MANTENIMIENTO.md](PLAN_MANTENIMIENTO.md): Plan integral de mantenimiento de la aplicación. Detalla la matriz de tareas operativas (diarias, semanales, mensuales), política de respaldos (RPO=24h, RTO=1h), scripts de backup con compresión gzip y checksum SHA-256, restauración automatizada probada (`restore-test`), rotación y retención (7 diarios y 4 semanales), programaciones con Cron y Systemd Timers, Tarea Programada de Windows, workflow semanal de Trivy en GitHub Actions y gestión de dependencias con Dependabot.
