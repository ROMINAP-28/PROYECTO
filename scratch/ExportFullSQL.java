import java.sql.*;
import java.util.*;
import java.io.*;
import com.travelink.config.ConexionDB;

public class ExportFullSQL {
    public static void main(String[] args) {
        StringBuilder sb = new StringBuilder();
        sb.append("-- ==========================================================\n");
        sb.append("-- BASE DE DATOS: DBTravelink\n");
        sb.append("-- SCRIPT DE TABLAS Y DATOS COMPLETOS\n");
        sb.append("-- Fecha de generación: ").append(new java.util.Date()).append("\n");
        sb.append("-- ==========================================================\n\n");
        sb.append("USE DBTravelink;\n");
        sb.append("SET FOREIGN_KEY_CHECKS = 0;\n\n");

        try (Connection conn = ConexionDB.getConnection()) {
            if (conn == null) {
                System.err.println("Error de conexión");
                return;
            }

            DatabaseMetaData md = conn.getMetaData();
            ResultSet rsTables = md.getTables(null, null, "%", new String[]{"TABLE"});
            List<String> tables = new ArrayList<>();
            while (rsTables.next()) {
                String t = rsTables.getString("TABLE_NAME");
                if (!t.equalsIgnoreCase("sys_config")) {
                    tables.add(t);
                }
            }
            Collections.sort(tables);

            for (String table : tables) {
                sb.append("-- --------------------------------------------------------\n");
                sb.append("-- Estructura y Datos de la tabla `").append(table).append("`\n");
                sb.append("-- --------------------------------------------------------\n\n");

                // Get CREATE TABLE statement
                try (Statement stmt = conn.createStatement();
                     ResultSet rsCreate = stmt.executeQuery("SHOW CREATE TABLE `" + table + "`")) {
                    if (rsCreate.next()) {
                        String createStmt = rsCreate.getString(2);
                        sb.append("DROP TABLE IF EXISTS `").append(table).append("`;\n");
                        sb.append(createStmt).append(";\n\n");
                    }
                } catch (Exception e) {
                    sb.append("-- Error obteniendo DDL para ").append(table).append(": ").append(e.getMessage()).append("\n\n");
                }

                // Get DATA
                try (Statement stmt = conn.createStatement();
                     ResultSet rsData = stmt.executeQuery("SELECT * FROM `" + table + "`")) {
                    ResultSetMetaData rsmd = rsData.getMetaData();
                    int colCount = rsmd.getColumnCount();

                    List<String> insertRows = new ArrayList<>();
                    while (rsData.next()) {
                        StringBuilder rowSb = new StringBuilder();
                        rowSb.append("(");
                        for (int i = 1; i <= colCount; i++) {
                            Object val = rsData.getObject(i);
                            if (val == null) {
                                rowSb.append("NULL");
                            } else if (val instanceof Number || val instanceof Boolean) {
                                rowSb.append(val.toString());
                            } else if (val instanceof java.sql.Timestamp || val instanceof java.sql.Date || val instanceof java.sql.Time) {
                                rowSb.append("'").append(val.toString()).append("'");
                            } else {
                                String strVal = val.toString().replace("\\", "\\\\").replace("'", "''").replace("\n", "\\n").replace("\r", "\\r");
                                rowSb.append("'").append(strVal).append("'");
                            }
                            if (i < colCount) {
                                rowSb.append(", ");
                            }
                        }
                        rowSb.append(")");
                        insertRows.add(rowSb.toString());
                    }

                    if (!insertRows.isEmpty()) {
                        sb.append("INSERT INTO `").append(table).append("` VALUES\n");
                        for (int i = 0; i < insertRows.size(); i++) {
                            sb.append("  ").append(insertRows.get(i));
                            if (i < insertRows.size() - 1) {
                                sb.append(",\n");
                            } else {
                                sb.append(";\n\n");
                            }
                        }
                    } else {
                        sb.append("-- Tabla `").append(table).append("` no contiene datos actualmente.\n\n");
                    }
                } catch (Exception e) {
                    sb.append("-- Error obteniendo datos para ").append(table).append(": ").append(e.getMessage()).append("\n\n");
                }
            }

            sb.append("SET FOREIGN_KEY_CHECKS = 1;\n");

            File outFile = new File("Agencias_Travelink.sql");
            try (FileWriter fw = new FileWriter(outFile)) {
                fw.write(sb.toString());
            }
            System.out.println("Script SQL exportado exitosamente en Agencias_Travelink.sql! Tamaño: " + outFile.length() + " bytes");

        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
