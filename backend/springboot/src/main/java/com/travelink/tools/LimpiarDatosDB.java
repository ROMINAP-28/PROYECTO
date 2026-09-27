package com.travelink.tools;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.Statement;

public class LimpiarDatosDB {

    private static final String HOST = "mysql-traveling-traveling.k.aivencloud.com";
    private static final int PORT = 19936;
    private static final String USER = "avnadmin";
    private static final String PASS = "AVNS_GohPsfjQ1YmM-IL7AoH";

    public static void main(String[] args) {
        String baseUrl = "jdbc:mysql://" + HOST + ":" + PORT + "/?useSSL=true&trustServerCertificate=true&allowPublicKeyRetrieval=true&serverTimezone=UTC&allowMultiQueries=true";

        System.out.println("=== LIMPIANDO TABLAS EN MYSQL AIVEN ===");
        try {
            Class.forName("com.mysql.cj.jdbc.Driver");
            try (Connection conn = DriverManager.getConnection(baseUrl, USER, PASS);
                 Statement stmt = conn.createStatement()) {

                String[] targets = new String[]{"DBTravelink", "travelink"};

                String[] tablesToEmpty = new String[]{
                    "Calificacion",
                    "MetodoPago",
                    "Pasajero",
                    "DetalleReserva",
                    "Reserva",
                    "TourFecha",
                    "Tour",
                    "Agencia",
                    "Usuario",
                    "Persona",
                    "Rol"
                };

                for (String targetDb : targets) {
                    System.out.println("\n------------------------------------------------");
                    System.out.println("Limpiando base de datos: " + targetDb);
                    System.out.println("------------------------------------------------");

                    stmt.execute("USE " + targetDb + ";");
                    stmt.execute("SET FOREIGN_KEY_CHECKS = 0;");

                    for (String table : tablesToEmpty) {
                        try {
                            stmt.execute("TRUNCATE TABLE " + table + ";");
                            System.out.println("  ✓ Tabla vaciada: " + table);
                        } catch (Exception ex) {
                            try {
                                stmt.execute("DELETE FROM " + table + ";");
                                stmt.execute("ALTER TABLE " + table + " AUTO_INCREMENT = 1;");
                                System.out.println("  ✓ Tabla limpiada con DELETE: " + table);
                            } catch (Exception ex2) {
                                System.err.println("  ! Aviso en " + table + ": " + ex2.getMessage());
                            }
                        }
                    }

                    // Re-insertar únicamente los 3 roles fundamentales para permitir registrar nuevos usuarios
                    stmt.execute("INSERT INTO Rol (idRol, nombreRol) VALUES (1, 'Turista'), (2, 'Agencia'), (3, 'Administrador');");
                    System.out.println("  ✓ Roles base listos (1: Turista, 2: Agencia, 3: Administrador)");

                    stmt.execute("SET FOREIGN_KEY_CHECKS = 1;");

                    // Mostrar conteo de registros en cada tabla
                    System.out.println("\nResumen de registros en " + targetDb + ":");
                    for (String table : tablesToEmpty) {
                        try {
                            ResultSet rs = stmt.executeQuery("SELECT COUNT(*) FROM " + table);
                            if (rs.next()) {
                                System.out.println("  - " + table + ": " + rs.getInt(1) + " registros");
                            }
                        } catch (Exception ignore) {}
                    }
                }

                System.out.println("\n=== ¡TODAS LAS TABLAS HAN SIDO VACIADAS CORRECTAMENTE! ===");
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
