package com.travelink.temp;

import com.travelink.config.ConexionDB;
import java.sql.Connection;
import java.sql.Statement;
import java.util.Random;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.ArrayList;

public class AlterTableGaleria {
    public static void main(String[] args) {
        try (Connection con = ConexionDB.getConnection();
             Statement st = con.createStatement()) {
            
            try {
                st.executeUpdate("ALTER TABLE PaqueteTuristico ADD COLUMN galeria TEXT");
                System.out.println("Columna galeria agregada");
            } catch (Exception e) {}
            
            // Randomize existing data
            String[] imgs = {
                "https://images.unsplash.com/photo-1510812431401-41d2bd2722f3?w=600",
                "https://images.unsplash.com/photo-1526392060635-9d6019884377?w=600",
                "https://images.unsplash.com/photo-1587595431973-160d0d94add1?w=600",
                "https://images.unsplash.com/photo-1590050752117-238cb0fb12b1?w=600"
            };
            
            Random r = new Random();
            
            try (ResultSet rs = st.executeQuery("SELECT idPaquete FROM PaqueteTuristico");
                 PreparedStatement ps = con.prepareStatement("UPDATE PaqueteTuristico SET cupoTotal=?, cuposDisponibles=?, minAsientosOferta=?, galeria=? WHERE idPaquete=?")) {
                
                ArrayList<Integer> ids = new ArrayList<>();
                while (rs.next()) ids.add(rs.getInt(1));
                
                for (int id : ids) {
                    int cupoTotal = 20 + r.nextInt(31); // 20 to 50
                    int minOferta = 2 + r.nextInt(6); // 2 to 7
                    int cuposDisp = minOferta + r.nextInt(cupoTotal - minOferta); // slightly above offer
                    if (r.nextBoolean()) cuposDisp = r.nextInt(minOferta + 1); // sometimes it's an offer
                    
                    String gal = "[\"" + imgs[r.nextInt(imgs.length)] + "\",\"" + imgs[r.nextInt(imgs.length)] + "\",\"" + imgs[r.nextInt(imgs.length)] + "\",\"" + imgs[r.nextInt(imgs.length)] + "\"]";
                    
                    ps.setInt(1, cupoTotal);
                    ps.setInt(2, cuposDisp);
                    ps.setInt(3, minOferta);
                    ps.setString(4, gal);
                    ps.setInt(5, id);
                    ps.executeUpdate();
                }
                System.out.println("Datos aleatorios actualizados");
            }
            
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
