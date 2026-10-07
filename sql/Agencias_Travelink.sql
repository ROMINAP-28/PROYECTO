-- ==========================================================
-- BASE DE DATOS: DBTravelink
-- SCRIPT DE TABLAS Y DATOS COMPLETOS
-- Fecha de generación: Wed Sep 30 20:52:40 PET 2026
-- ==========================================================

USE DBTravelink;
SET FOREIGN_KEY_CHECKS = 0;

-- --------------------------------------------------------
-- Estructura y Datos de la tabla `Agencia`
-- --------------------------------------------------------

DROP TABLE IF EXISTS `Agencia`;
CREATE TABLE "Agencia" (
  "idAgencia" int NOT NULL AUTO_INCREMENT,
  "idUsuario" int NOT NULL,
  "razonSocial" varchar(150) NOT NULL,
  "nombreComercial" varchar(150) DEFAULT NULL,
  "ruc" varchar(20) NOT NULL,
  "telefono" varchar(15) DEFAULT NULL,
  "email" varchar(100) DEFAULT NULL,
  "direccion" varchar(255) DEFAULT NULL,
  "descripcion" text,
  "logoUrl" varchar(255) DEFAULT NULL,
  "estado" varchar(20) DEFAULT 'ACTIVO',
  "porcentajeComision" decimal(4,2) NOT NULL DEFAULT '10.00',
  "promedioCalificacion" decimal(3,2) DEFAULT '5.00',
  "fechaRegistro" datetime DEFAULT CURRENT_TIMESTAMP,
  "comision" decimal(5,2) DEFAULT '15.00',
  PRIMARY KEY ("idAgencia"),
  UNIQUE KEY "idUsuario" ("idUsuario"),
  UNIQUE KEY "ruc" ("ruc"),
  CONSTRAINT "fk_agencia_usuario" FOREIGN KEY ("idUsuario") REFERENCES "Usuario" ("idUsuario") ON DELETE RESTRICT ON UPDATE CASCADE
);

INSERT INTO `Agencia` VALUES
  (1, 2, 'ANDES TOURS PERU S.A.C.', 'ANDES TOURS', '20601234567', '951234876', 'contacto@andestours.pe', 'Av. El Sol 450, Cusco', 'Especialistas en turismo cultural y de aventura.', NULL, 'ACTIVO', 15.00, 5.00, '2026-09-27T02:47:02', 15.00),
  (2, 3, 'INKA TRAVEL EXPERIENCES S.A.C.', 'INKA TRAVEL', '20609876543', '963258741', 'reservas@inkatravel.pe', 'Calle Plateros 320, Cusco', 'Tours personalizados y experiencias únicas.', NULL, 'ACTIVO', 15.00, 4.00, '2026-09-27T02:47:02', 15.00),
  (3, 5, 'AGENCIA ALEGRIA S.A', 'AGENCIAS ALEGRIA S.A', '10721439114', '910413260', 'alegria@gmail.com', 'Av. Porongoche C315', 'Agencia de turismo extremo', NULL, 'ACTIVO', 15.00, 4.50, '2026-09-27T02:47:02', 15.00),
  (5, 8, 'AGENCIA SELVA S.A', 'AGENCIA SELVA S.A', '10721439116', '910413260', 'selva@gmail.com', 'Habitar', 'SIN COMENTARIOS', NULL, 'ACTIVO', 15.00, 5.00, '2026-09-27T03:13:32', 15.00),
  (6, 9, 'TOUR AREQUIPA S.A.S', 'TOUR AREQUIPA', '10721439118', '910413260', 'arequipa@gmail.com', 'Av. Santa Isabel 514', '-', NULL, 'ACTIVO', 15.00, 4.50, '2026-09-27T16:27:18', 15.00),
  (7, 10, 'TOUR LIMA S.A', 'TOUR LIMA', '10721439113', '910413260', 'Lima@gmail.com', 'Av. Porongoche C315', '-', NULL, 'ACTIVO', 15.00, 4.67, '2026-09-27T16:55:01', 15.00);

-- --------------------------------------------------------
-- Estructura y Datos de la tabla `Calificacion`
-- --------------------------------------------------------

DROP TABLE IF EXISTS `Calificacion`;
CREATE TABLE "Calificacion" (
  "idCalificacion" int NOT NULL AUTO_INCREMENT,
  "idUsuario" int NOT NULL,
  "idAgencia" int NOT NULL,
  "idReserva" int NOT NULL,
  "estrellas" tinyint NOT NULL,
  "comentario" varchar(500) DEFAULT NULL,
  "fechaCalificacion" datetime DEFAULT CURRENT_TIMESTAMP,
  "eliminadoPorAdmin" tinyint(1) NOT NULL DEFAULT '0',
  "motivoEliminacion" varchar(255) DEFAULT NULL,
  PRIMARY KEY ("idCalificacion"),
  KEY "fk_calificacion_usuario" ("idUsuario"),
  KEY "fk_calificacion_agencia" ("idAgencia"),
  KEY "fk_calificacion_reserva" ("idReserva"),
  CONSTRAINT "fk_calificacion_agencia" FOREIGN KEY ("idAgencia") REFERENCES "Agencia" ("idAgencia") ON DELETE RESTRICT ON UPDATE CASCADE,
  CONSTRAINT "fk_calificacion_reserva" FOREIGN KEY ("idReserva") REFERENCES "Reserva" ("idReserva") ON DELETE CASCADE ON UPDATE CASCADE,
  CONSTRAINT "fk_calificacion_usuario" FOREIGN KEY ("idUsuario") REFERENCES "Usuario" ("idUsuario") ON DELETE RESTRICT ON UPDATE CASCADE,
  CONSTRAINT "Calificacion_chk_1" CHECK (((`estrellas` >= 1) and (`estrellas` <= 5)))
);

INSERT INTO `Calificacion` VALUES
  (50, 23, 1, 60, 5, '¡Increíble servicio y atención de primera en Tacna y Moquegua!', '2026-10-01T01:52:03', false, NULL),
  (51, 23, 2, 61, 4, 'Muy buen recorrido guiado por el Valle Sagrado.', '2026-10-01T01:52:05', false, NULL),
  (52, 23, 7, 62, 5, 'Fascinante recorrido por las catacumbas de Lima.', '2026-10-01T01:52:06', false, NULL),
  (53, 24, 1, 63, 5, 'Inolvidable experiencia en la selva de Madre de Dios.', '2026-10-01T01:52:08', false, NULL),
  (54, 25, 3, 64, 4, 'El bosque de piedras fue espectacular, los niños lo disfrutaron mucho.', '2026-10-01T01:52:10', false, NULL),
  (55, 25, 5, 65, 5, 'Excelente avistamiento de lobos marinos y pingüinos en Ilo.', '2026-10-01T01:52:11', false, NULL),
  (56, 26, 6, 66, 5, 'Los vinos y el macerado de damasco riquísimos. Súper recomendado.', '2026-10-01T01:52:13', false, NULL),
  (57, 26, 7, 67, 4, 'La comida criolla deliciosa y el café de Barranco excelente.', '2026-10-01T01:52:15', false, NULL),
  (58, 27, 1, 68, 5, 'Hermosa combinación de playa y campiña.', '2026-10-01T01:52:16', false, NULL),
  (59, 28, 2, 69, 4, 'Tours puntuales y hospedajes de buena categoría.', '2026-10-01T01:52:18', false, NULL),
  (60, 28, 5, 70, 5, 'Muy buena atención del guía marino.', '2026-10-01T01:52:19', false, NULL),
  (61, 29, 3, 71, 5, 'Una caminata hermosa a más de 4000 msnm.', '2026-10-01T01:52:21', false, NULL),
  (62, 30, 6, 72, 4, 'Tradición y arquitectura única en Moquegua.', '2026-10-01T01:52:22', false, NULL),
  (63, 31, 7, 73, 5, 'Las playas de Ilo son limpias y apacibles.', '2026-10-01T01:52:24', false, NULL),
  (64, 31, 1, 74, 5, 'Todo genial con Andes Tours.', '2026-10-01T01:52:25', false, NULL);

-- --------------------------------------------------------
-- Estructura y Datos de la tabla `Comision`
-- --------------------------------------------------------

DROP TABLE IF EXISTS `Comision`;
CREATE TABLE "Comision" (
  "idComision" int NOT NULL AUTO_INCREMENT,
  "porcentaje" decimal(10,2) DEFAULT '15.00',
  "montoComision" decimal(10,2) NOT NULL,
  "montoNeto" decimal(10,2) DEFAULT '0.00',
  "fechaCalculo" datetime DEFAULT CURRENT_TIMESTAMP,
  "estado" varchar(20) DEFAULT 'Pendiente',
  "idReserva" int DEFAULT NULL,
  "idAgencia" int DEFAULT NULL,
  "subtotal" decimal(10,2) NOT NULL DEFAULT '0.00',
  "porcentajeComision" decimal(4,2) NOT NULL DEFAULT '15.00',
  "montoAgencia" decimal(10,2) NOT NULL DEFAULT '0.00',
  "estadoLiquidacion" varchar(20) NOT NULL DEFAULT 'Pendiente',
  "fechaLiquidacion" datetime DEFAULT NULL,
  PRIMARY KEY ("idComision"),
  KEY "idReserva" ("idReserva")
);

INSERT INTO `Comision` VALUES
  (47, 15.00, 133.50, 0.00, '2026-10-01T01:52:04', 'Pendiente', 60, 1, 890.00, 15.00, 756.50, 'Pendiente', NULL),
  (48, 15.00, 96.00, 0.00, '2026-10-01T01:52:05', 'Pendiente', 61, 2, 640.00, 15.00, 544.00, 'Pendiente', NULL),
  (49, 15.00, 36.75, 0.00, '2026-10-01T01:52:07', 'Pendiente', 62, 7, 245.00, 15.00, 208.25, 'Pendiente', NULL),
  (50, 15.00, 217.50, 0.00, '2026-10-01T01:52:08', 'Pendiente', 63, 1, 1450.00, 15.00, 1232.50, 'Pendiente', NULL),
  (51, 15.00, 108.00, 0.00, '2026-10-01T01:52:10', 'Pendiente', 64, 3, 720.00, 15.00, 612.00, 'Pendiente', NULL),
  (52, 15.00, 56.25, 0.00, '2026-10-01T01:52:11', 'Pendiente', 65, 5, 375.00, 15.00, 318.75, 'Pendiente', NULL),
  (53, 15.00, 39.00, 0.00, '2026-10-01T01:52:13', 'Pendiente', 66, 6, 260.00, 15.00, 221.00, 'Pendiente', NULL),
  (54, 15.00, 42.75, 0.00, '2026-10-01T01:52:15', 'Pendiente', 67, 7, 285.00, 15.00, 242.25, 'Pendiente', NULL),
  (55, 15.00, 102.00, 0.00, '2026-10-01T01:52:16', 'Pendiente', 68, 1, 680.00, 15.00, 578.00, 'Pendiente', NULL),
  (56, 15.00, 129.00, 0.00, '2026-10-01T01:52:18', 'Pendiente', 69, 2, 860.00, 15.00, 731.00, 'Pendiente', NULL),
  (57, 15.00, 43.50, 0.00, '2026-10-01T01:52:19', 'Pendiente', 70, 5, 290.00, 15.00, 246.50, 'Pendiente', NULL),
  (58, 15.00, 72.00, 0.00, '2026-10-01T01:52:21', 'Pendiente', 71, 3, 480.00, 15.00, 408.00, 'Pendiente', NULL),
  (59, 15.00, 46.50, 0.00, '2026-10-01T01:52:22', 'Pendiente', 72, 6, 310.00, 15.00, 263.50, 'Pendiente', NULL),
  (60, 15.00, 34.50, 0.00, '2026-10-01T01:52:24', 'Pendiente', 73, 7, 230.00, 15.00, 195.50, 'Pendiente', NULL),
  (61, 15.00, 108.00, 0.00, '2026-10-01T01:52:25', 'Pendiente', 74, 1, 720.00, 15.00, 612.00, 'Pendiente', NULL);

-- --------------------------------------------------------
-- Estructura y Datos de la tabla `Destino`
-- --------------------------------------------------------

DROP TABLE IF EXISTS `Destino`;
CREATE TABLE "Destino" (
  "idDestino" int NOT NULL AUTO_INCREMENT,
  "nombre" varchar(150) NOT NULL,
  "descripcion" text,
  "ubicacion" varchar(255) DEFAULT NULL,
  "estado" varchar(20) DEFAULT 'ACTIVO',
  PRIMARY KEY ("idDestino")
);

INSERT INTO `Destino` VALUES
  (1, 'Machu Picchu', 'Cusco, Perú', NULL, 'ACTIVO'),
  (2, 'Lima', NULL, NULL, 'ACTIVO'),
  (3, 'Pasco', NULL, NULL, 'ACTIVO'),
  (4, 'Moquegua', NULL, NULL, 'ACTIVO'),
  (5, 'Ilo', NULL, NULL, 'ACTIVO'),
  (6, 'Madre de Dios', NULL, NULL, 'ACTIVO'),
  (7, 'Tacna', 'Ciudad heroica con rica historia, gastronomía y viñedos', NULL, 'ACTIVO');

-- --------------------------------------------------------
-- Estructura y Datos de la tabla `DetallePaquete`
-- --------------------------------------------------------

DROP TABLE IF EXISTS `DetallePaquete`;
CREATE TABLE "DetallePaquete" (
  "idPaquete" int NOT NULL,
  "idServicio" int NOT NULL,
  "cantidad" int DEFAULT '1',
  PRIMARY KEY ("idPaquete","idServicio"),
  KEY "idServicio" ("idServicio"),
  CONSTRAINT "DetallePaquete_ibfk_1" FOREIGN KEY ("idPaquete") REFERENCES "PaqueteTuristico" ("idPaquete") ON DELETE CASCADE,
  CONSTRAINT "DetallePaquete_ibfk_2" FOREIGN KEY ("idServicio") REFERENCES "ServicioTuristico" ("idServicio") ON DELETE CASCADE
);

INSERT INTO `DetallePaquete` VALUES
  (8, 52, 1),
  (9, 52, 1),
  (10, 52, 1),
  (11, 52, 1),
  (12, 52, 1);

-- --------------------------------------------------------
-- Estructura y Datos de la tabla `DetalleReserva`
-- --------------------------------------------------------

DROP TABLE IF EXISTS `DetalleReserva`;
CREATE TABLE "DetalleReserva" (
  "idDetalle" int NOT NULL AUTO_INCREMENT,
  "idReserva" int NOT NULL,
  "idTourFecha" int NOT NULL,
  "cantAdultos" int NOT NULL DEFAULT '1',
  "cantNinos" int NOT NULL DEFAULT '0',
  "cantBebes" int NOT NULL DEFAULT '0',
  "subtotal" decimal(10,2) NOT NULL,
  PRIMARY KEY ("idDetalle"),
  KEY "fk_detalle_reserva" ("idReserva"),
  KEY "fk_detalle_tourfecha" ("idTourFecha"),
  CONSTRAINT "fk_detalle_reserva" FOREIGN KEY ("idReserva") REFERENCES "Reserva" ("idReserva") ON DELETE CASCADE ON UPDATE CASCADE,
  CONSTRAINT "fk_detalle_tourfecha" FOREIGN KEY ("idTourFecha") REFERENCES "TourFecha" ("idTourFecha") ON DELETE RESTRICT ON UPDATE CASCADE
);

-- Tabla `DetalleReserva` no contiene datos actualmente.

-- --------------------------------------------------------
-- Estructura y Datos de la tabla `Disponibilidad`
-- --------------------------------------------------------

DROP TABLE IF EXISTS `Disponibilidad`;
CREATE TABLE "Disponibilidad" (
  "idDisponibilidad" int NOT NULL AUTO_INCREMENT,
  "fecha" date NOT NULL,
  "horaInicio" time DEFAULT NULL,
  "horaFin" time DEFAULT NULL,
  "cupoTotal" int NOT NULL,
  "cupoDisponible" int NOT NULL,
  "estado" varchar(20) DEFAULT 'DISPONIBLE',
  "idServicio" int NOT NULL,
  PRIMARY KEY ("idDisponibilidad"),
  KEY "idServicio" ("idServicio"),
  CONSTRAINT "Disponibilidad_ibfk_1" FOREIGN KEY ("idServicio") REFERENCES "ServicioTuristico" ("idServicio")
);

INSERT INTO `Disponibilidad` VALUES
  (2, '2026-10-01', '09:00:00', '23:00:00', 15, 15, 'DISPONIBLE', 11),
  (3, '2026-09-29', '11:00:00', '12:00:00', 11, 11, 'DISPONIBLE', 14),
  (4, '2026-09-28', '10:00:00', '13:00:00', 10, 10, 'DISPONIBLE', 15),
  (5, '2026-09-29', '23:00:00', '06:00:00', 5, 5, 'DISPONIBLE', 18),
  (6, '2026-10-02', '23:00:00', '13:00:00', 20, 20, 'DISPONIBLE', 19),
  (7, '2026-10-03', '23:00:00', '18:00:00', 15, 15, 'DISPONIBLE', 20),
  (8, '2026-10-03', '23:00:00', '18:00:00', 15, 15, 'DISPONIBLE', 21),
  (9, '2026-10-03', '23:00:00', '18:00:00', 15, 15, 'DISPONIBLE', 22),
  (10, '2026-10-03', '23:00:00', '18:00:00', 15, 15, 'DISPONIBLE', 23),
  (11, '2026-10-03', '23:00:00', '18:00:00', 15, 15, 'DISPONIBLE', 24),
  (12, '2026-10-03', '23:00:00', '18:00:00', 15, 15, 'DISPONIBLE', 26),
  (13, '2026-10-03', '23:00:00', '18:00:00', 15, 15, 'DISPONIBLE', 28),
  (14, '2026-10-03', '23:00:00', '18:00:00', 15, 15, 'DISPONIBLE', 27),
  (15, '2026-10-03', '23:00:00', '18:00:00', 15, 15, 'DISPONIBLE', 25),
  (16, '2026-10-01', '07:00:00', '09:00:00', 30, 30, 'DISPONIBLE', 29),
  (17, '2026-10-15', '08:00:00', '13:00:00', 20, 16, 'DISPONIBLE', 10),
  (18, '2026-10-22', '09:00:00', '14:00:00', 20, 20, 'DISPONIBLE', 10),
  (19, '2026-10-16', '09:00:00', '15:00:00', 18, 14, 'DISPONIBLE', 12);

-- --------------------------------------------------------
-- Estructura y Datos de la tabla `DocumentoIncorporacion`
-- --------------------------------------------------------

DROP TABLE IF EXISTS `DocumentoIncorporacion`;
CREATE TABLE "DocumentoIncorporacion" (
  "idDocumento" int NOT NULL AUTO_INCREMENT,
  "nombre" varchar(150) NOT NULL,
  "tipoDocumento" varchar(50) DEFAULT NULL,
  "url" varchar(255) NOT NULL,
  "descripcion" text,
  "fechaRegistro" datetime DEFAULT CURRENT_TIMESTAMP,
  "estado" varchar(20) DEFAULT 'ACTIVO',
  "idSolicitud" int NOT NULL,
  PRIMARY KEY ("idDocumento"),
  KEY "idSolicitud" ("idSolicitud"),
  CONSTRAINT "DocumentoIncorporacion_ibfk_1" FOREIGN KEY ("idSolicitud") REFERENCES "SolicitudIncorporacion" ("idSolicitud")
);

-- Tabla `DocumentoIncorporacion` no contiene datos actualmente.

-- --------------------------------------------------------
-- Estructura y Datos de la tabla `HistorialAgencia`
-- --------------------------------------------------------

DROP TABLE IF EXISTS `HistorialAgencia`;
CREATE TABLE "HistorialAgencia" (
  "idHistorial" int NOT NULL AUTO_INCREMENT,
  "idAgencia" int NOT NULL,
  "idAdministrador" int NOT NULL,
  "estadoAnterior" varchar(20) NOT NULL,
  "estadoNuevo" varchar(20) NOT NULL,
  "motivo" varchar(255) DEFAULT NULL,
  "fecha" datetime DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY ("idHistorial"),
  KEY "fk_hist_agencia" ("idAgencia"),
  KEY "fk_hist_admin" ("idAdministrador"),
  CONSTRAINT "fk_hist_admin" FOREIGN KEY ("idAdministrador") REFERENCES "Usuario" ("idUsuario"),
  CONSTRAINT "fk_hist_agencia" FOREIGN KEY ("idAgencia") REFERENCES "Agencia" ("idAgencia")
);

INSERT INTO `HistorialAgencia` VALUES
  (1, 1, 6, 'Pendiente', 'ACTIVO', 'Aprobación inicial de incorporación con documentación validada', '2026-09-07T02:47:06'),
  (2, 2, 6, 'Pendiente', 'ACTIVO', 'Registro y validación de RUC Sunat conforme', '2026-09-12T02:47:06'),
  (3, 1, 6, 'Pendiente', 'ACTIVO', 'Solicitud de incorporación aprobada por Administrador', '2026-09-27T03:07:53'),
  (4, 1, 6, 'Pendiente', 'ACTIVO', 'Solicitud de incorporación aprobada por Administrador', '2026-09-27T03:08:08'),
  (5, 2, 6, 'Pendiente', 'ACTIVO', 'Solicitud de incorporación aprobada por Administrador', '2026-09-27T03:08:12'),
  (6, 2, 6, 'ACTIVO', 'INACTIVO', 'Falta de turismo', '2026-09-27T03:08:48');

-- --------------------------------------------------------
-- Estructura y Datos de la tabla `Liquidacion`
-- --------------------------------------------------------

DROP TABLE IF EXISTS `Liquidacion`;
CREATE TABLE "Liquidacion" (
  "idLiquidacion" int NOT NULL AUTO_INCREMENT,
  "montoBruto" decimal(10,2) NOT NULL,
  "montoComision" decimal(10,2) NOT NULL,
  "montoNeto" decimal(10,2) NOT NULL,
  "fechaGeneracion" datetime DEFAULT CURRENT_TIMESTAMP,
  "fechaLiquidacion" datetime DEFAULT NULL,
  "estado" varchar(30) DEFAULT 'PENDIENTE',
  "idAgencia" int NOT NULL,
  PRIMARY KEY ("idLiquidacion"),
  KEY "idAgencia" ("idAgencia"),
  CONSTRAINT "Liquidacion_ibfk_1" FOREIGN KEY ("idAgencia") REFERENCES "Agencia" ("idAgencia")
);

-- Tabla `Liquidacion` no contiene datos actualmente.

-- --------------------------------------------------------
-- Estructura y Datos de la tabla `MetodoPago`
-- --------------------------------------------------------

DROP TABLE IF EXISTS `MetodoPago`;
CREATE TABLE "MetodoPago" (
  "idMetodoPago" int NOT NULL AUTO_INCREMENT,
  "idReserva" int NOT NULL,
  "tipoPago" varchar(20) NOT NULL,
  "monto" decimal(10,2) NOT NULL,
  "esAdelanto" tinyint(1) NOT NULL DEFAULT '0',
  "estadoPago" varchar(20) NOT NULL DEFAULT 'Pendiente',
  "numeroOperacion" varchar(100) DEFAULT NULL,
  "urlComprobante" varchar(255) DEFAULT NULL,
  "url" varchar(255) DEFAULT NULL,
  "fechaPago" datetime DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY ("idMetodoPago"),
  KEY "fk_pago_reserva" ("idReserva"),
  CONSTRAINT "fk_pago_reserva" FOREIGN KEY ("idReserva") REFERENCES "Reserva" ("idReserva") ON DELETE CASCADE ON UPDATE CASCADE
);

INSERT INTO `MetodoPago` VALUES
  (53, 60, 'Yape', 890.00, false, 'Completado', 'OP-1001', NULL, NULL, '2026-10-01T01:52:03'),
  (54, 61, 'Tarjeta', 640.00, false, 'Completado', 'OP-1002', NULL, NULL, '2026-10-01T01:52:05'),
  (55, 62, 'Transferencia', 245.00, false, 'Completado', 'OP-1003', NULL, NULL, '2026-10-01T01:52:06'),
  (56, 63, 'Tarjeta', 1450.00, false, 'Completado', 'OP-1004', NULL, NULL, '2026-10-01T01:52:07'),
  (57, 64, 'Yape', 720.00, false, 'Completado', 'OP-1005', NULL, NULL, '2026-10-01T01:52:09'),
  (58, 65, 'Tarjeta', 375.00, false, 'Completado', 'OP-1006', NULL, NULL, '2026-10-01T01:52:11'),
  (59, 66, 'PagoEfectivo', 260.00, false, 'Completado', 'OP-1007', NULL, NULL, '2026-10-01T01:52:13'),
  (60, 67, 'Yape', 285.00, false, 'Completado', 'OP-1008', NULL, NULL, '2026-10-01T01:52:14'),
  (61, 68, 'Transferencia', 680.00, false, 'Completado', 'OP-1009', NULL, NULL, '2026-10-01T01:52:16'),
  (62, 69, 'Tarjeta', 860.00, false, 'Completado', 'OP-1010', NULL, NULL, '2026-10-01T01:52:17'),
  (63, 70, 'Yape', 290.00, false, 'Completado', 'OP-1011', NULL, NULL, '2026-10-01T01:52:19'),
  (64, 71, 'Tarjeta', 480.00, false, 'Completado', 'OP-1012', NULL, NULL, '2026-10-01T01:52:20'),
  (65, 72, 'Yape', 310.00, false, 'Completado', 'OP-1013', NULL, NULL, '2026-10-01T01:52:22'),
  (66, 73, 'Transferencia', 230.00, false, 'Completado', 'OP-1014', NULL, NULL, '2026-10-01T01:52:23'),
  (67, 74, 'Tarjeta', 720.00, false, 'Completado', 'OP-1015', NULL, NULL, '2026-10-01T01:52:25');

-- --------------------------------------------------------
-- Estructura y Datos de la tabla `Notificacion`
-- --------------------------------------------------------

DROP TABLE IF EXISTS `Notificacion`;
CREATE TABLE "Notificacion" (
  "idNotificacion" int NOT NULL AUTO_INCREMENT,
  "titulo" varchar(150) NOT NULL,
  "mensaje" text NOT NULL,
  "tipo" varchar(50) DEFAULT NULL,
  "fechaEnvio" datetime DEFAULT CURRENT_TIMESTAMP,
  "estado" varchar(20) DEFAULT 'ENVIADA',
  "leida" tinyint(1) DEFAULT '0',
  "idUsuario" int NOT NULL,
  "icono" varchar(50) DEFAULT 'ti ti-bell',
  "color" varchar(30) DEFAULT 'blue',
  "enlace" varchar(255) DEFAULT NULL,
  PRIMARY KEY ("idNotificacion"),
  KEY "idUsuario" ("idUsuario"),
  CONSTRAINT "Notificacion_ibfk_1" FOREIGN KEY ("idUsuario") REFERENCES "Usuario" ("idUsuario")
);

INSERT INTO `Notificacion` VALUES
  (14, 'Nueva Solicitud de Agencia', 'La agencia PERU PACIFIC TOURS E.I.R.L. ha enviado su expediente para evaluación.', 'AGENCIA', '2026-10-01T01:52:25', 'ENVIADA', false, 6, 'ti ti-building-store', 'blue', NULL),
  (15, 'Solicitud Rechazada con Motivo', 'Se registró el rechazo formal de CHACHAPOYAS ADVENTURES por SOAT vencido.', 'AGENCIA', '2026-10-01T01:52:25', 'ENVIADA', false, 6, 'ti ti-alert-triangle', 'red', NULL),
  (16, 'Nueva Reserva Recibida', 'Reserva TRK-2026-001 confirmada por S/ 890.00 para la Ruta Heroica.', 'RESERVA', '2026-10-01T01:52:25', 'ENVIADA', false, 2, 'ti ti-calendar-event', 'green', NULL),
  (17, 'Reserva Confirmada', 'Tu reserva TRK-2026-001 fue registrada exitosamente con pago completado.', 'RESERVA', '2026-10-01T01:52:25', 'ENVIADA', false, 1, 'ti ti-circle-check', 'green', NULL);

-- --------------------------------------------------------
-- Estructura y Datos de la tabla `Pago`
-- --------------------------------------------------------

DROP TABLE IF EXISTS `Pago`;
CREATE TABLE "Pago" (
  "idPago" int NOT NULL AUTO_INCREMENT,
  "monto" decimal(10,2) NOT NULL,
  "fechaPago" datetime DEFAULT CURRENT_TIMESTAMP,
  "metodoPago" varchar(50) NOT NULL,
  "estado" varchar(30) DEFAULT 'COMPLETADO',
  "numeroOperacion" varchar(100) DEFAULT NULL,
  "idReserva" int NOT NULL,
  PRIMARY KEY ("idPago"),
  KEY "idReserva" ("idReserva"),
  CONSTRAINT "Pago_ibfk_1" FOREIGN KEY ("idReserva") REFERENCES "Reserva" ("idReserva")
);

INSERT INTO `Pago` VALUES
  (54, 890.00, '2026-10-01T01:52:03', 'Yape', 'COMPLETADO', 'OP-1001', 60),
  (55, 640.00, '2026-10-01T01:52:04', 'Tarjeta', 'COMPLETADO', 'OP-1002', 61),
  (56, 245.00, '2026-10-01T01:52:06', 'Transferencia', 'COMPLETADO', 'OP-1003', 62),
  (57, 1450.00, '2026-10-01T01:52:07', 'Tarjeta', 'COMPLETADO', 'OP-1004', 63),
  (58, 720.00, '2026-10-01T01:52:09', 'Yape', 'COMPLETADO', 'OP-1005', 64),
  (59, 375.00, '2026-10-01T01:52:11', 'Tarjeta', 'COMPLETADO', 'OP-1006', 65),
  (60, 260.00, '2026-10-01T01:52:13', 'PagoEfectivo', 'COMPLETADO', 'OP-1007', 66),
  (61, 285.00, '2026-10-01T01:52:14', 'Yape', 'COMPLETADO', 'OP-1008', 67),
  (62, 680.00, '2026-10-01T01:52:16', 'Transferencia', 'COMPLETADO', 'OP-1009', 68),
  (63, 860.00, '2026-10-01T01:52:17', 'Tarjeta', 'COMPLETADO', 'OP-1010', 69),
  (64, 290.00, '2026-10-01T01:52:19', 'Yape', 'COMPLETADO', 'OP-1011', 70),
  (65, 480.00, '2026-10-01T01:52:20', 'Tarjeta', 'COMPLETADO', 'OP-1012', 71),
  (66, 310.00, '2026-10-01T01:52:22', 'Yape', 'COMPLETADO', 'OP-1013', 72),
  (67, 230.00, '2026-10-01T01:52:23', 'Transferencia', 'COMPLETADO', 'OP-1014', 73),
  (68, 720.00, '2026-10-01T01:52:24', 'Tarjeta', 'COMPLETADO', 'OP-1015', 74);

-- --------------------------------------------------------
-- Estructura y Datos de la tabla `PaqueteTuristico`
-- --------------------------------------------------------

DROP TABLE IF EXISTS `PaqueteTuristico`;
CREATE TABLE "PaqueteTuristico" (
  "idPaquete" int NOT NULL AUTO_INCREMENT,
  "nombre" varchar(150) NOT NULL,
  "descripcion" text,
  "precio" decimal(10,2) NOT NULL,
  "duracion" varchar(50) DEFAULT NULL,
  "condiciones" text,
  "estado" varchar(20) DEFAULT 'ACTIVO',
  "idAgencia" int NOT NULL,
  "descuento" int DEFAULT '0',
  "imagen" varchar(255) DEFAULT '',
  PRIMARY KEY ("idPaquete"),
  KEY "idAgencia" ("idAgencia"),
  CONSTRAINT "PaqueteTuristico_ibfk_1" FOREIGN KEY ("idAgencia") REFERENCES "Agencia" ("idAgencia")
);

INSERT INTO `PaqueteTuristico` VALUES
  (8, 'Ruta Heroica y Valles del Pisco (Tacna + Moquegua)', 'Un viaje fascinante por la historia, los viñedos coloniales y las aguas termales relajantes del sur.', 890.00, '3 días / 2 noches', 'Incluye traslados interprovinciales y hospedaje seleccionado', 'PUBLICADO', 1, 15, '/img/tours/tacna_termales.jpg'),
  (9, 'Mar y Valle del Sur (Ilo + Moquegua)', 'Navegación con lobos marinos en Punta de Coles y descanso placentero en la campiña vitivinícola.', 680.00, '3 días / 2 noches', 'Incluye paseo en lancha y degustaciones artesanales', 'PUBLICADO', 1, 20, '/img/tours/ilo_punta_coles.jpg'),
  (10, 'Amazonía Profunda Tambopata (Madre de Dios)', 'Inmersión completa en la selva virgen con observación de fauna silvestre, canopy y navegación fluvial.', 1450.00, '4 días / 3 noches', 'Incluye ecolodge con pensión completa y guías nativos', 'PUBLICADO', 1, 25, '/img/tours/madrededios_sandoval.jpg'),
  (11, 'Expedición Andina y Bosque de Piedras (Pasco)', 'Explora las figuras de piedra más altas del mundo y navega en las aguas cristalinas de la Laguna Punrun.', 720.00, '2 días / 1 noche', 'Incluye transporte 4x4 y mate de coca', 'PUBLICADO', 1, 10, '/img/tours/pasco_huayllay.jpg'),
  (12, 'Lima Colonial y Sabores Criollos Express', 'Lo mejor de la capital: centros virreinales, templos coloniales, arte bohemio y la mejor gastronomía marina.', 550.00, '2 días / 1 noche', 'Incluye entradas y degustaciones gastronómicas', 'BORRADOR', 1, 0, '/img/tours/lima_colonial.jpg');

-- --------------------------------------------------------
-- Estructura y Datos de la tabla `Pasajero`
-- --------------------------------------------------------

DROP TABLE IF EXISTS `Pasajero`;
CREATE TABLE "Pasajero" (
  "idPasajero" int NOT NULL AUTO_INCREMENT,
  "idReserva" int NOT NULL,
  "nroDocumento" varchar(15) NOT NULL,
  "telefono" varchar(20) DEFAULT NULL,
  "nombre" varchar(50) NOT NULL,
  "apellidoPaterno" varchar(60) DEFAULT NULL,
  "apellidoMaterno" varchar(60) DEFAULT NULL,
  "apellidos" varchar(100) NOT NULL,
  "esTitular" tinyint(1) NOT NULL DEFAULT '0',
  PRIMARY KEY ("idPasajero"),
  KEY "fk_pasajero_reserva" ("idReserva"),
  CONSTRAINT "fk_pasajero_reserva" FOREIGN KEY ("idReserva") REFERENCES "Reserva" ("idReserva") ON DELETE CASCADE ON UPDATE CASCADE
);

INSERT INTO `Pasajero` VALUES
  (132, 60, '72019283', '981234567', 'Carlos Manuel', 'Rojas', 'Vargas', 'Rojas Vargas', true),
  (133, 60, '72948102', '981234567', 'Laura Patricia', 'Rojas', 'Morales', 'Rojas Morales', false),
  (134, 61, '72019283', '981234567', 'Carlos Manuel', 'Rojas', 'Vargas', 'Rojas Vargas', true),
  (135, 61, '72948102', '981234567', 'Laura Patricia', 'Rojas', 'Morales', 'Rojas Morales', false),
  (136, 62, '72019283', '981234567', 'Carlos Manuel', 'Rojas', 'Vargas', 'Rojas Vargas', true),
  (137, 62, '72948102', '981234567', 'Laura Patricia', 'Rojas', 'Morales', 'Rojas Morales', false),
  (138, 62, '61928374', '981234567', 'Diego Mateo', 'Rojas', 'Morales', 'Rojas Morales', false),
  (139, 63, '73192834', '982345678', 'Maria Elena', 'Sánchez', 'Gómez', 'Sánchez Gómez', true),
  (140, 64, '74283940', '983456789', 'Jorge Luis', 'Fernández', 'Pérez', 'Fernández Pérez', true),
  (141, 64, '72948102', '983456789', 'Laura Patricia', 'Fernández', 'Morales', 'Fernández Morales', false),
  (142, 64, '61928374', '983456789', 'Diego Mateo', 'Fernández', 'Morales', 'Fernández Morales', false),
  (143, 64, '51920384', '983456789', 'Thiago Benjamín', 'Fernández', 'Morales', 'Fernández Morales', false),
  (144, 65, '74283940', '983456789', 'Jorge Luis', 'Fernández', 'Pérez', 'Fernández Pérez', true),
  (145, 65, '72948102', '983456789', 'Laura Patricia', 'Fernández', 'Morales', 'Fernández Morales', false),
  (146, 65, '61928374', '983456789', 'Diego Mateo', 'Fernández', 'Morales', 'Fernández Morales', false),
  (147, 66, '75394051', '984567890', 'Ana Lucía', 'Morales', 'Castro', 'Morales Castro', true),
  (148, 66, '72948102', '984567890', 'Laura Patricia', 'Morales', 'Morales', 'Morales Morales', false),
  (149, 66, '61928374', '984567890', 'Diego Mateo', 'Morales', 'Morales', 'Morales Morales', false),
  (150, 67, '75394051', '984567890', 'Ana Lucía', 'Morales', 'Castro', 'Morales Castro', true),
  (151, 67, '72948102', '984567890', 'Laura Patricia', 'Morales', 'Morales', 'Morales Morales', false),
  (152, 67, '51920384', '984567890', 'Thiago Benjamín', 'Morales', 'Morales', 'Morales Morales', false),
  (153, 68, '76405162', '985678901', 'Diego Armando', 'Quispe', 'Mamani', 'Quispe Mamani', true),
  (154, 69, '77516273', '986789012', 'Sofia Isabel', 'Ramírez', 'Díaz', 'Ramírez Díaz', true),
  (155, 69, '72948102', '986789012', 'Laura Patricia', 'Ramírez', 'Morales', 'Ramírez Morales', false),
  (156, 69, '61928374', '986789012', 'Diego Mateo', 'Ramírez', 'Morales', 'Ramírez Morales', false),
  (157, 69, '62019283', '986789012', 'Valentina Sofía', 'Ramírez', 'Morales', 'Ramírez Morales', false),
  (158, 70, '77516273', '986789012', 'Sofia Isabel', 'Ramírez', 'Díaz', 'Ramírez Díaz', true),
  (159, 70, '72948102', '986789012', 'Laura Patricia', 'Ramírez', 'Morales', 'Ramírez Morales', false),
  (160, 71, '78627384', '987890123', 'Mateo Gabriel', 'Chávez', 'Flores', 'Chávez Flores', true),
  (161, 71, '72948102', '987890123', 'Laura Patricia', 'Chávez', 'Morales', 'Chávez Morales', false),
  (162, 71, '73819204', '987890123', 'Eduardo José', 'Chávez', 'Alvarado', 'Chávez Alvarado', false),
  (163, 72, '79738495', '988901234', 'Camila Andrea', 'Vásquez', 'Espinoza', 'Vásquez Espinoza', true),
  (164, 72, '72948102', '988901234', 'Laura Patricia', 'Vásquez', 'Morales', 'Vásquez Morales', false),
  (165, 72, '61928374', '988901234', 'Diego Mateo', 'Vásquez', 'Morales', 'Vásquez Morales', false),
  (166, 73, '70849506', '989012345', 'Renzo Fernando', 'Torres', 'Guerrero', 'Torres Guerrero', true),
  (167, 73, '72948102', '989012345', 'Laura Patricia', 'Torres', 'Morales', 'Torres Morales', false),
  (168, 73, '61928374', '989012345', 'Diego Mateo', 'Torres', 'Morales', 'Torres Morales', false),
  (169, 74, '70849506', '989012345', 'Renzo Fernando', 'Torres', 'Guerrero', 'Torres Guerrero', true);

-- --------------------------------------------------------
-- Estructura y Datos de la tabla `Permiso`
-- --------------------------------------------------------

DROP TABLE IF EXISTS `Permiso`;
CREATE TABLE "Permiso" (
  "idPermiso" int NOT NULL AUTO_INCREMENT,
  "nombre" varchar(50) NOT NULL,
  "descripcion" varchar(255) DEFAULT NULL,
  PRIMARY KEY ("idPermiso")
);

-- Tabla `Permiso` no contiene datos actualmente.

-- --------------------------------------------------------
-- Estructura y Datos de la tabla `Persona`
-- --------------------------------------------------------

DROP TABLE IF EXISTS `Persona`;
CREATE TABLE "Persona" (
  "idPersona" int NOT NULL AUTO_INCREMENT,
  "nombre" varchar(50) NOT NULL,
  "apellidoPaterno" varchar(50) NOT NULL,
  "apellidoMaterno" varchar(50) NOT NULL,
  "nroDocumento" varchar(15) NOT NULL,
  "telefono" varchar(15) DEFAULT NULL,
  "email" varchar(100) NOT NULL,
  PRIMARY KEY ("idPersona"),
  UNIQUE KEY "nroDocumento" ("nroDocumento"),
  UNIQUE KEY "email" ("email")
);

INSERT INTO `Persona` VALUES
  (1, 'TestNombre', 'TestPaterno', 'TestMaterno', '79998888', '999888777', 'testuser99@gmail.com'),
  (2, 'Carlos', 'Mendoza', 'Paredes', '45892147', '951234876', 'cmendoza@andestours.pe'),
  (3, 'Valeria', 'Rios', 'Gomez', '71239845', '963258741', 'contacto@inkatravel.pe'),
  (4, 'Briane', 'goznales', 'Alvares', '32659878', '987456321', 'BrianeInfantes@gmail.com'),
  (5, 'Romina', 'Paredes', 'Anculle', '72143911', '910413260', 'alegria@gmail.com'),
  (6, 'Admin', 'Admin', 'Sistema', '00000000', '999999999', 'admin@travelink.pe'),
  (8, 'Jorge', 'Alma', 'Alameda', '12131615', '999999999', 'selva@gmail.com'),
  (9, 'Walter', 'Oroya', 'Landa', '12131619', '888999777', 'arequipa@gmail.com'),
  (10, 'Marcos', 'Soliz', 'Mante', '75154251', '777444111', 'Lima@gmail.com'),
  (11, 'Mashy', 'Mashy', 'Mashy', '', '901360025', 'mashy0127@gmail.com'),
  (12, 'Andes', 'Tours', 'Admin', '11111111', '951234876', 'contacto@andestours.pe'),
  (23, 'Carlos Manuel', 'Rojas', 'Vargas', '72019283', '981234567', 'carlos.rojas@travelink.test'),
  (24, 'Maria Elena', 'Sánchez', 'Gómez', '73192834', '982345678', 'maria.sanchez@travelink.test'),
  (25, 'Jorge Luis', 'Fernández', 'Pérez', '74283940', '983456789', 'jorge.fernandez@travelink.test'),
  (26, 'Ana Lucía', 'Morales', 'Castro', '75394051', '984567890', 'ana.morales@travelink.test'),
  (27, 'Diego Armando', 'Quispe', 'Mamani', '76405162', '985678901', 'diego.quispe@travelink.test'),
  (28, 'Sofia Isabel', 'Ramírez', 'Díaz', '77516273', '986789012', 'sofia.ramirez@travelink.test'),
  (29, 'Mateo Gabriel', 'Chávez', 'Flores', '78627384', '987890123', 'mateo.chavez@travelink.test'),
  (30, 'Camila Andrea', 'Vásquez', 'Espinoza', '79738495', '988901234', 'camila.vasquez@travelink.test'),
  (31, 'Renzo Fernando', 'Torres', 'Guerrero', '70849506', '989012345', 'renzo.torres@travelink.test'),
  (32, 'Luciana Beatriz', 'Mendoza', 'Romero', '71950617', '990123456', 'luciana.mendoza@travelink.test');

-- --------------------------------------------------------
-- Estructura y Datos de la tabla `Promocion`
-- --------------------------------------------------------

DROP TABLE IF EXISTS `Promocion`;
CREATE TABLE "Promocion" (
  "idPromocion" int NOT NULL AUTO_INCREMENT,
  "nombre" varchar(100) NOT NULL,
  "nombreUsuario" varchar(50) NOT NULL,
  "descripcion" text,
  "descuento" decimal(5,2) NOT NULL,
  "fechaInicio" datetime NOT NULL,
  "fechaFin" datetime NOT NULL,
  "estado" varchar(20) DEFAULT 'ACTIVO',
  "idServicio" int NOT NULL,
  PRIMARY KEY ("idPromocion"),
  UNIQUE KEY "nombreUsuario" ("nombreUsuario"),
  KEY "idServicio" ("idServicio"),
  CONSTRAINT "Promocion_ibfk_1" FOREIGN KEY ("idServicio") REFERENCES "ServicioTuristico" ("idServicio")
);

-- Tabla `Promocion` no contiene datos actualmente.

-- --------------------------------------------------------
-- Estructura y Datos de la tabla `RecursoDigital`
-- --------------------------------------------------------

DROP TABLE IF EXISTS `RecursoDigital`;
CREATE TABLE "RecursoDigital" (
  "idRecurso" int NOT NULL AUTO_INCREMENT,
  "nombre" varchar(150) NOT NULL,
  "tipo" varchar(50) DEFAULT NULL,
  "url" varchar(255) NOT NULL,
  "descripcion" text,
  "fechaRegistro" datetime DEFAULT CURRENT_TIMESTAMP,
  "estado" varchar(20) DEFAULT 'ACTIVO',
  "idServicio" int NOT NULL,
  PRIMARY KEY ("idRecurso"),
  KEY "idServicio" ("idServicio"),
  CONSTRAINT "RecursoDigital_ibfk_1" FOREIGN KEY ("idServicio") REFERENCES "ServicioTuristico" ("idServicio")
);

-- Tabla `RecursoDigital` no contiene datos actualmente.

-- --------------------------------------------------------
-- Estructura y Datos de la tabla `Reserva`
-- --------------------------------------------------------

DROP TABLE IF EXISTS `Reserva`;
CREATE TABLE "Reserva" (
  "idReserva" int NOT NULL AUTO_INCREMENT,
  "idUsuario" int NOT NULL,
  "idAgencia" int DEFAULT '1',
  "nombreTour" varchar(150) DEFAULT 'Machu Picchu Clásico',
  "codigoReserva" varchar(20) NOT NULL,
  "fechaRegistro" datetime DEFAULT CURRENT_TIMESTAMP,
  "fechaInicio" date NOT NULL,
  "fechaFin" date NOT NULL,
  "estado" varchar(20) NOT NULL DEFAULT 'Pendiente',
  "motivoCancelacion" varchar(255) DEFAULT NULL,
  "total" decimal(10,2) NOT NULL,
  PRIMARY KEY ("idReserva"),
  UNIQUE KEY "codigoReserva" ("codigoReserva"),
  KEY "fk_reserva_usuario" ("idUsuario"),
  CONSTRAINT "fk_reserva_usuario" FOREIGN KEY ("idUsuario") REFERENCES "Usuario" ("idUsuario") ON DELETE RESTRICT ON UPDATE CASCADE
);

INSERT INTO `Reserva` VALUES
  (60, 23, 1, 'Ruta Heroica y Valles del Pisco', 'TRK-2026-001', '2026-10-01T01:52:02', '2026-10-03', '2026-10-05', 'CONFIRMADA', NULL, 890.00),
  (61, 23, 2, 'Valle Sagrado de los Incas', 'TRK-2026-002', '2026-10-01T01:52:04', '2026-10-05', '2026-10-07', 'CONFIRMADA', NULL, 640.00),
  (62, 23, 7, 'City Tour Lima Colonial y Catacumbas', 'TRK-2026-003', '2026-10-01T01:52:05', '2026-10-07', '2026-10-09', 'CONFIRMADA', NULL, 245.00),
  (63, 24, 1, 'Amazonía Profunda Tambopata', 'TRK-2026-004', '2026-10-01T01:52:07', '2026-10-09', '2026-10-11', 'CONFIRMADA', NULL, 1450.00),
  (64, 25, 3, 'Expedición Bosque de Piedras de Huayllay', 'TRK-2026-005', '2026-10-01T01:52:08', '2026-10-11', '2026-10-13', 'CONFIRMADA', NULL, 720.00),
  (65, 25, 5, 'Aventura Marina en Reserva Punta de Coles Ilo', 'TRK-2026-006', '2026-10-01T01:52:10', '2026-10-13', '2026-10-15', 'CONFIRMADA', NULL, 375.00),
  (66, 26, 6, 'Circuito Valle Viejo y Viñedos de Pocollay', 'TRK-2026-007', '2026-10-01T01:52:12', '2026-10-15', '2026-10-17', 'CONFIRMADA', NULL, 260.00),
  (67, 26, 7, 'Ruta Gastronómica y Bohemia en Barranco', 'TRK-2026-008', '2026-10-01T01:52:13', '2026-10-17', '2026-10-19', 'CONFIRMADA', NULL, 285.00),
  (68, 27, 1, 'Mar y Valle del Sur (Ilo + Moquegua)', 'TRK-2026-009', '2026-10-01T01:52:15', '2026-10-19', '2026-10-21', 'CONFIRMADA', NULL, 680.00),
  (69, 28, 2, 'Valle Sagrado de los Incas', 'TRK-2026-010', '2026-10-01T01:52:16', '2026-10-21', '2026-10-23', 'CONFIRMADA', NULL, 860.00),
  (70, 28, 5, 'Aventura Marina en Reserva Punta de Coles Ilo', 'TRK-2026-011', '2026-10-01T01:52:18', '2026-10-23', '2026-10-25', 'CONFIRMADA', NULL, 290.00),
  (71, 29, 3, 'Expedición Bosque de Piedras de Huayllay', 'TRK-2026-012', '2026-10-01T01:52:19', '2026-10-25', '2026-10-27', 'CONFIRMADA', NULL, 480.00),
  (72, 30, 6, 'Ruta Colonial del Vino y Campiñas de Moquegua', 'TRK-2026-013', '2026-10-01T01:52:21', '2026-10-27', '2026-10-29', 'CONFIRMADA', NULL, 310.00),
  (73, 31, 7, 'Circuito Costero Playas de Ilo y Malecón', 'TRK-2026-014', '2026-10-01T01:52:22', '2026-10-29', '2026-10-31', 'CONFIRMADA', NULL, 230.00),
  (74, 31, 1, 'Expedición Andina y Bosque de Piedras', 'TRK-2026-015', '2026-10-01T01:52:24', '2026-10-31', '2026-11-02', 'CONFIRMADA', NULL, 720.00);

-- --------------------------------------------------------
-- Estructura y Datos de la tabla `Rol`
-- --------------------------------------------------------

DROP TABLE IF EXISTS `Rol`;
CREATE TABLE "Rol" (
  "idRol" int NOT NULL AUTO_INCREMENT,
  "nombreRol" varchar(20) NOT NULL,
  PRIMARY KEY ("idRol"),
  UNIQUE KEY "nombreRol" ("nombreRol")
);

INSERT INTO `Rol` VALUES
  (3, 'Administrador'),
  (2, 'Agencia'),
  (1, 'Turista');

-- --------------------------------------------------------
-- Estructura y Datos de la tabla `RolPermiso`
-- --------------------------------------------------------

DROP TABLE IF EXISTS `RolPermiso`;
CREATE TABLE "RolPermiso" (
  "idRol" int NOT NULL,
  "idPermiso" int NOT NULL,
  PRIMARY KEY ("idRol","idPermiso"),
  KEY "idPermiso" ("idPermiso"),
  CONSTRAINT "RolPermiso_ibfk_1" FOREIGN KEY ("idRol") REFERENCES "Rol" ("idRol") ON DELETE CASCADE,
  CONSTRAINT "RolPermiso_ibfk_2" FOREIGN KEY ("idPermiso") REFERENCES "Permiso" ("idPermiso") ON DELETE CASCADE
);

-- Tabla `RolPermiso` no contiene datos actualmente.

-- --------------------------------------------------------
-- Estructura y Datos de la tabla `ServicioTuristico`
-- --------------------------------------------------------

DROP TABLE IF EXISTS `ServicioTuristico`;
CREATE TABLE "ServicioTuristico" (
  "idServicio" int NOT NULL AUTO_INCREMENT,
  "nombre" varchar(150) NOT NULL,
  "descripcion" text,
  "tipoServicio" varchar(50) DEFAULT NULL,
  "precio" decimal(10,2) NOT NULL,
  "duracion" varchar(50) DEFAULT NULL,
  "condiciones" text,
  "estado" varchar(20) DEFAULT 'ACTIVO',
  "fechaRegistro" datetime DEFAULT CURRENT_TIMESTAMP,
  "idAgencia" int NOT NULL,
  "idDestino" int NOT NULL,
  PRIMARY KEY ("idServicio"),
  KEY "idAgencia" ("idAgencia"),
  KEY "idDestino" ("idDestino"),
  CONSTRAINT "ServicioTuristico_ibfk_1" FOREIGN KEY ("idAgencia") REFERENCES "Agencia" ("idAgencia"),
  CONSTRAINT "ServicioTuristico_ibfk_2" FOREIGN KEY ("idDestino") REFERENCES "Destino" ("idDestino")
);

INSERT INTO `ServicioTuristico` VALUES
  (3, 'Valle Sagrado de los Incas', 'Historia y paisajes.', NULL, 320.00, NULL, NULL, 'ACTIVO', '2026-09-30T03:28:48', 2, 1),
  (11, 'Expedición Bosque de Piedras de Huayllay', 'a', 'Tour', 160.00, '4', 'a', 'ACTIVO', '2026-09-27T05:19:35', 5, 3),
  (13, 'Aventura Marina en Punta de Coles Ilo', '-', 'Tour', 145.00, '5', '-', 'ACTIVO', '2026-09-27T05:27:43', 5, 5),
  (14, 'Inmersión Selva Tambopata Madre de Dios', '-', 'Tour', 280.00, '5', '-', 'ACTIVO', '2026-09-27T05:34:02', 5, 6),
  (15, 'Ruta Histórica y Fuentes Termales Tacna', '-', 'Experiencia', 120.00, '4', '-', 'ACTIVO', '2026-09-27T05:36', 5, 7),
  (16, 'Lima', '-', 'Experiencia', 15.00, '4', '-', 'ACTIVO', '2026-09-27T05:48:32', 5, 1),
  (17, 'Lima', '-', 'Tour', 15.00, '4', '-', 'ACTIVO', '2026-09-27T05:48:52', 5, 1),
  (18, 'Lima', '-', 'Tour', 15.00, '4', '-', 'ACTIVO', '2026-09-27T05:52:05', 5, 1),
  (19, 'Tarapoto', '-', 'Experiencia', 100.00, '10', '-', 'ACTIVO', '2026-09-27T05:52:58', 5, 1),
  (20, 'Tarapoto', '-', 'Experiencia', 100.00, '10', '-', 'ACTIVO', '2026-09-27T06:16:37', 5, 1),
  (21, 'Tarapoto', '-', 'Experiencia', 100.00, '10', '-', 'ACTIVO', '2026-09-27T06:16:39', 5, 1),
  (22, 'Tarapoto', '-', 'Experiencia', 100.00, '10', '-', 'ACTIVO', '2026-09-27T06:16:42', 5, 1),
  (23, 'Tarapoto', '-', 'Experiencia', 100.00, '10', '-', 'ACTIVO', '2026-09-27T06:16:43', 5, 1),
  (24, 'Tarapoto', '-', 'Experiencia', 100.00, '10', '-', 'ACTIVO', '2026-09-27T06:16:46', 5, 1),
  (25, 'Tarapoto', '-', 'Experiencia', 100.00, '10', '-', 'ACTIVO', '2026-09-27T06:16:48', 5, 1),
  (26, 'Tarapoto', '-', 'Experiencia', 100.00, '10', '-', 'ACTIVO', '2026-09-27T06:16:53', 5, 1),
  (27, 'Tarapoto', '-', 'Experiencia', 100.00, '10', '-', 'ACTIVO', '2026-09-27T06:16:57', 5, 1),
  (28, 'Tarapoto', '-', 'Experiencia', 100.00, '10', '-', 'ACTIVO', '2026-09-27T06:17', 5, 1),
  (29, 'Camana', '-', 'Experiencia', 10.00, '4', '-', 'ACTIVO', '2026-09-27T06:28:31', 5, 1),
  (52, 'City Tour Lima Colonial y Catacumbas', 'Recorrido por la Plaza Mayor, Palacio de Gobierno, Catedral y criptas subterráneas de San Francisco, terminando en el Malecón de Miraflores.', NULL, 95.00, NULL, NULL, 'ACTIVO', '2026-09-30T04:22:40', 1, 2),
  (53, 'Ruta Gastronómica y Bohemia en Barranco', 'Caminata por el barrio bohemio de Barranco, Puente de los Suspiros, Bajada de Baños y cata de café peruano con degustación de platos criollos.', NULL, 110.00, NULL, NULL, 'ACTIVO', '2026-09-30T04:22:40', 1, 2),
  (54, 'Expedición Bosque de Piedras de Huayllay', 'Caminata por una de las maravillas geológicas del Perú a más de 4000 msnm. Figuras rocosas de animales prehistóricos y baño en aguas termales de La Calera.', NULL, 160.00, NULL, NULL, 'ACTIVO', '2026-09-30T04:22:40', 1, 3),
  (55, 'Laguna Punrun y Rutas Andinas de Pasco', 'Navegación en bote por la inmensa Laguna Punrun, avistamiento de aves altoandinas (flamencos y parihuanas) y visita a comunidades tradicionales.', NULL, 140.00, NULL, NULL, 'ACTIVO', '2026-09-30T04:22:40', 1, 3),
  (56, 'Ruta Heroica y Aguas Termales de Calientes Tacna', 'Visita al histórico Campo del Alto de la Alianza, complejo arqueológico de Miculla con sus puentes colgantes y relajante circuito en pozas termales medicinales.', NULL, 130.00, NULL, NULL, 'ACTIVO', '2026-09-30T04:22:40', 1, 7),
  (57, 'Circuito Valle Viejo y Viñedos de Pocollay', 'Tour por la campiña tacneña, casonas coloniales de Calana y Pachía, degustación de pisco tacneño, macerados de damasco y almuerzo de pastel de choclo.', NULL, 100.00, NULL, NULL, 'ACTIVO', '2026-09-30T04:22:40', 1, 7),
  (58, 'Inmersión Reserva Tambopata y Lago Sandoval', 'Expedición en bote por el río Madre de Dios y caminata en selva virgen. Observación de lobos gigantes de río, monos aulladores, perezosos y caimanes.', NULL, 290.00, NULL, NULL, 'ACTIVO', '2026-09-30T04:22:40', 1, 6),
  (59, 'Aventura Río Madre de Dios y Collpa de Loros', 'Canopy walkway sobre las copas de los árboles amazónicos a 35 metros de altura, kayak por el río y espectáculo multicolor matutino de loros y guacamayos.', NULL, 230.00, NULL, NULL, 'ACTIVO', '2026-09-30T04:22:40', 1, 6),
  (60, 'Aventura Marina en Reserva Punta de Coles Ilo', 'Navegación costera en lancha rápida hacia la reserva marina. Avistamiento de cientos de lobos marinos chuscos y finos, pingüinos de Humboldt y aves guaneras.', NULL, 145.00, NULL, NULL, 'ACTIVO', '2026-09-30T04:22:40', 1, 5),
  (61, 'Circuito Costero Playas de Ilo y Malecón', 'Día de sol y brisa en las paradisíacas playas Puerto Inglés y Pozo de Lis. Vista fotográfica en la Glorieta José Gálvez y degustación de mariscos frescos.', NULL, 90.00, NULL, NULL, 'ACTIVO', '2026-09-30T04:22:40', 1, 5),
  (62, 'Ruta Colonial del Vino y Campiñas de Moquegua', 'Paseo por las tradicionales bodegas pisqueras coloniales (Biondi, Rayito de Sol), cata de vinos aromáticos y recorrido arquitectónico por casonas con techos de mojinete.', NULL, 120.00, NULL, NULL, 'ACTIVO', '2026-09-30T04:22:40', 1, 4),
  (63, 'Expedición Valle de Torata y Cataratas de Mollesaja', 'Descubre los molinos coloniales de Torata, la repostería artesanal moqueguana (roscas de manteca) y la hermosa caída de agua de Mollesaja en un valle fértil.', NULL, 135.00, NULL, NULL, 'ACTIVO', '2026-09-30T04:22:40', 1, 4);

-- --------------------------------------------------------
-- Estructura y Datos de la tabla `SolicitudAgencia`
-- --------------------------------------------------------

DROP TABLE IF EXISTS `SolicitudAgencia`;
CREATE TABLE "SolicitudAgencia" (
  "idSolicitud" int NOT NULL AUTO_INCREMENT,
  "idUsuario" int NOT NULL,
  "razonSocial" varchar(150) NOT NULL,
  "ruc" varchar(20) NOT NULL,
  "representanteLegal" varchar(100) NOT NULL,
  "telefonoContacto" varchar(15) DEFAULT NULL,
  "correoContacto" varchar(100) DEFAULT NULL,
  "documentoRuc" varchar(255) DEFAULT NULL,
  "fechaSolicitud" datetime DEFAULT CURRENT_TIMESTAMP,
  "estado" varchar(20) NOT NULL DEFAULT 'Pendiente',
  "idAdministrador" int DEFAULT NULL,
  "fechaRevision" datetime DEFAULT NULL,
  "motivoRechazo" varchar(255) DEFAULT NULL,
  PRIMARY KEY ("idSolicitud"),
  KEY "fk_solicitud_usuario" ("idUsuario"),
  KEY "fk_solicitud_admin" ("idAdministrador"),
  CONSTRAINT "fk_solicitud_admin" FOREIGN KEY ("idAdministrador") REFERENCES "Usuario" ("idUsuario"),
  CONSTRAINT "fk_solicitud_usuario" FOREIGN KEY ("idUsuario") REFERENCES "Usuario" ("idUsuario")
);

INSERT INTO `SolicitudAgencia` VALUES
  (1, 2, 'PERUVIAN ADVENTURES S.A.C.', '20556677889', 'Raul Espinoza Gomez', '944112233', 'contacto@peruvianadv.pe', 'ficha_ruc_20556677889.pdf', '2026-09-27T00:47:05', 'Aceptado', 6, '2026-09-27T03:08:08', NULL),
  (2, 3, 'KUNTUR EXPEDITIONS E.I.R.L.', '20448899112', 'Maria Fernandez Cardenas', '988776655', 'reservas@kunturexpeditions.com', 'ruc_kuntur_2026.pdf', '2026-09-26T02:47:05', 'Aceptado', 6, '2026-09-27T03:08:11', NULL),
  (3, 2, 'AMAZON JUNGLE TOURS S.A.', '20112233445', 'Jorge Ramos Silva', '977441188', 'info@amazonjungle.pe', 'ruc_amazon.pdf', '2026-09-24T02:47:05', 'Pendiente', NULL, NULL, NULL),
  (4, 10, 'TOUR LIMA S.A', '10721439113', 'Marcos Soliz Mante', '910413260', 'Lima@gmail.com', NULL, '2026-09-27T16:55:01', 'Aceptado', 6, '2026-09-27T19:33:14', NULL),
  (14, 23, 'PERU PACIFIC TOURS E.I.R.L.', '20998877661', 'Hernán Castillo Silva', '941238901', 'contacto@pacifictours.pe', 'ficha_ruc_pacific.pdf', '2026-10-01T01:52:02', 'Pendiente', NULL, NULL, NULL),
  (15, 24, 'CHACHAPOYAS ADVENTURES S.A.C.', '20887766552', 'Sonia Alarcón Vega', '952349012', 'informes@chachapoyasadv.pe', 'ruc_chachapoyas.pdf', '2026-10-01T01:52:02', 'Rechazado', NULL, '2026-10-01T01:52:02', 'Falta de certificado MINCETUR actualizado y seguro SOAT turístico vencido'),
  (16, 25, 'VALLE SAGRADO EXPEDITIONS', '20776655443', 'Gonzalo Beltrán Rios', '963450123', 'reservas@vallesagradoexp.com', 'ruc_valle.pdf', '2026-10-01T01:52:02', 'Rechazado', NULL, '2026-10-01T01:52:02', 'Dirección fiscal declarada no coincide con el domicilio registrado en SUNAT');

-- --------------------------------------------------------
-- Estructura y Datos de la tabla `SolicitudIncorporacion`
-- --------------------------------------------------------

DROP TABLE IF EXISTS `SolicitudIncorporacion`;
CREATE TABLE "SolicitudIncorporacion" (
  "idSolicitud" int NOT NULL AUTO_INCREMENT,
  "fechaSolicitud" datetime DEFAULT CURRENT_TIMESTAMP,
  "estado" varchar(30) DEFAULT 'PENDIENTE',
  "observaciones" text,
  "fechaRespuesta" datetime DEFAULT NULL,
  "idAgencia" int NOT NULL,
  PRIMARY KEY ("idSolicitud"),
  KEY "idAgencia" ("idAgencia"),
  CONSTRAINT "SolicitudIncorporacion_ibfk_1" FOREIGN KEY ("idAgencia") REFERENCES "Agencia" ("idAgencia")
);

INSERT INTO `SolicitudIncorporacion` VALUES
  (1, '2026-09-26T23:00:46', 'PENDIENTE', 'Solicitud de incorporación enviada por la agencia.', NULL, 2),
  (2, '2026-09-27T00:42:14', 'PENDIENTE', 'Solicitud de incorporación enviada por la agencia.', NULL, 3),
  (3, '2026-09-27T03:13:32', 'PENDIENTE', 'Solicitud de incorporación enviada por la agencia.', NULL, 5),
  (4, '2026-09-27T16:27:18', 'PENDIENTE', 'Solicitud de incorporación enviada por la agencia.', NULL, 6);

-- --------------------------------------------------------
-- Estructura y Datos de la tabla `TipoServicio`
-- --------------------------------------------------------

DROP TABLE IF EXISTS `TipoServicio`;
CREATE TABLE "TipoServicio" (
  "idTipo" int NOT NULL AUTO_INCREMENT,
  "nombre" varchar(50) NOT NULL,
  "descripcion" varchar(255) DEFAULT NULL,
  "estado" varchar(20) DEFAULT 'ACTIVO',
  PRIMARY KEY ("idTipo"),
  UNIQUE KEY "nombre" ("nombre")
);

INSERT INTO `TipoServicio` VALUES
  (1, 'Naturaleza', 'Actividades de ecoturismo y contacto con flora y fauna', 'ACTIVO'),
  (2, 'Cultural', 'Rutas históricas, sitios arqueológicos y tradiciones locales', 'ACTIVO'),
  (3, 'Aventura', 'Trekking, deportes extremos y expediciones al aire libre', 'ACTIVO'),
  (4, 'Descanso', 'Relajación, termas, spas y desconexión total', 'ACTIVO');

-- --------------------------------------------------------
-- Estructura y Datos de la tabla `Tour`
-- --------------------------------------------------------

DROP TABLE IF EXISTS `Tour`;
CREATE TABLE "Tour" (
  "idTour" int NOT NULL AUTO_INCREMENT,
  "idAgencia" int NOT NULL,
  "slug" varchar(100) NOT NULL,
  "nombre" varchar(150) NOT NULL,
  "descripcion" text,
  "precioAdulto" decimal(10,2) NOT NULL,
  "precioNino" decimal(10,2) NOT NULL DEFAULT '0.00',
  "precioBebe" decimal(10,2) NOT NULL DEFAULT '0.00',
  "duracion" varchar(50) DEFAULT NULL,
  "ubicacion" varchar(100) DEFAULT NULL,
  "categoria" varchar(50) DEFAULT NULL,
  "calificacionPromedio" decimal(3,2) DEFAULT '5.00',
  "estado" varchar(20) DEFAULT 'ACTIVO',
  "idDestino" int DEFAULT '1',
  "aceptaBebes" tinyint(1) DEFAULT '0',
  "queIncluye" text,
  "queNoIncluye" text,
  "dias" int DEFAULT '1',
  "horas" int DEFAULT '6',
  PRIMARY KEY ("idTour"),
  UNIQUE KEY "slug" ("slug"),
  KEY "fk_tour_agencia" ("idAgencia"),
  CONSTRAINT "fk_tour_agencia" FOREIGN KEY ("idAgencia") REFERENCES "Agencia" ("idAgencia") ON DELETE RESTRICT ON UPDATE CASCADE
);

INSERT INTO `Tour` VALUES
  (3, 2, 'valle-sagrado', 'Valle Sagrado de los Incas', 'Historia y paisajes.', 320.00, 220.00, 90.00, '1 día', 'Cusco', 'Cultura', 4.60, 'ACTIVO', 1, false, NULL, NULL, 1, 6),
  (52, 1, 'city-tour-lima-colonial-y-catacumbas-2', 'City Tour Lima Colonial y Catacumbas', 'Recorrido por la Plaza Mayor, Palacio de Gobierno, Catedral y criptas subterráneas de San Francisco, terminando en el Malecón de Miraflores.', 95.00, 55.00, 0.00, '5 horas', NULL, 'Cultural', 5.00, 'ACTIVO', 2, true, 'Transporte turístico climatizado, Guía oficial bilingüe, Entrada a Catacumbas, Seguro de viaje', 'Almuerzo, Propinas, Gastos personales', 1, 5),
  (53, 1, 'ruta-gastronomica-y-bohemia-en-barranco-2', 'Ruta Gastronómica y Bohemia en Barranco', 'Caminata por el barrio bohemio de Barranco, Puente de los Suspiros, Bajada de Baños y cata de café peruano con degustación de platos criollos.', 110.00, 65.00, 0.00, '4 horas', NULL, 'Descanso', 5.00, 'ACTIVO', 2, true, 'Degustación de 4 platos criollos, Tostaduría de café especial, Guía turístico gastronómico', 'Bebidas alcohólicas adicionales, Souvenirs', 1, 4),
  (54, 1, 'expedicion-bosque-de-piedras-de-huayllay-3', 'Expedición Bosque de Piedras de Huayllay', 'Caminata por una de las maravillas geológicas del Perú a más de 4000 msnm. Figuras rocosas de animales prehistóricos y baño en aguas termales de La Calera.', 160.00, 95.00, 0.00, '1 día', NULL, 'Aventura', 5.00, 'ACTIVO', 3, false, 'Movilidad privada 4x4, Guía de alta montaña, Ticket de ingreso a Huayllay y Baños Termales, Box lunch andino', 'Ropa de abrigo, Desayuno, Propinas', 1, 8),
  (55, 1, 'laguna-punrun-y-rutas-andinas-de-pasco-3', 'Laguna Punrun y Rutas Andinas de Pasco', 'Navegación en bote por la inmensa Laguna Punrun, avistamiento de aves altoandinas (flamencos y parihuanas) y visita a comunidades tradicionales.', 140.00, 80.00, 0.00, '7 horas', NULL, 'Naturaleza', 5.00, 'ACTIVO', 3, false, 'Transporte rural, Paseo en bote con chaleco salvavidas, Guía local comunitario, Mate de coca para altura', 'Almuerzo campestre, Gastos personales', 1, 7),
  (56, 1, 'ruta-heroica-y-aguas-termales-de-calientes-tacna-7', 'Ruta Heroica y Aguas Termales de Calientes Tacna', 'Visita al histórico Campo del Alto de la Alianza, complejo arqueológico de Miculla con sus puentes colgantes y relajante circuito en pozas termales medicinales.', 130.00, 75.00, 0.00, '6 horas', NULL, 'Descanso', 5.00, 'ACTIVO', 7, true, 'Movilidad turística, Entrada al Museo Alto de la Alianza y Baños Termales, Guía especializado, Seguro SOAT turístico', 'Toallas, Ropa de baño, Almuerzo típico', 1, 6),
  (57, 1, 'circuito-valle-viejo-y-vinedos-de-pocollay-7', 'Circuito Valle Viejo y Viñedos de Pocollay', 'Tour por la campiña tacneña, casonas coloniales de Calana y Pachía, degustación de pisco tacneño, macerados de damasco y almuerzo de pastel de choclo.', 100.00, 60.00, 0.00, '5 horas', NULL, 'Cultural', 5.00, 'ACTIVO', 7, true, 'Transporte privado, Cata en 2 bodegas pisqueras artesanales, Guía local, Bocadillos regionales', 'Almuerzo a la carta, Compras de botellas de pisco', 1, 5),
  (58, 1, 'inmersion-reserva-tambopata-y-lago-sandoval-6', 'Inmersión Reserva Tambopata y Lago Sandoval', 'Expedición en bote por el río Madre de Dios y caminata en selva virgen. Observación de lobos gigantes de río, monos aulladores, perezosos y caimanes.', 290.00, 180.00, 0.00, '2 días / 1 noche', NULL, 'Naturaleza', 5.00, 'ACTIVO', 6, false, 'Traslado fluvial en canoa motorizada, Boleto SERNANP Tambopata, Alojamiento ecológico 1 noche, Alimentación completa amazónica, Botas de jebe', 'Bebidas enlatadas, Pasajes aéreos a Puerto Maldonado', 2, 24),
  (59, 1, 'aventura-rio-madre-de-dios-y-collpa-de-loros-6', 'Aventura Río Madre de Dios y Collpa de Loros', 'Canopy walkway sobre las copas de los árboles amazónicos a 35 metros de altura, kayak por el río y espectáculo multicolor matutino de loros y guacamayos.', 230.00, 140.00, 0.00, '8 horas', NULL, 'Aventura', 5.00, 'ACTIVO', 6, false, 'Equipos certificados de canopy y tirolina, Embarcación con motor fuera de borda, Desayuno a bordo, Guía naturalista con telescopio', 'Propinas para boteros, Bebidas gaseosas', 1, 8),
  (60, 1, 'aventura-marina-en-reserva-punta-de-coles-ilo-5', 'Aventura Marina en Reserva Punta de Coles Ilo', 'Navegación costera en lancha rápida hacia la reserva marina. Avistamiento de cientos de lobos marinos chuscos y finos, pingüinos de Humboldt y aves guaneras.', 145.00, 85.00, 0.00, '5 horas', NULL, 'Aventura', 5.00, 'ACTIVO', 5, true, 'Paseo marítimo guiado, Chalecos salvavidas certificados, Tasa portuaria, Guía turístico marino', 'Pastillas para el mareo, Almuerzo marino', 1, 5),
  (61, 1, 'circuito-costero-playas-de-ilo-y-malecon-5', 'Circuito Costero Playas de Ilo y Malecón', 'Día de sol y brisa en las paradisíacas playas Puerto Inglés y Pozo de Lis. Vista fotográfica en la Glorieta José Gálvez y degustación de mariscos frescos.', 90.00, 50.00, 0.00, '6 horas', NULL, 'Descanso', 5.00, 'ACTIVO', 5, true, 'Movilidad ejecutiva ida y vuelta a playas, Sombrilla playera instalada, Guía acompañante', 'Consumos en restaurantes playeros, Deportes acuáticos extras', 1, 6),
  (62, 1, 'ruta-colonial-del-vino-y-campinas-de-moquegua-4', 'Ruta Colonial del Vino y Campiñas de Moquegua', 'Paseo por las tradicionales bodegas pisqueras coloniales (Biondi, Rayito de Sol), cata de vinos aromáticos y recorrido arquitectónico por casonas con techos de mojinete.', 120.00, 70.00, 0.00, '5 horas', NULL, 'Cultural', 5.00, 'ACTIVO', 4, true, 'Transporte turístico, Entrada a 3 bodegas tradicionales, Degustación guiada de pisco y macerados, Guía oficial', 'Almuerzo campestre, Compras personales', 1, 5),
  (63, 1, 'expedicion-valle-de-torata-y-cataratas-de-mollesaja-4', 'Expedición Valle de Torata y Cataratas de Mollesaja', 'Descubre los molinos coloniales de Torata, la repostería artesanal moqueguana (roscas de manteca) y la hermosa caída de agua de Mollesaja en un valle fértil.', 135.00, 80.00, 0.00, '7 horas', NULL, 'Naturaleza', 5.00, 'ACTIVO', 4, true, 'Movilidad turística, Visita a molinos de piedra, Degustación de panes torateños, Caminata guiada a cataratas', 'Almuerzo, Ropa de recambio', 1, 7);

-- --------------------------------------------------------
-- Estructura y Datos de la tabla `TourFecha`
-- --------------------------------------------------------

DROP TABLE IF EXISTS `TourFecha`;
CREATE TABLE "TourFecha" (
  "idTourFecha" int NOT NULL AUTO_INCREMENT,
  "idTour" int NOT NULL,
  "fecha" date NOT NULL,
  "horaInicio" time NOT NULL,
  "cupoTotal" int NOT NULL,
  "cupoDisponible" int NOT NULL,
  "estado" varchar(20) DEFAULT 'DISPONIBLE',
  PRIMARY KEY ("idTourFecha"),
  KEY "fk_tourfecha_tour" ("idTour"),
  CONSTRAINT "fk_tourfecha_tour" FOREIGN KEY ("idTour") REFERENCES "Tour" ("idTour") ON DELETE RESTRICT ON UPDATE CASCADE
);

INSERT INTO `TourFecha` VALUES
  (3, 3, '2026-05-18', '07:00:00', 30, 30, 'DISPONIBLE');

-- --------------------------------------------------------
-- Estructura y Datos de la tabla `TourImagen`
-- --------------------------------------------------------

DROP TABLE IF EXISTS `TourImagen`;
CREATE TABLE "TourImagen" (
  "idImagen" int NOT NULL AUTO_INCREMENT,
  "idTour" int NOT NULL,
  "url" varchar(255) NOT NULL,
  "esPrincipal" tinyint(1) DEFAULT '0',
  "orden" int DEFAULT '0',
  PRIMARY KEY ("idImagen"),
  KEY "fk_tourimagen_tour" ("idTour"),
  CONSTRAINT "fk_tourimagen_tour" FOREIGN KEY ("idTour") REFERENCES "Tour" ("idTour") ON DELETE CASCADE ON UPDATE CASCADE
);

INSERT INTO `TourImagen` VALUES
  (48, 52, '/img/tours/lima_colonial.jpg', true, 1),
  (49, 53, '/img/tours/lima_barranco.jpg', true, 1),
  (50, 54, '/img/tours/pasco_huayllay.jpg', true, 1),
  (51, 55, '/img/tours/pasco_punrun.jpg', true, 1),
  (52, 56, '/img/tours/tacna_termales.jpg', true, 1),
  (53, 57, '/img/tours/tacna_valle_viejo.jpg', true, 1),
  (54, 58, '/img/tours/madrededios_sandoval.jpg', true, 1),
  (55, 59, '/img/tours/madrededios_collpa.jpg', true, 1),
  (56, 60, '/img/tours/ilo_punta_coles.jpg', true, 1),
  (57, 61, '/img/tours/ilo_playas.jpg', true, 1),
  (58, 62, '/img/tours/moquegua_ruta_vino.jpg', true, 1),
  (59, 63, '/img/tours/moquegua_torata.jpg', true, 1);

-- --------------------------------------------------------
-- Estructura y Datos de la tabla `Usuario`
-- --------------------------------------------------------

DROP TABLE IF EXISTS `Usuario`;
CREATE TABLE "Usuario" (
  "idUsuario" int NOT NULL AUTO_INCREMENT,
  "idPersona" int NOT NULL,
  "idRol" int NOT NULL,
  "nombreUsuario" varchar(30) NOT NULL,
  "contrasena" varchar(255) NOT NULL,
  "estado" varchar(20) DEFAULT 'ACTIVO',
  "fechaRegistro" datetime DEFAULT CURRENT_TIMESTAMP,
  "ultimoLogin" datetime DEFAULT NULL,
  PRIMARY KEY ("idUsuario"),
  UNIQUE KEY "nombreUsuario" ("nombreUsuario"),
  KEY "fk_usuario_persona" ("idPersona"),
  KEY "fk_usuario_rol" ("idRol"),
  CONSTRAINT "fk_usuario_persona" FOREIGN KEY ("idPersona") REFERENCES "Persona" ("idPersona") ON DELETE RESTRICT ON UPDATE CASCADE,
  CONSTRAINT "fk_usuario_rol" FOREIGN KEY ("idRol") REFERENCES "Rol" ("idRol") ON DELETE RESTRICT ON UPDATE CASCADE
);

INSERT INTO `Usuario` VALUES
  (1, 1, 1, 'testuser99', 'password123', 'ACTIVO', '2026-09-26T23:35:55', NULL),
  (2, 2, 2, 'Andestours', '123456', 'ACTIVO', '2026-09-26T23:39:13', NULL),
  (3, 3, 2, 'Inkatravel', '123456', 'ACTIVO', '2026-09-26T23:39:13', NULL),
  (4, 4, 1, 'briane', '123456', 'ACTIVO', '2026-09-26T23:56:15', NULL),
  (5, 5, 2, 'Roberto', '123456', 'ACTIVO', '2026-09-27T00:42:13', NULL),
  (6, 6, 3, 'admin', '123456', 'ACTIVO', '2026-09-27T02:01:44', NULL),
  (8, 8, 2, 'Agencia Selva', '123456', 'ACTIVO', '2026-09-27T03:13:32', NULL),
  (9, 9, 2, 'Tour Arequipa', '123456', 'ACTIVO', '2026-09-27T16:27:18', NULL),
  (10, 10, 2, 'Tour Lima', '123456', 'ACTIVO', '2026-09-27T16:55:01', NULL),
  (11, 11, 1, 'mashyel', 'Mashyeljavi0127!', 'ACTIVO', '2026-09-28T02:50:02', NULL),
  (23, 23, 1, 'turista01', 'pass123', 'ACTIVO', '2026-10-01T01:38:36', NULL),
  (24, 24, 1, 'turista02', 'pass123', 'ACTIVO', '2026-10-01T01:38:36', NULL),
  (25, 25, 1, 'turista03', 'pass123', 'ACTIVO', '2026-10-01T01:38:36', NULL),
  (26, 26, 1, 'turista04', 'pass123', 'ACTIVO', '2026-10-01T01:38:37', NULL),
  (27, 27, 1, 'turista05', 'pass123', 'ACTIVO', '2026-10-01T01:38:37', NULL),
  (28, 28, 1, 'turista06', 'pass123', 'ACTIVO', '2026-10-01T01:38:37', NULL),
  (29, 29, 1, 'turista07', 'pass123', 'ACTIVO', '2026-10-01T01:38:38', NULL),
  (30, 30, 1, 'turista08', 'pass123', 'ACTIVO', '2026-10-01T01:38:38', NULL),
  (31, 31, 1, 'turista09', 'pass123', 'ACTIVO', '2026-10-01T01:38:39', NULL),
  (32, 32, 1, 'turista10', 'pass123', 'ACTIVO', '2026-10-01T01:38:39', NULL);

SET FOREIGN_KEY_CHECKS = 1;
