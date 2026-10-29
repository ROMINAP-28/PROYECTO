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
            if (idUsuario <= 0) {
                response.put("status", "error");
                response.put("message", "No se pudo identificar al usuario autenticado. Cierra sesión e inicia sesión nuevamente con tu cuenta real.");
                return response;
            }

            // Obtener el tour y la agencia desde la fecha seleccionada en la base de datos.
            int idTourFechaParaReserva = 0;
            int idTourSolicitadoParaReserva = 0;
            try { idTourFechaParaReserva = Integer.parseInt(String.valueOf(reqData.getOrDefault("idTourFecha", "0"))); } catch (Exception ignored) {}
            try { idTourSolicitadoParaReserva = Integer.parseInt(String.valueOf(reqData.getOrDefault("idTour", "0"))); } catch (Exception ignored) {}
            if (idTourFechaParaReserva <= 0) {
                response.put("status", "error");
                response.put("message", "Selecciona una fecha disponible antes de confirmar la reserva.");
                return response;
            }
            String nombreTourSeleccionado = null;
            int idAgenciaSeleccionada = 0;
            int idTourValidado = 0;
            String sqlTourSeleccionado = "SELECT t.idTour, t.nombre, t.idAgencia FROM TourFecha tf INNER JOIN Tour t ON t.idTour = tf.idTour WHERE tf.idTourFecha = ? AND tf.fecha >= CURDATE() AND UPPER(tf.estado) = 'DISPONIBLE' FOR UPDATE";
            try (PreparedStatement psT = con.prepareStatement(sqlTourSeleccionado)) {
                psT.setInt(1, idTourFechaParaReserva);
                try (ResultSet rsT = psT.executeQuery()) {
                    if (rsT.next()) {
                        idTourValidado = rsT.getInt("idTour");
                        nombreTourSeleccionado = rsT.getString("nombre");
                        idAgenciaSeleccionada = rsT.getInt("idAgencia");
                    }
                }
            }
            if (idTourValidado <= 0 || (idTourSolicitadoParaReserva > 0 && idTourSolicitadoParaReserva != idTourValidado)) {
                response.put("status", "error");
                response.put("message", "El tour o la fecha elegida no está disponible o no coincide con la experiencia seleccionada.");
                return response;
            }

            String codigoReserva = String.valueOf(reqData.getOrDefault("codigo", "TRK-" + (100 + (int)(Math.random() * 900))));
            String fechaInicioStr = String.valueOf(reqData.getOrDefault("fechaInicio", reqData.getOrDefault("fechaServicio", new SimpleDateFormat("yyyy-MM-dd").format(new java.util.Date()))));
            String fechaFinStr = String.valueOf(reqData.getOrDefault("fechaFin", fechaInicioStr));
            String tipoPago = String.valueOf(reqData.getOrDefault("metodoPago", "Tarjeta"));
            String estadoReserva = "Efectivo".equalsIgnoreCase(tipoPago) || "Presencial".equalsIgnoreCase(tipoPago) ? "Pendiente" : "Confirmada";

            double total = 1050.00;
            try { total = Double.parseDouble(String.valueOf(reqData.getOrDefault("total", reqData.getOrDefault("precioTotal", "1050.00")))); } catch (Exception ignored) {}

            // 2. Insertar Reserva
            int idReservaGenerada = 0;
            String sqlReserva = "INSERT INTO Reserva (idUsuario, idAgencia, nombreTour, codigoReserva, fechaRegistro, fechaInicio, fechaFin, estado, total) VALUES (?, ?, ?, ?, NOW(), ?, ?, ?, ?)";
            try (PreparedStatement psR = con.prepareStatement(sqlReserva, Statement.RETURN_GENERATED_KEYS)) {
                psR.setInt(1, idUsuario);
                psR.setInt(2, idAgenciaSeleccionada);
                psR.setString(3, nombreTourSeleccionado);
                psR.setString(4, codigoReserva);
                psR.setString(5, fechaInicioStr);
                psR.setString(6, fechaFinStr);
                psR.setString(7, estadoReserva);
                psR.setBigDecimal(8, BigDecimal.valueOf(total));
                psR.executeUpdate();

                try (ResultSet rsR = psR.getGeneratedKeys()) {
                    if (rsR.next()) idReservaGenerada = rsR.getInt(1);
                }
            }

            // 3. Validar la fecha real seleccionada y reservar cupos de forma transaccional.
            int idTourFecha = 0;
            int idTourSolicitado = 0;
            try { idTourFecha = Integer.parseInt(String.valueOf(reqData.getOrDefault("idTourFecha", "0"))); } catch (Exception ignored) {}
            try { idTourSolicitado = Integer.parseInt(String.valueOf(reqData.getOrDefault("idTour", "0"))); } catch (Exception ignored) {}
            if (idTourFecha <= 0) {
                con.rollback();
                response.put("status", "error");
                response.put("message", "Selecciona una fecha disponible para la experiencia antes de confirmar.");
                return response;
            }
            int cantAdultos = 1, cantNinos = 0, cantBebes = 0;
            try { cantAdultos = Integer.parseInt(String.valueOf(reqData.getOrDefault("cantAdultos", "1"))); } catch (Exception ignored) {}
            try { cantNinos = Integer.parseInt(String.valueOf(reqData.getOrDefault("cantNinos", "0"))); } catch (Exception ignored) {}
            try { cantBebes = Integer.parseInt(String.valueOf(reqData.getOrDefault("cantBebes", "0"))); } catch (Exception ignored) {}
            int totalPasajeros = cantAdultos + cantNinos + cantBebes;
            if (cantAdultos < 1 || cantNinos < 0 || cantBebes < 0 || totalPasajeros < 1) {
                con.rollback();
                response.put("status", "error");
                response.put("message", "La cantidad de pasajeros no es válida.");
                return response;
            }
            int disponActual = 0;
            int idTourFechaReal = 0;
            String sqlCheckFecha = "SELECT idTour, cupoDisponible FROM TourFecha WHERE idTourFecha = ? AND fecha >= CURDATE() AND UPPER(estado) = 'DISPONIBLE' FOR UPDATE";
            try (PreparedStatement psCF = con.prepareStatement(sqlCheckFecha)) {
                psCF.setInt(1, idTourFecha);
                try (ResultSet rsCF = psCF.executeQuery()) {
                    if (rsCF.next()) {
                        idTourFechaReal = rsCF.getInt("idTour");
                        disponActual = rsCF.getInt("cupoDisponible");
                    }
                }
            }
            if (idTourFechaReal <= 0 || (idTourSolicitado > 0 && idTourSolicitado != idTourFechaReal)) {
                con.rollback();
                response.put("status", "error");
                response.put("message", "La fecha seleccionada ya no está disponible o no corresponde a esta experiencia. Regresa al detalle y selecciona otra fecha.");
                return response;
            }
            if (disponActual < totalPasajeros) {
                con.rollback();
                response.put("status", "error");
                response.put("message", "Solo quedan " + disponActual + " cupos disponibles para la fecha seleccionada.");
                return response;
            }
            String sqlUpF = "UPDATE TourFecha SET cupoDisponible = ?, estado = ? WHERE idTourFecha = ?";
            try (PreparedStatement psUF = con.prepareStatement(sqlUpF)) {
                int nuevoCupo = disponActual - totalPasajeros;
                psUF.setInt(1, nuevoCupo);
                psUF.setString(2, nuevoCupo == 0 ? "AGOTADO" : "DISPONIBLE");
                psUF.setInt(3, idTourFecha);
                psUF.executeUpdate();
            }
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

            // 4. Insertar titular y pasajeros adicionales que hayan completado sus datos.
            String sqlPasajero = "INSERT INTO Pasajero (idReserva, nroDocumento, nombre, apellidos, esTitular) VALUES (?, ?, ?, ?, ?)";
            try (PreparedStatement psP = con.prepareStatement(sqlPasajero)) {
                String nroDoc = String.valueOf(reqData.getOrDefault("dni", reqData.getOrDefault("nroDocumento", ""))).trim();
                String nombreP = String.valueOf(reqData.getOrDefault("nombre", "")).trim();
                String apellidosP = String.valueOf(reqData.getOrDefault("apellidos", "")).trim();
                if (nombreP.isEmpty() || apellidosP.isEmpty() || nroDoc.isEmpty()) {
                    throw new SQLException("Completa nombre, apellidos y documento del titular.");
                }
                psP.setInt(1, idReservaGenerada); psP.setString(2, nroDoc); psP.setString(3, nombreP); psP.setString(4, apellidosP); psP.setBoolean(5, true);
                psP.executeUpdate();
            }
            for (int i = 2; i <= totalPasajeros; i++) {
                String nombreExtra = String.valueOf(reqData.getOrDefault("pasajero" + i + "Nombre", "")).trim();
                String apellidosExtra = String.valueOf(reqData.getOrDefault("pasajero" + i + "Apellidos", "")).trim();
                String documentoExtra = String.valueOf(reqData.getOrDefault("pasajero" + i + "Dni", "")).trim();
                if (!nombreExtra.isEmpty() && !apellidosExtra.isEmpty() && !documentoExtra.isEmpty()) {
                    try (PreparedStatement psExtra = con.prepareStatement(sqlPasajero)) {
                        psExtra.setInt(1, idReservaGenerada); psExtra.setString(2, documentoExtra); psExtra.setString(3, nombreExtra); psExtra.setString(4, apellidosExtra); psExtra.setBoolean(5, false);
                        psExtra.executeUpdate();
                    }
                }
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

            // 6. Notificaciones Dinámicas (Turista + Agencia)
            try {
                String sqlNotif = "INSERT INTO Notificacion (idUsuario, titulo, mensaje, tipo) VALUES (?, ?, ?, ?)";
                try (PreparedStatement psN = con.prepareStatement(sqlNotif)) {
                    // Notificación para Turista
                    psN.setInt(1, idUsuario);
                    psN.setString(2, "Reserva Realizada");
                    psN.setString(3, "Tu reserva " + codigoReserva + " ha sido procesada correctamente.");
                    psN.setString(4, "EXITO");
                    psN.executeUpdate();

                    // Notificación para Agencia
                    psN.setInt(1, 2); // ID de usuario de agencia por defecto
                    psN.setString(2, "Nueva Reserva Recibida");
                    psN.setString(3, "Reserva " + codigoReserva + " recibida por S/ " + total + " (" + totalPasajeros + " espacios).");
                    psN.setString(4, "INFO");
                    psN.executeUpdate();
                }
            } catch (Exception ignored) {}


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

            if (idUsuario <= 0) {
                response.put("status", "error");
                response.put("message", "No se encontró una cuenta para el correo de la sesión. Cierra sesión e inicia sesión con tu cuenta registrada.");
                response.put("reservas", reservas);
                return response;
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
                        int bebes = rs.getInt("cantBebes");
                        int personasTotales = adultos + ninos + bebes;
                        if (personasTotales < 1) personasTotales = 1;

                        res.put("id", idRes);
                        res.put("codigo", code != null ? code : String.format("TRK-%03d", idRes));
                        res.put("titulo", rs.getString("tourNombre") != null ? rs.getString("tourNombre") : "Experiencia sin detalle");
                        res.put("ubicacion", rs.getString("tourUbicacion") != null ? rs.getString("tourUbicacion") : "Perú");
                        res.put("fechas", fechaFmt);
                        res.put("personas", personasTotales + (personasTotales == 1 ? " persona" : " personas"));
                        res.put("agencia", "Agencia: " + (rs.getString("agenciaNombre") != null ? rs.getString("agenciaNombre") : "Andes Tours"));
                        res.put("estado", rs.getString("estado") != null ? rs.getString("estado") : "Confirmada");
                        res.put("total", rs.getDouble("total"));
                        res.put("metodoPago", rs.getString("tipoPago") != null ? rs.getString("tipoPago") : "Tarjeta");
                        res.put("imagen", "../../img/valle.jpg");
                        Timestamp fReg = rs.getTimestamp("fechaRegistro");
                        res.put("fechaRegistro", fReg != null ? fReg.toString().substring(0, 10) : "");

                        List<Map<String, Object>> pasajeros = new ArrayList<>();
                        String sqlPasajeros = "SELECT nombre, apellidos, nroDocumento, esTitular FROM Pasajero WHERE idReserva = ? ORDER BY esTitular DESC, idPasajero ASC";
                        try (PreparedStatement psPas = con.prepareStatement(sqlPasajeros)) {
                            psPas.setInt(1, idRes);
                            try (ResultSet rsPas = psPas.executeQuery()) {
                                while (rsPas.next()) {
                                    Map<String, Object> pasajero = new HashMap<>();
                                    pasajero.put("nombre", rsPas.getString("nombre"));
                                    pasajero.put("apellidos", rsPas.getString("apellidos"));
                                    pasajero.put("documento", rsPas.getString("nroDocumento"));
                                    pasajero.put("titular", rsPas.getBoolean("esTitular"));
                                    pasajeros.add(pasajero);
                                }
                            }
                        }
                        res.put("pasajeros", pasajeros);
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
