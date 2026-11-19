# Auditoría y Seguridad del Software

Índice de documentación sobre el análisis de seguridad, pruebas DAST/SAST, hallazgos, pruebas de concepto (PoC) y remediación de TRAVELINK.

## Contenido

Para el inventario detallado de todos los artefactos de seguridad, planes, matrices y evidencias, consulte el catálogo central:
- [docs/INVENTARIO_DOCUMENTACION.md](../INVENTARIO_DOCUMENTACION.md)

### Documentos Destacados del Módulo
- [01-plan/PLAN_PRUEBAS_SEGURIDAD.md](01-plan/PLAN_PRUEBAS_SEGURIDAD.md): Plan maestro de pruebas de seguridad y alcance de la auditoría.
- [02-analisis/CONSIDERACIONES_SEGURIDAD_JAVA.md](02-analisis/CONSIDERACIONES_SEGURIDAD_JAVA.md): Resumen de SecurityConfig (autenticación, BCrypt, CSRF, CORS, RBAC, sesiones) vinculado a los hallazgos H-01 a H-09 y CVEs.
- [02-analisis/VALIDACION_FALSOS_POSITIVOS.md](02-analisis/VALIDACION_FALSOS_POSITIVOS.md): Triaje de las 16 alertas ZAP con peticiones y respuestas reales.
- [02-analisis/tabla-dependencias-cve.md](02-analisis/tabla-dependencias-cve.md): Catálogo de vulnerabilidades de dependencias y estado tras Spring Boot 3.3.13.
- [03-hallazgos/](03-hallazgos/): Fichas técnicas de hallazgos H-01 a H-09 con severidades CVSS.
- [04-poc/](04-poc/): Guías de explotación técnica y pruebas de concepto reproducibles.
- [05-informe/INFORME_FINAL.md](05-informe/INFORME_FINAL.md): Informe ejecutivo consolidado de seguridad.
