package scratch;
import java.sql.*;
import com.travelink.config.ConexionDB;

public class DBUpdater {
    public static void main(String[] args) {
        try (Connection con = ConexionDB.getConnection();
             Statement stmt = con.createStatement()) {
            
            // 1. Drop foreign key to make idTourFecha nullable
            try {
                stmt.execute("ALTER TABLE DetalleReserva DROP FOREIGN KEY fk_detalle_tourfecha");
            } catch(Exception e) { System.out.println("FK might not exist or already dropped"); }
            
            // 2. Modify idTourFecha to be NULLABLE
            stmt.execute("ALTER TABLE DetalleReserva MODIFY idTourFecha INT NULL");
            
            // 3. Add idPaquete column if it doesn't exist
            try {
                stmt.execute("ALTER TABLE DetalleReserva ADD idPaquete INT NULL");
                stmt.execute("ALTER TABLE DetalleReserva ADD CONSTRAINT fk_detalle_paquete FOREIGN KEY (idPaquete) REFERENCES Paquete(idPaquete) ON DELETE RESTRICT ON UPDATE CASCADE");
            } catch(Exception e) { System.out.println("idPaquete might already exist: " + e.getMessage()); }
            
            // 4. Re-add fk_detalle_tourfecha
            try {
                stmt.execute("ALTER TABLE DetalleReserva ADD CONSTRAINT fk_detalle_tourfecha FOREIGN KEY (idTourFecha) REFERENCES TourFecha(idTourFecha) ON DELETE RESTRICT ON UPDATE CASCADE");
            } catch(Exception e) {}
            
            System.out.println("Database updated successfully!");
        } catch(Exception e) {
            e.printStackTrace();
        }
    }
}
