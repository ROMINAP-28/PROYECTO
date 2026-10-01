-- ==========================================================
-- Travelink - Script de Limpieza, Esquema y Datos de Prueba
-- Fase 0: Eliminación limpia en orden estricto de claves foráneas
-- ==========================================================

CREATE DATABASE IF NOT EXISTS DBTravelink CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
USE DBTravelink;

SET FOREIGN_KEY_CHECKS = 0;

-- Borrado en orden inverso de dependencias
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

-- ==========================================================
-- ESTRUCTURA DE TABLAS
-- ==========================================================

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
    estado VARCHAR(20) DEFAULT 'PENDIENTE', -- ACTIVO, PENDIENTE, RECHAZADO
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
    estado VARCHAR(20) DEFAULT 'Pendiente', -- Pendiente, Aceptado, Rechazado
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
    estado VARCHAR(50) DEFAULT 'ACTIVO', -- ACTIVO, PAUSADO, DESACTIVADO_POR_ADMIN
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
    estado VARCHAR(20) DEFAULT 'PUBLICADO', -- PUBLICADO, BORRADOR, PAUSADO
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
    estado VARCHAR(20) DEFAULT 'DISPONIBLE', -- DISPONIBLE, AGOTADO, CERRADO
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
    estado VARCHAR(20) NOT NULL DEFAULT 'PENDIENTE', -- PENDIENTE, CONFIRMADA, CANCELADA, COMPLETADA
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
    estadoPago VARCHAR(20) NOT NULL DEFAULT 'PENDIENTE', -- PENDIENTE, COMPLETADO, RECHAZADO
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
    rolTarget VARCHAR(20) NOT NULL DEFAULT 'AGENCIA', -- TURISTA, AGENCIA, ADMINISTRADOR
    titulo VARCHAR(150) NOT NULL,
    mensaje TEXT NOT NULL,
    leido BOOLEAN DEFAULT FALSE,
    fechaRegistro DATETIME DEFAULT CURRENT_TIMESTAMP
);

-- ==========================================================
-- DATOS SEMILLA (8 Turistas, 4 Agencias, 1 Admin, 2 Solicitudes)
-- ==========================================================

INSERT INTO Rol (idRol, nombreRol) VALUES
(1, 'Turista'),
(2, 'Agencia'),
(3, 'Administrador');

-- 1 Admin + 4 Agencias + 8 Turistas = 13 Personas
INSERT INTO Persona (idPersona, nombre, apellidoPaterno, apellidoMaterno, nroDocumento, telefono, email) VALUES
(1, 'Admin', 'Travelink', 'Global', '00000001', '999999999', 'admin@travelink.pe'),
(2, 'Carlos', 'Mendoza', 'Paredes', '20601234', '951234876', 'contacto@andestours.pe'),
(3, 'Valeria', 'Rios', 'Gomez', '20609876', '963258741', 'reservas@inkatravel.pe'),
(4, 'Jorge', 'Soto', 'Vargas', '20605555', '988776655', 'contacto@amazonexpeditions.pe'),
(5, 'Mariana', 'Cruz', 'Lope', '20607777', '977665544', 'reservas@suraventura.pe'),
(6, 'Brian', 'Dayana', 'Infantes', '74829103', '987654321', 'briadayanainfantes@gmail.com'),
(7, 'Juan', 'Perez', 'Gomez', '71234567', '912345678', 'juan.perez@gmail.com'),
(8, 'Maria', 'Rodriguez', 'Silva', '72345678', '923456789', 'maria.rodriguez@gmail.com'),
(9, 'Pedro', 'Sanchez', 'Torres', '73456789', '934567890', 'pedro.sanchez@gmail.com'),
(10, 'Laura', 'Benitez', 'Ruiz', '74567890', '945678901', 'laura.benitez@gmail.com'),
(11, 'Diego', 'Morales', 'Castro', '75678901', '956789012', 'diego.morales@gmail.com'),
(12, 'Sofia', 'Chavez', 'Flores', '76789012', '967890123', 'sofia.chavez@gmail.com'),
(13, 'Gabriel', 'Nunez', 'Vidal', '77890123', '978901234', 'gabriel.nunez@gmail.com');

-- Usuarios
INSERT INTO Usuario (idUsuario, idPersona, idRol, nombreUsuario, contrasena, estado) VALUES
(1, 1, 3, 'admin_travelink', 'admin123', 'ACTIVO'),
(2, 2, 2, 'andestours_admin', 'agencia123', 'ACTIVO'),
(3, 3, 2, 'inkatravel_admin', 'agencia123', 'ACTIVO'),
(4, 4, 2, 'amazon_admin', 'agencia123', 'ACTIVO'),
(5, 5, 2, 'suraventura_admin', 'agencia123', 'ACTIVO'),
(6, 6, 1, 'briane', '12345678', 'ACTIVO'),
(7, 7, 1, 'juanp', '12345678', 'ACTIVO'),
(8, 8, 1, 'mariar', '12345678', 'ACTIVO'),
(9, 9, 1, 'pedros', '12345678', 'ACTIVO'),
(10, 10, 1, 'laurab', '12345678', 'ACTIVO'),
(11, 11, 1, 'diegom', '12345678', 'ACTIVO'),
(12, 12, 1, 'sofiac', '12345678', 'ACTIVO'),
(13, 13, 1, 'gabrieln', '12345678', 'ACTIVO');

-- Agencias
INSERT INTO Agencia (idAgencia, idUsuario, razonSocial, nombreComercial, ruc, telefono, email, direccion, descripcion, estado) VALUES
(1, 2, 'ANDES TOURS PERU S.A.C.', 'Andes Tours', '20601234567', '951234876', 'contacto@andestours.pe', 'Av. Sol 450, Lima', 'Especialistas en turismo cultural y de aventura en Lima y Pasco.', 'ACTIVO'),
(2, 3, 'INKA TRAVEL EXPERIENCES S.A.C.', 'Inka Travel', '20609876543', '963258741', 'reservas@inkatravel.pe', 'Calle Real 120, Pasco', 'Tours personalizados en la sierra y selva central.', 'ACTIVO'),
(3, 4, 'AMAZON EXPEDITIONS PERU E.I.R.L.', 'Amazon Expeditions', '20605555111', '988776655', 'contacto@amazonexpeditions.pe', 'Av. Aeropuerto 80, Tambopata', 'Inmersiones ecoturísticas en la selva amazónica.', 'PENDIENTE'),
(4, 5, 'SUR AVENTURA TACNA S.A.C.', 'Sur Aventura', '20607777222', '977665544', 'reservas@suraventura.pe', 'Av. Bolognesi 300, Tacna', 'Circuitos termales, bodegas vitivinícolas y playas del sur.', 'PENDIENTE');

-- Solicitudes de Agencia Pendientes
INSERT INTO SolicitudAgencia (idSolicitud, idUsuario, razonSocial, ruc, representanteLegal, telefonoContacto, correoContacto, documentoRuc, estado) VALUES
(1, 4, 'AMAZON EXPEDITIONS PERU E.I.R.L.', '20605555111', 'Jorge Soto Vargas', '988776655', 'contacto@amazonexpeditions.pe', 'ruc_amazon.pdf', 'Pendiente'),
(2, 5, 'SUR AVENTURA TACNA S.A.C.', '20607777222', 'Mariana Cruz Lope', '977665544', 'reservas@suraventura.pe', 'ruc_suraventura.pdf', 'Pendiente');

-- Destinos
INSERT INTO Destino (idDestino, nombre, estado) VALUES
(1, 'Lima', 'ACTIVO'),
(2, 'Pasco', 'ACTIVO'),
(3, 'Moquegua', 'ACTIVO'),
(4, 'Ilo', 'ACTIVO'),
(5, 'Madre de Dios', 'ACTIVO'),
(6, 'Tacna', 'ACTIVO');

-- Tours Registrados
INSERT INTO Tour (idTour, idAgencia, idDestino, slug, nombre, descripcion, precioAdulto, precioNino, precioBebe, duracion, categoria, calificacionPromedio, estado) VALUES
(1, 1, 1, 'city-tour-lima-colonial', 'City Tour Lima Colonial y Catacumbas', 'Recorrido histórico guiado por el Centro Virreinal de Lima, Basílica de San Francisco y catacumbas.', 80.00, 50.00, 0.00, '5 horas', 'Tour', 4.9, 'ACTIVO'),
(2, 1, 2, 'bosque-piedras-huayllay', 'Expedición Bosque de Piedras de Huayllay', 'Caminata entre espectaculares formaciones rocosas naturales a 4,300 msnm en Pasco.', 150.00, 100.00, 0.00, '1 día', 'Experiencia', 4.8, 'ACTIVO'),
(3, 2, 5, 'selva-tambopata-madrededios', 'Inmersión Selva Tambopata Madre de Dios', 'Ecoturismo en la Reserva Nacional Tambopata con avistamiento de fauna y paseo en canoa.', 450.00, 350.00, 0.00, '3 días', 'Tour', 5.0, 'ACTIVO'),
(4, 2, 6, 'fuentes-termales-tacna', 'Ruta Histórica y Fuentes Termales Tacna', 'Circuito patrimonial por Tacna y relajación en los baños termales de Calientes.', 90.00, 60.00, 0.00, '6 horas', 'Tour', 4.7, 'ACTIVO');

-- TourImagenes
INSERT INTO TourImagen (idImagen, idTour, url, esPrincipal, orden) VALUES
(1, 1, '../../img/lima-package.jpg', TRUE, 1),
(2, 2, '../../img/huayllay-package.jpg', TRUE, 1),
(3, 3, '../../img/tambopata-package.jpg', TRUE, 1),
(4, 4, '../../img/tacna-package.jpg', TRUE, 1);

-- Fechas y Cupos (Escenario: 30 cupos totales con 25 ocupados = 5 disponibles -> ENTRO EN OFERTAS por ser <= 20%)
INSERT INTO TourFecha (idTourFecha, idTour, fecha, horaInicio, horaFin, cupoTotal, cupoDisponible, estado) VALUES
(1, 1, '2026-10-15', '08:00:00', '13:00:00', 30, 5, 'DISPONIBLE'), -- 5 de 30 libres (16.6% -> OFERTA REGLA <= 20%)
(2, 2, '2026-10-18', '07:00:00', '17:00:00', 20, 14, 'DISPONIBLE'),
(3, 3, '2026-10-20', '09:00:00', '18:00:00', 15, 5, 'DISPONIBLE'),
(4, 4, '2026-10-22', '08:30:00', '14:30:00', 25, 0, 'CERRADO');

-- Paquetes
INSERT INTO Paquete (idPaquete, idAgencia, nombre, descripcion, precio, descuento, duracion, condiciones, imagenUrl, estado) VALUES
(1, 1, 'Lima Colonial y Sabores Criollos Express', 'Lo mejor de la capital: centros virreinales, templos coloniales, arte bohemio y gastronomía marina.', 550.00, 10.00, '2 días / 1 noche', 'Incluye traslados privados y guiado.', '../../img/lima-package.jpg', 'PUBLICADO'),
(2, 1, 'Expedición Andina y Bosque de Piedras (Pasco)', 'Explora las increíbles figuras de piedra más altas del mundo y navega en la Laguna Punrun.', 720.00, 15.00, '2 días / 1 noche', 'Hospedaje en Cerro de Pasco y traslados incluidos.', '../../img/huayllay-package.jpg', 'PUBLICADO');

-- PaqueteTour
INSERT INTO PaqueteTour (idPaqueteTour, idPaquete, idTour) VALUES
(1, 1, 1),
(2, 2, 2);

-- Notificaciones Semilla
INSERT INTO Notificacion (idNotificacion, idUsuario, rolTarget, titulo, mensaje, leido) VALUES
(1, 2, 'AGENCIA', 'Bienvenida a Travelink', 'Tu cuenta de agencia ANDES TOURS ha sido activada correctamente.', FALSE),
(2, 1, 'ADMINISTRADOR', 'Nueva solicitud de registro', 'La agencia Amazon Expeditions ha registrado una solicitud de incorporación.', FALSE);
