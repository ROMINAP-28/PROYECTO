package com.travelink.server;
import java.sql.*;
import com.travelink.config.ConexionDB;

public class QueryDestinos {
    public static void main(String[] args) {
        try (Connection con = ConexionDB.getConnection()) {
            System.out.println("=== DESTINOS Y SUS TOURS ===");
            String sql = "SELECT d.idDestino, d.nombre, d.descripcion, d.estado, " +
                         "(SELECT COUNT(*) FROM Tour t WHERE t.idDestino = d.idDestino AND UPPER(t.estado) = 'ACTIVO') AS toursActivos " +
                         "FROM Destino d ORDER BY d.idDestino ASC";
            try (Statement st = con.createStatement();
                 ResultSet rs = st.executeQuery(sql)) {
                while (rs.next()) {
                    System.out.println("ID: " + rs.getInt("idDestino") + 
                                       " | Nombre: " + rs.getString("nombre") + 
                                       " | Estado: " + rs.getString("estado") + 
                                       " | Tours Activos: " + rs.getInt("toursActivos"));
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
