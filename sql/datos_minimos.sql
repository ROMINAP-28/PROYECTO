-- ==========================================================
-- TRAVELINK - DATOS MÍNIMOS DEL SISTEMA
-- Roles del sistema, catálogo base de destinos y administrador inicial
-- ==========================================================

USE DBTravelink;

SET FOREIGN_KEY_CHECKS = 0;

-- 1. Roles del Sistema
INSERT INTO Rol (idRol, nombreRol) VALUES
(1, 'TURISTA'),
(2, 'AGENCIA'),
(3, 'ADMINISTRADOR')
ON DUPLICATE KEY UPDATE nombreRol = VALUES(nombreRol);

-- 2. Persona y Usuario Administrador Inicial
INSERT INTO Persona (idPersona, nombre, apellidoPaterno, apellidoMaterno, nroDocumento, telefono, email) VALUES
(1, 'Administrador', 'Travelink', 'Sistema', '00000001', '999888777', 'admin@travelink.pe')
ON DUPLICATE KEY UPDATE nombre = VALUES(nombre), email = VALUES(email);

INSERT INTO Usuario (idUsuario, idPersona, idRol, nombreUsuario, contrasena, estado) VALUES
(1, 1, 3, 'admin', '123456', 'ACTIVO')
ON DUPLICATE KEY UPDATE idRol = 3, contrasena = VALUES(contrasena), estado = 'ACTIVO';

-- 3. Catálogo de Destinos (24 Departamentos y Callao)
INSERT INTO Destino (idDestino, nombre, estado) VALUES
(1, 'Amazonas', 'ACTIVO'),
(2, 'Áncash', 'ACTIVO'),
(3, 'Apurímac', 'ACTIVO'),
(4, 'Arequipa', 'ACTIVO'),
(5, 'Ayacucho', 'ACTIVO'),
(6, 'Cajamarca', 'ACTIVO'),
(7, 'Callao', 'ACTIVO'),
(8, 'Cusco', 'ACTIVO'),
(9, 'Huancavelica', 'ACTIVO'),
(10, 'Huánuco', 'ACTIVO'),
(11, 'Ica', 'ACTIVO'),
(12, 'Junín', 'ACTIVO'),
(13, 'La Libertad', 'ACTIVO'),
(14, 'Lambayeque', 'ACTIVO'),
(15, 'Lima', 'ACTIVO'),
(16, 'Loreto', 'ACTIVO'),
(17, 'Madre de Dios', 'ACTIVO'),
(18, 'Moquegua', 'ACTIVO'),
(19, 'Pasco', 'ACTIVO'),
(20, 'Piura', 'ACTIVO'),
(21, 'Puno', 'ACTIVO'),
(22, 'San Martín', 'ACTIVO'),
(23, 'Tacna', 'ACTIVO'),
(24, 'Tumbes', 'ACTIVO'),
(25, 'Ucayali', 'ACTIVO')
ON DUPLICATE KEY UPDATE nombre = VALUES(nombre), estado = 'ACTIVO';

SET FOREIGN_KEY_CHECKS = 1;
