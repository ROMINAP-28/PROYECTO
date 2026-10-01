import java.sql.*;
import java.util.*;
import com.travelink.config.ConexionDB;

public class DumpDatabase {
    public static void main(String[] args) {
        try (Connection conn = ConexionDB.getConnection()) {
            if (conn == null) {
                System.err.println("Error: No connection to DBTravelink");
                return;
            }
            DatabaseMetaData md = conn.getMetaData();
            ResultSet rs = md.getTables(null, null, "%", new String[]{"TABLE"});
            List<String> tables = new ArrayList<>();
            while (rs.next()) {
                String tableName = rs.getString("TABLE_NAME");
                tables.add(tableName);
            }
            Collections.sort(tables);

            System.out.println("=== TABLAS EN DBTRAVELINK (" + tables.size() + ") ===");
            for (String table : tables) {
                try (Statement stmt = conn.createStatement();
                     ResultSet rCount = stmt.executeQuery("SELECT COUNT(*) FROM `" + table + "`")) {
                    if (rCount.next()) {
                        System.out.println(table + " -> " + rCount.getInt(1) + " registros");
                    }
                } catch (Exception e) {
                    System.out.println(table + " -> Error: " + e.getMessage());
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
