-- 1. Agregar columna de comision a la agencia si no existe
ALTER TABLE Agencia ADD COLUMN IF NOT EXISTS comision DECIMAL(5,2) DEFAULT 15.00;

-- 2. Asegurarse de que la tabla de Servicios tenga soporte para imágenes
ALTER TABLE Servicio ADD COLUMN IF NOT EXISTS imagenPrincipal VARCHAR(255) DEFAULT '';
ALTER TABLE Servicio ADD COLUMN IF NOT EXISTS imagen2 VARCHAR(255) DEFAULT '';
ALTER TABLE Servicio ADD COLUMN IF NOT EXISTS imagen3 VARCHAR(255) DEFAULT '';

-- Nota: Como el volumen de datos es muy grande, aquí tienes la estructura base para los primeros registros de ANDES TOURS.
-- Debes ejecutar este tipo de inserciones para poblar tu base de datos.

-- AGENCIA 1: ANDES TOURS
INSERT INTO Usuario (idRol, nombreUsuario, contrasena, estado) VALUES (2, 'andestours', '123456', 'ACTIVO');
SET @idUsuarioAndes = LAST_INSERT_ID();

INSERT INTO Agencia (idUsuario, razonSocial, nombreComercial, ruc, telefono, email, direccion, descripcion, estado, comision) 
VALUES (@idUsuarioAndes, 'ANDES TOURS PERU S.A.C.', 'ANDES TOURS', '20601234567', '951234876', 'contacto@andestours.pe', 'Av. Arequipa 1450, Lima', 'Agencia con más de 10 años llevando viajeros por los Andes...', 'ACTIVO', 15.00);
SET @idAgenciaAndes = LAST_INSERT_ID();

-- DESTINO CUSCO
INSERT INTO Destino (nombre, estado) VALUES ('Cusco', 'ACTIVO') ON DUPLICATE KEY UPDATE idDestino=LAST_INSERT_ID(idDestino);
SET @idCusco = LAST_INSERT_ID();

-- SERVICIO 1: Machu Picchu Clásico
INSERT INTO Servicio (idAgencia, idDestino, nombre, descripcion, tipoServicio, precio, duracion, estado, imagenPrincipal)
VALUES (@idAgenciaAndes, @idCusco, 'Machu Picchu Clásico 2 días / 1 noche', 'El paquete más completo para conocer la ciudadela inca sin apuros.', 'Cultural', 780.00, '2 días', 'ACTIVO', 'img/andes-cusco-machu-picchu-1.jpg');
SET @idServicio1 = LAST_INSERT_ID();

-- DISPONIBILIDAD SERVICIO 1
INSERT INTO Disponibilidad (idServicio, fecha, cupoTotal, cupoDisponible) VALUES (@idServicio1, '2026-10-09', 20, 20);
INSERT INTO Disponibilidad (idServicio, fecha, cupoTotal, cupoDisponible) VALUES (@idServicio1, '2026-10-23', 20, 20);

-- (El resto de las inserciones siguen esta misma estructura)
