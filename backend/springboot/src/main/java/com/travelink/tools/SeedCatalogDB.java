package com.travelink.tools;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.Statement;

public class SeedCatalogDB {

    private static final String HOST = "mysql-traveling-traveling.k.aivencloud.com";
    private static final int PORT = 19936;
    private static final String USER = "avnadmin";
    private static final String PASS = "AVNS_GohPsfjQ1YmM-IL7AoH";

    public static void main(String[] args) {
        String baseUrl = "jdbc:mysql://" + HOST + ":" + PORT + "/?useSSL=true&trustServerCertificate=true&allowPublicKeyRetrieval=true&serverTimezone=UTC&allowMultiQueries=true";

        System.out.println("=== INSERTANDO CATÁLOGO BASE DE TOURS Y AGENCIAS EN MYSQL AIVEN ===");
        try {
            Class.forName("com.mysql.cj.jdbc.Driver");
            try (Connection conn = DriverManager.getConnection(baseUrl, USER, PASS);
                 Statement stmt = conn.createStatement()) {

                String[] targets = new String[]{"DBTravelink", "travelink"};

                for (String targetDb : targets) {
                    System.out.println("\nInicializando catálogo en: " + targetDb);
                    stmt.execute("USE " + targetDb + ";");
                    stmt.execute("SET FOREIGN_KEY_CHECKS = 0;");

                    // 1. Asegurar Persona y Usuario para Agencias del sistema
                    stmt.execute("DELETE FROM Usuario WHERE idUsuario IN (2, 3);");
                    stmt.execute("DELETE FROM Persona WHERE idPersona IN (2, 3);");
                    stmt.execute("INSERT IGNORE INTO Persona (idPersona, nombre, apellidoPaterno, apellidoMaterno, nroDocumento, telefono, email) VALUES " +
                                 "(2, 'Carlos', 'Mendoza', 'Paredes', '45892147', '951234876', 'cmendoza@andestours.pe'), " +
                                 "(3, 'Valeria', 'Rios', 'Gomez', '71239845', '963258741', 'contacto@inkatravel.pe');");

                    stmt.execute("INSERT IGNORE INTO Usuario (idUsuario, idPersona, idRol, nombreUsuario, contrasena, estado) VALUES " +
                                 "(2, 2, 2, 'andestours_admin', '$2a$12$e8x5QJtF4qC6jG0P6PZbLeC5uK/bT5Tq9Zg9zQd7G1H0I2J3K4L5M', 'ACTIVO'), " +
                                 "(3, 3, 2, 'inkatravel_admin', '$2a$12$e8x5QJtF4qC6jG0P6PZbLeC5uK/bT5Tq9Zg9zQd7G1H0I2J3K4L5M', 'ACTIVO');");

                    // 2. Agencias
                    stmt.execute("DELETE FROM Agencia;");
                    stmt.execute("INSERT INTO Agencia (idAgencia, idUsuario, razonSocial, nombreComercial, ruc, telefono, email, direccion, descripcion) VALUES " +
                                 "(1, 2, 'ANDES TOURS PERU S.A.C.', 'Andes Tours', '20601234567', '951234876', 'contacto@andestours.pe', 'Av. El Sol 450, Cusco', 'Especialistas en turismo cultural y de aventura.'), " +
                                 "(2, 3, 'INKA TRAVEL EXPERIENCES S.A.C.', 'Inka Travel', '20609876543', '963258741', 'reservas@inkatravel.pe', 'Calle Plateros 320, Cusco', 'Tours personalizados y experiencias únicas.');");

                    // 3. Tours
                    stmt.execute("DELETE FROM Tour;");
                    stmt.execute("INSERT INTO Tour (idTour, idAgencia, slug, nombre, descripcion, precioAdulto, precioNino, precioBebe, duracion, ubicacion, categoria, calificacionPromedio) VALUES " +
                                 "(1, 1, 'machu-picchu', 'Machu Picchu Clásico', 'Descubre la maravilla del mundo.', 350.00, 250.00, 100.00, '1 día', 'Cusco', 'Cultura', 4.8), " +
                                 "(2, 1, '7-colores', 'Montaña de 7 Colores', 'Belleza natural de los Andes.', 280.00, 180.00, 80.00, '1 día', 'Cusco', 'Naturaleza', 4.7), " +
                                 "(3, 2, 'valle-sagrado', 'Valle Sagrado de los Incas', 'Historia y paisajes.', 320.00, 220.00, 90.00, '1 día', 'Cusco', 'Cultura', 4.6), " +
                                 "(4, 1, 'colca', 'Cañón del Colca Full Day', 'Avistamiento del Cóndor y baños termales.', 150.00, 100.00, 50.00, '1 día', 'Arequipa', 'Naturaleza', 4.9);");

                    // 4. TourFecha (Cupos disponibles)
                    stmt.execute("DELETE FROM TourFecha;");
                    stmt.execute("INSERT INTO TourFecha (idTourFecha, idTour, fecha, horaInicio, cupoTotal, cupoDisponible, estado) VALUES " +
                                 "(1, 1, '2026-05-15', '06:00:00', 30, 30, 'DISPONIBLE'), " +
                                 "(2, 2, '2026-05-16', '04:30:00', 25, 25, 'DISPONIBLE'), " +
                                 "(3, 3, '2026-05-18', '07:00:00', 30, 30, 'DISPONIBLE'), " +
                                 "(4, 4, '2026-05-20', '05:00:00', 20, 20, 'DISPONIBLE');");

                    stmt.execute("SET FOREIGN_KEY_CHECKS = 1;");
                    System.out.println("  ✓ Catálogo inicializado con éxito.");
                }

                System.out.println("\n=== ¡CATÁLOGO BASE Y ESTRUCTURA LISTA EN MYSQL AIVEN! ===");
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
