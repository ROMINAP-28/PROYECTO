# ARQUITECTURA DE SOFTWARE - TRAVELINK 🏛️

Este documento describe la arquitectura, patrones de diseño, componentes técnicos, seguridad y configuración del sistema **TRAVELINK** (Spring Boot 3.3.4 + Java 21).

---

## 1. Visión General de la Arquitectura

TRAVELINK adopta una arquitectura en capas desacoplada basada en el patrón **MVC (Modelo - Vista - Controlador)** extendido con **Capa de Servicios (Service Layer)** y una **Estrategia Dual de Persistencia (DAO)**:

```mermaid
graph TD
    subgraph Frontend ["Capa de Presentación (Frontend)"]
        UI_HTML[HTML5 Semántico]
        UI_CSS[CSS3 Modular]
        UI_JS[JavaScript Vanilla Async/Fetch]
    end

    subgraph Controllers ["Capa de Controladores (REST Controllers)"]
        AC_Auth[AuthApiController]
        AC_Public[PublicApiController]
        AC_Agencia[AgenciaApiController]
        AC_Admin[AdminApiController]
        AC_Report[ReportesController]
        GEH[GlobalExceptionHandler]
    end

    subgraph DTOs ["Objetos de Transferencia (DTOs)"]
        ReqDTO[Request DTOs (@Valid, @NotNull, @Email...)]
        ResDTO[Response DTOs (Sin campos sensibles)]
    end

    subgraph Services ["Capa de Servicios de Negocio (SOLID)"]
        S_Reserva[ReservaServicio / Impl]
        S_Tour[TourServicio / Impl]
        S_Agencia[AgenciaServicio / Impl]
        S_Usuario[UsuarioServicio / Impl]
        S_Calificacion[CalificacionServicio / Impl]
        S_Admin[AdminServicio / Impl]
        S_Import[ImportacionServicio / Impl]
    end

    subgraph Persistence ["Capa de Datos y Persistencia (DAO Dual)"]
        direction TB
        JPA_DAO["DAO Principal (Spring Data JPA)<br/>CRUD y consultas por entidad"]
        JDBC_DAO["Consultas Especializadas (JDBC)<br/>Agregaciones, JOINs masivos y dashboards"]
    end

    subgraph Database ["Base de Datos"]
        DB[(MySQL 8.x en Aiven / Local)]
    end

    Frontend -->|HTTP / JSON / Cookies CSRF| Controllers
    Controllers --> DTOs
    Controllers --> Services
    Services --> JPA_DAO
    Services --> JDBC_DAO
    JPA_DAO --> DB
    JDBC_DAO --> DB
    GEH -.->|Captura y enmascara errores| Controllers
```

---

## 2. Capas de la Aplicación

### 2.1. Capa de Presentación (Frontend)
- **Ubicación:** `Springboot/resources/static/`
- **Organización:**
  - `html/`: Vistas separadas por rol (`turista/`, `agencia/`, `administrador/`).
  - `css/`: Estilos CSS modularizados, sin dependencias de frameworks CSS pesados.
  - `js/`: Lógica asíncrona mediante `fetch()`, manejo de sesión en cookies/localStorage y manipulación DOM.
- **Regla Fundamental:** Separación estricta de HTML, CSS y JavaScript (sin JavaScript inline en atributos HTML).

### 2.2. Capa de Controladores REST (Spring MVC)
- **Anotaciones:** `@RestController`, `@RequestMapping`, `@GetMapping`, `@PostMapping`.
- **Responsabilidad:**
  - Recibir y deserializar peticiones HTTP.
  - Validar datos de entrada mediante anotaciones Bean Validation (`@Valid`).
  - Delegar la lógica de negocio a la capa de servicios.
  - Retornar DTOs con códigos de estado HTTP semánticos (`200 OK`, `201 Created`, `400 Bad Request`, `401 Unauthorized`, `403 Forbidden`, `404 Not Found`).
  - **No contiene lógica de negocio ni acceso directo a la base de datos.**

### 2.3. Capa de Transferencia de Datos (DTOs)
- **Ubicación:** `com.travelink.controlador.dto`
- **Propósito:**
  - **Desacoplamiento:** Aísla el modelo de dominio interno de la representación expuesta al cliente.
  - **Seguridad:** Previene la exposición de contraseñas, hashes, llaves privadas o metadatos de auditoría sensible.
  - **Validación Declarativa:** En los DTOs de entrada (`LoginRequest`, `RegistroTuristaRequest`, `RegistroAgenciaRequest`, `ReservaRequest`, `CalificacionRequest`) se aplican validaciones con `@NotBlank`, `@Size`, `@Email`, `@Min`, `@Max`.

### 2.4. Capa de Servicios de Negocio (Service Layer & SOLID)
- **Interfaces:** `com.travelink.servicio`
- **Implementaciones:** `com.travelink.servicio.impl`
- **Principios SOLID aplicados:**
  - **SRP (Single Responsibility Principle):** Cada servicio se enfoca exclusivamente en un área del dominio (reservas, agencias, catálogo de tours, auditoría de comisiones).
  - **OCP (Open/Closed Principle):** Se definen contratos en interfaces que permiten extender la lógica (por ejemplo agregar nuevos formatos de importación) sin modificar los clientes existentes.
  - **LSP (Liskov Substitution Principle):** Las implementaciones cumplen íntegramente los contratos definidos en las interfaces de servicio.
  - **ISP (Interface Segregation Principle):** Interfaces de servicio específicas para cada responsabilidad en lugar de una interfaz monolítica gigante.
  - **DIP (Dependency Inversion Principle):** Los controladores y servicios dependen de abstracciones (interfaces), y las dependencias se inyectan mediante constructores públicos.

### 2.5. Capa de Persistencia y Acceso a Datos (Estrategia DAO Dual)
El sistema mantiene dos capas complementarias con roles estrictamente delimitados para evitar duplicación:

| Componente | Paquete | Rol | Tipo de Operaciones |
|---|---|---|---|
| **Spring Data JPA Repositories** | `com.travelink.repositorio` | **DAO Principal** | CRUD estándar, búsquedas por llave primaria, consultas simples derivadas por nombre de método (`findById`, `findByEstado`, `findByRuc`). |
| **Persistencia JDBC Nativa** | `com.travelink.persistencia` | **Consultas Especializadas** | Agregaciones multidimensionales para dashboards, transacciones compuestas de reserva con múltiples tablas dependientes, y consultas de reportes donde JPA introduce sobrecarga innecesaria. Todas protegidas con `PreparedStatement`. |

---

## 3. Catálogo de Dependencias (`pom.xml`)

| Dependencia Maven | Versión | Propósito Arquitectural |
|---|---|---|
| `spring-boot-starter-web` | 3.3.4 | Motor HTTP embebido (Apache Tomcat), despachador Spring MVC, serialización Jackson JSON. |
| `spring-boot-starter-data-jpa` | 3.3.4 | Capa de persistencia JPA con Hibernate ORM y pool de conexiones HikariCP. |
| `spring-boot-starter-security` | 3.3.4 | Infraestructura de autenticación, autorización basada en roles, filtros de seguridad y encoder BCrypt. |
| `spring-boot-starter-validation` | 3.3.4 | Proveedor de validación Jakarta Bean Validation (Hibernate Validator) para DTOs. |
| `mysql-connector-j` | 8.3.0 | Driver JDBC oficial de alto rendimiento para MySQL 8.x. |
| `openpdf` | 1.3.39 | Generación programática de comprobantes y reportes administrativos en formato PDF (LGPL/MPL). |
| `poi` y `poi-ooxml` | 5.2.5 | Exportación e importación de libros de cálculo Excel (`.xlsx`) para reportes y carga masiva de cupos. |
| `spring-boot-starter-test` | 3.3.4 | Suite de pruebas unitarias y de integración con JUnit 5 (Jupiter), AssertJ y Mockito. |

---

## 4. Arquitectura de Seguridad

La seguridad está centralizada en `com.travelink.config.SecurityConfig` y `GlobalExceptionHandler`:

### 4.1. Autenticación y Cifrado
- **Algoritmo de Hash:** BCrypt con factor de costo configurable (`BCryptPasswordEncoder`).
- **Migración Transparente:** Si un usuario antiguo ingresa con contraseña en texto plano, el sistema valida la credencial y la rehashea automáticamente con BCrypt en la base de datos sin requerir restablecimiento manual.

### 4.2. Control de Fuerza Bruta (`LoginAttemptService`)
- Monitorea intentos fallidos por clave compuesta `IP:usuario`.
- Si se registran **5 intentos fallidos consecutivos**, la cuenta/IP queda bloqueada automáticamente durante **15 minutos**, respondiendo con HTTP `423 Locked`.
- Al autenticarse correctamente, el contador de intentos fallidos se reinicia a cero.

### 4.3. Protección CSRF (Cross-Site Request Forgery)
- Repositorio: `CookieCsrfTokenRepository.withHttpOnlyFalse()`.
- La cookie `XSRF-TOKEN` es leída por el frontend en `js/comun/api.js` y enviada de regreso en la cabecera HTTP `X-XSRF-TOKEN` en cada petición mutante (POST/PUT/DELETE).
- Los endpoints públicos de login, registro y catálogo público se configuran en `ignoringRequestMatchers`.

### 4.4. Control de Acceso Basado en Roles (RBAC)
- `/api/admin/**` y `/html/administrador/**`: Requiere `ROLE_ADMIN`.
- `/api/agencia/**` y `/html/agencia/**`: Requiere `ROLE_AGENCIA` y validación de solicitud de incorporación en estado `ACTIVO`/`Aceptado`.
- `/api/turista/**` y comprobantes: Requiere usuario autenticado.

### 4.5. Cabeceras de Seguridad HTTP
- `X-Content-Type-Options: nosniff`: Previene ataques de MIME-sniffing.
- `X-Frame-Options: SAMEORIGIN`: Protege contra ataques de Clickjacking permitiendo incrustación únicamente desde el mismo origen.
- `X-XSS-Protection: 1; mode=block`: Bloquea renderizado si detecta inyección de scripts cruzados reflejados.

### 4.6. Manejo Global de Excepciones (`GlobalExceptionHandler`)
- Implementado con `@RestControllerAdvice`.
- Intercepta `MethodArgumentNotValidException`, `IllegalArgumentException`, `AccessDeniedException` y `Exception`.
- **Garantía Crítica:** Nunca envía stack traces ni información interna al cliente HTTP. Todos los errores no controlados se registran en el archivo de log con nivel `ERROR` y se responde al cliente un JSON estandarizado:
  ```json
  {
    "status": "error",
    "message": "Ha ocurrido un error interno al procesar su solicitud. Por favor intente más tarde."
  }
  ```

---

## 5. Política y Configuración de Logging (Logback + SLF4J)

Configurado en `Springboot/resources/logback.xml`:
- **Directorio de logs:** `logs/`
- **Archivo principal:** `logs/travelink.log`
- **Rotación:** Diaria a medianoche (`TimeBasedRollingPolicy`) con compresión automática en archivos comprimidos `travelink-YYYY-MM-DD.log.gz`.
- **Historial:** Retención de 30 días con límite global acumulado de 1 GB.
- **Eventos Críticos Auditados:**
  1. Intentos de autenticación, accesos exitosos y bloqueos por fuerza bruta.
  2. Creación, confirmación y cancelación de reservas turísticas.
  3. Aprobación y rechazo de solicitudes de incorporación de agencias.
  4. Procesamiento de archivos de importación masiva (filas exitosas y filas con error).
  5. Descarga y generación de reportes oficiales (PDF y Excel).
