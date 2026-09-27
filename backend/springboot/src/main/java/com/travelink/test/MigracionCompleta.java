package com.travelink.test;

import com.travelink.config.ConexionDB;
import java.sql.*;

public class MigracionCompleta {
    public static void main(String[] args) {
        try (Connection con = ConexionDB.getConnection();
             Statement st = con.createStatement()) {

            System.out.println("=== APLICANDO MIGRACIONES DE BASE DE DATOS ===");

            // 1. Tabla Reserva: agregar idAgencia y nombreTour
            try {
                st.executeUpdate("ALTER TABLE Reserva ADD COLUMN idAgencia INT DEFAULT 1 AFTER idUsuario");
                System.out.println("Columna Reserva.idAgencia agregada.");
            } catch (SQLException e) {
                System.out.println("Reserva.idAgencia ya existe o aviso: " + e.getMessage());
            }

            try {
                st.executeUpdate("ALTER TABLE Reserva ADD COLUMN nombreTour VARCHAR(150) DEFAULT 'Machu Picchu Clásico' AFTER idAgencia");
                System.out.println("Columna Reserva.nombreTour agregada.");
            } catch (SQLException e) {
                System.out.println("Reserva.nombreTour ya existe o aviso: " + e.getMessage());
            }

            // 2. Tabla MetodoPago: agregar urlComprobante (o url) y numeroOperacion
            try {
                st.executeUpdate("ALTER TABLE MetodoPago ADD COLUMN urlComprobante VARCHAR(255) AFTER numeroOperacion");
                System.out.println("Columna MetodoPago.urlComprobante agregada.");
            } catch (SQLException e) {
                System.out.println("MetodoPago.urlComprobante ya existe o aviso: " + e.getMessage());
            }

            try {
                st.executeUpdate("ALTER TABLE MetodoPago MODIFY COLUMN numeroOperacion VARCHAR(100)");
                System.out.println("Columna MetodoPago.numeroOperacion ajustada.");
            } catch (SQLException e) {
                System.out.println("Aviso numeroOperacion: " + e.getMessage());
            }

            // 3. Tabla Pasajero: asegurar columnas nombre, apellidoPaterno, apellidoMaterno, telefono
            try {
                st.executeUpdate("ALTER TABLE Pasajero ADD COLUMN apellidoPaterno VARCHAR(60)");
            } catch (Exception ignored) {}
            try {
                st.executeUpdate("ALTER TABLE Pasajero ADD COLUMN apellidoMaterno VARCHAR(60)");
            } catch (Exception ignored) {}
            try {
                st.executeUpdate("ALTER TABLE Pasajero ADD COLUMN telefono VARCHAR(20)");
            } catch (Exception ignored) {}

            // 4. Limpiar datos de prueba si se requiere para empezar limpios
            st.executeUpdate("DELETE FROM Pasajero");
            st.executeUpdate("DELETE FROM DetalleReserva");
            st.executeUpdate("DELETE FROM MetodoPago");
            st.executeUpdate("DELETE FROM Calificacion");
            st.executeUpdate("DELETE FROM Reserva");
            System.out.println("Tablas de reservas limpiadas para pruebas frescas.");

            System.out.println("\n=== MIGRACION FINALIZADA CON EXITO ===");

        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
