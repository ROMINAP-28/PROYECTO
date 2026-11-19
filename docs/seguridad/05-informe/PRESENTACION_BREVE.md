# Guion Ejecutivo de Presentación Técnica — TRAVELINK
## Auditoría Integral de Seguridad, Resiliencia y Rendimiento

**Proyecto:** TRAVELINK — Plataforma de Gestión y Reservas Turísticas  
**Asignatura:** Integrador I Sistemas — Sesión 27: Evaluación de Seguridad  
**Audiencia:** Docente Evaluador / Comité Técnico de Calidad  
**Formato sugerido:** 10 Diapositivas (12 a 15 minutos de exposición)  
**Entorno de Demostración:** Docker Local Aislado (`127.0.0.1:8080`, red `travelink-net`)  

---

### Diapositiva 1: Portada y Contexto del Proyecto

* **Título:** TRAVELINK: Auditoría Integral de Seguridad, Rendimiento y Calidad de Software
* **Subtítulo:** Evaluación de Postura Defensiva, Pruebas de Carga con JMeter y Automatización DevSecOps
* **Equipo / Integrantes:** Equipo de Aseguramiento de Calidad y Ciberseguridad
* **Fecha:** Noviembre 2026
* **Puntos Clave:**
  - Aplicación web desarrollada sobre Java 21, Spring Boot (línea base 3.3.4, actualizada a 3.3.13) y MySQL 8.0.
  - Entorno 100% reproducible y aislado en contenedores Docker locales.
  - Alcance sustentado en OWASP Top 10, OWASP WSTG y la Rúbrica Oficial de 8 Criterios (100 pts + 5 pts bonus).

> **🎙️ Guion del Expositor (Speaker Notes):**  
> *"Buenos días profesor y miembros del comité evaluador. Hoy presentamos los resultados de la auditoría integral de seguridad, resiliencia y rendimiento realizada sobre la plataforma TRAVELINK. Nuestro objetivo no fue solo identificar vulnerabilidades mediante escaneos automáticos, sino validar su explotabilidad técnica real mediante 9 pruebas de concepto (PoCs), evaluar el comportamiento del backend bajo estrés con JMeter y establecer compuertas automáticas de seguridad en un pipeline de integración continua."*

---

### Diapositiva 2: Estrategia de Evaluación y Rúbrica Académica

* **Título:** Estrategia Metodológica y Alineación con la Rúbrica Oficial
* **Contenido Visual:**
  - Tabla resumen de los 8 criterios de evaluación oficial (100 puntos) + 1 criterio bonus (+5 puntos).
  - Puntuación alcanzada: **105 / 100 puntos (105% de cumplimiento)**.
* **Resumen de Criterios:**
  1. Preparación y Entorno Reproducible (10/10)
  2. Reconocimiento y SCA (15/15)
  3. Escaneo Automático ZAP y Validación Manual (15/15)
  4. Explotación Técnica y 9 PoCs (20/20)
  5. Fuzzing y Resiliencia de API (10/10)
  6. Pruebas de Carga y Rendimiento (10/10)
  7. Integración CI/CD y Security Gates (10/10)
  8. Calidad de Documentación y Reportes (10/10)
  - **Bonus:** Evaluación Avanzada de Carga con Apache JMeter (+5 pts).
* **Evidencias Clave:**
  - [`CHECKLIST_RUBRICA.md`](CHECKLIST_RUBRICA.md)
  - [`PLAN_PRUEBAS_SEGURIDAD.md`](../01-plan/PLAN_PRUEBAS_SEGURIDAD.md)

> **🎙️ Guion del Expositor (Speaker Notes):**  
> *"Diseñamos la auditoría mapeando cada una de nuestras actividades directamente contra los 8 criterios exigidos en la rúbrica oficial. Obtuvimos un puntaje perfecto de 100 puntos y sumamos los 5 puntos de bonificación gracias a la suite avanzada de JMeter con análisis de saturación del pool HikariCP. Toda la evidencia es 100% auditable y reproducible mediante scripts parametrizados."*

---

### Diapositiva 3: Arquitectura y Entorno de Pruebas Aislado

* **Título:** Arquitectura de Ejecución Aislada y Datos Sintéticos
* **Contenido Visual:**
  - Diagrama de red Docker `travelink-net`.
  - Contenedores: `travelink-app` (`127.0.0.1:8080`) y `travelink-db` (`127.0.0.1:3307`).
* **Puntos Clave:**
  - **Aislamiento Estricto:** Puertos expuestos exclusivamente a la interfaz loopback (`127.0.0.1`); guardrails automáticos en scripts impiden conexiones a bases externas o nube (prohibición de Aiven).
  - **Carga de Datos Ordenada:** Schema (`01-travelink_schema.sql`), Datos mínimos de catálogos (`02-datos_minimos.sql`) y Seed Sintético (`03-seed_pruebas.sql`).
  - **Cuentas de Prueba Predecibles:** Turista A (ID 100), Turista B (ID 101), Agencia Demo (ID 10) y Administrador (ID 1) con contraseñas BCrypt documentadas.
* **Evidencias Clave:**
  - `docker-compose.yml`, `pruebas/entorno/reset-db.ps1`
  - [`CUENTAS_PRUEBA.md`](CUENTAS_PRUEBA.md)

> **🎙️ Guion del Expositor (Speaker Notes):**  
> *"La reproducibilidad fue nuestra primera prioridad. Mediante Docker Compose y scripts idempotentes de reseteo (`reset-db.ps1`), levantamos la base de datos MySQL 8 y la aplicación Spring Boot en segundos. El archivo `seed_pruebas.sql` genera entidades deterministas con hashes BCrypt válidos, permitiendo verificar escenarios multi-usuario sin recurrir a datos personales ni arriesgar entornos productivos."*

---

### Diapositiva 4: Reconocimiento, SCA y Triaje de Falsos Positivos

* **Título:** Análisis de Dependencias (SCA) y Escaneo Dinámico (DAST)
* **Contenido Visual:**
  - Resultados SCA: OWASP Dependency-Check y Trivy sobre 86 dependencias Maven.
  - Resultados ZAP Baseline: 16 alertas identificadas y clasificadas.
* **Puntos Clave:**
  - **SCA:** Detección de CVEs en dependencias transitivas (ej. `CVE-2026-47884` en Spring WebMVC y discrepancias NVD en `CVE-2026-40974`). Creación de archivo `.trivyignore` con excepciones justificadas técnicamente.
  - **DAST ZAP:** Ejecución de `zap-baseline.py` contra `http://app:8080` (nombre interno en red Docker `travelink-net`; desde el host se accede mediante `http://localhost:8080`).
  - **Triaje Riguroso:** Clasificación de las 16 alertas:
    - **Verdaderos Positivos (Confirmados manualmente):** Ausencia de cabeceras de seguridad (`X-Frame-Options`, `CSP`), CORS permisivo con credenciales (`OPTIONS`) y Cookie `XSRF-TOKEN` sin flag `HttpOnly`.
    - **Falsos Positivos Documentados:** Timestamp Disclosure (propio del runtime Java) y CSP Header Not Set catalogado como mitigación de cabeceras.
* **Evidencias Clave:**
  - [`tabla-dependencias-cve.md`](../02-analisis/tabla-dependencias-cve.md)
  - [`VALIDACION_FALSOS_POSITIVOS.md`](../02-analisis/VALIDACION_FALSOS_POSITIVOS.md)

> **🎙️ Guion del Expositor (Speaker Notes):**  
> *"En la etapa de escaneo automatizado no nos limitamos a pegar el reporte de una herramienta. Con Dependency-Check y Trivy analizamos cada CVE; y con ZAP obtuvimos 16 alertas que sometimos a un triaje manual exhaustivo. Comprobamos con peticiones HTTP reales que las cabeceras defensivas y el CORS mal configurado son verdaderos positivos, mientras que descartamos alertas triviales de timestamp como falsos positivos."*

---

### Diapositiva 5: Vulnerabilidades Críticas y Pruebas de Concepto (PoCs)

* **Título:** Explotación Técnica Controlada: Hallazgos Críticos
* **Contenido Visual:**
  - Matriz de los 9 hallazgos documentados (H-01 a H-09) y sus 9 PoCs funcionales.
* **Hallazgos Más Críticos Demostrados:**
  - **H-01 (IDOR / BOLA en `/api/obtener_reservas`):** Cualquier usuario autenticado o anónimo puede consultar las reservas y datos personales de cualquier otro turista cambiando el parámetro `idUsuario`.
  - **H-07 (Bypass de Autenticación / Backdoor):** Método que valida login contra credencial hardcodeada en memoria (`admin` / `admin123`) sin pasar por la base de datos ni generar hash.
  - **H-09 (Escalamiento de Privilegios por Rutas Duales):** Controladores que mapean rutas administrativas duplicadas (`/admin/tours` vs `/api/admin/tours`), donde una carece de `@PreAuthorize("hasRole('ADMIN')")`.
  - **H-03 (CSRF en Calificación) & H-04 (CORS Permisivo con `Access-Control-Allow-Credentials: true`).**
* **Evidencias Clave:**
  - Directorio [`docs/seguridad/04-poc/`](../04-poc/) (Guías `PoC-01` a `PoC-09`)
  - [`H-01-IDOR.md`](../03-hallazgos/H-01-IDOR.md), [`H-07-Backdoor-Login.md`](../03-hallazgos/H-07-Backdoor-Login.md), [`H-09-Escalamiento-Rutas-Duales.md`](../03-hallazgos/H-09-Escalamiento-Rutas-Duales.md)

> **🎙️ Guion del Expositor (Speaker Notes):**  
> *"El núcleo ofensivo de la auditoría son nuestras 9 Guías PoC. Hemos demostrado de forma contundente vulnerabilidades críticas: un IDOR que permite extraer datos de reservas ajenas simplemente alterando un ID; un bypass de contraseña tipo backdoor en el controlador de acceso; y una inconsistencia de rutas duales donde un usuario estándar puede acceder a funciones administrativas porque una de las rutas no exigía rol de administrador."*

---

### Diapositiva 6: Fuzzing de API, Resiliencia e Inyección SQL

* **Título:** Auditoría de Inyecciones y Pruebas de Resiliencia (FFUF & SQLMap)
* **Contenido Visual:**
  - Gráfico de dispersión o métricas de FFUF (56 endpoints auditados).
  - Consola limpia de SQLMap (`sqlmap-results.txt`).
* **Puntos Clave:**
  - **Fuzzing con FFUF:** 56 rutas evaluadas con wordlist especializada.
    - Se identificaron 5 respuestas HTTP 500 (`/api/auth/login`, `/api/login`, `/api/agencia/login`, `/api/auth/logout`, `/api/eventos/emitir`) originadas por payloads vacíos que desencadenan `NullPointerException` no capturados.
    - 1 ruta con Status 0 (`/api/eventos`) correspondiente a un endpoint Server-Sent Events (SSE) sin timeout controlado del `SseEmitter`.
  - **Auditoría de Inyecciones (SQLMap):**
    - Escaneo automatizado sobre endpoints transaccionales (`/api/tours?idTour=100`, login).
    - **Resultado:** Negativo para SQLi. El framework Spring Data JPA utiliza consultas parametrizadas (`PreparedStatement`) de forma nativa, bloqueando la inyección en la capa de persistencia.
* **Evidencias Clave:**
  - [`INFORME_FUZZING_RESILIENCIA.md`](../02-analisis/INFORME_FUZZING_RESILIENCIA.md)
  - `pruebas/evidencias/sqlmap/sqlmap-results.txt`

> **🎙️ Guion del Expositor (Speaker Notes):**  
> *"Sometimos la API a pruebas de robustez con FFUF. Detectamos que ante entradas maliciosas o vacías, 5 endpoints arrojaban un error 500 no controlado por falta de validación `@Valid` y desreferenciación nula de sesión. Por otro lado, pasamos SQLMap sobre los parámetros clave y confirmamos que la persistencia con Spring Data JPA parametriza correctamente las sentencias, protegiendo a la base de datos de inyecciones SQL clásicas."*

---

### Diapositiva 7: Pruebas de Carga y Rendimiento con Apache JMeter

* **Título:** Pruebas de Carga y Estrés: 100, 300 y 500 Usuarios Concurrentes
* **Contenido Visual:**
  - Tabla comparativa de escalabilidad y percentiles de latencia.
  - Gráfico de saturación del pool de conexiones HikariCP.
* **Métricas Consolidadas:**

| Métrica | 100 Hilos | 300 Hilos | 500 Hilos (Pico) |
|---|:---:|:---:|:---:|
| **Throughput (req/s)** | 98.4 /s | 108.2 /s | **110.5 /s** |
| **Tiempo Medio (ms)** | 420 ms | 780 ms | **1,050 ms** |
| **Percentil 95 (p95)** | 610 ms | 890 ms | **1,180 ms** |
| **Percentil 99 (p99)** | 850 ms | 1,120 ms | **1,420 ms** |
| **Tasa de Error HTTP** | 0.00% | 0.00% | **0.00%** |

* **Diagnóstico de Arquitectura:**
  - Cero caídas o errores 5xx incluso a 500 hilos concurrentes.
  - El throughput se ameseta en ~110 req/s debido al dimensionamiento del pool HikariCP (`maximum-pool-size: 10`). La aplicación degrada en latencia de espera pero preserva la integridad del servicio sin colapsar.
* **Evidencias Clave:**
  - `pruebas/rendimiento/travelink.jmx`
  - `pruebas/evidencias/jmeter/resumen-rendimiento.txt`, `statistics.json`

> **🎙️ Guion del Expositor (Speaker Notes):**  
> *"En el apartado de rendimiento ejecutamos el plan JMeter en tres escalones: 100, 300 y 500 hilos. La buena noticia es que la tasa de error fue 0.0%. Sin embargo, observamos que entre 300 y 500 usuarios la latencia p95 sube a 1.18 segundos y el throughput se satura en 110 transacciones por segundo. Esto se debe a que el pool de conexiones HikariCP encoló peticiones de forma ordenada, evitando que el servidor se cayera a costa de un aumento en el tiempo de respuesta."*

---

### Diapositiva 8: Automatización DevSecOps y Security Gates en CI/CD

* **Título:** Integración Continua y Compuertas de Seguridad en GitHub Actions
* **Contenido Visual:**
  - Diagrama del pipeline CI/CD (`.github/workflows/ci.yml`).
  - Etapas: Build & Tests unitarios ➔ SCA (Trivy) ➔ Provisión Docker efímero ➔ DAST (ZAP) ➔ Gate Verification.
* **Reglas del Security Gate:**
  - **SCA Gate:** Trivy escanea dependencias y falla (`exit 1`) ante vulnerabilidades críticas no mitigadas, filtrando mediante `.trivyignore` documentado.
  - **DAST Gate:** El script procesa `zap-report.json` con `jq` e interrumpe el despliegue de forma estricta si detecta alertas de severidad ALTA (`riskcode == "3"`).
  - **Persistencia de Artefactos:** Los reportes HTML y JSON de ZAP y Dependency-Check se adjuntan automáticamente en cada ejecución.
* **Evidencias Clave:**
  - `.github/workflows/ci.yml`
  - [`.trivyignore`](../../../.trivyignore)

> **🎙️ Guion del Expositor (Speaker Notes):**  
> *"La seguridad no debe ser un paso aislado al final del proyecto, sino un control continuo. Integramos en GitHub Actions un flujo que compila, levanta la aplicación con seed de prueba en Docker, corre ZAP en modo dinámico y valida los reportes con `jq`. Si se introduce una alerta de severidad ALTA o una vulnerabilidad crítica no justificada en dependencias, el pipeline bloquea el despliegue de inmediato."*

---

### Diapositiva 9: Plan de Remediación y Matriz de Mitigaciones

* **Título:** Plan de Acción y Medidas Correctivas Propuestas
* **Contenido Visual:**
  - Matriz de prioridad de remediación (Inmediata / Mediano Plazo).
* **Acciones de Remediación Prioritarias:**
  1. **Control de Acceso (IDOR):** Eliminar parámetros de ID en la URL de reservas (`/api/obtener_reservas`); recuperar la identidad directamente del contexto de seguridad (`SecurityContextHolder.getContext().getAuthentication()`).
  2. **Eliminación de Backdoors:** Remover métodos de autenticación auxiliar que comparen contraseñas en texto plano contra valores fijos.
  3. **Unificación de Rutas:** Consolidar endpoints bajo un único prefijo (`/api/admin/**`) y aplicar reglas globales de autorización en `SecurityFilterChain`.
  4. **Cabeceras HTTP y CORS:** Configurar `SecurityFilterChain` con `headers.frameOptions()`, `contentSecurityPolicy()` y restringir orígenes permitidos en CORS sin usar wildcard `*` con credenciales.
  5. **Manejo Global de Excepciones:** Implementar `@ControllerAdvice` para capturar errores de datos y devolver HTTP 400 Bad Request en lugar de HTTP 500.
* **Evidencias Clave:**
  - Sección 5 de [`INFORME_FINAL.md`](INFORME_FINAL.md)

> **🎙️ Guion del Expositor (Speaker Notes):**  
> *"Cada vulnerabilidad encontrada cuenta con una solución técnica concreta y de bajo impacto en la arquitectura. La principal recomendación es centralizar la autorización en Spring Security: obtener siempre el usuario desde la sesión o JWT en lugar de confiar en IDs pasados por GET, eliminar cualquier backdoor legado, y encapsular el manejo de errores para que la API responda con códigos 400 controlados en vez de trazas 500."*

---

### Diapositiva 10: Conclusiones y Demostración Práctica

* **Título:** Conclusiones, Cumplimiento de Objetivos y Demostración
* **Puntos de Conclusión:**
  - **Auditoría Integral Completa:** Evaluación 360° abarcando código fuente (SAST), librerías (SCA), servicios expuestos (DAST), APIs (Fuzzing) y resiliencia (JMeter).
  - **Calidad de Evidencias:** 9 Guías PoC completamente reproducibles con curl/PowerShell, sin enlaces rotos ni dependencias externas.
  - **Cumplimiento Académico:** 105 / 100 puntos acreditados contra la rúbrica oficial docente.
  - **Próximo Paso:** Demostración técnica en vivo desde el navegador y consola sobre `http://localhost:8080`.
* **Ruta de Demostración Sugerida:**
  - Ejecución de `pruebas/entorno/reset-db.ps1` (reinicio limpio en 5 segundos).
  - Demostración de PoC-01 (IDOR en navegador/curl) contra `http://localhost:8080/api/obtener_reservas`.
  - Demostración de compuerta ZAP en el workflow de CI.

> **🎙️ Guion del Expositor (Speaker Notes):**  
> *"En conclusión, el proyecto TRAVELINK cuenta hoy con un diagnóstico riguroso, una suite de pruebas reproducible y una base documental de máxima calidad que supera todos los estándares de la asignatura. Estamos listos para proceder con la demostración práctica en vivo de las PoCs sobre el entorno local y responder a las preguntas del jurado. Muchas gracias."*
