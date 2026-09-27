package com.travelink.tools;

import com.travelink.config.ConexionDB;
import java.sql.*;

public class RegistrarAdminDB {
    public static void main(String[] args) {
        System.out.println("=== CONFIGURANDO USUARIO ADMINISTRADOR ===");
        try (Connection con = ConexionDB.getConnection()) {
            if (con == null) {
                System.err.println("No se pudo conectar a la base de datos.");
                return;
            }

            // 1. Asegurar que existe el Rol Administrador (idRol = 3)
            try (Statement st = con.createStatement()) {
                st.execute("INSERT IGNORE INTO Rol (idRol, nombreRol) VALUES (3, 'Administrador')");
            }

            // 2. Verificar o insertar Persona Admin
            int idPersonaAdmin = 0;
            String sqlCheckPersona = "SELECT idPersona FROM Persona WHERE email = 'admin@travelink.pe' OR nroDocumento = '00000000' OR nroDocumento = '00000001'";
            try (PreparedStatement ps = con.prepareStatement(sqlCheckPersona);
                 ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    idPersonaAdmin = rs.getInt("idPersona");
                    System.out.println("Persona Admin ya existe con ID: " + idPersonaAdmin);
                    // Actualizar nombre a Admin
                    String sqlUpdatePersona = "UPDATE Persona SET nombre = 'Admin', apellidoPaterno = 'Admin', apellidoMaterno = 'Sistema', telefono = '999999999' WHERE idPersona = ?";
                    try (PreparedStatement psUp = con.prepareStatement(sqlUpdatePersona)) {
                        psUp.setInt(1, idPersonaAdmin);
                        psUp.executeUpdate();
                    }
                } else {
                    String sqlInsertPersona = "INSERT INTO Persona (nombre, apellidoPaterno, apellidoMaterno, nroDocumento, telefono, email) VALUES ('Admin', 'Admin', 'Sistema', '00000000', '999999999', 'admin@travelink.pe')";
                    try (PreparedStatement psIn = con.prepareStatement(sqlInsertPersona, Statement.RETURN_GENERATED_KEYS)) {
                        psIn.executeUpdate();
                        ResultSet rsKeys = psIn.getGeneratedKeys();
                        if (rsKeys.next()) {
                            idPersonaAdmin = rsKeys.getInt(1);
                            System.out.println("Persona Admin creada con ID: " + idPersonaAdmin);
                        }
                    }
                }
            }

            // 3. Verificar o insertar/actualizar Usuario Admin
            String sqlCheckUser = "SELECT idUsuario FROM Usuario WHERE nombreUsuario = 'admin'";
            try (PreparedStatement ps = con.prepareStatement(sqlCheckUser);
                 ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    int idUsuario = rs.getInt("idUsuario");
                    String sqlUpdateUser = "UPDATE Usuario SET idPersona = ?, idRol = 3, contrasena = '123456', estado = 'ACTIVO' WHERE idUsuario = ?";
                    try (PreparedStatement psUp = con.prepareStatement(sqlUpdateUser)) {
                        psUp.setInt(1, idPersonaAdmin);
                        psUp.setInt(2, idUsuario);
                        psUp.executeUpdate();
                        System.out.println("Usuario 'admin' actualizado con idRol = 3 (Administrador) y contraseña = '123456'");
                    }
                } else {
                    String sqlInsertUser = "INSERT INTO Usuario (idPersona, idRol, nombreUsuario, contrasena, estado) VALUES (?, 3, 'admin', '123456', 'ACTIVO')";
                    try (PreparedStatement psIn = con.prepareStatement(sqlInsertUser)) {
                        psIn.setInt(1, idPersonaAdmin);
                        psIn.executeUpdate();
                        System.out.println("Usuario 'admin' insertado exitosamente con idRol = 3 (Administrador) y contraseña = '123456'");
                    }
                }
            }

            // 4. Mostrar listado de usuarios y roles actuales
            System.out.println("\n=== USUARIOS EN BASE DE DATOS ===");
            String sqlList = "SELECT u.idUsuario, u.nombreUsuario, u.contrasena, u.idRol, r.nombreRol, p.nombre, p.apellidoPaterno, p.email " +
                             "FROM Usuario u " +
                             "LEFT JOIN Rol r ON u.idRol = r.idRol " +
                             "LEFT JOIN Persona p ON u.idPersona = p.idPersona";
            try (Statement st = con.createStatement();
                 ResultSet rs = st.executeQuery(sqlList)) {
                while (rs.next()) {
                    System.out.printf("ID: %d | User: %s | Pass: %s | Rol: %s (id: %d) | Persona: %s %s | Email: %s%n",
                            rs.getInt("idUsuario"),
                            rs.getString("nombreUsuario"),
                            rs.getString("contrasena"),
                            rs.getString("nombreRol"),
                            rs.getInt("idRol"),
                            rs.getString("nombre"),
                            rs.getString("apellidoPaterno"),
                            rs.getString("email"));
                }
            }

        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
}
