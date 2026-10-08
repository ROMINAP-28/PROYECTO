package com.travelink.tools;

import com.travelink.config.ConexionDB;
import java.io.FileWriter;
import java.io.PrintWriter;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class RespaldoDB {
    public static void main(String[] args) {
        String backupFile = "respaldo_DBTravelink_20261008.sql";
        System.out.println("Iniciando respaldo completo de DBTravelink a " + backupFile + "...");

        try (Connection con = ConexionDB.getConnection();
             PrintWriter pw = new PrintWriter(new FileWriter(backupFile))) {

            if (con == null) {
                System.err.println("No se pudo conectar a la base de datos.");
                return;
            }

            pw.println("-- RESPALDO DBTravelink -- " + new java.util.Date());
            pw.println("SET FOREIGN_KEY_CHECKS = 0;\n");

            DatabaseMetaData meta = con.getMetaData();
            List<String> tables = new ArrayList<>();
            try (ResultSet rs = meta.getTables(null, null, "%", new String[]{"TABLE"})) {
                while (rs.next()) {
                    tables.add(rs.getString("TABLE_NAME"));
                }
            }

            for (String tableName : tables) {
                System.out.println("Respaldando tabla: " + tableName);
                pw.println("-- TABLA: " + tableName);

                // Export SQL Insert statements
                String query = "SELECT * FROM `" + tableName + "`";
                try (Statement st = con.createStatement(); ResultSet rs = st.executeQuery(query)) {
                    ResultSetMetaData rsMeta = rs.getMetaData();
                    int columnCount = rsMeta.getColumnCount();

                    while (rs.next()) {
                        StringBuilder sb = new StringBuilder();
                        sb.append("INSERT INTO `").append(tableName).append("` VALUES (");

                        for (int i = 1; i <= columnCount; i++) {
                            Object val = rs.getObject(i);
                            if (val == null) {
                                sb.append("NULL");
                            } else if (val instanceof Number) {
                                sb.append(val);
                            } else {
                                String strVal = val.toString().replace("'", "''").replace("\\", "\\\\");
                                sb.append("'").append(strVal).append("'");
                            }
                            if (i < columnCount) sb.append(", ");
                        }
                        sb.append(");");
                        pw.println(sb.toString());
                    }
                }
                pw.println();
            }

            pw.println("SET FOREIGN_KEY_CHECKS = 1;");
            System.out.println("=== RESPALDO COMPLETADO CON ÉXITO ===");

        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
