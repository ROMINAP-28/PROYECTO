package com.travelink.test;

import com.travelink.config.ConexionDB;
import java.sql.*;

public class DiagnosticoDB {
    public static void main(String[] args) {
        try (Connection con = ConexionDB.getConnection()) {
            DatabaseMetaData meta = con.getMetaData();
            System.out.println("=== TABLAS EN LA BASE DE DATOS ===");
            try (ResultSet rs = meta.getTables(null, null, "%", new String[]{"TABLE"})) {
                while (rs.next()) {
                    String tableName = rs.getString("TABLE_NAME");
                    System.out.println("\nTABLA: " + tableName);
                    try (ResultSet cols = meta.getColumns(null, null, tableName, "%")) {
                        while (cols.next()) {
                            System.out.println("   - " + cols.getString("COLUMN_NAME") + " (" + cols.getString("TYPE_NAME") + ")");
                        }
                    }
                }
            }

            System.out.println("\n=== REGISTROS ACTUALES EN TABLAS ===");
            printTable(con, "Persona");
            printTable(con, "Usuario");
            printTable(con, "Tour");
            printTable(con, "TourFecha");
            printTable(con, "Reserva");
            printTable(con, "Pasajero");
            printTable(con, "MetodoPago");
            printTable(con, "DetalleReserva");

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private static void printTable(Connection con, String tableName) {
        System.out.println("\n--- DATOS DE " + tableName + " ---");
        try (Statement st = con.createStatement();
             ResultSet rs = st.executeQuery("SELECT * FROM " + tableName)) {
            ResultSetMetaData rsmd = rs.getMetaData();
            int colCount = rsmd.getColumnCount();
            int count = 0;
            while (rs.next()) {
                count++;
                StringBuilder sb = new StringBuilder();
                for (int i = 1; i <= colCount; i++) {
                    sb.append(rsmd.getColumnName(i)).append(": ").append(rs.getString(i)).append(" | ");
                }
                System.out.println(sb.toString());
            }
            if (count == 0) System.out.println("(Tabla vacía)");
        } catch (SQLException e) {
            System.out.println("Error consultando " + tableName + ": " + e.getMessage());
        }
    }
}
