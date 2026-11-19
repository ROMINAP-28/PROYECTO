-- ==========================================================
-- TRAVELINK - SEED DE DATOS SINTÉTICOS PARA PRUEBAS DE SEGURIDAD
-- Entorno local exclusivamente. Datos 100% ficticios.
-- Estructura alineada exactamente con sql/travelink_schema.sql
-- ==========================================================

USE DBTravelink;

SET FOREIGN_KEY_CHECKS = 0;

-- 1. Personas sintéticas (IDs a partir de 100 para no chocar con datos_minimos)
INSERT INTO Persona (idPersona, nombre, apellidoPaterno, apellidoMaterno, nroDocumento, telefono, email) VALUES
(100, 'Turista', 'Alfa', 'Prueba', '10000001', '900000001', 'turista_a@test.local'),
(101, 'Turista', 'Beta', 'Prueba', '10000002', '900000002', 'turista_b@test.local'),
(102, 'Representante', 'Agencia', 'Demo', '10000003', '900000003', 'agencia_demo@test.local'),
(103, 'Admin', 'Seguro', 'Local', '10000004', '900000004', 'admin_seguro@test.local')
ON DUPLICATE KEY UPDATE nombre = VALUES(nombre), email = VALUES(email);

-- 2. Usuarios sintéticos con hashes BCrypt generados por Spring Security
-- Contraseñas reales:
-- turista_a@test.local   -> TestTurista#2026
-- turista_b@test.local   -> TestTurista#2027
-- agencia_demo@test.local -> TestAgencia#2026
-- admin_seguro@test.local -> TestAdmin#2026
INSERT INTO Usuario (idUsuario, idPersona, idRol, nombreUsuario, contrasena, estado) VALUES
(100, 100, 1, 'turista_a@test.local', '$2a$10$CrJmkehOIC2FYa/BKn9DHOAfa66sHxCTpp/BUCNS4BvD9bg7nMdGS', 'ACTIVO'),
(101, 101, 1, 'turista_b@test.local', '$2a$10$Dt8Z8dXSlhqTRG7l8sa/ZONMS1gVhuUII8bmuc9CUbaCNSmgtrTa.', 'ACTIVO'),
(102, 102, 2, 'agencia_demo@test.local', '$2a$10$mrKLcGFE3gRgcbfpxkLW5OKAnjtIgeUn1g0lz8nndDs0tudhC3HAy', 'ACTIVO'),
(103, 103, 3, 'admin_seguro@test.local', '$2a$10$XeHmUgMXoQseIKl0nXAX8etkY8zwe7oshdWgcrJEAeVhSdDPI9AZO', 'ACTIVO')
ON DUPLICATE KEY UPDATE contrasena = VALUES(contrasena), estado = 'ACTIVO';

-- 3. Agencia sintética vinculada al usuario 102
INSERT INTO Agencia (idAgencia, idUsuario, ruc, razonSocial, nombreComercial, direccion, telefono, email, estado, promedioCalificacion) VALUES
(10, 102, '20999999991', 'Agencia Demo S.A.C.', 'Agencia Demo Tours', 'Av. Las Pruebas 123, Lima', '900000003', 'agencia_demo@test.local', 'ACTIVO', 5.0)
ON DUPLICATE KEY UPDATE nombreComercial = VALUES(nombreComercial), estado = 'ACTIVO';

-- Solicitud de agencia aprobada (requerida por AuthApiController para permitir el login de agencia)
INSERT INTO SolicitudAgencia (idSolicitud, idUsuario, ruc, razonSocial, representanteLegal, telefonoContacto, correoContacto, estado, fechaSolicitud) VALUES
(10, 102, '20999999991', 'Agencia Demo S.A.C.', 'Representante Agencia Demo', '900000003', 'agencia_demo@test.local', 'Aceptado', NOW())
ON DUPLICATE KEY UPDATE estado = 'Aceptado';

-- 4. Tour y Fechas de prueba
INSERT INTO Tour (idTour, idAgencia, idDestino, slug, nombre, descripcion, itinerario, queIncluye, queNoIncluye, precioAdulto, precioNino, precioBebe, estado, aceptaBebes) VALUES
(100, 10, 8, 'tour-sintetico-machu-picchu', 'Tour Sintético Machu Picchu Mágico', 'Tour de prueba automatizada para entorno de laboratorio', 'Día 1: Recorrido de prueba', 'Transporte, guía', 'Almuerzos', 250.00, 150.00, 0.00, 'ACTIVO', 0),
(101, 10, 15, 'city-tour-lima-colonial', 'City Tour Lima Colonial Sintético', 'Tour por el centro histórico de Lima para laboratorio', 'Día 1: Centro histórico', 'Transporte', 'Propinas', 80.00, 50.00, 0.00, 'ACTIVO', 0)
ON DUPLICATE KEY UPDATE nombre = VALUES(nombre), estado = 'ACTIVO';

INSERT INTO TourFecha (idTourFecha, idTour, fecha, horaInicio, cupoTotal, cupoDisponible, estado) VALUES
(100, 100, DATE_ADD(CURDATE(), INTERVAL 15 DAY), '08:00:00', 30, 28, 'DISPONIBLE'),
(101, 101, DATE_ADD(CURDATE(), INTERVAL 20 DAY), '09:00:00', 25, 23, 'DISPONIBLE')
ON DUPLICATE KEY UPDATE estado = 'DISPONIBLE';

-- 5. Reservas distintas para Turista A y Turista B (Fundamental para la prueba de IDOR)
-- Reserva #500 -> Pertenece al Turista A (ID 100)
INSERT INTO Reserva (idReserva, idUsuario, idAgencia, idTour, idTourFecha, nombreTour, codigoReserva, fechaRegistro, fechaInicio, fechaFin, estado, total) VALUES
(500, 100, 10, 100, 100, 'Tour Sintético Machu Picchu Mágico', 'TRV-A100-TEST', NOW(), DATE_ADD(CURDATE(), INTERVAL 15 DAY), DATE_ADD(CURDATE(), INTERVAL 15 DAY), 'Confirmada', 500.00)
ON DUPLICATE KEY UPDATE codigoReserva = VALUES(codigoReserva);

INSERT INTO DetalleReserva (idDetalle, idReserva, idTourFecha, cantAdultos, cantNinos, cantBebes, subtotal) VALUES
(500, 500, 100, 2, 0, 0, 500.00)
ON DUPLICATE KEY UPDATE subtotal = VALUES(subtotal);

INSERT INTO Pasajero (idPasajero, idReserva, nroDocumento, nombre, apellidoPaterno, apellidoMaterno, esTitular, tipo) VALUES
(500, 500, '10000001', 'Turista', 'Alfa', 'Prueba', 1, 'ADULTO'),
(501, 500, '10000099', 'Acompañante', 'Alfa', 'Prueba', 0, 'ADULTO')
ON DUPLICATE KEY UPDATE nombre = VALUES(nombre);

INSERT INTO MetodoPago (idMetodoPago, idReserva, tipoPago, monto, esAdelanto, estadoPago, numeroOperacion, fechaPago) VALUES
(500, 500, 'Tarjeta', 500.00, 0, 'Completado', 'OP-SINT-A500', NOW())
ON DUPLICATE KEY UPDATE estadoPago = 'Completado';

INSERT INTO Pago (idPago, idReserva, monto, fechaPago, metodoPago, estado, numeroOperacion) VALUES
(500, 500, 500.00, NOW(), 'Tarjeta', 'COMPLETADO', 'OP-SINT-A500')
ON DUPLICATE KEY UPDATE estado = 'COMPLETADO';

-- Reserva #501 -> Pertenece al Turista B (ID 101)
INSERT INTO Reserva (idReserva, idUsuario, idAgencia, idTour, idTourFecha, nombreTour, codigoReserva, fechaRegistro, fechaInicio, fechaFin, estado, total) VALUES
(501, 101, 10, 101, 101, 'City Tour Lima Colonial Sintético', 'TRV-B101-TEST', NOW(), DATE_ADD(CURDATE(), INTERVAL 20 DAY), DATE_ADD(CURDATE(), INTERVAL 20 DAY), 'Confirmada', 160.00)
ON DUPLICATE KEY UPDATE codigoReserva = VALUES(codigoReserva);

INSERT INTO DetalleReserva (idDetalle, idReserva, idTourFecha, cantAdultos, cantNinos, cantBebes, subtotal) VALUES
(501, 501, 101, 2, 0, 0, 160.00)
ON DUPLICATE KEY UPDATE subtotal = VALUES(subtotal);

INSERT INTO Pasajero (idPasajero, idReserva, nroDocumento, nombre, apellidoPaterno, apellidoMaterno, esTitular, tipo) VALUES
(502, 501, '10000002', 'Turista', 'Beta', 'Prueba', 1, 'ADULTO'),
(503, 501, '10000098', 'Acompañante', 'Beta', 'Prueba', 0, 'ADULTO')
ON DUPLICATE KEY UPDATE nombre = VALUES(nombre);

INSERT INTO MetodoPago (idMetodoPago, idReserva, tipoPago, monto, esAdelanto, estadoPago, numeroOperacion, fechaPago) VALUES
(501, 501, 'Tarjeta', 160.00, 0, 'Completado', 'OP-SINT-B501', NOW())
ON DUPLICATE KEY UPDATE estadoPago = 'Completado';

INSERT INTO Pago (idPago, idReserva, monto, fechaPago, metodoPago, estado, numeroOperacion) VALUES
(501, 501, 160.00, NOW(), 'Tarjeta', 'COMPLETADO', 'OP-SINT-B501')
ON DUPLICATE KEY UPDATE estado = 'COMPLETADO';

SET FOREIGN_KEY_CHECKS = 1;
