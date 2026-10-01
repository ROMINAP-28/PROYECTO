-- ==========================================================
-- Base de Datos: TRAVELINK
-- Schema Principal Consolidado
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
DROP TABLE IF EXISTS PaqueteTour;
DROP TABLE IF EXISTS Paquete;
DROP TABLE IF EXISTS TourImagen;
DROP TABLE IF EXISTS Tour;
DROP TABLE IF EXISTS Destino;
DROP TABLE IF EXISTS SolicitudAgencia;
DROP TABLE IF EXISTS Agencia;
DROP TABLE IF EXISTS Usuario;
DROP TABLE IF EXISTS Persona;
DROP TABLE IF EXISTS Rol;

SET FOREIGN_KEY_CHECKS = 1;

CREATE TABLE Rol (
    idRol INT AUTO_INCREMENT PRIMARY KEY,
    nombreRol VARCHAR(20) NOT NULL UNIQUE
);

CREATE TABLE Persona (
    idPersona INT AUTO_INCREMENT PRIMARY KEY,
    nombre VARCHAR(50) NOT NULL,
    apellidoPaterno VARCHAR(50) NOT NULL,
    apellidoMaterno VARCHAR(50) NOT NULL,
    nroDocumento VARCHAR(15) NOT NULL UNIQUE,
    telefono VARCHAR(15),
    email VARCHAR(100) NOT NULL UNIQUE
);

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
    CONSTRAINT fk_agencia_usuario FOREIGN KEY (idUsuario) REFERENCES Usuario(idUsuario) ON DELETE RESTRICT ON UPDATE CASCADE
);

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

CREATE TABLE Destino (
    idDestino INT AUTO_INCREMENT PRIMARY KEY,
    nombre VARCHAR(100) NOT NULL UNIQUE,
    estado VARCHAR(20) DEFAULT 'ACTIVO'
);

CREATE TABLE Tour (
    idTour INT AUTO_INCREMENT PRIMARY KEY,
    idAgencia INT NOT NULL,
    idDestino INT NOT NULL,
    slug VARCHAR(100) NOT NULL UNIQUE,
    nombre VARCHAR(150) NOT NULL,
    descripcion TEXT,
    precioAdulto DECIMAL(10,2) NOT NULL,
    precioNino DECIMAL(10,2) NOT NULL DEFAULT 0.00,
    precioBebe DECIMAL(10,2) NOT NULL DEFAULT 0.00,
    duracion VARCHAR(50),
    categoria VARCHAR(50),
    calificacionPromedio DECIMAL(3,2) DEFAULT 5.00,
    estado VARCHAR(50) DEFAULT 'ACTIVO',
    CONSTRAINT fk_tour_agencia FOREIGN KEY (idAgencia) REFERENCES Agencia(idAgencia) ON DELETE RESTRICT ON UPDATE CASCADE,
    CONSTRAINT fk_tour_destino FOREIGN KEY (idDestino) REFERENCES Destino(idDestino) ON DELETE RESTRICT ON UPDATE CASCADE
);

CREATE TABLE TourImagen (
    idImagen INT AUTO_INCREMENT PRIMARY KEY,
    idTour INT NOT NULL,
    url VARCHAR(255) NOT NULL,
    esPrincipal BOOLEAN DEFAULT FALSE,
    orden INT DEFAULT 0,
    CONSTRAINT fk_tourimagen_tour FOREIGN KEY (idTour) REFERENCES Tour(idTour) ON DELETE CASCADE ON UPDATE CASCADE
);

CREATE TABLE Paquete (
    idPaquete INT AUTO_INCREMENT PRIMARY KEY,
    idAgencia INT NOT NULL,
    nombre VARCHAR(150) NOT NULL,
    descripcion TEXT,
    precio DECIMAL(10,2) NOT NULL DEFAULT 0.00,
    descuento DECIMAL(5,2) DEFAULT 0.00,
    duracion VARCHAR(50) DEFAULT '2 días / 1 noche',
    condiciones TEXT,
    imagenUrl VARCHAR(255),
    estado VARCHAR(20) DEFAULT 'PUBLICADO',
    CONSTRAINT fk_paquete_agencia FOREIGN KEY (idAgencia) REFERENCES Agencia(idAgencia) ON DELETE CASCADE ON UPDATE CASCADE
);

CREATE TABLE PaqueteTour (
    idPaqueteTour INT AUTO_INCREMENT PRIMARY KEY,
    idPaquete INT NOT NULL,
    idTour INT NOT NULL,
    CONSTRAINT fk_paquetetour_paquete FOREIGN KEY (idPaquete) REFERENCES Paquete(idPaquete) ON DELETE CASCADE ON UPDATE CASCADE,
    CONSTRAINT fk_paquetetour_tour FOREIGN KEY (idTour) REFERENCES Tour(idTour) ON DELETE CASCADE ON UPDATE CASCADE
);

CREATE TABLE TourFecha (
    idTourFecha INT AUTO_INCREMENT PRIMARY KEY,
    idTour INT NOT NULL,
    fecha DATE NOT NULL,
    horaInicio TIME NOT NULL DEFAULT '08:00:00',
    horaFin TIME DEFAULT '18:00:00',
    cupoTotal INT NOT NULL DEFAULT 30,
    cupoDisponible INT NOT NULL DEFAULT 30,
    estado VARCHAR(20) DEFAULT 'DISPONIBLE',
    CONSTRAINT fk_tourfecha_tour FOREIGN KEY (idTour) REFERENCES Tour(idTour) ON DELETE RESTRICT ON UPDATE CASCADE
);

CREATE TABLE Reserva (
    idReserva INT AUTO_INCREMENT PRIMARY KEY,
    idUsuario INT NOT NULL,
    idAgencia INT NOT NULL DEFAULT 1,
    codigoReserva VARCHAR(20) NOT NULL UNIQUE,
    nombreTour VARCHAR(150),
    fechaRegistro DATETIME DEFAULT CURRENT_TIMESTAMP,
    fechaInicio DATE NOT NULL,
    fechaFin DATE NOT NULL,
    estado VARCHAR(20) NOT NULL DEFAULT 'PENDIENTE',
    motivoCancelacion VARCHAR(255) NULL,
    total DECIMAL(10,2) NOT NULL,
    CONSTRAINT fk_reserva_usuario FOREIGN KEY (idUsuario) REFERENCES Usuario(idUsuario) ON DELETE RESTRICT ON UPDATE CASCADE,
    CONSTRAINT fk_reserva_agencia FOREIGN KEY (idAgencia) REFERENCES Agencia(idAgencia) ON DELETE RESTRICT ON UPDATE CASCADE
);

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

CREATE TABLE Pasajero (
    idPasajero INT AUTO_INCREMENT PRIMARY KEY,
    idReserva INT NOT NULL,
    nroDocumento VARCHAR(15) NOT NULL,
    nombre VARCHAR(50) NOT NULL,
    apellidos VARCHAR(100) NOT NULL,
    esTitular BOOLEAN NOT NULL DEFAULT FALSE,
    CONSTRAINT fk_pasajero_reserva FOREIGN KEY (idReserva) REFERENCES Reserva(idReserva) ON DELETE CASCADE ON UPDATE CASCADE
);

CREATE TABLE MetodoPago (
    idPago INT AUTO_INCREMENT PRIMARY KEY,
    idReserva INT NOT NULL,
    tipoPago VARCHAR(50) NOT NULL DEFAULT 'Tarjeta',
    metodoPago VARCHAR(50) DEFAULT 'Tarjeta de Crédito',
    monto DECIMAL(10,2) NOT NULL,
    esAdelanto BOOLEAN NOT NULL DEFAULT FALSE,
    estadoPago VARCHAR(20) NOT NULL DEFAULT 'PENDIENTE',
    numeroOperacion VARCHAR(50) NULL,
    fechaPago DATETIME DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_pago_reserva FOREIGN KEY (idReserva) REFERENCES Reserva(idReserva) ON DELETE CASCADE ON UPDATE CASCADE
);

CREATE TABLE Calificacion (
    idCalificacion INT AUTO_INCREMENT PRIMARY KEY,
    idUsuario INT NOT NULL,
    idAgencia INT NOT NULL,
    idReserva INT NOT NULL,
    estrellas TINYINT NOT NULL CHECK (estrellas >= 1 AND estrellas <= 5),
    comentario VARCHAR(500),
    fechaCalificacion DATETIME DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_calificacion_usuario FOREIGN KEY (idUsuario) REFERENCES Usuario(idUsuario) ON DELETE RESTRICT ON UPDATE CASCADE,
    CONSTRAINT fk_calificacion_agencia FOREIGN KEY (idAgencia) REFERENCES Agencia(idAgencia) ON DELETE RESTRICT ON UPDATE CASCADE,
    CONSTRAINT fk_calificacion_reserva FOREIGN KEY (idReserva) REFERENCES Reserva(idReserva) ON DELETE CASCADE ON UPDATE CASCADE
);

CREATE TABLE Comision (
    idComision INT AUTO_INCREMENT PRIMARY KEY,
    idAgencia INT NOT NULL,
    periodo VARCHAR(10) NOT NULL,
    toursVendidos INT NOT NULL DEFAULT 0,
    montoComision DECIMAL(10,2) NOT NULL,
    montoNeto DECIMAL(10,2) NOT NULL,
    estado VARCHAR(20) DEFAULT 'PENDIENTE',
    fechaPago DATETIME NULL,
    CONSTRAINT fk_comision_agencia FOREIGN KEY (idAgencia) REFERENCES Agencia(idAgencia) ON DELETE RESTRICT ON UPDATE CASCADE
);

CREATE TABLE Notificacion (
    idNotificacion INT AUTO_INCREMENT PRIMARY KEY,
    idUsuario INT NULL,
    rolTarget VARCHAR(20) NOT NULL DEFAULT 'AGENCIA',
    titulo VARCHAR(150) NOT NULL,
    mensaje TEXT NOT NULL,
    leido BOOLEAN DEFAULT FALSE,
    fechaRegistro DATETIME DEFAULT CURRENT_TIMESTAMP
);
