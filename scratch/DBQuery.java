import java.sql.*;
import com.travelink.config.ConexionDB;

public class DBQuery {
    public static void main(String[] args) throws Exception {
        try (Connection con = ConexionDB.getConnection()) {
            ResultSet rs = con.getMetaData().getColumns(null, null, "Tour", null);
            while (rs.next()) {
                System.out.println(rs.getString("COLUMN_NAME") + " - " + rs.getString("TYPE_NAME"));
            }
        }
    }
}
