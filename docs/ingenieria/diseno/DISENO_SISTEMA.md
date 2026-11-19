# Diseño del Sistema y Patrones de Software — TRAVELINK 🏛️

La plataforma **TRAVELINK** implementa una arquitectura desacoplada en capas fundamentada en principios de alta cohesión, bajo acoplamiento y separación estricta de responsabilidades.

---

## 1. Arquitectura en Capas (Layered Architecture)

```mermaid
flowchart TD
    subgraph Presentacion ["Capa de Presentación (Frontend)"]
        UI[Vanilla HTML5 / CSS3 / JavaScript ES6+ Modular]
    end

    subgraph Perimetro ["Capa Perimetral y Seguridad"]
        Nginx[Proxy Inverso Nginx / SSL / Cabeceras Defensivas]
        SecFilter[Spring SecurityFilterChain: BCrypt, Roles, CORS, CSRF]
    end

    subgraph Controladores ["Capa de Controladores / Adaptadores"]
        RESTCtrl["@RestController (Spring MVC)"]
        LegacyAdap["ServletHttpExchangeAdapter (Bridge a HttpHandler nativos)"]
    end

    subgraph Servicios ["Capa de Lógica de Negocio (Service Layer)"]
        SrvUsuario[UsuarioServicio]
        SrvReserva[ReservaServicio]
        SrvAgencia[AgenciaServicio]
        SrvComision[ComisionServicio]
        SrvCalificacion[CalificacionServicio]
        SrvReporte[ExcelReporteService / PdfReporteService]
    end

    subgraph PersistenciaDual ["Capa Dual de Persistencia"]
        JPA[Spring Data JPA / Hibernate ORM (17 Interfaces JpaRepository)]
        JDBC[JDBC Nativo con PreparedStatement y Pool HikariCP (ConexionDB)]
    end

    subgraph BaseDatos ["Capa de Almacenamiento"]
        MySQL[(MySQL 8.0 - 18 Tablas Relacionales)]
    end

    UI --> Nginx --> SecFilter
    SecFilter --> RESTCtrl
    SecFilter --> LegacyAdap
    RESTCtrl --> Servicios
    LegacyAdap --> Servicios
    Servicios --> JPA --> MySQL
    Servicios --> JDBC --> MySQL
```

---

## 2. Catálogo de Patrones de Diseño Implementados

### 2.1. Patrón Modelo - Vista - Controlador (MVC)
- **Modelo:** Clases en `com.travelink.entidades.*` mapeadas con anotaciones Jakarta Persistence (`@Entity`, `@Table`, `@Id`, `@ManyToOne`, `@OneToMany`).
- **Vista:** Vistas web estáticas servidas en `Springboot/resources/static/` desacopladas de la lógica backend (comunicación 100% vía JSON mediante API REST).
- **Controlador:** Clases en `com.travelink.controlador.*` encargadas de la validación de entrada (DTOs con Jakarta Bean Validation `@Valid`), orquestación de servicios y retorno de códigos HTTP estándar (`ResponseEntity<T>`).

### 2.2. Patrón Capa de Servicios (Service Layer)
- Centraliza las reglas de negocio, validaciones cruzadas y delimitación transaccional (`@Transactional`).
- Evita que los controladores accedan directamente a la persistencia, permitiendo desacoplar la lógica para pruebas unitarias con mocks (`Mockito`).

### 2.3. Estrategia Dual de Persistencia (Repository vs DAO Nativo)
TRAVELINK adopta una solución híbrida:
1. **Spring Data JPA (`com.travelink.repositorio.*`):** Para operaciones transaccionales estándar, CRUD, navegación de grafos de entidades y persistencia de auditoría (e.g. `ReservaRepositorio`, `UsuarioRepositorio`).
2. **DAO JDBC Nativo con HikariCP (`com.travelink.persistencia.*`):** Para agregaciones de alto rendimiento, reportes consolidados y dashboards donde las consultas complejas con múltiples `JOIN` y funciones analíticas son más eficientes en SQL plano parametrizado con `PreparedStatement`.

### 2.4. Patrón Adaptador (Adapter Pattern)
- La clase `com.travelink.server.ServletHttpExchangeAdapter` actúa como puente entre el contenedor de servlets de Spring Boot (Jakarta Servlet API) y los handlers HTTP legacy orientados a la especificación nativa `com.sun.net.httpserver.HttpHandler`. Esto permite preservar la lógica existente sin reescribir handlers preexistentes.

### 2.5. Patrón Interceptor / Cadena de Responsabilidad (Chain of Responsibility)
- Materializado en el `SecurityFilterChain` de Spring Security, donde cada filtro valida secuencialmente credenciales, tokens CSRF, cabeceras CORS, sesión HTTP y autorización por roles antes de delegar la petición al despachador principal.
