# Diccionario de Datos — Base de Datos TRAVELINK

Este documento contiene la especificación exhaustiva del esquema de base de datos relacional de TRAVELINK (`DBTravelink`), cubriendo las 18 tablas definidas en [`sql/travelink_schema.sql`](../../../sql/travelink_schema.sql).

---

## 1. Tabla: `Rol`
Define los roles del sistema utilizados para el control de acceso basado en roles (RBAC).

| Columna | Tipo de Dato | Nulo | Clave | Valor por Defecto | Descripción |
|---|---|---|---|---|---|
| `idRol` | `INT` | NO | PK (Auto) | Auto-incremental | Identificador único del rol de usuario. |
| `nombreRol` | `VARCHAR(20)` | NO | UK | Ninguno | Nombre único del rol (`ADMINISTRADOR`, `AGENCIA`, `TURISTA`). |

---

## 2. Tabla: `Persona`
Almacena los datos de identidad civil y demográfica de las personas asociadas a usuarios.

| Columna | Tipo de Dato | Nulo | Clave | Valor por Defecto | Descripción |
|---|---|---|---|---|---|
| `idPersona` | `INT` | NO | PK (Auto) | Auto-incremental | Identificador único del registro personal. |
| `nombre` | `VARCHAR(50)` | NO | Ninguno | Ninguno | Nombres de pila de la persona. |
| `apellidoPaterno`| `VARCHAR(50)` | NO | Ninguno | Ninguno | Primer apellido de la persona. |
| `apellidoMaterno`| `VARCHAR(50)` | NO | Ninguno | Ninguno | Segundo apellido de la persona. |
| `nroDocumento` | `VARCHAR(15)` | NO | UK | Ninguno | Número de documento de identidad (DNI, Pasaporte, CE). |
| `telefono` | `VARCHAR(15)` | SÍ | Ninguno | NULL | Número telefónico de contacto principal. |
| `email` | `VARCHAR(100)`| NO | UK | Ninguno | Correo electrónico personal único. |

---

## 3. Tabla: `Usuario`
Gestiona las credenciales de autenticación y el estado de la cuenta en la plataforma.

| Columna | Tipo de Dato | Nulo | Clave | Valor por Defecto | Descripción |
|---|---|---|---|---|---|
| `idUsuario` | `INT` | NO | PK (Auto) | Auto-incremental | Identificador único de la cuenta de usuario. |
| `idPersona` | `INT` | NO | FK | Ninguno | Referencia a `Persona(idPersona)` (ON DELETE RESTRICT). |
| `idRol` | `INT` | NO | FK | Ninguno | Referencia a `Rol(idRol)` (ON DELETE RESTRICT). |
| `nombreUsuario` | `VARCHAR(30)` | NO | UK | Ninguno | Nombre único de usuario para inicio de sesión. |
| `contrasena` | `VARCHAR(255)`| NO | Ninguno | Ninguno | Hash criptográfico de la contraseña (BCrypt). |
| `estado` | `VARCHAR(20)` | SÍ | Ninguno | `'ACTIVO'` | Estado operativo (`ACTIVO`, `INACTIVO`, `BLOQUEADO`). |
| `fechaRegistro` | `DATETIME` | SÍ | Ninguno | `CURRENT_TIMESTAMP` | Fecha y hora de creación de la cuenta. |

---

## 4. Tabla: `Agencia`
Perfil comercial, tributario y operacional de las agencias de viaje asociadas.

| Columna | Tipo de Dato | Nulo | Clave | Valor por Defecto | Descripción |
|---|---|---|---|---|---|
| `idAgencia` | `INT` | NO | PK (Auto) | Auto-incremental | Identificador único de la agencia turística. |
| `idUsuario` | `INT` | NO | FK, UK | Ninguno | Referencia a `Usuario(idUsuario)` (ON DELETE RESTRICT). |
| `razonSocial` | `VARCHAR(150)`| NO | Ninguno | Ninguno | Razón social formal inscrita en el registro fiscal. |
| `nombreComercial`| `VARCHAR(150)`| SÍ | Ninguno | NULL | Nombre de marca comercial de la agencia. |
| `ruc` | `VARCHAR(20)` | NO | UK | Ninguno | Registro Único de Contribuyente / Tax ID fiscal. |
| `telefono` | `VARCHAR(15)` | SÍ | Ninguno | NULL | Teléfono institucional de atención al cliente. |
| `email` | `VARCHAR(100)`| SÍ | Ninguno | NULL | Correo electrónico corporativo de contacto. |
| `direccion` | `VARCHAR(255)`| SÍ | Ninguno | NULL | Domicilio fiscal o dirección física de la agencia. |
| `descripcion` | `TEXT` | SÍ | Ninguno | NULL | Reseña o descripción corporativa de la agencia. |
| `logoUrl` | `VARCHAR(255)`| SÍ | Ninguno | NULL | Ruta o URL de la imagen del logotipo institucional. |
| `estado` | `VARCHAR(20)` | SÍ | Ninguno | `'PENDIENTE'` | Estado (`PENDIENTE`, `APROBADA`, `RECHAZADA`, `SUSPENDIDA`). |
| `porcentajeComision`| `DECIMAL(5,2)`| SÍ | Ninguno | `15.00` | Porcentaje de comisión pactado con la plataforma (15%). |
| `promedioCalificacion`| `DECIMAL(3,2)`| SÍ | Ninguno | `0.00` | Calificación promedio histórica (escala 0.00 a 5.00). |
| `fechaRegistro` | `DATETIME` | SÍ | Ninguno | `CURRENT_TIMESTAMP` | Fecha de creación del registro de la agencia. |
| `comision` | `DECIMAL(10,2)`| SÍ | Ninguno | `0.00` | Saldo acumulado o pendiente de comisiones. |

---

## 5. Tabla: `SolicitudAgencia`
Almacena las solicitudes de afiliación enviadas por empresas turísticas para revisión.

| Columna | Tipo de Dato | Nulo | Clave | Valor por Defecto | Descripción |
|---|---|---|---|---|---|
| `idSolicitud` | `INT` | NO | PK (Auto) | Auto-incremental | Identificador de la solicitud de afiliación. |
| `idUsuario` | `INT` | NO | FK | Ninguno | Referencia a `Usuario(idUsuario)` (ON DELETE CASCADE). |
| `razonSocial` | `VARCHAR(150)`| NO | Ninguno | Ninguno | Razón social postulante. |
| `ruc` | `VARCHAR(20)` | NO | Ninguno | Ninguno | RUC presentado para verificación. |
| `representanteLegal`| `VARCHAR(150)`| NO | Ninguno | Ninguno | Nombre del apoderado o representante legal. |
| `telefonoContacto`| `VARCHAR(15)`| SÍ | Ninguno | NULL | Teléfono del solicitante. |
| `correoContacto`| `VARCHAR(100)`| SÍ | Ninguno | NULL | Correo de notificaciones de la solicitud. |
| `documentoRuc` | `VARCHAR(255)`| SÍ | Ninguno | NULL | Ruta del archivo de constancia RUC o ficha tributaria. |
| `estado` | `VARCHAR(20)` | SÍ | Ninguno | `'Pendiente'` | Estado de la solicitud (`Pendiente`, `Aprobada`, `Rechazada`). |
| `fechaSolicitud`| `DATETIME` | SÍ | Ninguno | `CURRENT_TIMESTAMP` | Fecha de envío de la solicitud. |

---

## 6. Tabla: `HistorialAgencia`
Auditoría de las transiciones de estado de las agencias turísticas.

| Columna | Tipo de Dato | Nulo | Clave | Valor por Defecto | Descripción |
|---|---|---|---|---|---|
| `idHistorial` | `INT` | NO | PK (Auto) | Auto-incremental | Identificador del evento de auditoría. |
| `idAgencia` | `INT` | NO | FK | Ninguno | Referencia a `Agencia(idAgencia)` (ON DELETE CASCADE). |
| `idAdministrador`| `INT` | SÍ | Ninguno | NULL | ID del administrador que autorizó el cambio de estado. |
| `estadoAnterior`| `VARCHAR(20)` | SÍ | Ninguno | NULL | Estado antes de la modificación. |
| `estadoNuevo` | `VARCHAR(20)` | SÍ | Ninguno | NULL | Nuevo estado asignado. |
| `motivo` | `VARCHAR(255)`| SÍ | Ninguno | NULL | Justificación o motivo del cambio de estado. |
| `fecha` | `DATETIME` | SÍ | Ninguno | `CURRENT_TIMESTAMP` | Marca temporal del evento. |

---

## 7. Tabla: `Destino`
Catálogo de regiones y destinos turísticos geográficos.

| Columna | Tipo de Dato | Nulo | Clave | Valor por Defecto | Descripción |
|---|---|---|---|---|---|
| `idDestino` | `INT` | NO | PK (Auto) | Auto-incremental | Identificador único del destino turístico. |
| `nombre` | `VARCHAR(100)`| NO | UK | Ninguno | Nombre de la localidad o destino (ej. Cusco, Arequipa). |
| `descripcion` | `TEXT` | SÍ | Ninguno | NULL | Descripción turística del atractivo o región. |
| `estado` | `VARCHAR(20)` | SÍ | Ninguno | `'ACTIVO'` | Estado de publicación (`ACTIVO`, `INACTIVO`). |

---

## 8. Tabla: `Tour`
Catálogo central de paquetes turísticos ofrecidos por las agencias.

| Columna | Tipo de Dato | Nulo | Clave | Valor por Defecto | Descripción |
|---|---|---|---|---|---|
| `idTour` | `INT` | NO | PK (Auto) | Auto-incremental | Identificador único del paquete turístico. |
| `idAgencia` | `INT` | NO | FK | Ninguno | Referencia a `Agencia(idAgencia)` (ON DELETE RESTRICT). |
| `idDestino` | `INT` | NO | FK | Ninguno | Referencia a `Destino(idDestino)` (ON DELETE RESTRICT). |
| `slug` | `VARCHAR(100)`| NO | UK | Ninguno | Identificador legible único para rutas web amigables. |
| `nombre` | `VARCHAR(150)`| NO | Ninguno | Ninguno | Nombre comercial del tour. |
| `descripcion` | `TEXT` | SÍ | Ninguno | NULL | Descripción detallada de la experiencia. |
| `descripcionCorta`| `VARCHAR(255)`| SÍ | Ninguno | NULL | Resumen para tarjetas de catálogo y vistas previas. |
| `itinerario` | `TEXT` | SÍ | Ninguno | NULL | Cronograma detallado de actividades por día/hora. |
| `aceptaNinos` | `TINYINT(1)` | NO | Ninguno | `1` | Bandera booleana: admite reservas para niños. |
| `edadMaxNino` | `TINYINT` | NO | Ninguno | `11` | Límite superior de edad considerado para tarifa niño. |
| `edadMaxBebe` | `TINYINT` | NO | Ninguno | `2` | Límite superior de edad para infantes/bebés. |
| `descuento` | `DECIMAL(5,2)`| NO | Ninguno | `0.00` | Porcentaje promocional de descuento aplicable. |
| `condiciones` | `TEXT` | SÍ | Ninguno | NULL | Términos de cancelación y políticas de la agencia. |
| `minAsientosOferta`| `INT` | NO | Ninguno | `5` | Umbral mínimo de asientos para aplicar promociones. |
| `precioAdulto` | `DECIMAL(10,2)`| NO | Ninguno | Ninguno | Tarifa por pasajero adulto. |
| `precioNino` | `DECIMAL(10,2)`| NO | Ninguno | `0.00` | Tarifa diferenciada para niños. |
| `precioBebe` | `DECIMAL(10,2)`| NO | Ninguno | `0.00` | Tarifa para infantes (generalmente gratuita o mínima). |
| `duracion` | `VARCHAR(50)` | SÍ | Ninguno | NULL | Texto descriptivo de la duración (ej. "Full Day", "3D/2N"). |
| `ubicacion` | `VARCHAR(100)`| SÍ | Ninguno | NULL | Punto de encuentro o referencia geográfica. |
| `categoria` | `VARCHAR(50)` | SÍ | Ninguno | NULL | Categoría turística (Aventura, Cultural, Gastronómico). |
| `calificacionPromedio`| `DECIMAL(3,2)`| SÍ | Ninguno | `5.00` | Puntuación media histórica del tour. |
| `estado` | `VARCHAR(50)` | SÍ | Ninguno | `'ACTIVO'` | Estado del tour (`ACTIVO`, `INACTIVO`, `BORRADOR`). |
| `aceptaBebes` | `TINYINT(1)` | NO | Ninguno | `1` | Bandera booleana: admite infantes. |
| `queIncluye` | `TEXT` | SÍ | Ninguno | NULL | Servicios, traslados o comidas incluidos en el precio. |
| `queNoIncluye` | `TEXT` | SÍ | Ninguno | NULL | Gastos personales o propinas excluidas. |
| `dias` | `INT` | NO | Ninguno | `1` | Duración en días completos. |
| `horas` | `INT` | NO | Ninguno | `6` | Duración adicional en horas. |

---

## 9. Tabla: `TourFecha`
Programación de salidas en el calendario con control de aforo y cupos.

| Columna | Tipo de Dato | Nulo | Clave | Valor por Defecto | Descripción |
|---|---|---|---|---|---|
| `idTourFecha` | `INT` | NO | PK (Auto) | Auto-incremental | Identificador único de la salida programada. |
| `idTour` | `INT` | NO | FK | Ninguno | Referencia a `Tour(idTour)` (ON DELETE RESTRICT). |
| `fecha` | `DATE` | NO | UK (comp) | Ninguno | Fecha de ejecución de la excursión. |
| `horaInicio` | `TIME` | NO | UK (comp) | `'08:00:00'` | Hora de partida. |
| `horaFin` | `TIME` | SÍ | Ninguno | `'18:00:00'` | Hora estimada de culminación. |
| `cupoTotal` | `INT` | NO | Ninguno | `30` | Capacidad máxima de pasajeros por grupo. |
| `cupoDisponible`| `INT` | NO | Ninguno | `30` | Asientos disponibles remanentes para reserva. |
| `estado` | `VARCHAR(20)` | SÍ | Ninguno | `'DISPONIBLE'` | Estado de la salida (`DISPONIBLE`, `AGOTADO`, `CANCELADO`). |

---

## 10. Tabla: `TourImagen`
Galería multimedia de fotografías asociadas a cada paquete turístico.

| Columna | Tipo de Dato | Nulo | Clave | Valor por Defecto | Descripción |
|---|---|---|---|---|---|
| `idImagen` | `INT` | NO | PK (Auto) | Auto-incremental | Identificador único de la imagen. |
| `idTour` | `INT` | NO | FK | Ninguno | Referencia a `Tour(idTour)` (ON DELETE CASCADE). |
| `url` | `VARCHAR(255)`| NO | Ninguno | Ninguno | Ruta relativa o URL pública del archivo de imagen. |
| `esPrincipal` | `BOOLEAN` | SÍ | Ninguno | `FALSE` | Marca de foto de portada en catálogo. |
| `orden` | `INT` | SÍ | Ninguno | `0` | Secuencia de presentación en el carrusel de la ficha. |

---

## 11. Tabla: `Reserva`
Cabecera del contrato de reserva de viaje entre el turista y la plataforma.

| Columna | Tipo de Dato | Nulo | Clave | Valor por Defecto | Descripción |
|---|---|---|---|---|---|
| `idReserva` | `INT` | NO | PK (Auto) | Auto-incremental | Identificador único de la reserva. |
| `idUsuario` | `INT` | NO | FK | Ninguno | Turista titular (`Usuario.idUsuario`). |
| `idAgencia` | `INT` | NO | FK | `1` | Agencia proveedora (`Agencia.idAgencia`). |
| `idTour` | `INT` | SÍ | FK | NULL | Referencia opcional a `Tour(idTour)`. |
| `idTourFecha` | `INT` | SÍ | FK | NULL | Salida calendarizada (`TourFecha.idTourFecha`). |
| `nombreTour` | `VARCHAR(150)`| SÍ | Ninguno | NULL | Título histórico del tour al momento de la compra. |
| `codigoReserva`| `VARCHAR(20)` | NO | UK | Ninguno | Código alfanumérico único para comprobantes y vouchers. |
| `fechaRegistro`| `DATETIME` | SÍ | Ninguno | `CURRENT_TIMESTAMP` | Fecha de creación de la solicitud de reserva. |
| `fechaInicio` | `DATE` | NO | Ninguno | Ninguno | Fecha pactada de partida. |
| `fechaFin` | `DATE` | NO | Ninguno | Ninguno | Fecha estimada de retorno. |
| `estado` | `VARCHAR(20)` | NO | Ninguno | `'PENDIENTE'` | Estado (`PENDIENTE`, `CONFIRMADA`, `CANCELADA`, `COMPLETADA`). |
| `motivoCancelacion`| `VARCHAR(255)`| SÍ | Ninguno | NULL | Motivo documentado si la reserva fue anulada. |
| `total` | `DECIMAL(10,2)`| NO | Ninguno | Ninguno | Importe monetario total a pagar por la reserva. |

---

## 12. Tabla: `DetalleReserva`
Desglose cuantitativo y tarifario de los asientos solicitados por grupo etario.

| Columna | Tipo de Dato | Nulo | Clave | Valor por Defecto | Descripción |
|---|---|---|---|---|---|
| `idDetalle` | `INT` | NO | PK (Auto) | Auto-incremental | Identificador único del renglón de detalle. |
| `idReserva` | `INT` | NO | FK | Ninguno | Referencia a `Reserva(idReserva)` (ON DELETE CASCADE). |
| `idTourFecha` | `INT` | NO | FK | Ninguno | Referencia a `TourFecha(idTourFecha)`. |
| `cantAdultos` | `INT` | NO | Ninguno | `1` | Número de pasajeros adultos. |
| `cantNinos` | `INT` | NO | Ninguno | `0` | Número de pasajeros niños. |
| `cantBebes` | `INT` | NO | Ninguno | `0` | Número de pasajeros infantes. |
| `tipo` | `VARCHAR(20)` | SÍ | Ninguno | `'ADULTO'` | Discriminador de tipo de tarifa. |
| `cantidad` | `INT` | SÍ | Ninguno | `1` | Total de asientos agrupados en el ítem. |
| `precioUnitario`| `DECIMAL(10,2)`| SÍ | Ninguno | `0.00` | Precio unitario aplicado al momento de reservar. |
| `subtotal` | `DECIMAL(10,2)`| NO | Ninguno | Ninguno | Subtotal acumulado para este ítem. |
| `idPaquete` | `INT` | SÍ | Ninguno | NULL | Referencia histórica de paquete opcional. |

---

## 13. Tabla: `Pasajero`
Registro nominal y legal de cada uno de los integrantes del viaje.

| Columna | Tipo de Dato | Nulo | Clave | Valor por Defecto | Descripción |
|---|---|---|---|---|---|
| `idPasajero` | `INT` | NO | PK (Auto) | Auto-incremental | Identificador único del pasajero. |
| `idReserva` | `INT` | NO | FK | Ninguno | Referencia a `Reserva(idReserva)` (ON DELETE CASCADE). |
| `nroDocumento` | `VARCHAR(15)` | NO | Ninguno | Ninguno | Documento de identidad del pasajero para manifiesto. |
| `nombre` | `VARCHAR(50)` | NO | Ninguno | Ninguno | Nombres del viajero. |
| `apellidoPaterno`| `VARCHAR(50)`| SÍ | Ninguno | NULL | Primer apellido del viajero. |
| `apellidoMaterno`| `VARCHAR(50)`| SÍ | Ninguno | NULL | Segundo apellido del viajero. |
| `esTitular` | `BOOLEAN` | NO | Ninguno | `FALSE` | Indica si es el comprador responsable de la reserva. |
| `tipo` | `VARCHAR(20)` | NO | Ninguno | `'ADULTO'` | Segmento etario (`ADULTO`, `NINO`, `BEBE`). |

---

## 14. Tabla: `MetodoPago`
Registro de intentos de pago e información transaccional de pasarela.

| Columna | Tipo de Dato | Nulo | Clave | Valor por Defecto | Descripción |
|---|---|---|---|---|---|
| `idMetodoPago` | `INT` | NO | PK (Auto) | Auto-incremental | Identificador del registro transaccional. |
| `idReserva` | `INT` | NO | FK | Ninguno | Referencia a `Reserva(idReserva)` (ON DELETE CASCADE). |
| `tipoPago` | `VARCHAR(50)` | NO | Ninguno | `'Tarjeta'` | Modalidad de abono (`Tarjeta`, `Transferencia`, `Yape/Plin`). |
| `monto` | `DECIMAL(10,2)`| NO | Ninguno | Ninguno | Importe transaccionado. |
| `esAdelanto` | `BOOLEAN` | NO | Ninguno | `FALSE` | Indica si es un abono parcial o total. |
| `estadoPago` | `VARCHAR(20)` | NO | Ninguno | `'PENDIENTE'` | Estado de verificación del pago. |
| `numeroOperacion`| `VARCHAR(50)`| SÍ | Ninguno | NULL | Código de autorización bancaria / operación. |
| `marcaTarjeta` | `VARCHAR(50)` | SÍ | Ninguno | NULL | Marca procesadora (Visa, Mastercard, Amex). |
| `ultimosDigitos`| `VARCHAR(10)` | SÍ | Ninguno | NULL | Últimos 4 dígitos para comprobante impreso. |
| `urlComprobante`| `VARCHAR(255)`| SÍ | Ninguno | NULL | Enlace al comprobante digital. |
| `url` | `VARCHAR(255)`| SÍ | Ninguno | NULL | Enlace adicional de pasarela. |
| `fechaPago` | `DATETIME` | SÍ | Ninguno | `CURRENT_TIMESTAMP` | Marca temporal del abono. |

---

## 15. Tabla: `Pago`
Estructura persistente de confirmación de cobros conciliados.

| Columna | Tipo de Dato | Nulo | Clave | Valor por Defecto | Descripción |
|---|---|---|---|---|---|
| `idPago` | `INT` | NO | PK (Auto) | Auto-incremental | Identificador de cobro formal. |
| `idReserva` | `INT` | SÍ | FK | NULL | Referencia a `Reserva(idReserva)` (ON DELETE CASCADE). |
| `monto` | `DECIMAL(10,2)`| NO | Ninguno | Ninguno | Monto total liquidado. |
| `fechaPago` | `DATETIME` | SÍ | Ninguno | NULL | Fecha de confirmación financiera. |
| `metodoPago` | `VARCHAR(50)` | SÍ | Ninguno | NULL | Método utilizado. |
| `estado` | `VARCHAR(50)` | SÍ | Ninguno | `'COMPLETADO'` | Estado financiero (`COMPLETADO`, `ANULADO`). |
| `numeroOperacion`| `VARCHAR(100)`| SÍ | Ninguno | NULL | Referencia bancaria única de auditoría. |

---

## 16. Tabla: `Calificacion`
Sistema de reseñas y retroalimentación de calidad de los tours concluidos.

| Columna | Tipo de Dato | Nulo | Clave | Valor por Defecto | Descripción |
|---|---|---|---|---|---|
| `idCalificacion`| `INT` | NO | PK (Auto) | Auto-incremental | Identificador de la reseña. |
| `idUsuario` | `INT` | NO | FK | Ninguno | Turista autor (`Usuario.idUsuario`). |
| `idAgencia` | `INT` | NO | FK | Ninguno | Agencia evaluada (`Agencia.idAgencia`). |
| `idReserva` | `INT` | NO | FK | Ninguno | Reserva obligatoria de respaldo (`Reserva.idReserva`). |
| `estrellas` | `TINYINT` | NO | CHECK | Ninguno | Puntuación obligatoria entera de 1 a 5 estrellas. |
| `comentario` | `VARCHAR(500)`| SÍ | Ninguno | NULL | Opinión y comentarios cualitativos del turista. |
| `fechaCalificacion`| `DATETIME`| SÍ | Ninguno | `CURRENT_TIMESTAMP` | Fecha de publicación de la reseña. |
| `eliminadoPorAdmin`| `BOOLEAN` | SÍ | Ninguno | `FALSE` | Bandera de moderación administrativa. |
| `motivoEliminacion`| `VARCHAR(255)`| SÍ | Ninguno | NULL | Justificación de baja por moderación. |
| `flageada` | `BOOLEAN` | SÍ | Ninguno | `FALSE` | Marca de alerta de contenido sensible o inapropiado. |
| `motivoFlag` | `VARCHAR(255)`| SÍ | Ninguno | NULL | Razón del reporte automático o manual. |
| `fechaFlag` | `DATETIME` | SÍ | Ninguno | NULL | Fecha de reporte. |
| `estadoRevision`| `VARCHAR(20)` | SÍ | Ninguno | `'APROBADO'` | Estado de moderación (`APROBADO`, `REPORTADO`, `RECHAZADO`). |

---

## 17. Tabla: `Comision`
Cálculo y control de liquidación del porcentaje retenido por la plataforma a las agencias.

| Columna | Tipo de Dato | Nulo | Clave | Valor por Defecto | Descripción |
|---|---|---|---|---|---|
| `idComision` | `INT` | NO | PK (Auto) | Auto-incremental | Identificador de la liquidación de comisión. |
| `idAgencia` | `INT` | NO | FK | Ninguno | Agencia comisionable (`Agencia.idAgencia`). |
| `idReserva` | `INT` | SÍ | Ninguno | NULL | Reserva asociada que generó la comisión. |
| `periodo` | `VARCHAR(10)` | NO | Ninguno | Ninguno | Periodo contable de corte (ej. `'2026-11'`). |
| `subtotal` | `DECIMAL(10,2)`| SÍ | Ninguno | `0.00` | Subtotal base para cálculo. |
| `porcentaje` | `DECIMAL(5,2)`| SÍ | Ninguno | `15.00` | Porcentaje pactado de comisión (15.00%). |
| `porcentajeComision`| `DECIMAL(5,2)`| SÍ | Ninguno | `15.00` | Duplicado de seguridad del porcentaje pactado. |
| `montoComision`| `DECIMAL(10,2)`| NO | Ninguno | Ninguno | Monto recaudado retenido por la plataforma TRAVELINK. |
| `montoNeto` | `DECIMAL(10,2)`| NO | Ninguno | Ninguno | Monto neto residual a liquidar en favor de la agencia. |
| `montoAgencia` | `DECIMAL(10,2)`| SÍ | Ninguno | `0.00` | Liquidación efectiva a transferir a la agencia. |
| `montoTravelink`| `DECIMAL(10,2)`| SÍ | Ninguno | `0.00` | Utilidad retenida por el operador. |
| `ventasTotales`| `DECIMAL(10,2)`| SÍ | Ninguno | `0.00` | Volumen total bruto vendido en el periodo. |
| `estado` | `VARCHAR(20)` | SÍ | Ninguno | `'PENDIENTE'` | Estado operativo (`PENDIENTE`, `LIQUIDADA`, `CANCELADA`). |
| `estadoLiquidacion`| `VARCHAR(20)`| SÍ | Ninguno | `'PENDIENTE'` | Estado de transferencia bancaria de la liquidación. |
| `fechaCalculo` | `DATETIME` | SÍ | Ninguno | `CURRENT_TIMESTAMP` | Fecha de devengo del cobro. |
| `fechaLiquidacion`| `DATETIME` | SÍ | Ninguno | NULL | Fecha efectiva de abono a la agencia. |

---

## 18. Tabla: `Notificacion`
Bandeja de mensajes de notificación del sistema y alertas operativas para usuarios.

| Columna | Tipo de Dato | Nulo | Clave | Valor por Defecto | Descripción |
|---|---|---|---|---|---|
| `idNotificacion`| `INT` | NO | PK (Auto) | Auto-incremental | Identificador único de la notificación. |
| `idUsuario` | `INT` | SÍ | Ninguno | NULL | Destinatario individual (`Usuario.idUsuario`) si aplica. |
| `rolTarget` | `VARCHAR(20)` | NO | Ninguno | `'AGENCIA'` | Rol destinatario (`ADMINISTRADOR`, `AGENCIA`, `TURISTA`). |
| `titulo` | `VARCHAR(150)`| NO | Ninguno | Ninguno | Asunto o título breve de la notificación. |
| `mensaje` | `TEXT` | NO | Ninguno | Ninguno | Contenido descriptivo del aviso u orden operativa. |
| `leido` | `BOOLEAN` | SÍ | Ninguno | `FALSE` | Marca de lectura del mensaje por el usuario. |
| `fechaRegistro`| `DATETIME` | SÍ | Ninguno | `CURRENT_TIMESTAMP` | Marca temporal de despacho de la notificación. |
