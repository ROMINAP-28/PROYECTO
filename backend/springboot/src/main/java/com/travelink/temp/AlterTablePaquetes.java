package com.travelink.temp;

import com.travelink.config.ConexionDB;
import java.sql.Connection;
import java.sql.Statement;

public class AlterTablePaquetes {
    public static void main(String[] args) {
        try (Connection con = ConexionDB.getConnection();
             Statement st = con.createStatement()) {
            
            // Adding columns if they don't exist
            String[] queries = {
                "ALTER TABLE PaqueteTuristico ADD COLUMN cupoTotal INT DEFAULT 30",
                "ALTER TABLE PaqueteTuristico ADD COLUMN cuposDisponibles INT DEFAULT 30",
                "ALTER TABLE PaqueteTuristico ADD COLUMN minAsientosOferta INT DEFAULT 5",
                "ALTER TABLE PaqueteTuristico ADD COLUMN servicios TEXT"
            };
            
            for (String sql : queries) {
                try {
                    st.executeUpdate(sql);
                    System.out.println("Ejecutado exitosamente: " + sql);
                } catch (Exception e) {
                    System.out.println("Posible error o la columna ya existe: " + e.getMessage());
                }
            }
            
            // Set dummy services for existing data so they don't look empty
            st.executeUpdate("UPDATE PaqueteTuristico SET servicios = 'City Tour y guiado especializado,Traslado privado' WHERE servicios IS NULL");
            System.out.println("Servicios iniciales actualizados.");
            
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
