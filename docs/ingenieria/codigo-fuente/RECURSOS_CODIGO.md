# Recursos Técnicos y Gestión del Código Fuente — TRAVELINK

Este documento especifica la organización de paquetes Java, el árbol de dependencias Maven (`pom.xml`), los perfiles de configuración de entorno (`application*.properties`), las políticas de registro y auditoría (`logback.xml`), y las fichas técnicas de importación y exportación de ficheros.

---

## 1. Árbol de Paquetes y Responsabilidades (`com.travelink.*`)

| Paquete | Responsabilidad Arquitectural | Clases Clave |
|---|---|---|
| `com.travelink` | Clase principal de arranque de Spring Boot (`@SpringBootApplication`). | `Main.java` / `TravelinkApplication.java` |
| `com.travelink.config` | Configuración de seguridad, pool de conexiones JDBC nativo y utilitarios RBAC. | `ConexionDB.java`, `SecurityConfig.java`, `SecurityUtils.java`, `CustomUserDetailsService.java` |
| `com.travelink.controlador` | Controladores REST generales y orquestación de endpoints de la API. | `AuthApiController.java`, `PublicApiController.java`, `AdminApiController.java`, `AgenciaApiController.java`, `CalificacionControlador.java` |
| `com.travelink.controlador.admin` | Handlers especializados para casos de uso administrativos. | `AgenciasAdminHandler.java`, `ComisionesAdminHandler.java`, `CalidadAdminHandler.java`, `UsuariosAdminHandler.java`, `ReservasAdminHandler.java` |
| `com.travelink.controlador.agencia`| Handlers modulares para operaciones comerciales de las agencias. | `DashboardAgenciaHandler.java`, `PaquetesAgenciaHandler.java`, `DisponibilidadAgenciaHandler.java`, `PagosAgenciaHandler.java`, `ReservasAgenciaHandler.java` |
| `com.travelink.controlador.turista`| Handlers de experiencia y consumo para turistas registrados. | `GuardarReservaHandler.java`, `CancelarReservaHandler.java`, `CalificarHandler.java`, `EliminarResenaHandler.java`, `ObtenerReservasHandler.java` |
| `com.travelink.controlador.dto` | Objetos de Transferencia de Datos para validación de entrada y desacoplamiento de API. | `LoginRequest.java`, `AuthResponse.java`, `ReservaRequest.java`, `ReservaDto.java`, `TourRequest.java`, `TourDto.java`, `ImportacionResultadoDto.java` |
| `com.travelink.entidades` | Entidades del dominio mapeadas con JPA / Hibernate contra MySQL. | `Usuario.java`, `Persona.java`, `Agencia.java`, `Tour.java`, `TourFecha.java`, `Reserva.java`, `Pago.java`, `Comision.java`, `Calificacion.java` (18 entidades) |
| `com.travelink.repositorio` | Repositorios de persistencia basados en Spring Data JPA (`JpaRepository`). | `UsuarioRepositorio.java`, `ReservaRepositorio.java`, `TourRepositorio.java`, `TourFechaRepositorio.java`, `PagoRepositorio.java`, `ComisionRepositorio.java` |
| `com.travelink.persistencia` | Capa DAO nativa mediante JDBC directo (`PreparedStatement`) para alto rendimiento. | `UsuarioDAO.java`, `ReservaDAO.java`, `TourDAO.java`, `AgenciaDAO.java`, `PagoDAO.java`, `DestinoDAO.java`, `NotificacionDAO.java` |
| `com.travelink.servicio` | Servicios transaccionales con reglas de negocio del dominio turístico. | `UsuarioServicio.java`, `ReservaServicio.java`, `TourServicio.java`, `PagoServicio.java`, `AgenciaServicio.java` |
| `com.travelink.reportes` | Generación de documentos PDF vectoriales y hojas de cálculo Excel en streaming. | `ReportesController.java`, `PdfReporteService.java`, `ExcelReporteService.java` |
| `com.travelink.server` | Adaptadores de compatibilidad HTTP y servlet para entornos de contenedor. | `ServletHttpExchangeAdapter.java` |

---

## 2. Catálogo de Dependencias Maven (`pom.xml`)

| Dependencia (`groupId:artifactId`) | Versión | Justificación Técnica y Motivo en TRAVELINK |
|---|---|---|
| `org.springframework.boot:spring-boot-starter-web` | 3.3.13 | Soporte para API REST MVC, servidor embebido Apache Tomcat y serialización Jackson JSON. |
| `org.springframework.boot:spring-boot-starter-security` | 3.3.13 | Autenticación basada en sesiones, codificador criptográfico `BCryptPasswordEncoder` y reglas de autorización RBAC. |
| `org.springframework.boot:spring-boot-starter-data-jpa` | 3.3.13 | Abstracción de acceso a datos con Hibernate y Spring Data JPA para las 18 entidades del dominio. |
| `org.springframework.boot:spring-boot-starter-validation` | 3.3.13 | Validación declarativa de beans (`@NotNull`, `@Size`, `@Email`) en los DTOs de entrada. |
| `com.mysql:mysql-connector-j` | 8.3.0 | Driver JDBC oficial para comunicación de alto rendimiento con la base de datos MySQL 8.0. |
| `com.github.librepdf:openpdf` | 1.3.39 | Generación de comprobantes oficiales de reserva (vouchers de viaje) y reportes ejecutivos en PDF vectorial (fork libre de iText sin licencias AGPL restrictivas). |
| `org.apache.poi:poi-ooxml` | 5.2.5 | Construcción y lectura de libros de cálculo Microsoft Excel OOXML (`.xlsx`) mediante `SXSSFWorkbook` con bajo consumo de memoria RAM. |
| `org.springframework.boot:spring-boot-starter-test` | 3.3.13 | Frameworks de pruebas automatizadas: JUnit 5 Jupiter, AssertJ y Mockito para pruebas unitarias de servicios. |
| `org.testcontainers:junit-jupiter` | 1.19.8 | Orquestación de pruebas de integración con contenedores efímeros controlados por JUnit. |
| `org.testcontainers:mysql` | 1.19.8 | Contenedor dinámico de MySQL 8.0 para validar la concurrencia y transacciones ACID en `ReservaPersistenciaIntegracionTest`. |
| `org.jacoco:jacoco-maven-plugin` | 0.8.12 | Medición y reporte de cobertura de código ejecutable (líneas e instrucciones) en la suite de pruebas. |

---

## 3. Perfiles y Configuración de Entorno (`application*.properties`)

TRAVELINK maneja perfiles desacoplados para desarrollo local y producción, garantizando que **nunca se escriban credenciales ni secretos en el código fuente**.

### 3.1 Perfil Predeterminado (`application.properties`)
- **Puerto del Servidor**: `server.port=8080`.
- **Desacoplamiento de Secretos**:
  - `spring.datasource.url=${DB_URL}`
  - `spring.datasource.username=${DB_USERNAME}`
  - `spring.datasource.password=${DB_PASSWORD}`
- **Pool de Conexiones HikariCP**:
  - `maximum-pool-size=15`, `minimum-idle=3`, `idle-timeout=30000ms`, `max-lifetime=180000ms`.
- **Gestión de Esquema JPA**:
  - `spring.jpa.hibernate.ddl-auto=none` (el esquema relacional se versiona estrictamente mediante `sql/travelink_schema.sql`).
- **Seguridad de Sesiones y Cookies**:
  - `server.servlet.session.timeout=120m`
  - `server.servlet.session.cookie.name=JSESSIONID`
  - `server.servlet.session.cookie.http-only=true` (Previene robo de sesión mediante ataques XSS).
  - `server.servlet.session.cookie.same-site=lax` (Mitigación CSRF perimetral).
- **Compresión HTTP**:
  - `server.compression.enabled=true` para tipos MIME JSON, HTML, CSS y JS con umbral mínimo de 1024 bytes.

### 3.2 Perfil de Desarrollo (`application-dev.properties`)
- Desactiva el caché estático (`spring.web.resources.cache.period=0`) para permitir la recarga en vivo de archivos HTML, CSS y JavaScript desde el sistema de archivos local (`Springboot/resources/static/`).

---

## 4. Configuración de Registro y Auditoría (`logback.xml`)

El subsistema de registro de TRAVELINK utiliza Logback con escaneo de cambios cada 30 segundos (`scan="true" scanPeriod="30 seconds"`):

### 4.1 Appenders Configurados
1. **`CONSOLE` (`ch.qos.logback.core.ConsoleAppender`)**:
   - Salida a la terminal estándar (`stdout`) con formato coloreado según la severidad (`%clr`), thread activo, nombre del logger y mensaje en codificación UTF-8.
2. **`FILE` (`ch.qos.logback.core.rolling.RollingFileAppender`)**:
   - Archivo de salida activo: `logs/travelink.log`.
   - **Política de Rotación**: `TimeBasedRollingPolicy` diaria con compresión gzip (`logs/travelink-%d{yyyy-MM-dd}.log.gz`).
   - **Retención**: Historial conservado automáticamente durante 30 días (`maxHistory=30`).
   - **Límite de Disco**: Capacidad máxima global de logs en disco fijada en 1 GB (`totalSizeCap=1GB`).

### 4.2 Niveles de Severidad por Paquete
- `com.travelink`: Nivel `INFO` (con `additivity="false"` para evitar duplicación de líneas en consola y archivo).
- `org.springframework`: Nivel `INFO`.
- `org.apache.catalina`: Nivel `WARN` (evita ruido en accesos normales a servlets).
- `com.zaxxer.hikari`: Nivel `INFO` (audita la salud y apertura de conexiones del pool).

---

## 5. Fichas Técnicas de Importación y Exportación de Ficheros

### 5.1 Ficha de Importación: Carga Masiva de Disponibilidad
- **Ruta de Endpoint**: `POST /api/agencia/importar/disponibilidad`
- **Controlador / Script Cliente**: `PaquetesAgenciaHandler.java` / `static/js/agencia/agencia_importar.js`
- **Formatos Aceptados**: `.xlsx` (Excel OOXML) y `.csv`.
- **Límite de Tamaño**: Máximo 5 MB por archivo.
- **DTO de Respuesta**: `com.travelink.controlador.dto.ImportacionResultadoDto`
  - `status`: `"success"` / `"warning"` / `"error"`.
  - `totalFilas`: Cantidad total de registros leídos.
  - `filasImportadas`: Registros insertados satisfactoriamente en `TourFecha`.
  - `filasError`: Filas descartadas por validación.
  - `errores`: Lista con `fila` (número exacto en el Excel) y `motivo` (ej. fecha pasada, precio negativo, cupo menor a 1).

### 5.2 Ficha de Exportación: Libros Excel Multitenant
- **Rutas de Endpoint**: `/api/agencia/reportes/*` y `/api/admin/reportes/*`
- **Servicio Responsable**: `com.travelink.reportes.ExcelReporteService`
- **Tecnología**: Apache POI 5.2.5 con `SXSSFWorkbook(100)` (streaming a disco tras 100 filas).
- **Estilos Gráficos**: Cabeceras con relleno azul marino (`DARK_BLUE`), tipografía blanca en negrita, bordes finos, formateo de moneda (`S/ #,##0.00`) y autoajuste automático de ancho de columnas (`trackAllColumnsForAutoSizing`).
- **Seguridad**: Cabecera `Cache-Control: no-cache, no-store, must-revalidate` para evitar persistencia de estados financieros en proxies intermedios.


---

## 6. Organización Estructural y Árbol de Paquetes Java

## 1. Visión General del Proyecto

TRAVELINK está estructurado como un monolito modular implementado en Java 17 con Spring Boot 3.3.13, organizado bajo el paquete raíz `com.travelink`. El frontend estático reside en `Springboot/resources/static/` desacoplado por vistas de rol, interactuando con el backend mediante una API REST en formato JSON.

---

## 2. Árbol de Paquetes Java (`Springboot/src/com/travelink`)

```text
com.travelink
├── config/           # Configuraciones de seguridad, beans de Spring y conexión JDBC directa
├── controlador/      # Controladores REST expuestos vía Spring Web MVC
├── entidades/        # Entidades del modelo de dominio mapeadas con JPA / Hibernate
├── persistencia/     # Capa DAO nativa mediante JDBC directo (Connection pool / DataSource)
├── reportes/         # Servicios de exportación en PDF (OpenPDF) y Excel (Apache POI)
├── repositorio/      # Repositorios JPA basados en Spring Data JPA (JpaRepository)
├── server/           # Adaptadores de protocolo y compatibilidad de servlet
└── servicio/         # Capa de lógica de negocio y orquestación transaccional
```

---

## 3. Catálogo de Clases por Módulo

### 3.1 Módulo de Configuración (`com.travelink.config`)
- `ConexionDB`: Gestor de conexiones JDBC nativas con lectura centralizada de variables de entorno (`DB_URL`, `DB_USER`, `DB_PASS`).
- `SecurityConfig`: Configuración del filtro de seguridad (`SecurityFilterChain`), definición de reglas RBAC (`permitAll`, `hasRole('ADMIN')`, etc.), encriptador `BCryptPasswordEncoder` y configuración de sesiones.
- `SecurityUtils`: Métodos auxiliares para la extracción segura del ID de usuario autenticado y rol activo desde el `SecurityContext`.
- `CustomUserDetailsService`: Implementación de `UserDetailsService` de Spring Security para autenticar usuarios contra la tabla `Usuario`.

### 3.2 Módulo de Controladores (`com.travelink.controlador`)
- `AuthController`: Registro de turistas/agencias, inicio de sesión, refresco de sesión y logout.
- `AdminController`: Gestión de agencias afiliadas, aprobación de solicitudes de registro, comisiones y métricas de plataforma.
- `AgenciaController`: Administración de catálogo de tours, fechas, cupos y balance financiero de la agencia.
- `DestinoController`: Catálogo público de destinos turísticos, ciudades y regiones.
- `TourController`: Consulta pública y detallada de tours, búsqueda con filtros combinados y disponibilidad de fechas.
- `ReservaController`: Creación del borrador de reserva, asociación de pasajeros, bloqueo de cupos y cancelación.
- `PagoController`: Registro de comprobantes de pago, integración simulada y confirmación transaccional.
- `TuristaController`: Panel del turista, historial de reservas activas y pasadas, descarga de vouchers.
- `PerfilController`: Actualización de datos de perfil, cambio de contraseñas y teléfonos de contacto.
- `NotificacionController`: Bandeja de notificaciones en tiempo real / polling para alertas operativas.
- `UsuarioController`: Gestión administrativa de cuentas de usuario.
- `TestController`: Endpoints de verificación de estado y pruebas de integración.

### 3.3 Módulo de Dominio y Entidades (`com.travelink.entidades`)
Contiene 18 entidades mapeadas contra el esquema relacional MySQL:
`Rol`, `Persona`, `Usuario`, `Agencia`, `SolicitudAgencia`, `HistorialAgencia`, `Destino`, `Tour`, `TourFecha`, `TourImagen`, `Reserva`, `DetalleReserva`, `Pasajero`, `MetodoPago`, `Pago`, `Calificacion`, `Comision`, `Notificacion`.

### 3.4 Capa de Persistencia Híbrida

#### Repositorios JPA (`com.travelink.repositorio`)
Interfaces de Spring Data JPA con consultas declarativas y métodos derivados:
- `UsuarioRepositorio`, `PersonaRepositorio`, `RolRepositorio`
- `AgenciaRepositorio`, `SolicitudAgenciaRepositorio`, `HistorialAgenciaRepositorio`
- `DestinoRepositorio`, `TourRepositorio`, `TourFechaRepositorio`, `TourImagenRepositorio`
- `ReservaRepositorio`, `DetalleReservaRepositorio`, `PasajeroRepositorio`
- `PagoRepositorio`, `MetodoPagoRepositorio`, `ComisionRepositorio`
- `CalificacionRepositorio`, `NotificacionRepositorio`

#### Capa DAO JDBC Directo (`com.travelink.persistencia`)
Implementaciones JDBC nativas para optimizar consultas de alta concurrencia, reportes complejos y operaciones transaccionales específicas:
- `AgenciaDAO`, `DestinoDAO`, `NotificacionDAO`, `PagoDAO`, `ReservaDAO`, `TourDAO`, `UsuarioDAO`.

### 3.5 Módulo de Servicios de Negocio (`com.travelink.servicio`)
- `AgenciaServicio`: Validación de RUC, flujo de verificación y actualización de datos fiscales.
- `DestinoServicio`: Normalización de ubicaciones geográficas y mantenimiento de catálogo.
- `NotificacionServicio`: Generación y despacho de eventos de notificación hacia usuarios.
- `PagoServicio`: Validación de montos, verificación de duplicados de referencia y cálculo de comisiones.
- `ReservaServicio`: Verificación de cupos disponibles (`TourFecha`), reserva transaccional y asignación de código único.
- `TourServicio`: Publicación de itinerarios, fijación de precios y gestión de inventario de fechas.
- `UsuarioServicio`: Cifrado de credenciales con BCrypt, asignación de roles iniciales y activación de cuentas.

### 3.6 Módulo de Reportes (`com.travelink.reportes`)
- `ReportesController`: Endpoints de descarga con control de aislamiento multitenant.
- `PdfReporteService`: Maquetación y renderizado con OpenPDF.
- `ExcelReporteService`: Construcción de libros `.xlsx` con Apache POI.

---

## 4. Frontend Desacoplado (`Springboot/resources/static/`)

El cliente web es una interfaz ligera basada en tecnologías web estándar (HTML5 semántico, CSS3 con variables personalizadas y JavaScript modular vainilla):
- `/index.html`: Portal principal con motor de búsqueda y tours destacados.
- `/html/turista/`: Vistas de exploración, reserva interactiva y panel de viajes del turista.
- `/html/agencia/`: Portal de la agencia (creación de tours, gestión de fechas, balance de cobros).
- `/html/administrador/`: Tablero de control de plataforma, auditoría y aprobación de agencias.
- `/css/`: Estilos globales y temas visuales (`global.css`, `header.css`, etc.).
- `/js/`: Módulos de lógica de presentación (`auth.js`, `catalogo.js`, `reservas.js`, etc.).
