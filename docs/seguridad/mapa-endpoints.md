# Mapa de Endpoints y Superficie de Ataque — TRAVELINK

**Proyecto:** TRAVELINK — Plataforma de Gestión y Reservas Turísticas  
**Fecha de Elaboración:** Noviembre 2026  
**Entorno de Evaluación:** Local Reproducible (`http://127.0.0.1:8080`)  
**Rama:** `feature/seguridad-pruebas`  

---

## 1. Resumen Ejecutivo de la Superficie de Exposición

El análisis de la configuración de seguridad (`com.travelink.config.SecurityConfig`) y de los controladores REST y handlers (`com.travelink.controlador.*`) reveló una arquitectura mixta:
- Un framework moderno basado en **Spring Boot 3.3.4** y **Spring Security 6**.
- Un subsistema de controladores Spring REST (`@RestController`) que coexiste con controladores adaptadores delegando peticiones a handlers HTTP nativos de tipo `com.sun.net.httpserver.HttpHandler` (`ServletHttpExchangeAdapter`).
- Un modelo de autorización con roles: `ROLE_TURISTA` (Rol 1), `ROLE_AGENCIA` (Rol 2) y `ROLE_ADMIN` (Rol 3).

### Hallazgos Arquitectónicos Clave en el Mapeo:
1. **Exposición de APIs de Estado sin Autenticación (`permitAll()`):** Endpoints sensibles como `/api/obtener_reservas`, `/api/guardar_reserva`, `/api/cancelar_reserva` y `/api/calificar` están configurados con acceso público irrestricto en `SecurityConfig`.
2. **Desactivación Masiva de CSRF:** Más de 20 rutas que modifican estado (POST, PUT, DELETE) se encuentran listadas en `ignoringRequestMatchers()`.
3. **Inconsistencias de Autorización por Rutas Duales:** Endpoints de administración que no poseen el prefijo `/api/admin/` (ej. `/api/usuarios`, `/api/comisiones`, `/api/comision/liquidar`, `/api/calidad/eliminar`) eluden la regla `.requestMatchers("/api/admin/**").hasRole("ADMIN")` y quedan expuestos a cualquier usuario autenticado bajo `.anyRequest().authenticated()`.
4. **CORS Excesivamente Permisivo:** `allowedOriginPatterns("*")` con `allowCredentials(true)` expone la totalidad de los endpoints a peticiones autenticadas desde orígenes arbitrarios.

---

## 2. Inventario Consolidado de Endpoints por Módulo

### 2.1. Módulo de Autenticación y Sesión

| Método | Ruta | Controlador / Handler | Requiere Auth | Rol Permitido | Parámetros (Query / Body / Header) | Tipo de Dato Manejado | Nivel de Riesgo | Observaciones de Seguridad |
|---|---|---|---|---|---|---|---|---|
| `POST` | `/api/auth/login` | `AuthApiController.login` | No | Anónimo / Todos | **Body (JSON):** `correo`/`usuario`/`email`, `clave`/`contrasena`/`password`, `rolEsperado`/`portal` | Credenciales de acceso | **Alto** | Valida contra BCrypt pero mantiene bypass para contraseñas en texto plano y backdoor de claves débiles en `UsuarioServicioImpl`. Posee protección contra fuerza bruta (bloqueo por 15 min tras 5 fallos). |
| `POST` | `/api/login` | `AuthApiController.login` | No | Anónimo / Todos | **Body (JSON):** ídem anterior | Credenciales de acceso | **Alto** | Alias de login. |
| `POST` | `/api/agencia/login` | `AuthApiController.login` | No | Anónimo / Todos | **Body (JSON):** ídem anterior | Credenciales de acceso | **Alto** | Valida que el rol sea `ROLE_AGENCIA` y que cuente con solicitud aprobada. |
| `POST` | `/api/admin/login` | `AuthApiController.login` | No | Anónimo / Todos | **Body (JSON):** ídem anterior | Credenciales de acceso | **Alto** | Valida que el rol resuelto sea `ROLE_ADMIN`. |
| `POST` | `/api/auth/logout` | `AuthApiController.logout` | No | Autenticado / Anónimo | Ninguno (invalida sesión HTTP existente) | Token de sesión | **Bajo** | Invalida `HttpSession` y limpia el `SecurityContextHolder`. |
| `POST` | `/api/logout` | `AuthApiController.logout` | No | Autenticado / Anónimo | Ninguno | Token de sesión | **Bajo** | Alias de logout. |
| `GET` | `/api/auth/me` | `AuthApiController.me` | Sí | Cualquier autenticado | **Cookie:** `JSESSIONID` | Perfil de usuario (PII) | **Bajo** | Retorna datos del usuario en sesión (`idUsuario`, nombres, apellidos, correo, documento, rol, `idAgencia`). |

---

### 2.2. Módulo de Registro de Usuarios

| Método | Ruta | Controlador / Handler | Requiere Auth | Rol Permitido | Parámetros (Query / Body / Header) | Tipo de Dato Manejado | Nivel de Riesgo | Observaciones de Seguridad |
|---|---|---|---|---|---|---|---|---|
| `POST` | `/api/registro` | `PublicApiController.registro` → `RegistroHandler` | No | Anónimo | **Body (JSON/Form):** `nombre`, `apellidoPaterno`, `apellidoMaterno`, `usuario`, `correo`, `password`, `telefono`, `nroDocumento` | PII (Identidad, contacto y credenciales) | **Medio** | Valida formato DNI y teléfono. Hashea contraseña con BCrypt. CSRF ignorado. |
| `POST` | `/api/agencia/registro` | `AgenciaApiController.registro` → `RegistroAgenciaHandler` | No | Anónimo | **Body (JSON/Form):** `nombreUsuario`, `correo`, `contrasena`, `razonSocial`, `nombreComercial`, `ruc`, `telefonoEmpresa`, `telefonoResponsable`, `direccion`, etc. | PII y Datos Financieros/Fiscales | **Medio** | Crea cuenta con rol AGENCIA (Rol 2) en estado ACTIVO y solicitud asociada pendiente de revisión admin. CSRF ignorado. |

---

### 2.3. Módulo Turista y Gestión de Reservas

| Método | Ruta | Controlador / Handler | Requiere Auth | Rol Permitido | Parámetros (Query / Body / Header) | Tipo de Dato Manejado | Nivel de Riesgo | Observaciones de Seguridad |
|---|---|---|---|---|---|---|---|---|
| `GET` | `/api/obtener_reservas` | `PublicApiController.obtenerReservas` → `ObtenerReservasHandler` | No (`permitAll`) | Anónimo / Cualquier usuario | **Query:** `idUsuario` (int), `correo`/`email` (string) | PII, Reservas y Datos Financieros parciales | **CRÍTICO** | **IDOR / BOLA Directo:** Si se pasa `idUsuario` en la URL, omite la sesión del solicitante y devuelve todas las reservas, pasajeros (nombres, DNI), itinerarios, montos, marca y últimos dígitos de tarjeta del usuario consultado. |
| `GET` | `/api/turista/reservas` | `PublicApiController.guardarReserva` (GET) → `ObtenerReservasHandler` | No (`permitAll`) | Anónimo / Cualquier usuario | **Query:** `idUsuario`, `correo` | PII y Datos Financieros | **CRÍTICO** | Alias vulnerable a IDOR. |
| `GET` | `/api/reservas` | `PublicApiController.guardarReserva` (GET) → `ObtenerReservasHandler` | No (`permitAll`) | Anónimo / Cualquier usuario | **Query:** `idUsuario`, `correo` | PII y Datos Financieros | **CRÍTICO** | Alias vulnerable a IDOR. |
| `POST` | `/api/guardar_reserva` | `PublicApiController.guardarReserva` (POST) → `GuardarReservaHandler` | No (`permitAll`) | Anónimo / Autenticado | **Body (JSON):** `idTour`, `idTourFecha`, `cantAdultos`, `cantNinos`, `cantBebes`, `pasajeros` (array con DNI, nombres), `metodoPago`, `marcaTarjeta`, `ultimosDigitos` | PII y Datos Financieros | **Alto** | CSRF ignorado. Permite registrar reservas sin autenticación previa (turista invitado) asociando datos ingresados directamente. |
| `POST` | `/api/turista/reservas` | `PublicApiController.guardarReserva` (POST) → `GuardarReservaHandler` | No (`permitAll`) | Anónimo / Autenticado | **Body (JSON):** ídem anterior | PII y Financiero | **Alto** | Alias de creación de reserva. |
| `POST` | `/api/reservas` | `PublicApiController.guardarReserva` (POST) → `GuardarReservaHandler` | No (`permitAll`) | Anónimo / Autenticado | **Body (JSON):** ídem anterior | PII y Financiero | **Alto** | Alias de creación de reserva. |
| `POST` / `DELETE` / `PUT` | `/api/cancelar_reserva` | `PublicApiController.cancelarReserva` → `CancelarReservaHandler` | No (`permitAll`) | Anónimo / Autenticado | **Query/Body:** `idReserva`, `codigoReserva`/`codigo`, `motivo` | Estado de Reservas | **CRÍTICO** | **BOLA / IDOR en Cancelación:** La validación de propiedad de la reserva solo se ejecuta si existe una sesión autenticada (`sessionUserId != null`). Si la petición se envía sin sesión, la validación se omite y la reserva indicada se cancela en la BD. |
| `GET` | `/api/turista/reservas/{id}/comprobante.pdf` | Spring Web Controller | Sí | `TURISTA`, `AGENCIA`, `ADMIN` | **Path:** `id` (int) | Financiero y PII (Comprobante PDF) | **Medio** | Generación de comprobante en formato PDF. Requiere validar que la reserva pertenezca al usuario en sesión. |

---

### 2.4. Módulo de Calificaciones y Reseñas

| Método | Ruta | Controlador / Handler | Requiere Auth | Rol Permitido | Parámetros (Query / Body / Header) | Tipo de Dato Manejado | Nivel de Riesgo | Observaciones de Seguridad |
|---|---|---|---|---|---|---|---|---|
| `GET` | `/api/calificaciones` / `/api/calificar` | `PublicApiController.calificar` (GET) → `CalificarHandler.obtenerCalificaciones` | No (`permitAll`) | Anónimo / Público | **Query:** `idAgencia`, `agencia`, `idTour`, `tour` | Reseñas y opiniones | **Medio** | Los campos `turista` y `comentario` no son saneados contra HTML al responder en JSON, permitiendo Stored XSS en el cliente frontend. |
| `POST` | `/api/calificar` | `PublicApiController.calificar` (POST) → `CalificarHandler.guardarCalificacion` | No (`permitAll`) | Anónimo / Autenticado | **Body (JSON/Form):** `idReserva`, `idAgencia`, `estrellas`, `comentario` | Contenido de opiniones | **Alto** | No exige autenticación obligatoria. Almacena payloads HTML/JavaScript sin filtrado en la columna `comentario`. CSRF ignorado. |
| `PUT` | `/api/calificar` | `PublicApiController.calificar` (PUT) → `CalificarHandler.editarCalificacion` | No (`permitAll`) | Anónimo / Autenticado | **Body/Query:** `idCalificacion`, `estrellas`, `comentario` | Reseñas | **Alto** | **BOLA Directo:** Permite a cualquier usuario o atacante anónimo editar la puntuación y comentario de cualquier reseña arbitraria por ID sin comprobar autoría. |
| `DELETE` | `/api/calificar` | `PublicApiController.calificar` (DELETE) → `CalificarHandler.eliminarCalificacion` | No (`permitAll`) | Anónimo / Autenticado | **Body/Query:** `idCalificacion` | Control de contenido | **Alto** | **BOLA Directo:** Permite a cualquier usuario o atacante anónimo dar de baja lógica (`eliminadoPorAdmin=1`) cualquier calificación del sistema. |

---

### 2.5. Módulo Catálogo Público (Tours, Destinos y Agencias)

| Método | Ruta | Controlador / Handler | Requiere Auth | Rol Permitido | Parámetros (Query / Body / Header) | Tipo de Dato Manejado | Nivel de Riesgo | Observaciones de Seguridad |
|---|---|---|---|---|---|---|---|---|
| `GET` | `/api/destinos` | `PublicApiController.destinos` → `DestinosAdminHandler` | No (`permitAll`) | Anónimo | **Query:** `idDestino`, `estado` | Catálogo de departamentos | **Bajo** | Consulta de lectura pública. Utiliza `PreparedStatement`. |
| `GET` | `/api/destinos/tours` | `PublicApiController.destinosTours` → `DestinosToursAdminHandler` | No (`permitAll`) | Anónimo | **Query:** filtros de catálogo | Catálogo y paquetes | **Bajo** | Consulta de lectura pública. |
| `GET` | `/api/tours` / `/api/tours/**` | `PublicApiController.tours` → `ToursAdminHandler` | No (`permitAll`) | Anónimo | **Query:** `idTour`, `slug`, `categoria`, `destino`, `precioMin`, `precioMax`, `q` | Catálogo de tours | **Bajo** | Búsqueda y listado público con filtros. Consultas SQL parametrizadas. |
| `GET` | `/api/agencias` / `/api/agencias/**` | `PublicApiController.agencias` → `AgenciasAdminHandler` | No (`permitAll`) | Anónimo | **Query:** filtros de agencias | Directorio comercial | **Bajo** | Información comercial pública de agencias verificadas. |

---

### 2.6. Módulo de Tiempo Real y Eventos (SSE)

| Método | Ruta | Controlador / Handler | Requiere Auth | Rol Permitido | Parámetros (Query / Body / Header) | Tipo de Dato Manejado | Nivel de Riesgo | Observaciones de Seguridad |
|---|---|---|---|---|---|---|---|---|
| `GET` | `/api/eventos` | `EventosController.suscribirEventos` | No (`permitAll`) | Anónimo | Ninguno (Stream SSE) | Eventos en tiempo real | **Medio** | Canal abierto SSE. Potencial vector de denegación de servicio por agotamiento de conexiones concurrentes en el servidor. |
| `GET` | `/api/eventos/poll` | `EventosController.sondeoEventos` | No (`permitAll`) | Anónimo | Ninguno | Estado de eventos | **Bajo** | Fallback de sondeo periódico liviano. |
| `POST` | `/api/eventos/emitir` | `EventosController.emitirManual` | No (`permitAll`) | Anónimo | **Body (JSON):** `tipo`, `data` | Mensajería de eventos | **Alto** | **Falta de Control de Acceso:** Permite a un atacante anónimo inyectar eventos falsos a todos los navegadores conectados a la plataforma. CSRF ignorado. |

---

### 2.7. Módulo de Administración del Sistema

| Método | Ruta | Controlador / Handler | Requiere Auth | Rol Permitido | Parámetros (Query / Body / Header) | Tipo de Dato Manejado | Nivel de Riesgo | Observaciones de Seguridad |
|---|---|---|---|---|---|---|---|---|
| `GET` | `/api/admin/dashboard` | `AdminApiController.adminDashboard` → `DashboardAdminHandler` | Sí | `ROLE_ADMIN` | Ninguno | Métricas financieras y de negocio | **Bajo** | Protegido correctamente por RBAC (`hasRole("ADMIN")`). |
| `GET` / `POST` / `PUT` / `DELETE` | `/api/admin/agencias` | `AdminApiController.adminAgencias` → `AgenciasAdminHandler` | Sí | `ROLE_ADMIN` | **Body/Query:** `idAgencia`, `idSolicitud`, `accion` (`aprobar`/`rechazar`) | PII y Documentación de Agencias | **Medio** | Operaciones administrativas de agencias. Protegido por RBAC; CSRF ignorado. |
| `GET` | `/api/admin/notificaciones` | `AdminApiController.adminNotificaciones` → `NotificacionesAdminHandler` | Sí | `ROLE_ADMIN` | **Query:** `leido`, `tipo` | Alertas del sistema | **Bajo** | Consulta interna. |
| `GET` / `POST` / `PUT` / `DELETE` | `/api/admin/destinos` | `AdminApiController.adminDestinos` → `DestinosAdminHandler` | Sí | `ROLE_ADMIN` | Parámetros de destinos | Catálogo geográfico | **Medio** | Modificación de destinos. CSRF ignorado. |
| `GET` / `POST` / `PUT` / `DELETE` | `/api/admin/destinos/tours` | `AdminApiController.adminDestinosTours` → `DestinosToursAdminHandler` | Sí | `ROLE_ADMIN` | Relaciones destino-tour | Catálogo | **Medio** | CSRF ignorado. |
| `GET` / `POST` / `PUT` / `DELETE` | `/api/admin/tours` | `AdminApiController.adminTours` → `ToursAdminHandler` | Sí | `ROLE_ADMIN` | Parámetros de tours | Catálogo de tours | **Medio** | Modificación global de tours. CSRF ignorado. |
| `GET` / `POST` | `/api/admin/usuarios` | `AdminApiController.adminUsuarios` → `UsuariosAdminHandler` | Sí | `ROLE_ADMIN` | **Query/Body:** `idUsuario`, `estado` | PII de usuarios | **Medio** | Listado y cambio de estado de usuarios. CSRF ignorado. |
| `GET` / `POST` | `/api/usuarios`, `/api/usuario/guardar`, `/api/usuario/eliminar` | `AdminApiController.adminUsuarios` → `UsuariosAdminHandler` | Sí | Cualquier rol autenticado | **Query/Body:** `idUsuario`, `estado` | PII de usuarios | **CRÍTICO** | **Escalamiento de Privilegios:** Rutas alternativas sin prefijo `/api/admin/` permiten a turistas y agencias listar usuarios o desactivar cuentas administrativas. |
| `GET` / `POST` | `/api/admin/reservas`, `/api/admin/reservas/eliminar` | `AdminApiController.adminReservas` → `ReservasAdminHandler` | Sí | `ROLE_ADMIN` | **Query/Body:** `idReserva` | Reservas del sistema | **Medio** | Baja lógica de reservas por admin. CSRF ignorado. |
| `POST` | `/api/reserva/eliminar` | `AdminApiController.adminReservas` → `ReservasAdminHandler` | Sí | Cualquier rol autenticado | **Query/Body:** `idReserva` | Reservas del sistema | **Alto** | Ruta alias desprotegida de la restricción `ADMIN`. |
| `GET` | `/api/admin/comisiones` | `AdminApiController.adminComisiones` → `ComisionesAdminHandler` | Sí | `ROLE_ADMIN` | Filtros de liquidación | Datos contables (15%) | **Bajo** | Protegido por RBAC. |
| `GET` | `/api/comisiones` | `AdminApiController.adminComisiones` → `ComisionesAdminHandler` | Sí | Cualquier rol autenticado | Filtros de liquidación | Datos contables (15%) | **Alto** | Fuga de información financiera a cualquier usuario autenticado. |
| `POST` | `/api/admin/comision/liquidar` | `AdminApiController.liquidarComision` → `LiquidarComisionHandler` | Sí | `ROLE_ADMIN` | **Body:** `idComision`, `monto` | Transacciones contables | **Medio** | Protegido por RBAC; CSRF ignorado. |
| `POST` | `/api/comision/liquidar` | `AdminApiController.liquidarComision` → `LiquidarComisionHandler` | Sí | Cualquier rol autenticado | **Body:** `idComision`, `monto` | Transacciones contables | **Alto** | Un turista puede liquidar comisiones de agencias vía la ruta alias. |
| `GET` | `/api/admin/calidad` | `AdminApiController.adminCalidad` → `CalidadAdminHandler` | Sí | `ROLE_ADMIN` | Métricas de calidad | Calidad de agencias | **Bajo** | Protegido por RBAC. |
| `GET` | `/api/calidad` | `AdminApiController.adminCalidad` → `CalidadAdminHandler` | Sí | Cualquier rol autenticado | Métricas de calidad | Calidad de agencias | **Medio** | Ruta alias accesible por cualquier rol. |
| `POST` | `/api/admin/calidad/eliminar` | `AdminApiController.eliminarCalidad` → `EliminarResenaHandler` | Sí | `ROLE_ADMIN` | **Query/Body:** `idCalificacion` | Moderación de reseñas | **Medio** | Eliminación administrativa de reseñas. CSRF ignorado. |
| `POST` | `/api/calidad/eliminar` | `AdminApiController.eliminarCalidad` → `EliminarResenaHandler` | Sí | Cualquier rol autenticado | **Query/Body:** `idCalificacion` | Moderación de reseñas | **Alto** | Ruta alias desprotegida de la regla `ADMIN`. |

---

### 2.8. Módulo de Portal de Agencias de Viaje

| Método | Ruta | Controlador / Handler | Requiere Auth | Rol Permitido | Parámetros (Query / Body / Header) | Tipo de Dato Manejado | Nivel de Riesgo | Observaciones de Seguridad |
|---|---|---|---|---|---|---|---|---|
| `GET` | `/api/agencia/dashboard` | `AgenciaApiController.dashboard` → `DashboardAgenciaHandler` | Sí | `ROLE_AGENCIA` | Ninguno | Métricas operacionales | **Bajo** | Protegido por `hasRole("AGENCIA")`. |
| `GET` | `/api/agencia/destinos` | `AgenciaApiController.destinos` → `DestinosAgenciaHandler` | Sí | `ROLE_AGENCIA` | Catálogo de destinos | Catálogo | **Bajo** | Protegido por RBAC. |
| `GET` | `/api/agencia/tipos` | `AgenciaApiController.tipos` → `TiposAgenciaHandler` | Sí | `ROLE_AGENCIA` | Tipos de tours | Catálogo | **Bajo** | Protegido por RBAC. |
| `GET` | `/api/agencia/servicios` | `AgenciaApiController.servicios` → `ServiciosAgenciaHandler` | Sí | `ROLE_AGENCIA` | Servicios incluidos | Catálogo | **Bajo** | Protegido por RBAC. |
| `GET` / `POST` / `PUT` / `DELETE` | `/api/agencia/disponibilidad` | `AgenciaApiController.disponibilidad` → `DisponibilidadAgenciaHandler` | Sí | `ROLE_AGENCIA` | Parámetros de salidas y cupos | Cupos y fechas operativas | **Medio** | CSRF ignorado. |
| `GET` / `POST` / `PUT` / `DELETE` | `/api/agencia/paquetes` | `AgenciaApiController.paquetes` → `PaquetesAgenciaHandler` | Sí | `ROLE_AGENCIA` | **Body/Query:** Datos del paquete, `idTour`, `idTourFecha` | Catálogo e itinerarios de agencia | **Alto** | En la eliminación y pausado de salidas individuales (`eliminarSalida`), no se verifica que la fecha pertenezca a un tour de la agencia en sesión (BOLA horizontal entre agencias). CSRF ignorado. |
| `GET` | `/api/agencia/reservas` | `AgenciaApiController.reservas` → `ReservasAgenciaHandler` | Sí | `ROLE_AGENCIA` | Filtros de reservas | PII de turistas y pasajeros | **Medio** | Valida el `idAgencia` inyectado por la sesión. |
| `GET` | `/api/agencia/pagos` | `AgenciaApiController.pagos` → `PagosAgenciaHandler` | Sí | `ROLE_AGENCIA` | Filtros de liquidación | Datos financieros de la agencia | **Medio** | Reporte contable de la agencia autenticada. |
| `POST` | `/api/agencia/importar/disponibilidad` | `AgenciaApiController.importarDisponibilidad` | Sí | `ROLE_AGENCIA` | **Multipart:** `archivo` / `file` (.xlsx / .csv) | Archivos de disponibilidad | **Medio** | Subida de archivos procesada por Apache POI. Requiere control de tamaño y mitigación contra Zip-Bomb/XML-Bomb. |
| `GET` | `/api/agencia/plantilla/disponibilidad.xlsx` | `AgenciaApiController.descargarPlantilla` | Sí | `ROLE_AGENCIA` | Ninguno | Plantilla Excel descargable | **Bajo** | Generación dinámica de plantilla sin datos sensibles. |

---

## 3. Matriz de Rutas HTML y Control de Acceso Web

| Ruta Web (Frontend) | Nivel de Protección | Regla Spring Security | Redirección en Fallo |
|---|---|---|---|
| `/`, `/index.html`, `/favicon.ico` | Público | `permitAll()` | N/A |
| `/css/**`, `/js/**`, `/img/**` | Público | `permitAll()` | N/A |
| `/html/turista/index.html`, `/detalle.html`, `/experiencias.html`, `/agencias.html`, `/ofertas.html`, `/nosotros.html`, `/detalle-agencia.html`, `/form_reserva.html` | Público | `permitAll()` | N/A |
| `/html/turista/login.html`, `/html/turista/registro.html` | Público | `permitAll()` | N/A |
| `/html/administrador/login.html` | Público | `permitAll()` | N/A |
| `/html/agencia/R_agencia_*.html` (Login, registro, asistentes) | Público | `permitAll()` | N/A |
| `/html/turista/reservas.html` | Protegido | `authenticated()` | Redirige a `/html/turista/login.html?redirect=...` |
| `/html/administrador/**` | Protegido | `hasRole("ADMIN")` | Redirige a `/html/administrador/login.html` |
| `/html/agencia/**` (Paneles de gestión) | Protegido | `hasRole("AGENCIA")` | Redirige a `/html/agencia/R_agencia_login.html` |
