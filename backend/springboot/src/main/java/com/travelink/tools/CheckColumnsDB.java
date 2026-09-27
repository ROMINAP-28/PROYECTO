package com.travelink.tools;

import com.travelink.config.ConexionDB;
import java.sql.*;

public class CheckColumnsDB {
    public static void main(String[] args) {
        try (Connection con = ConexionDB.getConnection()) {
            String[] tables = {"Agencia", "Calificacion", "Comision", "SolicitudAgencia", "HistorialAgencia", "Tour", "Usuario"};
            DatabaseMetaData meta = con.getMetaData();
            for (String t : tables) {
                System.out.println("=== TABLA: " + t + " ===");
                try (ResultSet rs = meta.getColumns(null, null, t, null)) {
                    while (rs.next()) {
                        System.out.println("  - " + rs.getString("COLUMN_NAME") + " (" + rs.getString("TYPE_NAME") + ")");
                    }
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
}
