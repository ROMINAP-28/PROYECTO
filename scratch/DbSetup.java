import java.sql.*;
import com.travelink.config.ConexionDB;

public class DbSetup {
    public static void main(String[] args) {
        try (Connection conn = ConexionDB.getConnection();
             Statement stmt = conn.createStatement()) {
            
            System.out.println("Poblando Disponibilidad para Andes Tours (idAgencia=1)...");
            stmt.executeUpdate("DELETE FROM Disponibilidad WHERE idServicio IN (SELECT idServicio FROM ServicioTuristico WHERE idAgencia = 1)");
            
            String sqlInsert = "INSERT INTO Disponibilidad (fecha, horaInicio, horaFin, cupoTotal, cupoDisponible, estado, idServicio) " +
                "SELECT ?, ?, ?, ?, ?, ?, s.idServicio FROM ServicioTuristico s WHERE s.idAgencia = 1 AND s.nombre = ?";
            
            String[][] dispData = {
                {"2026-10-15", "08:00:00", "13:00:00", "20", "16", "DISPONIBLE", "City Tour Lima Colonial y Moderno"},
                {"2026-10-22", "09:00:00", "14:00:00", "20", "20", "DISPONIBLE", "City Tour Lima Colonial y Moderno"},
                {"2026-10-18", "07:00:00", "15:00:00", "15", "12", "DISPONIBLE", "Expedición Bosque de Piedras de Huayllay"},
                {"2026-10-25", "07:00:00", "15:00:00", "15", "0", "CERRADO", "Expedición Bosque de Piedras de Huayllay"},
                {"2026-10-16", "09:00:00", "15:00:00", "18", "14", "DISPONIBLE", "Ruta del Pisco y Campiña de Moquegua"},
                {"2026-10-20", "08:30:00", "12:30:00", "12", "8", "DISPONIBLE", "Aventura Marina en Punta de Coles Ilo"},
                {"2026-10-24", "06:00:00", "18:00:00", "10", "4", "DISPONIBLE", "Inmersión Selva Tambopata Madre de Dios"},
                {"2026-10-17", "08:00:00", "15:00:00", "16", "10", "DISPONIBLE", "Ruta Histórica y Fuentes Termales Tacna"}
            };

            for (String[] d : dispData) {
                PreparedStatement ps = conn.prepareStatement(sqlInsert);
                ps.setDate(1, java.sql.Date.valueOf(d[0]));
                ps.setTime(2, java.sql.Time.valueOf(d[1]));
                ps.setTime(3, java.sql.Time.valueOf(d[2]));
                ps.setInt(4, Integer.parseInt(d[3]));
                ps.setInt(5, Integer.parseInt(d[4]));
                ps.setString(6, d[5]);
                ps.setString(7, d[6]);
                ps.executeUpdate();
            }
            System.out.println("Disponibilidad poblada exitosamente!");
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
