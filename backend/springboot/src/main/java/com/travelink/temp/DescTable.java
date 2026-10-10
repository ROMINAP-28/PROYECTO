package com.travelink.temp;

import com.travelink.config.ConexionDB;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.Statement;

public class DescTable {
    public static void main(String[] args) {
        try (Connection con = ConexionDB.getConnection();
             Statement st = con.createStatement();
             ResultSet rs = st.executeQuery("SELECT * FROM PaqueteTuristico LIMIT 1")) {
            ResultSetMetaData rsmd = rs.getMetaData();
            int colCount = rsmd.getColumnCount();
            for (int i = 1; i <= colCount; i++) {
                System.out.println(rsmd.getColumnName(i) + " - " + rsmd.getColumnTypeName(i));
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
