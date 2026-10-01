package com.travelink.tools;

import com.travelink.config.ConexionDB;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.Statement;

public class InsertarReservasPendientes {
    public static void main(String[] args) {
        try (Connection con = ConexionDB.getConnection()) {
            if (con == null) {
                System.out.println("No se pudo conectar a la base de datos");
                return;
            }

            // Insert pending reservation for Ruta del Vino y Pisco en Moquegua
            String sqlRes = "INSERT INTO Reserva (idUsuario, idAgencia, nombreTour, codigoReserva, fechaRegistro, fechaInicio, fechaFin, estado, total) VALUES (?, ?, ?, ?, NOW(), ?, ?, ?, ?)";
            int idRes1 = 0;
            try (PreparedStatement ps = con.prepareStatement(sqlRes, Statement.RETURN_GENERATED_KEYS)) {
                ps.setInt(1, 1);
                ps.setInt(2, 1);
                ps.setString(3, "Ruta del Vino y Campiñas de Moquegua");
                ps.setString(4, "RES-2026-005");
                ps.setString(5, "2026-11-05");
                ps.setString(6, "2026-11-05");
                ps.setString(7, "PENDIENTE");
                ps.setBigDecimal(8, new java.math.BigDecimal("240.00"));
                ps.executeUpdate();
                try (ResultSet rs = ps.getGeneratedKeys()) {
                    if (rs.next()) idRes1 = rs.getInt(1);
                }
            }

            if (idRes1 > 0) {
                String sqlPago = "INSERT INTO Pago (idReserva, monto, metodoPago, fechaPago, estado, numeroOperacion) VALUES (?, ?, ?, NOW(), 'PENDIENTE', ?)";
                try (PreparedStatement ps = con.prepareStatement(sqlPago)) {
                    ps.setInt(1, idRes1);
                    ps.setBigDecimal(2, new java.math.BigDecimal("240.00"));
                    ps.setString(3, "PagoEfectivo / Transferencia");
                    ps.setString(4, "OP-183920");
                    ps.executeUpdate();
                }
                String sqlPas = "INSERT INTO Pasajero (idReserva, nroDocumento, nombre, apellidos, edad, tipoSeguro, esTitular) VALUES (?, '74829103', 'Carlos', 'Mendoza Quispe', 28, 'Básico', 1)";
                try (PreparedStatement ps = con.prepareStatement(sqlPas)) {
                    ps.setInt(1, idRes1);
                    ps.executeUpdate();
                }
                System.out.println("[OK] Reserva 005 insertada con id " + idRes1);
            }

            // Insert another pending reservation for Tacna
            int idRes2 = 0;
            try (PreparedStatement ps = con.prepareStatement(sqlRes, Statement.RETURN_GENERATED_KEYS)) {
                ps.setInt(1, 1);
                ps.setInt(2, 1);
                ps.setString(3, "Ruta Heroica y Aguas Termales de Calientes Tacna");
                ps.setString(4, "RES-2026-006");
                ps.setString(5, "2026-11-12");
                ps.setString(6, "2026-11-12");
                ps.setString(7, "PENDIENTE");
                ps.setBigDecimal(8, new java.math.BigDecimal("380.00"));
                ps.executeUpdate();
                try (ResultSet rs = ps.getGeneratedKeys()) {
                    if (rs.next()) idRes2 = rs.getInt(1);
                }
            }

            if (idRes2 > 0) {
                String sqlPago = "INSERT INTO Pago (idReserva, monto, metodoPago, fechaPago, estado, numeroOperacion) VALUES (?, ?, ?, NOW(), 'PENDIENTE', ?)";
                try (PreparedStatement ps = con.prepareStatement(sqlPago)) {
                    ps.setInt(1, idRes2);
                    ps.setBigDecimal(2, new java.math.BigDecimal("380.00"));
                    ps.setString(3, "Yape / Tarjeta");
                    ps.setString(4, "OP-774912");
                    ps.executeUpdate();
                }
                String sqlPas = "INSERT INTO Pasajero (idReserva, nroDocumento, nombre, apellidos, edad, tipoSeguro, esTitular) VALUES (?, '71928401', 'Lucia', 'Fernandez Ramos', 25, 'Premium', 1)";
                try (PreparedStatement ps = con.prepareStatement(sqlPas)) {
                    ps.setInt(1, idRes2);
                    ps.executeUpdate();
                }
                System.out.println("[OK] Reserva 006 insertada con id " + idRes2);
            }

            System.out.println("Reservas pendientes listas para pruebas!");
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
