package com.travelink.controlador.agencia;

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

public class ReservasAgenciaHandler implements HttpHandler {

    @Override
    public void handle(HttpExchange exchange) throws IOException {
        if ("OPTIONS".equalsIgnoreCase(exchange.getRequestMethod())) {
            JavaApiServer.enableCORS(exchange);
            exchange.sendResponseHeaders(200, -1);
            return;
        }

        if ("POST".equalsIgnoreCase(exchange.getRequestMethod())) {
            procesarPostReserva(exchange);
            return;
        }

        if (!"GET".equalsIgnoreCase(exchange.getRequestMethod())) {
            JavaApiServer.sendJsonResponse(exchange, 405, "{\"status\":\"error\",\"message\":\"Método no permitido\"}");
            return;
        }

        int idAgencia = 0;
        Object sessAgencia = exchange.getAttribute("ID_AGENCIA");
        if (sessAgencia instanceof Integer) {
            idAgencia = (Integer) sessAgencia;
        }
        if (idAgencia <= 0) {
            Map<String, String> params = JavaApiServer.parseQueryParams(exchange.getRequestURI().getQuery());
            try {
                if (params.containsKey("idAgencia")) {
                    idAgencia = Integer.parseInt(params.get("idAgencia"));
                }
            } catch (Exception ignored) {}
        }
        if (idAgencia <= 0) idAgencia = 1;

        String sql = """
        SELECT
            r.idReserva,
            r.codigoReserva,
            r.idUsuario,
            r.idAgencia,
            COALESCE(r.nombreTour, 'Tour Turístico') AS nombreTour,
            COALESCE(DATE_FORMAT(r.fechaRegistro, '%d/%m/%Y'), r.fechaRegistro) AS fechaRegistro,
            COALESCE(DATE_FORMAT(r.fechaInicio, '%d/%m/%Y'), r.fechaInicio) AS fechaInicio,
            COALESCE(DATE_FORMAT(r.fechaFin, '%d/%m/%Y'), r.fechaFin) AS fechaFin,
            tf.fecha AS fechaSalida,
            TIME_FORMAT(tf.horaInicio, '%H:%i') AS horaSalida,
            CONCAT(DATE_FORMAT(COALESCE(tf.fecha, r.fechaInicio), '%d/%m/%Y'), ' - ', COALESCE(TIME_FORMAT(tf.horaInicio, '%H:%i'), '08:00')) AS fechaHoraSalida,
            r.estado,
            r.motivoCancelacion,
            r.total,
            COALESCE(CONCAT(p.nombre, ' ', p.apellidoPaterno), u.nombreUsuario, 'Turista') AS nombreUsuario,
            COALESCE(dr.cantAdultos, 1) AS cantAdultos,
            COALESCE(dr.cantNinos, 0) AS cantNinos,
            COALESCE(dr.cantBebes, 0) AS cantBebes,
            t.precioAdulto,
            t.precioNino,
            t.precioBebe,
            COALESCE(NULLIF((SELECT COUNT(*) FROM Pasajero ps WHERE ps.idReserva = r.idReserva), 0), 2) AS cantidadPersonas,
            COALESCE(
                (SELECT SUM(pg.monto) FROM Pago pg WHERE pg.idReserva = r.idReserva),
                (SELECT SUM(mp.monto) FROM MetodoPago mp WHERE mp.idReserva = r.idReserva),
                r.total
            ) AS totalPagado,
            COALESCE(
                (SELECT pg.estado FROM Pago pg WHERE pg.idReserva = r.idReserva LIMIT 1),
                (SELECT mp.estadoPago FROM MetodoPago mp WHERE mp.idReserva = r.idReserva LIMIT 1),
                'COMPLETADO'
            ) AS estadoPago
        FROM Reserva r
        LEFT JOIN Usuario u ON u.idUsuario = r.idUsuario
        LEFT JOIN Persona p ON u.idPersona = p.idPersona
        LEFT JOIN DetalleReserva dr ON r.idReserva = dr.idReserva AND (dr.tipo IS NULL OR dr.tipo = 'ADULTO')
        LEFT JOIN TourFecha tf ON r.idTourFecha = tf.idTourFecha
        LEFT JOIN Tour t ON (r.idTour = t.idTour OR tf.idTour = t.idTour)
        WHERE r.idAgencia = ?
        ORDER BY r.fechaRegistro DESC
        """;

        StringBuilder json = new StringBuilder();
        json.append("{\"status\":\"success\",\"reservas\":[");

        try (Connection con = ConexionDB.getConnection()) {
            if (con == null) {
                JavaApiServer.sendJsonResponse(exchange, 500, "{\"status\":\"error\",\"message\":\"No se pudo conectar a la base de datos\"}");
                return;
            }

            try (PreparedStatement ps = con.prepareStatement(sql)) {
                ps.setInt(1, idAgencia);
                try (ResultSet rs = ps.executeQuery()) {
                    boolean primero = true;
                    while (rs.next()) {
                        if (!primero) json.append(",");
                        primero = false;

                        String estadoPago = rs.getString("estadoPago");
                        BigDecimal total = rs.getBigDecimal("total");
                        if (total == null) total = BigDecimal.ZERO;
                        BigDecimal totalPagado = rs.getBigDecimal("totalPagado");
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
                                .append("\"idReserva\":").append(rs.getInt("idReserva")).append(",")
                                .append("\"codigoReserva\":\"").append(JavaApiServer.jsonEscape(rs.getString("codigoReserva"))).append("\",")
                                .append("\"codigo\":\"").append(JavaApiServer.jsonEscape(rs.getString("codigoReserva"))).append("\",")
                                .append("\"idUsuario\":").append(rs.getInt("idUsuario")).append(",")
                                .append("\"idAgencia\":").append(rs.getInt("idAgencia")).append(",")
                                .append("\"nombreCliente\":\"").append(JavaApiServer.jsonEscape(rs.getString("nombreUsuario"))).append("\",")
                                .append("\"nombreTour\":\"").append(JavaApiServer.jsonEscape(rs.getString("nombreTour"))).append("\",")
                                .append("\"fechaRegistro\":\"").append(JavaApiServer.jsonEscape(rs.getString("fechaRegistro"))).append("\",")
                                .append("\"fechaInicio\":\"").append(JavaApiServer.jsonEscape(rs.getString("fechaInicio"))).append("\",")
                                .append("\"fechaFin\":\"").append(JavaApiServer.jsonEscape(rs.getString("fechaFin"))).append("\",")
                                .append("\"fechaSalida\":\"").append(rs.getString("fechaSalida") != null ? rs.getString("fechaSalida") : rs.getString("fechaInicio")).append("\",")
                                .append("\"horaSalida\":\"").append(rs.getString("horaSalida") != null ? rs.getString("horaSalida") : "08:00").append("\",")
                                .append("\"fechaHoraSalida\":\"").append(JavaApiServer.jsonEscape(rs.getString("fechaHoraSalida"))).append("\",")
                                .append("\"cantidadPersonas\":").append(rs.getInt("cantidadPersonas")).append(",")
                                .append("\"cantAdultos\":").append(adultos).append(",")
                                .append("\"cantNinos\":").append(ninos).append(",")
                                .append("\"cantBebes\":").append(bebes).append(",")
                                .append("\"desglose\":\"").append(JavaApiServer.jsonEscape(desglose)).append("\",")
                                .append("\"estado\":\"").append(JavaApiServer.jsonEscape(rs.getString("estado"))).append("\",")
                                .append("\"motivoCancelacion\":\"").append(JavaApiServer.jsonEscape(rs.getString("motivoCancelacion"))).append("\",")
                                .append("\"total\":").append(total.toPlainString()).append(",")
                                .append("\"totalPagado\":").append(totalPagado == null ? "0.00" : totalPagado.toPlainString()).append(",")
                                .append("\"comision\":").append(comision.toPlainString()).append(",")
                                .append("\"estadoPago\":\"").append(JavaApiServer.jsonEscape(estadoPago)).append("\"")
                                .append("}");
                    }
                }
            }

            json.append("]}");
            JavaApiServer.sendJsonResponse(exchange, 200, json.toString());

        } catch (Exception e) {
            e.printStackTrace();
            JavaApiServer.sendJsonResponse(exchange, 500, "{\"status\":\"error\",\"message\":\"Error al consultar las reservas\"}");
        }
    }

    private void procesarPostReserva(HttpExchange exchange) throws IOException {
        String body = JavaApiServer.readRequestBody(exchange);
        Map<String, Object> params = JavaApiServer.parseJsonOrFormParams(body);
        String action = String.valueOf(params.getOrDefault("action", "")).trim().toLowerCase();
        int idReserva = 0;
        try { idReserva = Integer.parseInt(String.valueOf(params.getOrDefault("idReserva", "0"))); } catch (Exception ignored) {}
        int idAgencia = 1;
        try { idAgencia = Integer.parseInt(String.valueOf(params.getOrDefault("idAgencia", "1"))); } catch (Exception ignored) {}

        if (idReserva <= 0) {
            JavaApiServer.sendJsonResponse(exchange, 400, "{\"status\":\"error\",\"message\":\"ID de reserva inválido\"}");
            return;
        }

        try (Connection con = ConexionDB.getConnection()) {
            if ("confirmar".equals(action)) {
                String sqlCheck = "SELECT estado FROM Reserva WHERE idReserva = ? AND idAgencia = ?";
                try (PreparedStatement psC = con.prepareStatement(sqlCheck)) {
                    psC.setInt(1, idReserva);
                    psC.setInt(2, idAgencia);
                    try (ResultSet rs = psC.executeQuery()) {
                        if (!rs.next()) {
                            JavaApiServer.sendJsonResponse(exchange, 404, "{\"status\":\"error\",\"message\":\"Reserva no encontrada\"}");
                            return;
                        }
                        String estado = rs.getString("estado");
                        if (!"PENDIENTE".equalsIgnoreCase(estado)) {
                            JavaApiServer.sendJsonResponse(exchange, 400, "{\"status\":\"error\",\"message\":\"Solo se pueden confirmar reservas pendientes (estado actual: " + estado + ")\"}");
                            return;
                        }
                    }
                }
                try (PreparedStatement psUp = con.prepareStatement("UPDATE Reserva SET estado = 'CONFIRMADA' WHERE idReserva = ? AND idAgencia = ?")) {
                    psUp.setInt(1, idReserva);
                    psUp.setInt(2, idAgencia);
                    psUp.executeUpdate();
                }
                try (PreparedStatement psP = con.prepareStatement("UPDATE Pago SET estado = 'COMPLETADO' WHERE idReserva = ?")) {
                    psP.setInt(1, idReserva);
                    psP.executeUpdate();
                }
                JavaApiServer.sendJsonResponse(exchange, 200, "{\"status\":\"success\",\"message\":\"Reserva confirmada exitosamente\"}");
                return;
            } else if ("cancelar".equals(action)) {
                boolean ok = JavaApiServer.reservaRepositorio.cancelarReserva(idReserva, "Cancelado por la agencia");
                if (ok) {
                    JavaApiServer.sendJsonResponse(exchange, 200, "{\"status\":\"success\",\"message\":\"Reserva cancelada exitosamente y cupos devueltos\"}");
                } else {
                    JavaApiServer.sendJsonResponse(exchange, 400, "{\"status\":\"error\",\"message\":\"No se pudo cancelar la reserva\"}");
                }
                return;
            }
            JavaApiServer.sendJsonResponse(exchange, 400, "{\"status\":\"error\",\"message\":\"Acción no válida\"}");
        } catch (Exception e) {
            e.printStackTrace();
            JavaApiServer.sendJsonResponse(exchange, 500, "{\"status\":\"error\",\"message\":\"Error al procesar reserva: " + JavaApiServer.jsonEscape(e.getMessage()) + "\"}");
        }
    }
}
