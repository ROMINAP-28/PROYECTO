package com.travelink.config;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public class ConexionDB {

    // Credenciales leídas desde variables de entorno con fallbacks configurados
    private static final String DEFAULT_URL =
            "jdbc:mysql://mysql-traveling-traveling.k.aivencloud.com:19936/DBTravelink?useSSL=true&trustServerCertificate=true&allowPublicKeyRetrieval=true&serverTimezone=UTC";
    private static final String DEFAULT_USER = "avnadmin";
    private static final String DEFAULT_PASS = "AVNS_GohPsfjQ1YmM-IL7AoH";

    public static String getUrl() {
        String env = System.getenv("DB_URL");
        return (env != null && !env.isBlank()) ? env : DEFAULT_URL;
    }

    public static String getUser() {
        String env = System.getenv("DB_USER");
        return (env != null && !env.isBlank()) ? env : DEFAULT_USER;
    }

    public static String getPassword() {
        String env = System.getenv("DB_PASSWORD");
        return (env != null && !env.isBlank()) ? env : DEFAULT_PASS;
    }

    public static Connection getConnection() {
        try {
            Class.forName("com.mysql.cj.jdbc.Driver");
            Connection con = DriverManager.getConnection(getUrl(), getUser(), getPassword());
            return con;
        } catch (ClassNotFoundException e) {
            System.err.println("ERROR: No se encontró el driver JDBC de MySQL.");
            e.printStackTrace();
        } catch (SQLException e) {
            System.err.println("ERROR DE CONEXION A LA BASE DE DATOS:");
            e.printStackTrace();
        }
        return null;
    }
}