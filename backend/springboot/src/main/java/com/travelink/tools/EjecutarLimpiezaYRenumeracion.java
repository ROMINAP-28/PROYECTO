package com.travelink.tools;

import com.travelink.config.ConexionDB;
import java.sql.*;
import java.util.*;

public class EjecutarLimpiezaYRenumeracion {

    public static void main(String[] args) {
        System.out.println("===============================================================");
        System.out.println(" RENUMERACIÓN FINAL EXACTA DE TOUR Y SERVICIO TURISTICO (1..N)");
        System.out.println("===============================================================");

        try (Connection con = ConexionDB.getConnection()) {
            if (con == null) {
                System.err.println("Error de conexión a la base de datos.");
                return;
            }

            Statement st = con.createStatement();
            st.execute("SET FOREIGN_KEY_CHECKS = 0;");

            // 1. Eliminar cualquier ServicioTuristico que no sea un Tour real
            st.execute("DELETE FROM ServicioTuristico WHERE idServicio NOT IN (SELECT idTour FROM Tour)");

            // 2. Mapeo estricto 1..N para Tour y ServicioTuristico
            st.execute("DROP TABLE IF EXISTS tmp_map_tour_serv");
            st.execute("CREATE TABLE tmp_map_tour_serv (old_id INT PRIMARY KEY, new_id INT)");
            st.execute("INSERT INTO tmp_map_tour_serv (old_id, new_id) " +
                       "SELECT idTour, ROW_NUMBER() OVER (ORDER BY idTour) FROM Tour");

            // Actualizar FKs
            st.execute("UPDATE Disponibilidad d JOIN tmp_map_tour_serv m ON d.idServicio = m.old_id SET d.idServicio = m.new_id + 50000");
            st.execute("UPDATE Disponibilidad SET idServicio = idServicio - 50000 WHERE idServicio > 50000");

            st.execute("UPDATE DetallePaquete dp JOIN tmp_map_tour_serv m ON dp.idServicio = m.old_id SET dp.idServicio = m.new_id + 50000");
            st.execute("UPDATE DetallePaquete SET idServicio = idServicio - 50000 WHERE idServicio > 50000");

            st.execute("UPDATE Promocion pr JOIN tmp_map_tour_serv m ON pr.idServicio = m.old_id SET pr.idServicio = m.new_id + 50000");
            st.execute("UPDATE Promocion SET idServicio = idServicio - 50000 WHERE idServicio > 50000");

            // Actualizar PKs
            st.execute("UPDATE Tour t JOIN tmp_map_tour_serv m ON t.idTour = m.old_id SET t.idTour = m.new_id + 50000");
            st.execute("UPDATE Tour SET idTour = idTour - 50000 WHERE idTour > 50000");

            st.execute("UPDATE ServicioTuristico s JOIN tmp_map_tour_serv m ON s.idServicio = m.old_id SET s.idServicio = m.new_id + 50000");
            st.execute("UPDATE ServicioTuristico SET idServicio = idServicio - 50000 WHERE idServicio > 50000");

            st.execute("DROP TABLE IF EXISTS tmp_map_tour_serv");

            resetAutoIncrement(st, "Tour", "idTour");
            resetAutoIncrement(st, "ServicioTuristico", "idServicio");

            st.execute("SET FOREIGN_KEY_CHECKS = 1;");

            System.out.println("\n===============================================================");
            System.out.println(" VERIFICACIÓN FINAL:");
            System.out.println("===============================================================");

            verificarTabla(st, "Persona", "idPersona");
            verificarTabla(st, "Usuario", "idUsuario");
            verificarTabla(st, "Agencia", "idAgencia");
            verificarTabla(st, "Tour", "idTour");
            verificarTabla(st, "ServicioTuristico", "idServicio");
            verificarTabla(st, "PaqueteTuristico", "idPaquete");
            verificarTabla(st, "Reserva", "idReserva");
            verificarTabla(st, "Calificacion", "idCalificacion");

            ResultSet rsDiff = st.executeQuery(
                "SELECT COUNT(*) FROM Tour t LEFT JOIN ServicioTuristico s ON t.idTour = s.idServicio WHERE s.idServicio IS NULL"
            );
            if (rsDiff.next()) {
                System.out.println(">>> Diferencias Tour vs ServicioTuristico: " + rsDiff.getInt(1) + " (Esperado: 0)");
            }

            System.out.println("\n=== ¡RENUMERACIÓN PERFECTA Y COMPLETA 1..N FINALIZADA! ===");

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private static void resetAutoIncrement(Statement st, String tabla, String pkCol) throws SQLException {
        ResultSet rs = st.executeQuery("SELECT COALESCE(MAX(`" + pkCol + "`), 0) + 1 FROM `" + tabla + "`");
        if (rs.next()) {
            int nextId = rs.getInt(1);
            st.execute("ALTER TABLE `" + tabla + "` AUTO_INCREMENT = " + nextId);
        }
    }

    private static void verificarTabla(Statement st, String tabla, String pkCol) throws SQLException {
        ResultSet rs = st.executeQuery("SELECT MIN(`" + pkCol + "`), MAX(`" + pkCol + "`), COUNT(*) FROM `" + tabla + "`");
        if (rs.next()) {
            System.out.printf("  Tabla %-18s -> Min ID: %d | Max ID: %d | Total filas: %d%n",
                tabla, rs.getInt(1), rs.getInt(2), rs.getInt(3));
        }
    }
}
