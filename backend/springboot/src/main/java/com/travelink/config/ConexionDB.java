
package com.travelink.config;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public class ConexionDB {

    private static final String URL =
            "jdbc:mysql://mysql-traveling-traveling.k.aivencloud.com:19936/DBTravelink?useSSL=true&trustServerCertificate=true&allowPublicKeyRetrieval=true&serverTimezone=UTC";

    private static final String USER = "avnadmin";
    private static final String PASSWORD = System.getenv("DB_PASSWORD");

    public static Connection getConnection() {
        System.out.println("=== PROBANDO CONEXION A AIVEN ===");

        if (PASSWORD == null || PASSWORD.isBlank()) {
            System.err.println("ERROR: No se ha configurado DB_PASSWORD.");
            return null;
        }

        try {
            Class.forName("com.mysql.cj.jdbc.Driver");

            Connection con = DriverManager.getConnection(
                    URL, USER, PASSWORD
            );

            System.out.println("Conexion exitosa a la base de datos.");
            return con;

        } catch (ClassNotFoundException e) {
            System.err.println("ERROR: No se encontro el driver MySQL.");
            e.printStackTrace();

        } catch (SQLException e) {
            System.err.println("ERROR DE CONEXION A AIVEN:");
            e.printStackTrace();
        }

        return null;
    }
}