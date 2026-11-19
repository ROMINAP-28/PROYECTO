# Patrones de Diseño y Principios SOLID en TRAVELINK

Este documento analiza la implementación de patrones de arquitectura y diseño de software en TRAVELINK, respaldado con referencias exactas al código fuente del repositorio (`Springboot/src/com/travelink/*`).

---

## 1. Patrón DTO (Data Transfer Object)

### 1.1 Ubicación en el Código
- Directorio: `Springboot/src/com/travelink/controlador/dto/`
- Catálogo de DTOs:
  - `LoginRequest.java`: Credenciales de acceso (`correo`, `password`).
  - `AuthResponse.java`: Respuesta de autenticación (`status`, `token`, `usuario`).
  - `RegistroTuristaRequest.java` / `RegistroAgenciaRequest.java`: Payloads de alta de usuarios sin exponer entidades relacionales internas.
  - `ReservaRequest.java` / `ReservaDto.java`: Transferencia de datos de reserva y pasajeros desacoplados de JPA.
  - `TourRequest.java` / `TourDto.java`: Entrada y salida de catálogo de itinerarios.
  - `PasajeroRequest.java` / `PasajeroDto.java`: Desglose de pasajeros vinculados a una reserva.
  - `CalificacionRequest.java` / `CalificacionDto.java`: Puntuaciones y comentarios de reseñas.
  - `ComisionDto.java`: Transferencia de saldos y porcentajes de liquidación.
  - `ImportacionResultadoDto.java`: Consolidado de filas importadas, omitidas y lista de errores con número de fila.
  - `ApiResponse.java`: Envoltorio genérico estándar (`status`, `message`, `data`).

### 1.2 Justificación Técnica
1. **Seguridad y Prevención de Over-Posting**: Evita que un cliente malicioso modifique campos sensibles directamente en el modelo de base de datos (como `esAdmin`, `estado`, o `saldoComision`) mediante inyección directa de JSON.
2. **Eliminación de Bucles de Serialización Bidireccional**: En las relaciones JPA (`@OneToMany` y `@ManyToOne` entre `Reserva`, `Tour` y `Usuario`), serializar la entidad directamente generaría desbordamiento de pila (`StackOverflowError`) por referencias circulares. El DTO transporta solo datos planos requeridos por la vista.
3. **Contrato de API Estable**: Permite que el esquema de base de datos relacional evolucione sin romper el contrato JSON consumido por el frontend en JavaScript.

---

## 2. Patrón de Persistencia Híbrida: Repository vs DAO

TRAVELINK implementa una arquitectura híbrida de acceso a datos para equilibrar agilidad de desarrollo y rendimiento en consultas pesadas:

```text
┌────────────────────────────────────────────────────────┐
│                   CAPA DE SERVICIO                     │
│               (com.travelink.servicio)                 │
└──────────────────────────┬─────────────────────────────┘
                           │
             ┌─────────────┴─────────────┐
             ▼                           ▼
┌──────────────────────────┐┌──────────────────────────┐
│   SPRING DATA JPA REPO   ││      NATIVE JDBC DAO     │
│(com.travelink.repositorio││(com.travelink.persistencia│
└────────────┬─────────────┘└────────────┬─────────────┘
             │ Hibernate                 │ PreparedStatement
             ▼                           ▼
┌────────────────────────────────────────────────────────┐
│                   BASE DE DATOS MYSQL                  │
└────────────────────────────────────────────────────────┘
```

### 2.1 Repositorios JPA (`com.travelink.repositorio`)
- **Clases Representativas**: `ReservaRepositorio.java`, `UsuarioRepositorio.java`, `TourRepositorio.java`, `PagoRepositorio.java`.
- **Mecanismo**: Interfaces que extienden `org.springframework.data.jpa.repository.JpaRepository<T, ID>`.
- **Casos de Uso**:
  - Operaciones CRUD estándar (`save`, `findById`, `delete`).
  - Consultas semánticas derivadas (`findByCorreo`, `findByEstado`).
  - Mapeo automático de ciclo de vida de entidades y relaciones navegables (`Reserva -> Pasajero`).

### 2.2 Capa DAO JDBC Nativa (`com.travelink.persistencia`)
- **Clases Representativas**: `ReservaDAO.java`, `UsuarioDAO.java`, `TourDAO.java`, `AgenciaDAO.java`, `PagoDAO.java`, `DestinoDAO.java`, `NotificacionDAO.java`.
- **Mecanismo**: Conexiones JDBC obtenidas de `ConexionDB.getConnection()`, utilizando `PreparedStatement` parametrizados y mapeo manual de `ResultSet`.
- **Casos de Uso**:
  - Consultas analíticas complejas con múltiples `JOIN` requeridas para los reportes Excel/PDF de `ReportesController`.
  - Operaciones de inserción o lectura en bloque donde el overhead de Hibernate (tracking de estado, dirty checking y reflection) perjudica el rendimiento.
  - Aislamiento transaccional específico sin interferencia de la sesión de Hibernate.

---

## 3. Patrón MVC (Model-View-Controller)

El proyecto estructura la separación de responsabilidades en tres capas desacopladas:

1. **Modelo (Model)**:
   - Entidades de Dominio: `Springboot/src/com/travelink/entidades/` (`Tour.java`, `Reserva.java`, `Usuario.java`, etc.).
   - Lógica de Negocio y Reglas de Dominio: `Springboot/src/com/travelink/servicio/` (`ReservaServicio.java`, `PagoServicio.java`, `UsuarioServicio.java`).
2. **Vista (View)**:
   - Ubicación: `Springboot/resources/static/`
   - Implementación: Interfaz cliente basada en HTML5 semántico, CSS modular y scripts JavaScript vainilla organizados por rol (`html/turista/`, `html/agencia/`, `html/administrador/`). No renderiza vistas en el servidor con plantillas (desacoplamiento REST).
3. **Controlador (Controller)**:
   - Controladores REST: `Springboot/src/com/travelink/controlador/` (`AuthApiController.java`, `PublicApiController.java`, `AdminApiController.java`, `AgenciaApiController.java`, etc.).
   - Mapea las solicitudes HTTP (`@GetMapping`, `@PostMapping`, `@PutMapping`, `@DeleteMapping`), valida los DTOs de entrada y delega la ejecución transaccional a los servicios, retornando respuestas JSON estandarizadas.

---

## 4. Aplicación de Principios SOLID

### 4.1 S — Single Responsibility Principle (Principio de Responsabilidad Única)
- **`ExcelReporteService.java`** (`Springboot/src/com/travelink/reportes/ExcelReporteService.java`):
  Tiene la responsabilidad exclusiva de construir libros de cálculo en formato `.xlsx` con estilos y tipografías de Apache POI. No procesa pagos, no valida contraseñas ni gestiona sesiones HTTP.
- **`SecurityUtils.java`** (`Springboot/src/com/travelink/config/SecurityUtils.java`):
  Responsable únicamente de consultar el `SecurityContextHolder` de Spring Security para extraer de forma segura el ID y rol del usuario autenticado.
- **`UsuarioServicio.java`** (`Springboot/src/com/travelink/servicio/UsuarioServicio.java`):
  Centraliza exclusivamente las operaciones de negocio sobre cuentas de usuario: verificación de unicidad de correo, aplicación del hash BCrypt y asignación de rol inicial.

### 4.2 O — Open/Closed Principle (Principio Abierto/Cerrado)
- **`SecurityConfig.java`** (`Springboot/src/com/travelink/config/SecurityConfig.java`):
  El bean `SecurityFilterChain` está configurado para permitir la adición de nuevos filtros perimetrales (`addFilterBefore`, políticas CORS, reglas de autorización por URL) sin necesidad de modificar el núcleo del framework ni alterar las clases de los controladores.
- **Estructura Modular de Handlers por Rol**:
  Bajo `com.travelink.controlador.admin.*`, `agencia.*` y `turista.*`, cada caso de uso reside en un handler especializado (`ComisionesAdminHandler`, `CalidadAdminHandler`, `CancelarReservaHandler`). Se pueden incorporar nuevos requerimientos de negocio creando un nuevo handler sin abrir ni modificar los existentes.

### 4.3 L — Liskov Substitution Principle (Principio de Sustitución de Liskov)
- **`CustomUserDetailsService.java`** (`Springboot/src/com/travelink/config/CustomUserDetailsService.java`):
  Implementa la interfaz estándar de Spring Security `org.springframework.security.core.userdetails.UserDetailsService`. El framework puede sustituir la implementación por defecto en memoria o LDAP por esta clase sin que el mecanismo global de autenticación falle o deba ser modificado.
- **Jerarquía de `JpaRepository`**:
  Todas las interfaces en `com.travelink.repositorio` heredan de `JpaRepository`. Cualquier servicio que reciba un repositorio puede invocar operaciones genéricas (`findAll`, `save`, `count`) garantizando un comportamiento determinista según el contrato de Spring Data.

### 4.4 I — Interface Segregation Principle (Principio de Segregación de Interfaces)
- **Repositorios Segregados por Entidad**:
  En lugar de una interfaz de repositorio sobrecargada con métodos para todo el sistema, cada entidad posee su propia interfaz granular: `TourFechaRepositorio`, `PagoRepositorio`, `CalificacionRepositorio`.
  Por ejemplo, `PagoServicio` solo inyecta `PagoRepositorio`, `ReservaRepositorio` y `ComisionRepositorio`, ignorando métodos de auditoría o configuraciones que no le competen.

### 4.5 D — Dependency Inversion Principle (Principio de Inversión de Dependencias)
- **Inyección de Dependencias por Constructor**:
  Tanto controladores como servicios dependen de abstracciones y componentes inyectados por el contenedor de inversión de control (IoC) de Spring, evitando el uso de `new` o acoplamiento a implementaciones concretas:
  ```java
  // Ejemplo en ReportesController.java:
  public ReportesController(PdfReporteService pdfReporteService,
                            ExcelReporteService excelReporteService,
                            AgenciaServicio agenciaServicio) {
      this.pdfReporteService = pdfReporteService;
      this.excelReporteService = excelReporteService;
      this.agenciaServicio = agenciaServicio;
  }
  ```
  Esto permite que en las pruebas unitarias (`Springboot/test/com/travelink/servicio/ReservaServicioTest.java`) se inyecten dobles de prueba (`@Mock`) gestionados por Mockito sin levantar la base de datos real.
