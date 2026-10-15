package com.travelink.controlador.admin;

import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import com.travelink.config.ConexionDB;
import com.travelink.persistencia.ReservaPersistencia;
import com.travelink.server.JavaApiServer;

import java.io.IOException;
import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.Map;

public class ReservasAdminHandler implements HttpHandler {
    @Override
    public void handle(HttpExchange exchange) throws IOException {
        JavaApiServer.enableCORS(exchange);
        if ("OPTIONS".equalsIgnoreCase(exchange.getRequestMethod())) {
            exchange.sendResponseHeaders(200, -1);
            return;
        }

        String path = exchange.getRequestURI().getPath();
        String method = exchange.getRequestMethod();

        // 1. GESTION DE ELIMINACION / ACTUALIZACION
        if ("POST".equalsIgnoreCase(method) || "DELETE".equalsIgnoreCase(method)) {
            String body = JavaApiServer.readRequestBody(exchange);
            Map<String, Object> params = JavaApiServer.parseJsonOrFormParams(body);
            Map<String, String> queryParams = JavaApiServer.parseQueryParams(exchange.getRequestURI().getQuery());

            int idReserva = 0;
            try {
                if (params.containsKey("idReserva")) {
                    idReserva = Integer.parseInt(String.valueOf(params.get("idReserva")));
                } else if (params.containsKey("id")) {
                    idReserva = Integer.parseInt(String.valueOf(params.get("id")));
                } else if (queryParams.containsKey("idReserva")) {
                    idReserva = Integer.parseInt(queryParams.get("idReserva"));
                } else if (queryParams.containsKey("id")) {
                    idReserva = Integer.parseInt(queryParams.get("id"));
                }
            } catch (Exception ignored) {}

            String codigo = String.valueOf(params.getOrDefault("codigo", queryParams.getOrDefault("codigo", ""))).trim();
            String accion = String.valueOf(params.getOrDefault("action", params.getOrDefault("accion", ""))).trim();
            String nuevoEstado = String.valueOf(params.getOrDefault("estado", "")).trim();

            if (idReserva <= 0 && !codigo.isEmpty()) {
                try (Connection con = ConexionDB.getConnection();
                     PreparedStatement ps = con.prepareStatement("SELECT idReserva FROM Reserva WHERE codigoReserva = ?")) {
                    ps.setString(1, codigo);
                    try (ResultSet rs = ps.executeQuery()) {
                        if (rs.next()) idReserva = rs.getInt(1);
                    }
                } catch (Exception ignored) {}
            }

            // ACCION: ELIMINAR RESERVA CON DEVOLUCION DE CUPOS Y CASCADA TRANSACCIONAL
            if ("DELETE".equalsIgnoreCase(method) || "eliminar".equalsIgnoreCase(accion) || path.endsWith("/eliminar") || "Eliminada".equalsIgnoreCase(nuevoEstado)) {
                if (idReserva <= 0) {
                    JavaApiServer.sendJsonResponse(exchange, 400, "{\"status\":\"error\",\"message\":\"ID de reserva requerido para eliminar\"}");
                    return;
                }

                Connection con = null;
                try {
                    con = ConexionDB.getConnection();
                    con.setAutoCommit(false);

                    // a. Obtener info de la reserva para devolver cupos si no estaba ya cancelada
                    String estadoActual = "";
                    int idTourFecha = 0;
                    int cantAdultos = 1;
                    int cantNinos = 0;
                    int cantBebes = 0;
                    boolean aceptaBebes = false;
                    BigDecimal precioBebe = BigDecimal.ZERO;
                    String codRes = "";

                    String sqlInfo = """
                        SELECT r.codigoReserva, r.estado, r.idTourFecha,
                               dr.cantAdultos, dr.cantNinos, dr.cantBebes,
                               t.aceptaBebes, t.precioBebe
                        FROM Reserva r
                        LEFT JOIN DetalleReserva dr ON r.idReserva = dr.idReserva AND (dr.tipo IS NULL OR dr.tipo = 'ADULTO')
                        LEFT JOIN TourFecha tf ON r.idTourFecha = tf.idTourFecha
                        LEFT JOIN Tour t ON (r.idTour = t.idTour OR tf.idTour = t.idTour)
                        WHERE r.idReserva = ?
                    """;
                    try (PreparedStatement psI = con.prepareStatement(sqlInfo)) {
                        psI.setInt(1, idReserva);
                        try (ResultSet rsI = psI.executeQuery()) {
                            if (rsI.next()) {
                                codRes = rsI.getString("codigoReserva");
                                estadoActual = rsI.getString("estado");
                                idTourFecha = rsI.getInt("idTourFecha");
                                cantAdultos = rsI.getInt("cantAdultos");
                                if (cantAdultos < 1) cantAdultos = 1;
                                cantNinos = rsI.getInt("cantNinos");
                                cantBebes = rsI.getInt("cantBebes");
                                aceptaBebes = rsI.getBoolean("aceptaBebes");
                                precioBebe = rsI.getBigDecimal("precioBebe");
                                if (precioBebe == null) precioBebe = BigDecimal.ZERO;
                            } else {
                                con.rollback();
                                JavaApiServer.sendJsonResponse(exchange, 404, "{\"status\":\"error\",\"message\":\"Reserva no encontrada\"}");
                                return;
                            }
                        }
                    }

                    // b. Devolver cupos atómicamente si no estaba cancelada
                    if (!"Cancelada".equalsIgnoreCase(estadoActual)) {
                        boolean bebeOcupaCupo = aceptaBebes && (precioBebe.compareTo(BigDecimal.ZERO) > 0);
                        int cuposADevolver = cantAdultos + cantNinos + (bebeOcupaCupo ? cantBebes : 0);
                        if (idTourFecha > 0 && cuposADevolver > 0) {
                            try (PreparedStatement psRest = con.prepareStatement(
                                    "UPDATE TourFecha SET cupoDisponible = cupoDisponible + ?, estado = 'DISPONIBLE' WHERE idTourFecha = ?")) {
                                psRest.setInt(1, cuposADevolver);
                                psRest.setInt(2, idTourFecha);
                                psRest.executeUpdate();
                            }
                        }
                    }

                    // c. Cascada transaccional: eliminar dependencias
                    try (PreparedStatement ps = con.prepareStatement("DELETE FROM Calificacion WHERE idReserva = ?")) {
                        ps.setInt(1, idReserva);
                        ps.executeUpdate();
                    }
                    try (PreparedStatement ps = con.prepareStatement("DELETE FROM Pago WHERE idReserva = ?")) {
                        ps.setInt(1, idReserva);
                        ps.executeUpdate();
                    }
                    try (PreparedStatement ps = con.prepareStatement("DELETE FROM MetodoPago WHERE idReserva = ?")) {
                        ps.setInt(1, idReserva);
                        ps.executeUpdate();
                    }
                    try (PreparedStatement ps = con.prepareStatement("DELETE FROM Comision WHERE idReserva = ?")) {
                        ps.setInt(1, idReserva);
                        ps.executeUpdate();
                    }
                    try (PreparedStatement ps = con.prepareStatement("DELETE FROM Pasajero WHERE idReserva = ?")) {
                        ps.setInt(1, idReserva);
                        ps.executeUpdate();
                    }
                    try (PreparedStatement ps = con.prepareStatement("DELETE FROM DetalleReserva WHERE idReserva = ?")) {
                        ps.setInt(1, idReserva);
                        ps.executeUpdate();
                    }
                    try (PreparedStatement ps = con.prepareStatement("DELETE FROM Reserva WHERE idReserva = ?")) {
                        ps.setInt(1, idReserva);
                        ps.executeUpdate();
                    }

                    con.commit();
                    JavaApiServer.sendJsonResponse(exchange, 200, "{\"status\":\"success\",\"message\":\"Reserva eliminada con éxito y cupos devueltos.\"}");
                    return;
                } catch (Exception e) {
                    if (con != null) {
                        try { con.rollback(); } catch (Exception ignored) {}
                    }
                    e.printStackTrace();
                    JavaApiServer.sendJsonResponse(exchange, 500, "{\"status\":\"error\",\"message\":\"Error al eliminar reserva: " + JavaApiServer.jsonEscape(e.getMessage()) + "\"}");
                    return;
                } finally {
                    if (con != null) {
                        try { con.setAutoCommit(true); con.close(); } catch (Exception ignored) {}
                    }
                }
            }

            // ACCION: CANCELAR O ACTUALIZAR ESTADO
            if (nuevoEstado.isEmpty()) {
                JavaApiServer.sendJsonResponse(exchange, 400, "{\"status\":\"error\",\"message\":\"Estado inválido\"}");
                return;
            }

            if ("Cancelada".equalsIgnoreCase(nuevoEstado)) {
                boolean ok = JavaApiServer.reservaRepositorio.cancelarReserva(idReserva, "Cancelado por el Administrador");
                if (ok) {
                    notificarCambioReserva(idReserva, codigo, "Cancelada");
                    JavaApiServer.sendJsonResponse(exchange, 200, "{\"status\":\"success\",\"message\":\"Estado de reserva actualizado a Cancelada y cupos devueltos\"}");
                } else {
                    JavaApiServer.sendJsonResponse(exchange, 400, "{\"status\":\"error\",\"message\":\"No se pudo cancelar la reserva\"}");
                }
                return;
            }

            String sqlUpdate = "UPDATE Reserva SET estado = ? WHERE idReserva = ? OR (codigoReserva = ? AND ? != '')";
            try (Connection con = ConexionDB.getConnection();
                 PreparedStatement ps = con.prepareStatement(sqlUpdate)) {
                ps.setString(1, nuevoEstado);
                ps.setInt(2, idReserva);
                ps.setString(3, codigo);
                ps.setString(4, codigo);
                int filas = ps.executeUpdate();
                if (filas > 0) {
                    notificarCambioReserva(idReserva, codigo, nuevoEstado);
                    JavaApiServer.sendJsonResponse(exchange, 200, "{\"status\":\"success\",\"message\":\"Estado de reserva actualizado a " + nuevoEstado + "\"}");
                } else {
                    JavaApiServer.sendJsonResponse(exchange, 404, "{\"status\":\"error\",\"message\":\"No se encontró la reserva con ID " + idReserva + " o código " + codigo + "\"}");
                }
            } catch (Exception e) {
                e.printStackTrace();
                JavaApiServer.sendJsonResponse(exchange, 500, "{\"status\":\"error\",\"message\":\"Error al actualizar estado en BD: " + JavaApiServer.jsonEscape(e.getMessage()) + "\"}");
            }
            return;
        }

        // 2. LISTADO COMPLETO CON LEFT JOINS ROBUSTOS
        StringBuilder json = new StringBuilder("{\"status\":\"success\",\"data\":[");
        boolean primero = true;
        String sql = """
            SELECT
                r.idReserva AS id,
                r.codigoReserva AS codigo,
                COALESCE(NULLIF(TRIM(CONCAT(COALESCE(p.nombre, ''), ' ', COALESCE(p.apellidoPaterno, ''))), ''), u.nombreUsuario, 'Turista') AS turista,
                COALESCE(p.email, '') AS email,
                COALESCE(r.nombreTour, t.nombre, 'Tour Turístico') AS tour,
                COALESCE(NULLIF(a.razonSocial, ''), a.nombreComercial, 'Agencia') AS agencia,
                r.fechaInicio AS fechaTour,
                tf.fecha AS fechaSalida,
                TIME_FORMAT(tf.horaInicio, '%H:%i') AS horaSalida,
                CONCAT(DATE_FORMAT(COALESCE(tf.fecha, r.fechaInicio), '%d/%m/%Y'), ' - ', COALESCE(TIME_FORMAT(tf.horaInicio, '%H:%i'), '08:00')) AS fechaHoraSalida,
                COALESCE(dr.cantAdultos, 1) AS cantAdultos,
                COALESCE(dr.cantNinos, 0) AS cantNinos,
                COALESCE(dr.cantBebes, 0) AS cantBebes,
                (COALESCE(dr.cantAdultos, 1) + COALESCE(dr.cantNinos, 0) + COALESCE(dr.cantBebes, 0)) AS cupos,
                t.precioAdulto,
                t.precioNino,
                t.precioBebe,
                r.total,
                r.estado,
                DATE(r.fechaRegistro) AS fechaReserva
            FROM Reserva r
            LEFT JOIN Usuario u ON r.idUsuario = u.idUsuario
            LEFT JOIN Persona p ON u.idPersona = p.idPersona
            LEFT JOIN Agencia a ON r.idAgencia = a.idAgencia
            LEFT JOIN DetalleReserva dr ON r.idReserva = dr.idReserva AND (dr.tipo IS NULL OR dr.tipo = 'ADULTO')
            LEFT JOIN TourFecha tf ON r.idTourFecha = tf.idTourFecha
            LEFT JOIN Tour t ON (r.idTour = t.idTour OR tf.idTour = t.idTour)
            ORDER BY r.idReserva DESC
        """;
        try (Connection con = ConexionDB.getConnection();
             PreparedStatement ps = con.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                if (!primero) json.append(",");
                primero = false;

                BigDecimal total = rs.getBigDecimal("total");
                if (total == null) total = BigDecimal.ZERO;
                BigDecimal comision = total.multiply(new BigDecimal("0.15")).setScale(2, java.math.RoundingMode.HALF_UP);

                int adultos = rs.getInt("cantAdultos");
                if (adultos < 1) adultos = 1;
                int ninos = rs.getInt("cantNinos");
                int bebes = rs.getInt("cantBebes");
                BigDecimal pAdulto = rs.getBigDecimal("precioAdulto");
                BigDecimal pNino = rs.getBigDecimal("precioNino");
                BigDecimal pBebe = rs.getBigDecimal("precioBebe");
                String desglose = ReservaPersistencia.formatDesglose(adultos, pAdulto, ninos, pNino, bebes, pBebe);

                json.append("{")
                    .append("\"id\":").append(rs.getInt("id")).append(",")
                    .append("\"codigo\":\"").append(JavaApiServer.jsonEscape(rs.getString("codigo"))).append("\",")
                    .append("\"turista\":\"").append(JavaApiServer.jsonEscape(rs.getString("turista"))).append("\",")
                    .append("\"email\":\"").append(JavaApiServer.jsonEscape(rs.getString("email"))).append("\",")
                    .append("\"tour\":\"").append(JavaApiServer.jsonEscape(rs.getString("tour"))).append("\",")
                    .append("\"agencia\":\"").append(JavaApiServer.jsonEscape(rs.getString("agencia"))).append("\",")
                    .append("\"fechaTour\":\"").append(rs.getString("fechaTour")).append("\",")
                    .append("\"fechaSalida\":\"").append(rs.getString("fechaSalida") != null ? rs.getString("fechaSalida") : rs.getString("fechaTour")).append("\",")
                    .append("\"horaSalida\":\"").append(rs.getString("horaSalida") != null ? rs.getString("horaSalida") : "08:00").append("\",")
                    .append("\"fechaHoraSalida\":\"").append(JavaApiServer.jsonEscape(rs.getString("fechaHoraSalida"))).append("\",")
                    .append("\"cupos\":").append(rs.getInt("cupos")).append(",")
                    .append("\"cantAdultos\":").append(adultos).append(",")
                    .append("\"cantNinos\":").append(ninos).append(",")
                    .append("\"cantBebes\":").append(bebes).append(",")
                    .append("\"desglose\":\"").append(JavaApiServer.jsonEscape(desglose)).append("\",")
                    .append("\"total\":").append(total.toPlainString()).append(",")
                    .append("\"comision\":").append(comision.toPlainString()).append(",")
                    .append("\"estado\":\"").append(JavaApiServer.jsonEscape(rs.getString("estado"))).append("\",")
                    .append("\"fechaReserva\":\"").append(rs.getString("fechaReserva")).append("\"")
                    .append("}");
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        json.append("]}");
        JavaApiServer.sendJsonResponse(exchange, 200, json.toString());
    }

    private void notificarCambioReserva(int idReserva, String codigo, String nuevoEstado) {
        try (Connection con = ConexionDB.getConnection()) {
            int idUsuario = 0;
            String cod = codigo;
            String tour = "Tour";

            String sqlSel = "SELECT idUsuario, codigoReserva, COALESCE(nombreTour, 'Tour Turístico') AS tour FROM Reserva WHERE idReserva = ? OR codigoReserva = ?";
            try (PreparedStatement ps = con.prepareStatement(sqlSel)) {
                ps.setInt(1, idReserva);
                ps.setString(2, codigo != null ? codigo : "");
                try (ResultSet rs = ps.executeQuery()) {
                    if (rs.next()) {
                        idUsuario = rs.getInt("idUsuario");
                        if (cod == null || cod.isEmpty()) cod = rs.getString("codigoReserva");
                        tour = rs.getString("tour");
                    }
                }
            }

            if (idUsuario > 0) {
                boolean confirmada = "Confirmada".equalsIgnoreCase(nuevoEstado);
                String titulo = confirmada ? "Reserva Confirmada" : "Reserva Cancelada";
                String mensaje = confirmada
                    ? "Tu reserva " + cod + " para " + tour + " ha sido confirmada con éxito."
                    : "Tu reserva " + cod + " para " + tour + " ha sido cancelada por el administrador.";
                String tipo = confirmada ? "EXITO" : "ALERTA";
                String icono = confirmada ? "ti ti-circle-check" : "ti ti-circle-x";
                String color = confirmada ? "green" : "red";

                try (PreparedStatement psNotif = con.prepareStatement(
                        "INSERT INTO Notificacion (idUsuario, titulo, mensaje, tipo, icono, color, leida, fechaEnvio) VALUES (?, ?, ?, ?, ?, ?, 0, NOW())")) {
                    psNotif.setInt(1, idUsuario);
                    psNotif.setString(2, titulo);
                    psNotif.setString(3, mensaje);
                    psNotif.setString(4, tipo);
                    psNotif.setString(5, icono);
                    psNotif.setString(6, color);
                    psNotif.executeUpdate();
                }
            }
        } catch (Exception ignored) {}
    }
}
