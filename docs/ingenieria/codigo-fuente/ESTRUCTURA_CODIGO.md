# Estructura y Organización del Código Fuente

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
