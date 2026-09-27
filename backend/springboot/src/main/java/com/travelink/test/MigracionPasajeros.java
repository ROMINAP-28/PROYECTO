package com.travelink.test;

import com.travelink.config.ConexionDB;
import java.sql.*;

public class MigracionPasajeros {
    public static void main(String[] args) {
        try (Connection con = ConexionDB.getConnection();
             Statement st = con.createStatement()) {

            System.out.println("Agregando columnas a tabla Pasajero si no existen...");
            try {
                st.executeUpdate("ALTER TABLE Pasajero ADD COLUMN apellidoPaterno VARCHAR(60) AFTER nombre");
                System.out.println("Columna apellidoPaterno agregada.");
            } catch (SQLException e) {
                System.out.println("Columna apellidoPaterno ya existe o error: " + e.getMessage());
            }

            try {
                st.executeUpdate("ALTER TABLE Pasajero ADD COLUMN apellidoMaterno VARCHAR(60) AFTER apellidoPaterno");
                System.out.println("Columna apellidoMaterno agregada.");
            } catch (SQLException e) {
                System.out.println("Columna apellidoMaterno ya existe o error: " + e.getMessage());
            }

            try {
                st.executeUpdate("ALTER TABLE Pasajero ADD COLUMN telefono VARCHAR(20) AFTER nroDocumento");
                System.out.println("Columna telefono agregada.");
            } catch (SQLException e) {
                System.out.println("Columna telefono ya existe o error: " + e.getMessage());
            }

            try {
                st.executeUpdate("ALTER TABLE MetodoPago MODIFY COLUMN numeroOperacion VARCHAR(255)");
                System.out.println("Columna MetodoPago.numeroOperacion ampliada a VARCHAR(255).");
            } catch (SQLException e) {
                System.out.println("Error ampliando numeroOperacion: " + e.getMessage());
            }

            System.out.println("Migración completada con éxito!");
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
