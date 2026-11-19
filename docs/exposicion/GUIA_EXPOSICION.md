# Guía de Exposición y Defensa Técnica de TRAVELINK

## 1. Estructura y Tiempos de la Presentación

| Bloque | Tema | Duración Sugerida | Puntos Clave a Destacar |
|---|---|---|---|
| **1. Introducción** | Problema y Propuesta de Valor | 2 min | Intermediación turística digital, digitalización de agencias pequeñas, formalización del turismo y transparencia en comisiones. |
| **2. Arquitectura** | Diseño y Stack Tecnológico | 3 min | Monolito modular con Java 17, Spring Boot 3.3.13, MySQL 8.0, frontend desacoplado por rol y persistencia híbrida (JPA/JDBC). |
| **3. Live Demo** | Flujo Crítico de Negocio en Vivo | 5 min | 1. Registro de agencia y aprobación por admin.<br>2. Creación de tour y fechas.<br>3. Búsqueda y reserva por turista.<br>4. Pago simulado y descarga de voucher PDF.<br>5. Exportación de reporte Excel. |
| **4. Calidad y Pruebas** | Pirámide de Pruebas y Cobertura | 3 min | 20 pruebas unitarias JUnit 5 + Mockito, integración con Testcontainers, JMeter soportando 200 usuarios concurrentes sin degradación. |
| **5. Seguridad (DevSecOps)** | Hallazgos, Mitigaciones y CI/CD | 4 min | DAST con OWASP ZAP (0 vulnerabilidades High), SCA con Trivy (parcheado a Spring Boot 3.3.13), auditoría de falsos positivos y pipeline en GitHub Actions. |
| **6. Preguntas y Respuestas** | Ronda de Jurado / Docente | 3 min | Justificación de decisiones técnicas, balance de seguridad vs disponibilidad y trabajo futuro. |

---

## 2. Checklist para la Demostración en Vivo (Live Demo)

Antes de iniciar la exposición, verificar:
- [ ] Docker Desktop activo y memoria RAM disponible (> 4 GB libres).
- [ ] Contenedores arriba con `docker compose up -d` y estado healthy en `docker compose ps`.
- [ ] Base de datos poblada con las semillas de prueba (`sql/seed_pruebas.sql`).
- [ ] Navegador preparado con 3 perfiles o ventanas de incógnito separadas:
  - Ventana 1: Turista (`turista1@ejemplo.com`).
  - Ventana 2: Agencia (`contacto@andes.com`).
  - Ventana 3: Administrador de plataforma (`admin@travelink.com`).
- [ ] Terminal abierta con salida lista de `docker compose logs` o JMeter GUI si se solicita prueba en caliente.

---

## 3. Matriz de Preguntas Frecuentes y Argumentación Técnica

### P1: ¿Por qué eligieron una arquitectura monolítica en vez de microservicios?
> **Respuesta**: Para la escala de una plataforma turística departamental/nacional inicial, un monolito modular con Spring Boot ofrece menor sobrecarga operativa (sin service meshes, latencias de red internas ni transacciones distribuidas 2PC), permitiendo aplicar transaccionalidad ACID directa en operaciones críticas como la reserva de cupos y pago, manteniendo un despliegue unificado y económico en un único VPS o contenedor Docker.

### P2: ¿Cómo resolvieron el problema de sobreventa (overbooking) en tours con cupos limitados?
> **Respuesta**: En `ReservaServicio`, la reserva se procesa dentro de una transacción `@Transactional(isolation = Isolation.REPEATABLE_READ)`. Se descuenta el cupo disponible en `TourFecha` y se valida que `cuposDisponibles >= pasajeros`. Si dos solicitudes coinciden en el mismo milisegundo, la segunda falla de forma determinista y se informa al usuario sin corromper los cupos.

### P3: ¿Por qué actualizaron a Spring Boot 3.3.13 en lugar de migrar directamente a Spring Boot 3.5+?
> **Respuesta**: La serie 3.3.13 representa el último parche de soporte público gratuito de la línea 3.3.x, mitigando los CVEs críticos identificados por Trivy sin introducir riesgos de ruptura de API ni cambios incompatibles en Hibernate/Spring Security que requerirían una reescritura extensa del código base a escasos días de la entrega final. Los CVEs restantes están formalmente documentados en `.trivyignore` y en el análisis de falsos positivos.
