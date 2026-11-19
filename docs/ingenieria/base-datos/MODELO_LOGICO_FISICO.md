# Modelo Lógico y Físico de Base de Datos — TRAVELINK

Este documento especifica el modelo relacional de base de datos de TRAVELINK, extraído directamente del script DDL de producción [`sql/travelink_schema.sql`](../../../sql/travelink_schema.sql), desglosando el diagrama Entidad-Relación (ER), el modelo lógico y el modelo físico de implementación en MySQL 8.0.

---

## 1. Diagrama Entidad-Relación Global (Mermaid erDiagram)

```mermaid
erDiagram
    Rol ||--o{ Usuario : "asigna rol"
    Persona ||--|| Usuario : "pertenece a"
    Usuario ||--o| Agencia : "administra"
    Usuario ||--o{ SolicitudAgencia : "registra solicitud"
    Usuario ||--o{ Reserva : "genera"
    Usuario ||--o{ Calificacion : "emite"
    Usuario ||--o{ Notificacion : "recibe"

    Agencia ||--o{ Tour : "ofrece catálogo"
    Agencia ||--o{ HistorialAgencia : "registra cambios"
    Agencia ||--o{ Reserva : "recibe reserva"
    Agencia ||--o{ Calificacion : "es evaluada"
    Agencia ||--o{ Comision : "liquida comision"

    Destino ||--o{ Tour : "ubica"
    Tour ||--o{ TourFecha : "calendariza salidas"
    Tour ||--o{ TourImagen : "contiene imagenes"
    Tour ||--o{ Reserva : "referencia tour"

    TourFecha ||--o{ Reserva : "asigna salida"
    TourFecha ||--o{ DetalleReserva : "asocia fecha"

    Reserva ||--|{ DetalleReserva : "desglosa"
    Reserva ||--|{ Pasajero : "incluye"
    Reserva ||--o{ MetodoPago : "registra transaccion"
    Reserva ||--o| Pago : "recibe cobro"
    Reserva ||--o| Calificacion : "valora experiencia"
    Reserva ||--o| Comision : "origina comision"

    Rol {
        int idRol PK
        string nombreRol UK
    }

    Persona {
        int idPersona PK
        string nombre
        string apellidoPaterno
        string apellidoMaterno
        string nroDocumento UK
        string telefono
        string email UK
    }

    Usuario {
        int idUsuario PK
        int idPersona FK
        int idRol FK
        string nombreUsuario UK
        string contrasena
        string estado
        datetime fechaRegistro
    }

    Agencia {
        int idAgencia PK
        int idUsuario FK
        string razonSocial
        string ruc UK
        string estado
        decimal porcentajeComision
        decimal comision
    }

    SolicitudAgencia {
        int idSolicitud PK
        int idUsuario FK
        string razonSocial
        string ruc
        string estado
        datetime fechaSolicitud
    }

    HistorialAgencia {
        int idHistorial PK
        int idAgencia FK
        string estadoAnterior
        string estadoNuevo
        datetime fecha
    }

    Destino {
        int idDestino PK
        string nombre UK
        string estado
    }

    Tour {
        int idTour PK
        int idAgencia FK
        int idDestino FK
        string slug UK
        string nombre
        decimal precioAdulto
        string estado
    }

    TourFecha {
        int idTourFecha PK
        int idTour FK
        date fecha
        time horaInicio
        int cupoTotal
        int cupoDisponible
        string estado
    }

    TourImagen {
        int idImagen PK
        int idTour FK
        string url
        boolean esPrincipal
        int orden
    }

    Reserva {
        int idReserva PK
        int idUsuario FK
        int idAgencia FK
        int idTour FK
        int idTourFecha FK
        string codigoReserva UK
        date fechaInicio
        date fechaFin
        string estado
        decimal total
    }

    DetalleReserva {
        int idDetalle PK
        int idReserva FK
        int idTourFecha FK
        int cantAdultos
        int cantNinos
        decimal subtotal
    }

    Pasajero {
        int idPasajero PK
        int idReserva FK
        string nroDocumento
        string nombre
        boolean esTitular
        string tipo
    }

    MetodoPago {
        int idMetodoPago PK
        int idReserva FK
        string tipoPago
        decimal monto
        string estadoPago
    }

    Pago {
        int idPago PK
        int idReserva FK
        decimal monto
        string metodoPago
        string estado
        string numeroOperacion
    }

    Calificacion {
        int idCalificacion PK
        int idUsuario FK
        int idAgencia FK
        int idReserva FK
        tinyint estrellas
        string comentario
        string estadoRevision
    }

    Comision {
        int idComision PK
        int idAgencia FK
        int idReserva FK
        string periodo
        decimal porcentaje
        decimal montoComision
        decimal montoNeto
        string estado
    }

    Notificacion {
        int idNotificacion PK
        int idUsuario FK
        string rolTarget
        string titulo
        string mensaje
        boolean leido
    }
```

---

## 2. Modelo Lógico de Datos

El modelo lógico organiza la persistencia en cuatro módulos de negocio principales:

### 2.1 Módulo de Identidad y Acceso (RBAC)
- **`Rol`**: Catálogo maestro de roles del sistema (`ADMIN`, `AGENCIA`, `TURISTA`).
- **`Persona`**: Datos personales y demográficos desacoplados de las credenciales de autenticación (nombres, documento nacional, correo personal).
- **`Usuario`**: Cuenta de seguridad para login (nombre de usuario único, hash BCrypt, estado y rol asignado).

### 2.2 Módulo de Afiliación de Agencias
- **`Agencia`**: Perfil comercial y fiscal de la empresa de turismo (RUC único, razón social, dirección, porcentaje de comisión fijado al 15%).
- **`SolicitudAgencia`**: Registro de peticiones de registro remitidas por agencias pendientes de validación por el administrador.
- **`HistorialAgencia`**: Pista de auditoría del ciclo de vida y cambios de estado de la agencia (`PENDIENTE` -> `APROBADA` / `RECHAZADA`).

### 2.3 Módulo de Catálogo Turístico
- **`Destino`**: Regiones y locaciones turísticas normalizadas (ej. Cusco, Arequipa, Máncora).
- **`Tour`**: Ficha del paquete turístico ofrecido por una agencia, con slug web único, precios diferenciados (adulto, niño, bebé) y políticas de inclusión.
- **`TourFecha`**: Salidas programadas con control estricto de cupo (`cupoTotal`, `cupoDisponible`) y restricción de unicidad por fecha/hora.
- **`TourImagen`**: Galería fotográfica asociada con indicador de portada (`esPrincipal`) y orden de visualización.

### 2.4 Módulo Transaccional: Reservas, Pasajeros y Liquidación
- **`Reserva`**: Encabezado del contrato de viaje, asociado al turista, agencia, fecha y código alfanumérico único.
- **`DetalleReserva`**: Desglose de asientos reservados y subtotales por grupo etario.
- **`Pasajero`**: Registro nominal de cada viajero (documento de identidad, titularidad y edad).
- **`MetodoPago` y `Pago`**: Transacciones financieras, comprobantes y referencias de operación.
- **`Comision`**: Conciliación de comisiones de plataforma (15%) adeudadas por la agencia.
- **`Calificacion`**: Reseñas verificadas vinculadas obligatoriamente a una reserva concluida (1 a 5 estrellas).
- **`Notificacion`**: Despacho de avisos y alertas operativas a los perfiles de usuario.

---

## 3. Modelo Físico de Datos (MySQL 8.0)

### 3.1 Motor de Almacenamiento y Codificación
- **Motor**: `InnoDB` (compatibilidad transaccional ACID, claves foráneas y bloqueo a nivel de fila).
- **Charset / Collation**: `utf8mb4` / `utf8mb4_unicode_ci` (soporte completo de caracteres internacionales, tildes y símbolos).

### 3.2 Catálogo de Índices de Rendimiento

| Tabla | Nombre del Índice | Columnas Indexadas | Propósito de Optimización |
|---|---|---|---|
| `Tour` | `idx_tour_agencia_estado` | `(idAgencia, estado)` | Filtro rápido en portal de agencia para tours activos |
| `Tour` | `idx_tour_destino` | `(idDestino)` | Búsqueda pública por destino en portal principal |
| `TourFecha` | `uq_tour_fecha_hora` | `(idTour, fecha, horaInicio)` | Restricción UNIQUE: previene duplicidad de salidas idénticas |
| `TourFecha` | `idx_tourfecha_fecha` | `(idTour, fecha)` | Búsqueda de disponibilidad en calendario |
| `TourImagen` | `idx_tourimagen_orden` | `(idTour, esPrincipal, orden)` | Carga prioritaria de imagen de portada en catálogo |
| `Reserva` | `idx_reserva_usuario` | `(idUsuario)` | Historial de compras en portal del turista |
| `Reserva` | `idx_reserva_agencia` | `(idAgencia)` | Bandeja de reservas recibidas por la agencia |
| `Reserva` | `idx_reserva_tourfecha` | `(idTourFecha)` | Control de cupos y cruce con salidas |
| `Reserva` | `idx_reserva_estado` | `(estado)` | Filtros de reportes analíticos |
| `Calificacion` | `idx_calificacion_agencia` | `(idAgencia)` | Cálculo de promedio de satisfacción por agencia |
| `Calificacion` | `idx_calificacion_reserva` | `(idReserva)` | Restricción y verificación de compra previa |

### 3.3 Políticas de Integridad Referencial
- **`ON DELETE RESTRICT ON UPDATE CASCADE`**: Aplicada en entidades principales (`Usuario`, `Rol`, `Persona`, `Agencia`, `Destino`, `Tour`) para evitar eliminaciones accidentales en cascada que dejen registros huérfanos en auditoría financiera.
- **`ON DELETE CASCADE ON UPDATE CASCADE`**: Aplicada en detalles subordinados (`DetalleReserva`, `Pasajero`, `TourImagen`, `MetodoPago`) donde la eliminación del registro maestro justifica la eliminación de sus componentes asociados.
