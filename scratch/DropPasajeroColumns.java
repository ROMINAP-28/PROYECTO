import java.sql.*;
import com.travelink.config.ConexionDB;

public class DropPasajeroColumns {
    public static void main(String[] args) {
        System.out.println("Altering table Pasajero to drop tipoSeguro and edad columns...");
        try (Connection conn = ConexionDB.getConnection();
             Statement stmt = conn.createStatement()) {
            if (conn == null) {
                System.err.println("Error: connection failed.");
                return;
            }
            try {
                stmt.executeUpdate("ALTER TABLE Pasajero DROP COLUMN tipoSeguro");
                System.out.println("[OK] Column tipoSeguro dropped from Pasajero.");
            } catch (Exception e) {
                System.out.println("[INFO] tipoSeguro column drop: " + e.getMessage());
            }

            try {
                stmt.executeUpdate("ALTER TABLE Pasajero DROP COLUMN edad");
                System.out.println("[OK] Column edad dropped from Pasajero.");
            } catch (Exception e) {
                System.out.println("[INFO] edad column drop: " + e.getMessage());
            }

            System.out.println("Alter table finished!");
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
