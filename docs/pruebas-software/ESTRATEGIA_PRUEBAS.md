# Estrategia de Pruebas de Software y Aseguramiento de Calidad

## 1. Pirámide de Pruebas

TRAVELINK aplica una estrategia piramidal de pruebas para garantizar la confiabilidad funcional, resiliencia y seguridad de la plataforma turística:

```text
              / \
             /   \
            / E2E \       <- Navegación manual / Verificación ZAP PoC
           /-------\
          /  CARGA  \     <- Apache JMeter (100-200 usuarios concurrentes)
         /-----------\
        / INTEGRACIÓN \   <- Testcontainers MySQL 8.0 (ReservaPersistenciaIntegracionTest)
       /---------------\
      /   UNITARIAS     \ <- 20 pruebas JUnit 5 + Mockito (Servicios de negocio)
     /-------------------\
```

---

## 2. Niveles de Pruebas Implementados

### 2.1 Pruebas Unitarias (JUnit 5 + Mockito)
- **Ubicación**: `Springboot/test/com/travelink/servicio/`
- **Total de Casos**: 20 pruebas unitarias automatizadas.
- **Alcance**:
  - `UsuarioServicioTest`: Cifrado BCrypt, prevención de correos duplicados, asignación de rol turista.
  - `ReservaServicioTest`: Cálculo de costos totales por pasajeros, validación de cupos disponibles en `TourFecha`, rechazo de fechas pasadas.
  - `PagoServicioTest`: Validación de montos contra total de reserva, rechazo de pagos duplicados.
  - `TourServicioTest`: Filtros de búsqueda por destino y rango de precios, validación de cupo mínimo.
  - `AgenciaServicioTest`: Validación de formato RUC y registro de solicitud.
- **Aislamiento**: Uso estricto de `@ExtendWith(MockitoExtension.class)` y `@Mock` para aislar repositorios y conexiones de base de datos.
- **Comando de Ejecución**:
  ```bash
  ./mvnw -B test
  ```

### 2.2 Pruebas de Integración (Testcontainers)
- **Clase**: `ReservaPersistenciaIntegracionTest`
- **Infraestructura**: Contenedor dinámico Docker de MySQL 8.0 gestionado por Testcontainers.
- **Objetivo**: Validar el aislamiento transaccional (`ACID`) y bloqueos de concurrencia al reservar cupos simultáneos sobre la misma fecha de tour.
- **Evidencia**: Registrada en `pruebas/evidencias/integracion-testcontainers.txt`.

### 2.3 Cobertura de Código (JaCoCo)
- **Plugin Maven**: `jacoco-maven-plugin` versión 0.8.12.
- **Métricas Registradas**: Cobertura de líneas e instrucciones en la capa de servicios (`com.travelink.servicio.*`).
- **Generación de Reporte**:
  ```bash
  ./mvnw -B test jacoco:report
  ```
- **Ruta de Salida**: `target/site/jacoco/index.html`.

---

## 3. Pruebas de Rendimiento y Estrés (JMeter)

- **Script de Prueba**: `pruebas/rendimiento/jmeter/travelink.jmx`
- **Escenarios**:
  1. Catálogo público de destinos (`GET /api/destinos`).
  2. Búsqueda y disponibilidad de tours (`GET /api/tours`).
  3. Carga concurrente escalonada: 100 usuarios en 30 segundos; 200 usuarios concurrentes en pico.
- **Métricas Clave**: Tiempo de respuesta promedio < 120 ms, percentil 95 < 250 ms, tasa de error 0.00%.
- **Evidencias**: Almacenadas en `pruebas/evidencias/jmeter/` y consolidadas en `docs/seguridad/05-informe/INFORME_FINAL.md`.

---

## 4. Pruebas de Seguridad y Resiliencia

- **DAST (ZAP)**: Escaneo dinámico sobre APIs y frontend (`zap-full-scan`).
- **SCA / CVE (Trivy)**: Análisis de dependencias en build (`pom.xml`) con `.trivyignore` auditado.
- **Fuzzing (wfuzz/ffuf)**: Inyección de diccionarios en parámetros de reserva y catálogo.
- **SQL Injection (SQLMap)**: Pruebas automatizadas sobre endpoints REST con queries sanitizadas.
- **Detalle Completo**: Consultar `docs/seguridad/01-plan/PLAN_PRUEBAS_SEGURIDAD.md`.
