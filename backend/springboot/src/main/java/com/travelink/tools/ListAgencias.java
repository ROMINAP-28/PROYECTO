package com.travelink.tools;

import com.travelink.config.ConexionDB;
import java.sql.*;

public class ListAgencias {
    public static void main(String[] args) {
        try (Connection conn = ConexionDB.getConnection()) {
            if (conn == null) return;
            System.out.println("=== TABLAS EN DBTravelink ===");
            DatabaseMetaData meta = conn.getMetaData();
            try (ResultSet rs = meta.getTables("DBTravelink", null, "%", new String[]{"TABLE"})) {
                while (rs.next()) {
                    System.out.println("Table: " + rs.getString("TABLE_NAME"));
                }
            }

            System.out.println("\n=== DESTINO ===");
            try (Statement s = conn.createStatement(); ResultSet rs = s.executeQuery("SELECT * FROM Destino")) {
                ResultSetMetaData rm = rs.getMetaData();
                for (int i = 1; i <= rm.getColumnCount(); i++) System.out.print(rm.getColumnName(i) + " | ");
                System.out.println();
                while (rs.next()) {
                    System.out.printf("%d | %s | %s%n", rs.getInt(1), rs.getString(2), rs.getString(3));
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
