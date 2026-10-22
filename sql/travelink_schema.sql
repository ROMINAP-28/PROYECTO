-- ==========================================================
-- Base de Datos: TRAVELINK
-- Schema Principal Consolidado (17 Tablas Activas)
-- ==========================================================

CREATE DATABASE IF NOT EXISTS DBTravelink CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
USE DBTravelink;

SET FOREIGN_KEY_CHECKS = 0;

DROP TABLE IF EXISTS Notificacion;
DROP TABLE IF EXISTS Comision;
DROP TABLE IF EXISTS Calificacion;
DROP TABLE IF EXISTS MetodoPago;
DROP TABLE IF EXISTS Pasajero;
DROP TABLE IF EXISTS DetalleReserva;
DROP TABLE IF EXISTS Reserva;
DROP TABLE IF EXISTS TourFecha;
DROP TABLE IF EXISTS TourImagen;
DROP TABLE IF EXISTS Tour;
DROP TABLE IF EXISTS Destino;
DROP TABLE IF EXISTS HistorialAgencia;
DROP TABLE IF EXISTS SolicitudAgencia;
DROP TABLE IF EXISTS Agencia;
DROP TABLE IF EXISTS Usuario;
DROP TABLE IF EXISTS Persona;
DROP TABLE IF EXISTS Rol;

SET FOREIGN_KEY_CHECKS = 1;

-- 1. Rol
CREATE TABLE Rol (
    idRol INT AUTO_INCREMENT PRIMARY KEY,
    nombreRol VARCHAR(20) NOT NULL UNIQUE
);

-- 2. Persona
CREATE TABLE Persona (
    idPersona INT AUTO_INCREMENT PRIMARY KEY,
    nombre VARCHAR(50) NOT NULL,
    apellidoPaterno VARCHAR(50) NOT NULL,
    apellidoMaterno VARCHAR(50) NOT NULL,
    nroDocumento VARCHAR(15) NOT NULL UNIQUE,
    telefono VARCHAR(15),
    email VARCHAR(100) NOT NULL UNIQUE
);

-- 3. Usuario
CREATE TABLE Usuario (
    idUsuario INT AUTO_INCREMENT PRIMARY KEY,
    idPersona INT NOT NULL,
    idRol INT NOT NULL,
    nombreUsuario VARCHAR(30) NOT NULL UNIQUE,
    contrasena VARCHAR(255) NOT NULL,
    estado VARCHAR(20) DEFAULT 'ACTIVO',
    fechaRegistro DATETIME DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_usuario_persona FOREIGN KEY (idPersona) REFERENCES Persona(idPersona) ON DELETE RESTRICT ON UPDATE CASCADE,
    CONSTRAINT fk_usuario_rol FOREIGN KEY (idRol) REFERENCES Rol(idRol) ON DELETE RESTRICT ON UPDATE CASCADE
);

-- 4. Agencia
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
    estado VARCHAR(20) DEFAULT 'PENDIENTE',
    porcentajeComision DECIMAL(5,2) DEFAULT 15.00,
    promedioCalificacion DECIMAL(3,2) DEFAULT 0.00,
    fechaRegistro DATETIME DEFAULT CURRENT_TIMESTAMP,
    comision DECIMAL(10,2) DEFAULT 0.00,
    CONSTRAINT fk_agencia_usuario FOREIGN KEY (idUsuario) REFERENCES Usuario(idUsuario) ON DELETE RESTRICT ON UPDATE CASCADE
);

-- 5. SolicitudAgencia
CREATE TABLE SolicitudAgencia (
    idSolicitud INT AUTO_INCREMENT PRIMARY KEY,
    idUsuario INT NOT NULL,
    razonSocial VARCHAR(150) NOT NULL,
    ruc VARCHAR(20) NOT NULL,
    representanteLegal VARCHAR(150) NOT NULL,
    telefonoContacto VARCHAR(15),
    correoContacto VARCHAR(100),
    documentoRuc VARCHAR(255),
    estado VARCHAR(20) DEFAULT 'Pendiente',
    fechaSolicitud DATETIME DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_solicitud_usuario FOREIGN KEY (idUsuario) REFERENCES Usuario(idUsuario) ON DELETE CASCADE ON UPDATE CASCADE
);

-- 6. HistorialAgencia
CREATE TABLE HistorialAgencia (
    idHistorial INT AUTO_INCREMENT PRIMARY KEY,
    idAgencia INT NOT NULL,
    idAdministrador INT NULL,
    estadoAnterior VARCHAR(20),
    estadoNuevo VARCHAR(20),
    motivo VARCHAR(255),
    fecha DATETIME DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_historial_agencia FOREIGN KEY (idAgencia) REFERENCES Agencia(idAgencia) ON DELETE CASCADE ON UPDATE CASCADE
);

-- 7. Destino
CREATE TABLE Destino (
    idDestino INT AUTO_INCREMENT PRIMARY KEY,
    nombre VARCHAR(100) NOT NULL UNIQUE,
    estado VARCHAR(20) DEFAULT 'ACTIVO'
);

-- 8. Tour (Paquete Turístico)
CREATE TABLE Tour (
    idTour INT AUTO_INCREMENT PRIMARY KEY,
    idAgencia INT NOT NULL,
    idDestino INT NOT NULL,
    slug VARCHAR(100) NOT NULL UNIQUE,
    nombre VARCHAR(150) NOT NULL,
    descripcion TEXT,
    descripcionCorta VARCHAR(255) NULL,
    itinerario TEXT NULL,
    aceptaNinos TINYINT(1) NOT NULL DEFAULT 1,
    edadMaxNino TINYINT NOT NULL DEFAULT 11,
    edadMaxBebe TINYINT NOT NULL DEFAULT 2,
    descuento DECIMAL(5,2) NOT NULL DEFAULT 0.00,
    condiciones TEXT NULL,
    minAsientosOferta INT NOT NULL DEFAULT 5,
    precioAdulto DECIMAL(10,2) NOT NULL,
    precioNino DECIMAL(10,2) NOT NULL DEFAULT 0.00,
    precioBebe DECIMAL(10,2) NOT NULL DEFAULT 0.00,
    duracion VARCHAR(50),
    ubicacion VARCHAR(100),
    categoria VARCHAR(50),
    calificacionPromedio DECIMAL(3,2) DEFAULT 5.00,
    estado VARCHAR(50) DEFAULT 'ACTIVO',
    aceptaBebes TINYINT(1) NOT NULL DEFAULT 1,
    queIncluye TEXT,
    queNoIncluye TEXT,
    dias INT NOT NULL DEFAULT 1,
    horas INT NOT NULL DEFAULT 6,
    INDEX idx_tour_agencia_estado (idAgencia, estado),
    INDEX idx_tour_destino (idDestino),
    CONSTRAINT fk_tour_agencia FOREIGN KEY (idAgencia) REFERENCES Agencia(idAgencia) ON DELETE RESTRICT ON UPDATE CASCADE,
    CONSTRAINT fk_tour_destino FOREIGN KEY (idDestino) REFERENCES Destino(idDestino) ON DELETE RESTRICT ON UPDATE CASCADE
);

-- 9. TourFecha (Salidas: fecha, hora y cupo)
CREATE TABLE TourFecha (
    idTourFecha INT AUTO_INCREMENT PRIMARY KEY,
    idTour INT NOT NULL,
    fecha DATE NOT NULL,
    horaInicio TIME NOT NULL DEFAULT '08:00:00',
    horaFin TIME DEFAULT '18:00:00',
    cupoTotal INT NOT NULL DEFAULT 30,
    cupoDisponible INT NOT NULL DEFAULT 30,
    estado VARCHAR(20) DEFAULT 'DISPONIBLE',
    UNIQUE KEY uq_tour_fecha_hora (idTour, fecha, horaInicio),
    INDEX idx_tourfecha_fecha (idTour, fecha),
    CONSTRAINT fk_tourfecha_tour FOREIGN KEY (idTour) REFERENCES Tour(idTour) ON DELETE RESTRICT ON UPDATE CASCADE
);

-- 10. TourImagen (Imágenes del Tour)
CREATE TABLE TourImagen (
    idImagen INT AUTO_INCREMENT PRIMARY KEY,
    idTour INT NOT NULL,
    url VARCHAR(255) NOT NULL,
    esPrincipal BOOLEAN DEFAULT FALSE,
    orden INT DEFAULT 0,
    INDEX idx_tourimagen_orden (idTour, esPrincipal, orden),
    CONSTRAINT fk_tourimagen_tour FOREIGN KEY (idTour) REFERENCES Tour(idTour) ON DELETE CASCADE ON UPDATE CASCADE
);

-- 11. Reserva
CREATE TABLE Reserva (
    idReserva INT AUTO_INCREMENT PRIMARY KEY,
    idUsuario INT NOT NULL,
    idAgencia INT NOT NULL DEFAULT 1,
    idTour INT NULL,
    idTourFecha INT NULL,
    nombreTour VARCHAR(150),
    codigoReserva VARCHAR(20) NOT NULL UNIQUE,
    fechaRegistro DATETIME DEFAULT CURRENT_TIMESTAMP,
    fechaInicio DATE NOT NULL,
    fechaFin DATE NOT NULL,
    estado VARCHAR(20) NOT NULL DEFAULT 'PENDIENTE',
    motivoCancelacion VARCHAR(255) NULL,
    total DECIMAL(10,2) NOT NULL,
    CONSTRAINT fk_reserva_usuario FOREIGN KEY (idUsuario) REFERENCES Usuario(idUsuario) ON DELETE RESTRICT ON UPDATE CASCADE,
    CONSTRAINT fk_reserva_agencia FOREIGN KEY (idAgencia) REFERENCES Agencia(idAgencia) ON DELETE RESTRICT ON UPDATE CASCADE,
    CONSTRAINT fk_reserva_tour FOREIGN KEY (idTour) REFERENCES Tour(idTour) ON DELETE RESTRICT ON UPDATE CASCADE,
    CONSTRAINT fk_reserva_tourfecha FOREIGN KEY (idTourFecha) REFERENCES TourFecha(idTourFecha) ON DELETE RESTRICT ON UPDATE CASCADE
);

-- 12. DetalleReserva
CREATE TABLE DetalleReserva (
    idDetalle INT AUTO_INCREMENT PRIMARY KEY,
    idReserva INT NOT NULL,
    idTourFecha INT NOT NULL,
    cantAdultos INT NOT NULL DEFAULT 1,
    cantNinos INT NOT NULL DEFAULT 0,
    cantBebes INT NOT NULL DEFAULT 0,
    subtotal DECIMAL(10,2) NOT NULL,
    idPaquete INT NULL,
    CONSTRAINT fk_detalle_reserva FOREIGN KEY (idReserva) REFERENCES Reserva(idReserva) ON DELETE CASCADE ON UPDATE CASCADE,
    CONSTRAINT fk_detalle_tourfecha FOREIGN KEY (idTourFecha) REFERENCES TourFecha(idTourFecha) ON DELETE RESTRICT ON UPDATE CASCADE
);

-- 13. Pasajero
CREATE TABLE Pasajero (
    idPasajero INT AUTO_INCREMENT PRIMARY KEY,
    idReserva INT NOT NULL,
    nroDocumento VARCHAR(15) NOT NULL,
    nombre VARCHAR(50) NOT NULL,
    apellidoPaterno VARCHAR(50) NULL,
    apellidoMaterno VARCHAR(50) NULL,
    esTitular BOOLEAN NOT NULL DEFAULT FALSE,
    tipo VARCHAR(20) NOT NULL DEFAULT 'ADULTO',
    CONSTRAINT fk_pasajero_reserva FOREIGN KEY (idReserva) REFERENCES Reserva(idReserva) ON DELETE CASCADE ON UPDATE CASCADE
);

-- 14. MetodoPago
CREATE TABLE MetodoPago (
    idMetodoPago INT AUTO_INCREMENT PRIMARY KEY,
    idReserva INT NOT NULL,
    tipoPago VARCHAR(50) NOT NULL DEFAULT 'Tarjeta',
    monto DECIMAL(10,2) NOT NULL,
    esAdelanto BOOLEAN NOT NULL DEFAULT FALSE,
    estadoPago VARCHAR(20) NOT NULL DEFAULT 'PENDIENTE',
    numeroOperacion VARCHAR(50) NULL,
    urlComprobante VARCHAR(255) NULL,
    url VARCHAR(255) NULL,
    fechaPago DATETIME DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_pago_reserva FOREIGN KEY (idReserva) REFERENCES Reserva(idReserva) ON DELETE CASCADE ON UPDATE CASCADE
);

-- 15. Calificacion
CREATE TABLE Calificacion (
    idCalificacion INT AUTO_INCREMENT PRIMARY KEY,
    idUsuario INT NOT NULL,
    idAgencia INT NOT NULL,
    idReserva INT NOT NULL,
    estrellas TINYINT NOT NULL CHECK (estrellas >= 1 AND estrellas <= 5),
    comentario VARCHAR(500),
    fechaCalificacion DATETIME DEFAULT CURRENT_TIMESTAMP,
    eliminadoPorAdmin BOOLEAN DEFAULT FALSE,
    motivoEliminacion VARCHAR(255) NULL,
    flageada BOOLEAN DEFAULT FALSE,
    motivoFlag VARCHAR(255) NULL,
    fechaFlag DATETIME NULL,
    estadoRevision VARCHAR(20) DEFAULT 'APROBADO',
    CONSTRAINT fk_calificacion_usuario FOREIGN KEY (idUsuario) REFERENCES Usuario(idUsuario) ON DELETE RESTRICT ON UPDATE CASCADE,
    CONSTRAINT fk_calificacion_agencia FOREIGN KEY (idAgencia) REFERENCES Agencia(idAgencia) ON DELETE RESTRICT ON UPDATE CASCADE,
    CONSTRAINT fk_calificacion_reserva FOREIGN KEY (idReserva) REFERENCES Reserva(idReserva) ON DELETE CASCADE ON UPDATE CASCADE
);

-- 16. Comision
CREATE TABLE Comision (
    idComision INT AUTO_INCREMENT PRIMARY KEY,
    idAgencia INT NOT NULL,
    idReserva INT NULL,
    periodo VARCHAR(10) NOT NULL,
    subtotal DECIMAL(10,2) DEFAULT 0.00,
    porcentaje DECIMAL(5,2) DEFAULT 15.00,
    porcentajeComision DECIMAL(5,2) DEFAULT 15.00,
    montoComision DECIMAL(10,2) NOT NULL,
    montoNeto DECIMAL(10,2) NOT NULL,
    montoAgencia DECIMAL(10,2) DEFAULT 0.00,
    montoTravelink DECIMAL(10,2) DEFAULT 0.00,
    ventasTotales DECIMAL(10,2) DEFAULT 0.00,
    estado VARCHAR(20) DEFAULT 'PENDIENTE',
    estadoLiquidacion VARCHAR(20) DEFAULT 'PENDIENTE',
    fechaCalculo DATETIME DEFAULT CURRENT_TIMESTAMP,
    fechaLiquidacion DATETIME NULL,
    CONSTRAINT fk_comision_agencia FOREIGN KEY (idAgencia) REFERENCES Agencia(idAgencia) ON DELETE RESTRICT ON UPDATE CASCADE
);

-- 17. Notificacion
CREATE TABLE Notificacion (
    idNotificacion INT AUTO_INCREMENT PRIMARY KEY,
    idUsuario INT NULL,
    rolTarget VARCHAR(20) NOT NULL DEFAULT 'AGENCIA',
    titulo VARCHAR(150) NOT NULL,
    mensaje TEXT NOT NULL,
    leido BOOLEAN DEFAULT FALSE,
    fechaRegistro DATETIME DEFAULT CURRENT_TIMESTAMP
);
