package com.travelink.persistencia;

import com.travelink.config.ConexionDB;
import com.travelink.entidades.DetalleReserva;
import com.travelink.entidades.MetodoPago;
import com.travelink.entidades.Pasajero;
import com.travelink.entidades.Reserva;

import java.math.BigDecimal;
import java.sql.*;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

public class ReservaPersistencia {

    public Map<String, Object> guardarReservaCompleta(Map<String, Object> reqData) {
        Map<String, Object> response = new HashMap<>();
        Connection con = null;

        try {
            con = ConexionDB.getConnection();
            con.setAutoCommit(false);

            String correo = String.valueOf(reqData.getOrDefault("email", reqData.getOrDefault("correo", "")));
            int idUsuario = 0;
            try { idUsuario = Integer.parseInt(String.valueOf(reqData.get("idUsuario"))); } catch (Exception ignored) {}

            // 1. Obtener idUsuario por correo si no vino explícito
            if (idUsuario <= 0 && !correo.trim().isEmpty()) {
                String sqlU = "SELECT u.idUsuario FROM Usuario u INNER JOIN Persona p ON u.idPersona = p.idPersona WHERE p.email = ? OR u.nombreUsuario = ?";
                try (PreparedStatement psU = con.prepareStatement(sqlU)) {
                    psU.setString(1, correo);
                    psU.setString(2, correo);
                    try (ResultSet rsU = psU.executeQuery()) {
                        if (rsU.next()) idUsuario = rsU.getInt(1);
                    }
                }
            }
            if (idUsuario <= 0) idUsuario = 1;

            String codigoReserva = String.valueOf(reqData.getOrDefault("codigo", "TRK-" + (100 + (int)(Math.random() * 900))));
            String fechaInicioStr = String.valueOf(reqData.getOrDefault("fechaInicio", reqData.getOrDefault("fechaServicio", new SimpleDateFormat("yyyy-MM-dd").format(new java.util.Date()))));
            String fechaFinStr = String.valueOf(reqData.getOrDefault("fechaFin", fechaInicioStr));
            String tipoPago = String.valueOf(reqData.getOrDefault("metodoPago", "Tarjeta"));
            String estadoReserva = "Efectivo".equalsIgnoreCase(tipoPago) || "Presencial".equalsIgnoreCase(tipoPago) ? "Pendiente" : "Confirmada";

            double total = 1050.00;
            try { total = Double.parseDouble(String.valueOf(reqData.getOrDefault("total", reqData.getOrDefault("precioTotal", "1050.00")))); } catch (Exception ignored) {}

            // 2. Insertar Reserva
            int idReservaGenerada = 0;
            String sqlReserva = "INSERT INTO Reserva (idUsuario, codigoReserva, fechaRegistro, fechaInicio, fechaFin, estado, total) VALUES (?, ?, NOW(), ?, ?, ?, ?)";
            try (PreparedStatement psR = con.prepareStatement(sqlReserva, Statement.RETURN_GENERATED_KEYS)) {
                psR.setInt(1, idUsuario);
                psR.setString(2, codigoReserva);
                psR.setString(3, fechaInicioStr);
                psR.setString(4, fechaFinStr);
                psR.setString(5, estadoReserva);
                psR.setBigDecimal(6, BigDecimal.valueOf(total));
                psR.executeUpdate();

                try (ResultSet rsR = psR.getGeneratedKeys()) {
                    if (rsR.next()) idReservaGenerada = rsR.getInt(1);
                }
            }

            // 3. Insertar DetalleReserva
            int idTourFecha = 1; // default primer cupo disponible
            int cantAdultos = 2;
            int cantNinos = 1;
            int cantBebes = 0;
            try { cantAdultos = Integer.parseInt(String.valueOf(reqData.getOrDefault("cantAdultos", reqData.getOrDefault("adultos", "2")))); } catch (Exception ignored) {}
            try { cantNinos = Integer.parseInt(String.valueOf(reqData.getOrDefault("cantNinos", reqData.getOrDefault("ninos", "1")))); } catch (Exception ignored) {}
            try { cantBebes = Integer.parseInt(String.valueOf(reqData.getOrDefault("cantBebes", reqData.getOrDefault("bebes", "0")))); } catch (Exception ignored) {}

            String sqlDetalle = "INSERT INTO DetalleReserva (idReserva, idTourFecha, cantAdultos, cantNinos, cantBebes, subtotal) VALUES (?, ?, ?, ?, ?, ?)";
            try (PreparedStatement psD = con.prepareStatement(sqlDetalle)) {
                psD.setInt(1, idReservaGenerada);
                psD.setInt(2, idTourFecha);
                psD.setInt(3, cantAdultos);
                psD.setInt(4, cantNinos);
                psD.setInt(5, cantBebes);
                psD.setBigDecimal(6, BigDecimal.valueOf(total));
                psD.executeUpdate();
            }

            // 4. Insertar Pasajero Titular
            String sqlPasajero = "INSERT INTO Pasajero (idReserva, nroDocumento, nombre, apellidos, edad, tipoSeguro, esTitular) VALUES (?, ?, ?, ?, ?, ?, ?)";
            try (PreparedStatement psP = con.prepareStatement(sqlPasajero)) {
                String nroDoc = String.valueOf(reqData.getOrDefault("dni", reqData.getOrDefault("nroDocumento", "74829103")));
                String nombreP = String.valueOf(reqData.getOrDefault("nombre", "Ana"));
                String apellidosP = String.valueOf(reqData.getOrDefault("apellidos", "Garcia"));
                int edad = 25;
                try { edad = Integer.parseInt(String.valueOf(reqData.getOrDefault("edad", "25"))); } catch (Exception ignored) {}
                String seguro = String.valueOf(reqData.getOrDefault("seguro", reqData.getOrDefault("tipoSeguro", "SIS")));

                psP.setInt(1, idReservaGenerada);
                psP.setString(2, nroDoc);
                psP.setString(3, nombreP);
                psP.setString(4, apellidosP);
                psP.setInt(5, edad);
                psP.setString(6, seguro);
                psP.setBoolean(7, true);
                psP.executeUpdate();
            }

            // 5. Insertar MetodoPago
            int idPagoGenerado = 0;
            boolean esAdelanto = "Efectivo".equalsIgnoreCase(tipoPago) || "Presencial".equalsIgnoreCase(tipoPago);
            String estadoPago = esAdelanto ? "Pendiente" : "Completado";

            String sqlPago = "INSERT INTO MetodoPago (idReserva, tipoPago, monto, esAdelanto, estadoPago, numeroOperacion, fechaPago) VALUES (?, ?, ?, ?, ?, ?, NOW())";
            try (PreparedStatement psM = con.prepareStatement(sqlPago, Statement.RETURN_GENERATED_KEYS)) {
                psM.setInt(1, idReservaGenerada);
                psM.setString(2, tipoPago);
                psM.setBigDecimal(3, BigDecimal.valueOf(total));
                psM.setBoolean(4, esAdelanto);
                psM.setString(5, estadoPago);
                psM.setString(6, codigoReserva);
                psM.executeUpdate();

                try (ResultSet rsM = psM.getGeneratedKeys()) {
                    if (rsM.next()) idPagoGenerado = rsM.getInt(1);
                }
            }

            con.commit();

            Map<String, Object> data = new HashMap<>();
            data.put("idReserva", idReservaGenerada);
            data.put("idPago", idPagoGenerado);
            data.put("codigo", codigoReserva);
            data.put("estado", estadoReserva);
            data.put("total", total);
            data.put("metodoPago", tipoPago);

            response.put("status", "success");
            response.put("message", "Reserva guardada exitosamente");
            response.put("data", data);
            return response;

        } catch (Exception e) {
            if (con != null) {
                try { con.rollback(); } catch (SQLException ex) { ex.printStackTrace(); }
            }
            e.printStackTrace();
            response.put("status", "error");
            response.put("message", "Error al guardar reserva: " + e.getMessage());
            return response;
        } finally {
            if (con != null) {
                try { con.setAutoCommit(true); con.close(); } catch (SQLException e) { e.printStackTrace(); }
            }
        }
    }

    public Map<String, Object> obtenerReservasPorUsuario(String correo, int idUsuario) {
        Map<String, Object> response = new HashMap<>();
        List<Map<String, Object>> reservas = new ArrayList<>();

        try (Connection con = ConexionDB.getConnection()) {
            if (idUsuario <= 0 && correo != null && !correo.trim().isEmpty()) {
                String sqlU = "SELECT u.idUsuario FROM Usuario u INNER JOIN Persona p ON u.idPersona = p.idPersona WHERE p.email = ? OR u.nombreUsuario = ?";
                try (PreparedStatement psU = con.prepareStatement(sqlU)) {
                    psU.setString(1, correo);
                    psU.setString(2, correo);
                    try (ResultSet rsU = psU.executeQuery()) {
                        if (rsU.next()) idUsuario = rsU.getInt(1);
                    }
                }
            }

            String query = "SELECT r.idReserva, r.codigoReserva, r.fechaRegistro, r.fechaInicio, r.fechaFin, r.estado, r.total, " +
                           "t.nombre AS tourNombre, t.ubicacion AS tourUbicacion, a.nombreComercial AS agenciaNombre, " +
                           "mp.tipoPago, mp.estadoPago, d.cantAdultos, d.cantNinos, d.cantBebes " +
                           "FROM Reserva r " +
                           "LEFT JOIN DetalleReserva d ON r.idReserva = d.idReserva " +
                           "LEFT JOIN TourFecha tf ON d.idTourFecha = tf.idTourFecha " +
                           "LEFT JOIN Tour t ON tf.idTour = t.idTour " +
                           "LEFT JOIN Agencia a ON t.idAgencia = a.idAgencia " +
                           "LEFT JOIN MetodoPago mp ON r.idReserva = mp.idReserva ";

            if (idUsuario > 0) {
                query += "WHERE r.idUsuario = ? ";
            }
            query += "ORDER BY r.idReserva DESC";

            try (PreparedStatement ps = con.prepareStatement(query)) {
                if (idUsuario > 0) ps.setInt(1, idUsuario);
                try (ResultSet rs = ps.executeQuery()) {
                    SimpleDateFormat sdf = new SimpleDateFormat("dd MMM. yyyy");
                    while (rs.next()) {
                        Map<String, Object> res = new HashMap<>();
                        int idRes = rs.getInt("idReserva");
                        String code = rs.getString("codigoReserva");
                        Date fInicio = rs.getDate("fechaInicio");
                        Date fFin = rs.getDate("fechaFin");
                        String fechaFmt = fInicio != null ? sdf.format(fInicio) + (fFin != null && !fFin.equals(fInicio) ? " - " + sdf.format(fFin) : "") : "Próximamente";

                        int adultos = rs.getInt("cantAdultos");
                        int ninos = rs.getInt("cantNinos");
                        int personasTotales = adultos + ninos;
                        if (personasTotales < 1) personasTotales = 1;

                        res.put("id", idRes);
                        res.put("codigo", code != null ? code : String.format("TRK-%03d", idRes));
                        res.put("titulo", rs.getString("tourNombre") != null ? rs.getString("tourNombre") : "Machu Picchu Clásico");
                        res.put("ubicacion", rs.getString("tourUbicacion") != null ? rs.getString("tourUbicacion") : "Cusco, Perú");
                        res.put("fechas", fechaFmt);
                        res.put("personas", personasTotales + (personasTotales == 1 ? " persona" : " personas"));
                        res.put("agencia", "Agencia: " + (rs.getString("agenciaNombre") != null ? rs.getString("agenciaNombre") : "Andes Tours"));
                        res.put("estado", rs.getString("estado") != null ? rs.getString("estado") : "Confirmada");
                        res.put("total", rs.getDouble("total"));
                        res.put("metodoPago", rs.getString("tipoPago") != null ? rs.getString("tipoPago") : "Tarjeta");
                        res.put("imagen", "../../img/valle.jpg");
                        Timestamp fReg = rs.getTimestamp("fechaRegistro");
                        res.put("fechaRegistro", fReg != null ? fReg.toString().substring(0, 10) : "");

                        reservas.add(res);
                    }
                }
            }

            response.put("status", "success");
            response.put("reservas", reservas);
            return response;
        } catch (Exception e) {
            e.printStackTrace();
            response.put("status", "error");
            response.put("message", "Error al obtener reservas: " + e.getMessage());
            return response;
        }
    }

    public boolean cancelarReserva(int idReserva, String motivo) {
        String sql = "UPDATE Reserva SET estado = 'Cancelada', motivoCancelacion = ? WHERE idReserva = ?";
        try (Connection con = ConexionDB.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setString(1, motivo != null ? motivo : "Cancelado por el usuario");
            ps.setInt(2, idReserva);
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }
}
