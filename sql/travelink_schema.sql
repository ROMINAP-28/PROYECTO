-- ==========================================================
-- Base de Datos: TRAVELINK
-- Sistema de Reservas, Agencias y Experiencias Turísticas
-- ==========================================================

CREATE DATABASE IF NOT EXISTS travelink CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
USE travelink;

-- Desactivar temporalmente revisión de claves foráneas para recreación limpia
SET FOREIGN_KEY_CHECKS = 0;

DROP TABLE IF EXISTS Calificacion;
DROP TABLE IF EXISTS MetodoPago;
DROP TABLE IF EXISTS Pasajero;
DROP TABLE IF EXISTS DetalleReserva;
DROP TABLE IF EXISTS Reserva;
DROP TABLE IF EXISTS TourFecha;
DROP TABLE IF EXISTS Tour;
DROP TABLE IF EXISTS Agencia;
DROP TABLE IF EXISTS Usuario;
DROP TABLE IF EXISTS Persona;
DROP TABLE IF EXISTS Rol;

SET FOREIGN_KEY_CHECKS = 1;

-- ==========================================================
-- 1. TABLA: Rol
-- ==========================================================
CREATE TABLE Rol (
    idRol INT AUTO_INCREMENT PRIMARY KEY,
    nombreRol VARCHAR(20) NOT NULL UNIQUE -- Turista / Agencia / Administrador
);

-- ==========================================================
-- 2. TABLA: Persona
-- ==========================================================
CREATE TABLE Persona (
    idPersona INT AUTO_INCREMENT PRIMARY KEY,
    nombre VARCHAR(50) NOT NULL,
    apellidoPaterno VARCHAR(50) NOT NULL,
    apellidoMaterno VARCHAR(50) NOT NULL,
    nroDocumento VARCHAR(15) NOT NULL UNIQUE,
    telefono VARCHAR(15),
    email VARCHAR(100) NOT NULL UNIQUE
);

-- ==========================================================
-- 3. TABLA: Usuario
-- ==========================================================
CREATE TABLE Usuario (
    idUsuario INT AUTO_INCREMENT PRIMARY KEY,
    idPersona INT NOT NULL,
    idRol INT NOT NULL,
    nombreUsuario VARCHAR(30) NOT NULL UNIQUE,
    contrasena VARCHAR(255) NOT NULL, -- Hasheada, nunca en texto plano
    estado VARCHAR(20) DEFAULT 'ACTIVO',
    fechaRegistro DATETIME DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_usuario_persona FOREIGN KEY (idPersona) REFERENCES Persona(idPersona) ON DELETE RESTRICT ON UPDATE CASCADE,
    CONSTRAINT fk_usuario_rol FOREIGN KEY (idRol) REFERENCES Rol(idRol) ON DELETE RESTRICT ON UPDATE CASCADE
);

-- ==========================================================
-- 4. TABLA: Agencia
-- ==========================================================
CREATE TABLE Agencia (
    idAgencia INT AUTO_INCREMENT PRIMARY KEY,
    idUsuario INT NOT NULL UNIQUE,
    razonSocial VARCHAR(150) NOT NULL,
    nombreComercial VARCHAR(150),
    ruc VARCHAR(20) NOT NULL UNIQUE,
    telefono VARCHAR(15),
    email VARCHAR(100),
    direccion VARCHAR(255),
    descripcion TEXT,
    logoUrl VARCHAR(255),
    estado VARCHAR(20) DEFAULT 'ACTIVO',
    CONSTRAINT fk_agencia_usuario FOREIGN KEY (idUsuario) REFERENCES Usuario(idUsuario) ON DELETE RESTRICT ON UPDATE CASCADE
);

-- ==========================================================
-- 5. TABLA: Tour
-- ==========================================================
CREATE TABLE Tour (
    idTour INT AUTO_INCREMENT PRIMARY KEY,
    idAgencia INT NOT NULL,
    slug VARCHAR(100) NOT NULL UNIQUE,
    nombre VARCHAR(150) NOT NULL,
    descripcion TEXT,
    precioAdulto DECIMAL(10,2) NOT NULL,
    precioNino DECIMAL(10,2) NOT NULL DEFAULT 0.00,
    precioBebe DECIMAL(10,2) NOT NULL DEFAULT 0.00,
    duracion VARCHAR(50),
    ubicacion VARCHAR(100),
    categoria VARCHAR(50), -- Cultura / Naturaleza / Aventura / Historia / Gastronomía / Relax
    calificacionPromedio DECIMAL(3,2) DEFAULT 5.00,
    estado VARCHAR(20) DEFAULT 'ACTIVO',
    CONSTRAINT fk_tour_agencia FOREIGN KEY (idAgencia) REFERENCES Agencia(idAgencia) ON DELETE RESTRICT ON UPDATE CASCADE
);

-- ==========================================================
-- 6. TABLA: TourFecha (Disponibilidad de Fechas y Cupos)
-- ==========================================================
CREATE TABLE TourFecha (
    idTourFecha INT AUTO_INCREMENT PRIMARY KEY,
    idTour INT NOT NULL,
    fecha DATE NOT NULL,
    horaInicio TIME NOT NULL,
    cupoTotal INT NOT NULL,
    cupoDisponible INT NOT NULL,
    estado VARCHAR(20) DEFAULT 'DISPONIBLE',
    CONSTRAINT fk_tourfecha_tour FOREIGN KEY (idTour) REFERENCES Tour(idTour) ON DELETE RESTRICT ON UPDATE CASCADE
);

-- ==========================================================
-- 7. TABLA: Reserva
-- ==========================================================
CREATE TABLE Reserva (
    idReserva INT AUTO_INCREMENT PRIMARY KEY,
    idUsuario INT NOT NULL, -- Titular de la reserva
    codigoReserva VARCHAR(20) NOT NULL UNIQUE,
    fechaRegistro DATETIME DEFAULT CURRENT_TIMESTAMP,
    fechaInicio DATE NOT NULL,
    fechaFin DATE NOT NULL,
    estado VARCHAR(20) NOT NULL DEFAULT 'Pendiente', -- Pendiente / Confirmada / Cancelada
    motivoCancelacion VARCHAR(255) NULL,
    total DECIMAL(10,2) NOT NULL,
    CONSTRAINT fk_reserva_usuario FOREIGN KEY (idUsuario) REFERENCES Usuario(idUsuario) ON DELETE RESTRICT ON UPDATE CASCADE
);

-- ==========================================================
-- 8. TABLA: DetalleReserva
-- ==========================================================
CREATE TABLE DetalleReserva (
    idDetalle INT AUTO_INCREMENT PRIMARY KEY,
    idReserva INT NOT NULL,
    idTourFecha INT NOT NULL,
    cantAdultos INT NOT NULL DEFAULT 1,
    cantNinos INT NOT NULL DEFAULT 0,
    cantBebes INT NOT NULL DEFAULT 0,
    subtotal DECIMAL(10,2) NOT NULL,
    CONSTRAINT fk_detalle_reserva FOREIGN KEY (idReserva) REFERENCES Reserva(idReserva) ON DELETE CASCADE ON UPDATE CASCADE,
    CONSTRAINT fk_detalle_tourfecha FOREIGN KEY (idTourFecha) REFERENCES TourFecha(idTourFecha) ON DELETE RESTRICT ON UPDATE CASCADE
);

-- ==========================================================
-- 9. TABLA: Pasajero
-- ==========================================================
CREATE TABLE Pasajero (
    idPasajero INT AUTO_INCREMENT PRIMARY KEY,
    idReserva INT NOT NULL,
    nroDocumento VARCHAR(15) NOT NULL,
    nombre VARCHAR(50) NOT NULL,
    apellidos VARCHAR(100) NOT NULL,
    edad INT NOT NULL,
    tipoSeguro VARCHAR(20) NOT NULL, -- SIS / Particular / Ninguno
    esTitular BOOLEAN NOT NULL DEFAULT FALSE,
    CONSTRAINT fk_pasajero_reserva FOREIGN KEY (idReserva) REFERENCES Reserva(idReserva) ON DELETE CASCADE ON UPDATE CASCADE
);

-- ==========================================================
-- 10. TABLA: MetodoPago
-- ==========================================================
CREATE TABLE MetodoPago (
    idMetodoPago INT AUTO_INCREMENT PRIMARY KEY,
    idReserva INT NOT NULL,
    tipoPago VARCHAR(20) NOT NULL, -- Efectivo / Tarjeta / Yape / Plin
    monto DECIMAL(10,2) NOT NULL,
    esAdelanto BOOLEAN NOT NULL DEFAULT FALSE, -- true solo si Efectivo
    estadoPago VARCHAR(20) NOT NULL DEFAULT 'Pendiente', -- Pendiente / Completado / Rechazado
    numeroOperacion VARCHAR(50) NULL,
    fechaPago DATETIME DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_pago_reserva FOREIGN KEY (idReserva) REFERENCES Reserva(idReserva) ON DELETE CASCADE ON UPDATE CASCADE
);

-- ==========================================================
-- 11. TABLA: Calificacion
-- ==========================================================
CREATE TABLE Calificacion (
    idCalificacion INT AUTO_INCREMENT PRIMARY KEY,
    idUsuario INT NOT NULL,
    idAgencia INT NOT NULL,
    idReserva INT NOT NULL, -- Trazabilidad de la experiencia realizada
    estrellas TINYINT NOT NULL CHECK (estrellas >= 1 AND estrellas <= 5),
    comentario VARCHAR(500),
    fechaCalificacion DATETIME DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_calificacion_usuario FOREIGN KEY (idUsuario) REFERENCES Usuario(idUsuario) ON DELETE RESTRICT ON UPDATE CASCADE,
    CONSTRAINT fk_calificacion_agencia FOREIGN KEY (idAgencia) REFERENCES Agencia(idAgencia) ON DELETE RESTRICT ON UPDATE CASCADE,
    CONSTRAINT fk_calificacion_reserva FOREIGN KEY (idReserva) REFERENCES Reserva(idReserva) ON DELETE CASCADE ON UPDATE CASCADE
);

-- ==========================================================
-- DATOS INICIALES (SEEDS / DML)
-- ==========================================================

-- Roles del Sistema
INSERT INTO Rol (idRol, nombreRol) VALUES
(1, 'Turista'),
(2, 'Agencia'),
(3, 'Administrador');

-- Personas
INSERT INTO Persona (idPersona, nombre, apellidoPaterno, apellidoMaterno, nroDocumento, telefono, email) VALUES
(1, 'Brian', 'Dayana', 'Infantes', '74829103', '987654321', 'briadayanainfantes@gmail.com'),
(2, 'Carlos', 'Mendoza', 'Paredes', '45892147', '951234876', 'cmendoza@andestours.pe'),
(3, 'Valeria', 'Rios', 'Gomez', '71239845', '963258741', 'contacto@inkatravel.pe'),
(4, 'Admin', 'Travelink', 'Global', '00000001', '999999999', 'admin@travelink.pe');

-- Usuarios (contraseñas con hash bcrypt simulado/ejemplo)
INSERT INTO Usuario (idUsuario, idPersona, idRol, nombreUsuario, contrasena, estado) VALUES
(1, 1, 1, 'briane', '$2a$12$e8x5QJtF4qC6jG0P6PZbLeC5uK/bT5Tq9Zg9zQd7G1H0I2J3K4L5M', 'ACTIVO'),
(2, 2, 2, 'andestours_admin', '$2a$12$e8x5QJtF4qC6jG0P6PZbLeC5uK/bT5Tq9Zg9zQd7G1H0I2J3K4L5M', 'ACTIVO'),
(3, 3, 2, 'inkatravel_admin', '$2a$12$e8x5QJtF4qC6jG0P6PZbLeC5uK/bT5Tq9Zg9zQd7G1H0I2J3K4L5M', 'ACTIVO'),
(4, 4, 3, 'admin_travelink', '$2a$12$e8x5QJtF4qC6jG0P6PZbLeC5uK/bT5Tq9Zg9zQd7G1H0I2J3K4L5M', 'ACTIVO');

-- Agencias
INSERT INTO Agencia (idAgencia, idUsuario, razonSocial, nombreComercial, ruc, telefono, email, direccion, descripcion) VALUES
(1, 2, 'ANDES TOURS PERU S.A.C.', 'Andes Tours', '20601234567', '951234876', 'contacto@andestours.pe', 'Av. El Sol 450, Cusco', 'Especialistas en turismo cultural y de aventura en la región del Cusco.'),
(2, 3, 'INKA TRAVEL EXPERIENCES S.A.C.', 'Inka Travel', '20609876543', '963258741', 'reservas@inkatravel.pe', 'Calle Plateros 320, Cusco', 'Tours personalizados y experiencias arqueológicas únicas.');

-- Tours
INSERT INTO Tour (idTour, idAgencia, slug, nombre, descripcion, precioAdulto, precioNino, precioBebe, duracion, ubicacion, categoria, calificacionPromedio) VALUES
(1, 1, 'machu-picchu', 'Machu Picchu Clásico', 'Descubre la maravilla del mundo en una experiencia inolvidable con guías certificados y tren panorámico.', 350.00, 250.00, 100.00, '1 día', 'Cusco', 'Cultura', 4.8),
(2, 1, '7-colores', 'Montaña de 7 Colores', 'La belleza natural de los Andes en todo su esplendor con caminata guiada y traslados incluidos.', 280.00, 180.00, 80.00, '1 día', 'Cusco', 'Naturaleza', 4.7),
(3, 2, 'valle-sagrado', 'Valle Sagrado de los Incas', 'Historia, cultura y paisajes increíbles visitando Pisac, Ollantaytambo y Chinchero.', 320.00, 220.00, 90.00, '1 día', 'Cusco', 'Cultura', 4.6),
(4, 1, 'humantay', 'Laguna Humantay', 'Impresionante laguna de aguas turquesas al pie del nevado Humantay.', 150.00, 100.00, 50.00, '1 día', 'Cusco', 'Naturaleza', 4.9);

-- Fechas y Cupos para Tours
INSERT INTO TourFecha (idTourFecha, idTour, fecha, horaInicio, cupoTotal, cupoDisponible, estado) VALUES
(1, 1, '2025-10-12', '06:00:00', 30, 22, 'DISPONIBLE'),
(2, 2, '2025-10-15', '04:30:00', 25, 18, 'DISPONIBLE'),
(3, 3, '2025-10-20', '07:00:00', 30, 25, 'DISPONIBLE'),
(4, 4, '2025-10-22', '05:00:00', 20, 15, 'DISPONIBLE');

-- Reserva de Ejemplo
INSERT INTO Reserva (idReserva, idUsuario, codigoReserva, fechaRegistro, fechaInicio, fechaFin, estado, motivoCancelacion, total) VALUES
(1, 1, 'TRK-001', '2025-09-20 10:30:00', '2025-10-12', '2025-10-12', 'Confirmada', NULL, 1050.00);

-- Detalle de la Reserva (2 adultos y 1 niño)
INSERT INTO DetalleReserva (idDetalle, idReserva, idTourFecha, cantAdultos, cantNinos, cantBebes, subtotal) VALUES
(1, 1, 1, 2, 1, 0, 1050.00);

-- Pasajeros asociados a la Reserva
INSERT INTO Pasajero (idPasajero, idReserva, nroDocumento, nombre, apellidos, edad, tipoSeguro, esTitular) VALUES
(1, 1, '74829103', 'Ana', 'Garcia', 25, 'SIS', TRUE),
(2, 1, '87654321', 'Carlos', 'Garcia', 30, 'Particular', FALSE),
(3, 1, '11223344', 'Lucia', 'Garcia', 12, 'SIS', FALSE);

-- Método de Pago
INSERT INTO MetodoPago (idMetodoPago, idReserva, tipoPago, monto, esAdelanto, estadoPago, numeroOperacion, fechaPago) VALUES
(1, 1, 'Tarjeta', 1050.00, FALSE, 'Completado', 'OP-98234710', '2025-09-20 10:35:00');

-- Calificación
INSERT INTO Calificacion (idCalificacion, idUsuario, idAgencia, idReserva, estrellas, comentario, fechaCalificacion) VALUES
(1, 1, 1, 1, 5, 'Excelente experiencia con Andes Tours, todo puntual y el guía muy preparado.', '2025-10-13 14:00:00');
