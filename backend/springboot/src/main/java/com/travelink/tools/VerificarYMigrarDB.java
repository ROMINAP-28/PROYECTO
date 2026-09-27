package com.travelink.tools;

import com.travelink.config.ConexionDB;
import java.sql.*;

public class VerificarYMigrarDB {
    public static void main(String[] args) {
        try (Connection con = ConexionDB.getConnection();
             Statement st = con.createStatement()) {

            System.out.println("--- VERIFICANDO Y APLICANDO COLUMNAS EN BASE DE DATOS ---");

            // 1. Tabla Reserva: idAgencia, nombreTour
            try {
                st.executeUpdate("ALTER TABLE Reserva ADD COLUMN idAgencia INT DEFAULT 1 AFTER idUsuario");
                System.out.println("[OK] Reserva.idAgencia agregada");
            } catch (SQLException e) {
                System.out.println("[INFO] Reserva.idAgencia: " + e.getMessage());
            }

            try {
                st.executeUpdate("ALTER TABLE Reserva ADD COLUMN nombreTour VARCHAR(150) DEFAULT 'Machu Picchu Clásico' AFTER idAgencia");
                System.out.println("[OK] Reserva.nombreTour agregada");
            } catch (SQLException e) {
                System.out.println("[INFO] Reserva.nombreTour: " + e.getMessage());
            }

            // 2. Tabla MetodoPago: urlComprobante, url, numeroOperacion
            try {
                st.executeUpdate("ALTER TABLE MetodoPago ADD COLUMN urlComprobante VARCHAR(255) AFTER numeroOperacion");
                System.out.println("[OK] MetodoPago.urlComprobante agregada");
            } catch (SQLException e) {
                System.out.println("[INFO] MetodoPago.urlComprobante: " + e.getMessage());
            }

            try {
                st.executeUpdate("ALTER TABLE MetodoPago ADD COLUMN url VARCHAR(255) AFTER urlComprobante");
                System.out.println("[OK] MetodoPago.url agregada");
            } catch (SQLException e) {
                System.out.println("[INFO] MetodoPago.url: " + e.getMessage());
            }

            try {
                st.executeUpdate("ALTER TABLE MetodoPago MODIFY COLUMN numeroOperacion VARCHAR(100)");
                System.out.println("[OK] MetodoPago.numeroOperacion modificada a VARCHAR(100)");
            } catch (SQLException e) {
                System.out.println("[INFO] MetodoPago.numeroOperacion: " + e.getMessage());
            }

            // 3. Tabla Pasajero: apellidoPaterno, apellidoMaterno, telefono
            try {
                st.executeUpdate("ALTER TABLE Pasajero ADD COLUMN apellidoPaterno VARCHAR(60)");
                System.out.println("[OK] Pasajero.apellidoPaterno agregada");
            } catch (SQLException e) {
                System.out.println("[INFO] Pasajero.apellidoPaterno: " + e.getMessage());
            }

            try {
                st.executeUpdate("ALTER TABLE Pasajero ADD COLUMN apellidoMaterno VARCHAR(60)");
                System.out.println("[OK] Pasajero.apellidoMaterno agregada");
            } catch (SQLException e) {
                System.out.println("[INFO] Pasajero.apellidoMaterno: " + e.getMessage());
            }

            try {
                st.executeUpdate("ALTER TABLE Pasajero ADD COLUMN telefono VARCHAR(20)");
                System.out.println("[OK] Pasajero.telefono agregada");
            } catch (SQLException e) {
                System.out.println("[INFO] Pasajero.telefono: " + e.getMessage());
            }

            // 4. Mostrar estructura actual de las tablas
            mostrarEstructura(con, "Reserva");
            mostrarEstructura(con, "Pasajero");
            mostrarEstructura(con, "MetodoPago");
            mostrarEstructura(con, "Calificacion");

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private static void mostrarEstructura(Connection con, String tabla) throws SQLException {
        System.out.println("\n--- Estructura de tabla: " + tabla + " ---");
        try (Statement st = con.createStatement();
             ResultSet rs = st.executeQuery("DESCRIBE " + tabla)) {
            while (rs.next()) {
                System.out.printf("  %-20s %-20s %-10s\n",
                    rs.getString("Field"),
                    rs.getString("Type"),
                    rs.getString("Null")
                );
            }
        }
    }
}
