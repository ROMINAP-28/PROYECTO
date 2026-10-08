package com.travelink.tools;

import com.travelink.config.ConexionDB;
import java.sql.*;

public class VerAccesos {
    public static void main(String[] args) {
        System.out.println("=== CREDENCIALES DE ACCESO EN BASE DE DATOS (DBTravelink) ===");
        try (Connection con = ConexionDB.getConnection()) {
            if (con == null) {
                System.err.println("No se pudo conectar a la base de datos.");
                return;
            }

            String sql = "SELECT u.idUsuario, u.idRol, r.nombreRol, u.nombreUsuario, u.contrasena, u.estado, p.email, p.nombre, p.apellidoPaterno, a.idAgencia, a.nombreComercial " +
                         "FROM Usuario u " +
                         "LEFT JOIN Persona p ON u.idPersona = p.idPersona " +
                         "LEFT JOIN Rol r ON u.idRol = r.idRol " +
                         "LEFT JOIN Agencia a ON u.idUsuario = a.idUsuario " +
                         "ORDER BY u.idRol, u.idUsuario";

            try (Statement st = con.createStatement(); ResultSet rs = st.executeQuery(sql)) {
                while (rs.next()) {
                    int idU = rs.getInt("idUsuario");
                    int idR = rs.getInt("idRol");
                    String rol = rs.getString("nombreRol");
                    String user = rs.getString("nombreUsuario");
                    String pass = rs.getString("contrasena");
                    String est = rs.getString("estado");
                    String email = rs.getString("email");
                    String nom = rs.getString("nombre") + " " + (rs.getString("apellidoPaterno") != null ? rs.getString("apellidoPaterno") : "");
                    int idAg = rs.getInt("idAgencia");
                    String nomAg = rs.getString("nombreComercial");

                    System.out.printf("idUsuario: %-2d | Rol: %-12s (idRol=%d) | User: %-20s | Email: %-30s | Pass: %-15s | Agencia: %s%n",
                            idU, (rol != null ? rol : "Rol " + idR), idR, user, (email != null ? email : "-"), pass, (nomAg != null ? nomAg + " (idAgencia=" + idAg + ")" : "-"));
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
