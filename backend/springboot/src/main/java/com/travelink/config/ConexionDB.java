package com.travelink.config;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public class ConexionDB {
    private static final String URL = "jdbc:mysql://mysql-traveling-traveling.k.aivencloud.com:19936/DBTravelink?useSSL=true&trustServerCertificate=true&allowPublicKeyRetrieval=true&serverTimezone=UTC";
    private static final String USER = "avnadmin";
    private static final String PASSWORD = "AVNS_GohPsfjQ1YmM-IL7AoH";

    public static Connection getConnection() {

        System.out.println("=== PROBANDO CONEXION A AIVEN ===");
        System.out.println("URL: " + URL);
        System.out.println("USER: " + USER);

        Connection con = null;

        try {
            Class.forName("com.mysql.cj.jdbc.Driver");

            con = DriverManager.getConnection(URL, USER, PASSWORD);

            System.out.println("Conexion exitosa a la base de datos.");

        } catch (ClassNotFoundException e) {
            System.out.println("ERROR: No se encontro el driver MySQL.");
            e.printStackTrace();

        } catch (SQLException e) {
            System.out.println("=== ERROR REAL AIVEN ===");
            e.printStackTrace();
        }

        return con;
    }
}
