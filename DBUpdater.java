import java.nio.file.Files;
import java.nio.file.Paths;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.Statement;

public class DBUpdater {
    public static void main(String[] args) {
        String url = "jdbc:mysql://mysql-traveling-traveling.k.aivencloud.com:19936/DBTravelink?useSSL=true&trustServerCertificate=true&allowPublicKeyRetrieval=true&serverTimezone=UTC&allowMultiQueries=true";
        String user = "avnadmin";
        String password = "AVNS_GohPsfjQ1YmM-IL7AoH";

        try {
            Class.forName("com.mysql.cj.jdbc.Driver");
            try (Connection con = DriverManager.getConnection(url, user, password);
                 Statement stmt = con.createStatement()) {
                
                String sql = new String(Files.readAllBytes(Paths.get("D:/ProyectosU/PROYECTO/Agencias_Travelink.sql")));
                stmt.execute(sql);
                System.out.println("Script ejecutado correctamente.");
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
