package com.travelink.temp;

import com.travelink.config.ConexionDB;
import java.sql.Connection;
import java.sql.PreparedStatement;

public class UpdateEstado {
    public static void main(String[] args) {
        try (Connection con = ConexionDB.getConnection()) {
            if (con != null) {
                String sql = "UPDATE PaqueteTuristico SET estado = 'PUBLICADO' WHERE estado IS NULL OR TRIM(estado) = '' OR TRIM(estado) = 'BORRADOR'";
                try (PreparedStatement ps = con.prepareStatement(sql)) {
                    int rows = ps.executeUpdate();
                    System.out.println("Filas actualizadas en PaqueteTuristico: " + rows);
                }
            } else {
                System.out.println("No se pudo conectar a la BD");
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
