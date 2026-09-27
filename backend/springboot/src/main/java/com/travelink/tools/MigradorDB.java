package com.travelink.tools;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

public class MigradorDB {

    private static final String HOST = "mysql-traveling-traveling.k.aivencloud.com";
    private static final int PORT = 19936;
    private static final String USER = "avnadmin";
    private static final String PASS = "AVNS_GohPsfjQ1YmM-IL7AoH";

    public static void main(String[] args) {
        String baseUrl = "jdbc:mysql://" + HOST + ":" + PORT + "/?useSSL=true&trustServerCertificate=true&allowPublicKeyRetrieval=true&serverTimezone=UTC&allowMultiQueries=true";

        System.out.println("=== INICIANDO CONEXIÓN A MYSQL AIVEN ===");
        try {
            Class.forName("com.mysql.cj.jdbc.Driver");
            try (Connection conn = DriverManager.getConnection(baseUrl, USER, PASS);
                 Statement stmt = conn.createStatement()) {

                System.out.println("Conexión básica establecida exitosamente!");

                // 1. Listar bases de datos existentes
                System.out.println("\n--- BASES DE DATOS EN EL SERVIDOR ---");
                ResultSet rs = stmt.executeQuery("SHOW DATABASES;");
                List<String> databases = new ArrayList<>();
                while (rs.next()) {
                    String db = rs.getString(1);
                    databases.add(db);
                    System.out.println("  - " + db);
                }

                // 2. Crear y usar base de datos DBTravelink y travelink
                stmt.execute("CREATE DATABASE IF NOT EXISTS DBTravelink CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;");
                stmt.execute("CREATE DATABASE IF NOT EXISTS travelink CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;");

                // Ejecutaremos la migración tanto en DBTravelink como en travelink para garantizar compatibilidad total
                String[] targets = new String[]{"DBTravelink", "travelink"};

                File sqlFile = new File("d:/ProyectosU/PresentaciónTravelink/sql/travelink_schema.sql");
                if (!sqlFile.exists()) {
                    sqlFile = new File("sql/travelink_schema.sql");
                }

                List<String> sqlStatements = parseSqlStatements(sqlFile);
                System.out.println("\nTotal de sentencias SQL a ejecutar: " + sqlStatements.size());

                for (String targetDb : targets) {
                    System.out.println("\n========================================================");
                    System.out.println(">>> EJECUTANDO MIGRACIÓN EN LA BASE DE DATOS: " + targetDb);
                    System.out.println("========================================================");
                    
                    stmt.execute("USE " + targetDb + ";");

                    for (int i = 0; i < sqlStatements.size(); i++) {
                        String rawSql = sqlStatements.get(i).trim();
                        if (rawSql.isEmpty()) continue;
                        if (rawSql.toUpperCase().startsWith("CREATE DATABASE") || rawSql.toUpperCase().startsWith("USE ")) {
                            continue; // ya estamos en la base de datos correcta
                        }

                        try {
                            stmt.execute(rawSql);
                            // Resumen corto de la instrucción
                            String preview = rawSql.length() > 60 ? rawSql.substring(0, 60).replace("\n", " ") + "..." : rawSql.replace("\n", " ");
                            System.out.println(" [OK] " + preview);
                        } catch (Exception ex) {
                            System.err.println(" [ERROR] " + ex.getMessage() + " en SQL:\n" + rawSql);
                        }
                    }

                    // Verificar tablas resultantes
                    System.out.println("\n--- TABLAS CREADAS EN " + targetDb + " ---");
                    ResultSet rsTables = stmt.executeQuery("SHOW TABLES;");
                    while (rsTables.next()) {
                        String table = rsTables.getString(1);
                        System.out.println("  ✓ " + table);
                    }
                }

                System.out.println("\n=== ¡MIGRACIÓN COMPLETADA CON ÉXITO EN MYSQL AIVEN! ===");
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private static List<String> parseSqlStatements(File file) {
        List<String> list = new ArrayList<>();
        try (BufferedReader reader = new BufferedReader(new FileReader(file))) {
            StringBuilder sb = new StringBuilder();
            String line;
            while ((line = reader.readLine()) != null) {
                String trimmed = line.trim();
                if (trimmed.startsWith("--") || trimmed.startsWith("/*") || trimmed.isEmpty()) {
                    continue;
                }
                sb.append(line).append("\n");
                if (trimmed.endsWith(";")) {
                    String stmt = sb.toString().trim();
                    if (stmt.endsWith(";")) {
                        stmt = stmt.substring(0, stmt.length() - 1).trim();
                    }
                    if (!stmt.isEmpty()) {
                        list.add(stmt);
                    }
                    sb.setLength(0);
                }
            }
            if (sb.length() > 0) {
                String remaining = sb.toString().trim();
                if (!remaining.isEmpty()) {
                    list.add(remaining);
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return list;
    }
}
