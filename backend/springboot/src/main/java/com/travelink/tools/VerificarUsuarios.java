package com.travelink.tools;

import com.travelink.config.ConexionDB;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.Statement;

public class VerificarUsuarios {
    public static void main(String[] args) {
        try (Connection con = ConexionDB.getConnection();
             Statement stmt = con.createStatement()) {
            
            System.out.println("=== USUARIOS Y PERSONAS EN MYSQL AIVEN ===");
            ResultSet rs = stmt.executeQuery(
                "SELECT u.idUsuario, u.nombreUsuario, p.nombre, p.apellidoPaterno, p.email, p.nroDocumento, u.fechaRegistro " +
                "FROM Usuario u INNER JOIN Persona p ON u.idPersona = p.idPersona"
            );
            
            int count = 0;
            while (rs.next()) {
                count++;
                System.out.println("  [" + count + "] ID: " + rs.getInt("idUsuario") + 
                                   " | Usuario: " + rs.getString("nombreUsuario") + 
                                   " | Nombre: " + rs.getString("nombre") + " " + rs.getString("apellidoPaterno") + 
                                   " | DNI: " + rs.getString("nroDocumento") + 
                                   " | Email: " + rs.getString("email"));
            }
            if (count == 0) {
                System.out.println("No hay usuarios registrados aún.");
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
