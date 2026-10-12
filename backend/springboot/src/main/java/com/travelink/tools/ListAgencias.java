package com.travelink.tools;

import com.travelink.config.ConexionDB;
import java.sql.*;

public class ListAgencias {
    public static void main(String[] args) {
        try (Connection conn = ConexionDB.getConnection()) {
            if (conn == null) return;
            System.out.println("=== TABLAS EN DBTravelink ===");
            DatabaseMetaData meta = conn.getMetaData();
            try (ResultSet rs = meta.getTables("DBTravelink", null, "%", new String[]{"TABLE"})) {
                while (rs.next()) {
                    System.out.println("Table: " + rs.getString("TABLE_NAME"));
                }
            }

            System.out.println("\n=== RESERVAS ===");
            try (Statement s = conn.createStatement(); ResultSet rs = s.executeQuery("SELECT idReserva, idAgencia, codigoReserva, nombreTour, total, estado FROM Reserva")) {
                while (rs.next()) {
                    System.out.printf("%d | idAg:%d | %s | %s | %s | %s%n", rs.getInt(1), rs.getInt(2), rs.getString(3), rs.getString(4), rs.getString(5), rs.getString(6));
                }
            }
            System.out.println("\n=== PAGOS ===");
            try (Statement s = conn.createStatement(); ResultSet rs = s.executeQuery("SELECT idPago, idReserva, monto, metodoPago, estado FROM Pago")) {
                while (rs.next()) {
                    System.out.printf("%d | idRes:%d | %s | %s | %s%n", rs.getInt(1), rs.getInt(2), rs.getString(3), rs.getString(4), rs.getString(5));
                }
            }
            System.out.println("\n=== CALIFICACION ===");
            try (Statement s = conn.createStatement(); ResultSet rs = s.executeQuery("SELECT idCalificacion, idUsuario, idAgencia, idReserva, estrellas, comentario FROM Calificacion")) {
                while (rs.next()) {
                    System.out.printf("%d | idU:%d | idAg:%d | idRes:%d | %d est | %s%n", rs.getInt(1), rs.getInt(2), rs.getInt(3), rs.getInt(4), rs.getInt(5), rs.getString(6));
                }
            }
            System.out.println("\n=== SOLICITUD AGENCIA ===");
            try (Statement s = conn.createStatement(); ResultSet rs = s.executeQuery("SELECT idSolicitud, idUsuario, razonSocial, ruc, estado FROM SolicitudAgencia")) {
                while (rs.next()) {
                    System.out.printf("%d | idU:%d | %s | %s | %s%n", rs.getInt(1), rs.getInt(2), rs.getString(3), rs.getString(4), rs.getString(5));
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
