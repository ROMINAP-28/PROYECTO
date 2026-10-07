package com.travelink.controlador.agencia;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.io.File;
import java.io.OutputStream;
import java.nio.charset.StandardCharsets;
import com.travelink.entidades.Usuario;

import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import java.io.IOException;
import java.util.Map;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.Statement;
import com.travelink.config.ConexionDB;
import com.travelink.server.JavaApiServer;
import java.util.Locale;
import java.util.ArrayList;
import java.util.List;
import com.travelink.controlador.UsuarioControlador;
import com.travelink.controlador.CalificacionControlador;
import com.travelink.repositorio.ReservaRepositorio;

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
                JavaApiServer.sendJsonResponse(exchange, 405,
                        "{\"status\":\"error\",\"message\":\"Método no permitido\"}");
                return;
            }

            Map<String, String> params =
                    JavaApiServer.parseQueryParams(exchange.getRequestURI().getQuery());

            int idAgencia;
            try {
                idAgencia = Integer.parseInt(
                        params.getOrDefault("idAgencia", "0"));
            } catch (NumberFormatException e) {
                idAgencia = 0;
            }

            if (idAgencia <= 0) {
                JavaApiServer.sendJsonResponse(exchange, 400,
                        "{\"status\":\"error\",\"message\":\"ID de agencia inválido\"}");
                return;
            }

            String sql = """
            SELECT
                r.idReserva,
                r.codigoReserva,
                r.idUsuario,
                r.idAgencia,
                r.nombreTour,
                r.fechaRegistro,
                r.fechaInicio,
                r.fechaFin,
                r.estado,
                r.motivoCancelacion,
                r.total,
                u.nombreUsuario,
                COALESCE(NULLIF((SELECT COUNT(*) FROM Pasajero p WHERE p.idReserva = r.idReserva), 0), 2) AS cantidadPersonas,
                (
                    SELECT COALESCE(SUM(p.monto), 0)
                    FROM Pago p
                    WHERE p.idReserva = r.idReserva
                ) AS totalPagado,
                (
                    SELECT p.estado
                    FROM Pago p
                    WHERE p.idReserva = r.idReserva
                    ORDER BY p.fechaPago DESC, p.idPago DESC
                    LIMIT 1
                ) AS estadoPago
            FROM Reserva r
            LEFT JOIN Usuario u
                ON u.idUsuario = r.idUsuario
            WHERE r.idAgencia = ?
            ORDER BY r.fechaRegistro DESC
            """;

            StringBuilder json = new StringBuilder();
            json.append("{\"status\":\"success\",\"reservas\":[");

            try (Connection con = ConexionDB.getConnection()) {

                if (con == null) {
                    JavaApiServer.sendJsonResponse(exchange, 500,
                            "{\"status\":\"error\",\"message\":\"No se pudo conectar a la base de datos\"}");
                    return;
                }

                try (PreparedStatement ps = con.prepareStatement(sql)) {
                    ps.setInt(1, idAgencia);

                    try (ResultSet rs = ps.executeQuery()) {
                        boolean primero = true;

                        while (rs.next()) {
                            if (!primero) {
                                json.append(",");
                            }
                            primero = false;

                            String estadoPago = rs.getString("estadoPago");
                            java.math.BigDecimal total =
                                    rs.getBigDecimal("total");
                            java.math.BigDecimal totalPagado =
                                    rs.getBigDecimal("totalPagado");

                            json.append("{")
                                    .append("\"idReserva\":")
                                    .append(rs.getInt("idReserva")).append(",")

                                    .append("\"codigoReserva\":\"")
                                    .append(JavaApiServer.jsonEscape(rs.getString("codigoReserva")))
                                    .append("\",")

                                    .append("\"idUsuario\":")
                                    .append(rs.getInt("idUsuario")).append(",")

                                    .append("\"idAgencia\":")
                                    .append(rs.getInt("idAgencia")).append(",")

                                    .append("\"nombreCliente\":\"")
                                    .append(JavaApiServer.jsonEscape(rs.getString("nombreUsuario")))
                                    .append("\",")

                                    .append("\"nombreTour\":\"")
                                    .append(JavaApiServer.jsonEscape(rs.getString("nombreTour")))
                                    .append("\",")

                                    .append("\"fechaRegistro\":\"")
                                    .append(JavaApiServer.jsonEscape(rs.getString("fechaRegistro")))
                                    .append("\",")

                                    .append("\"fechaInicio\":\"")
                                    .append(JavaApiServer.jsonEscape(rs.getString("fechaInicio")))
                                    .append("\",")

                                    .append("\"fechaFin\":\"")
                                    .append(JavaApiServer.jsonEscape(rs.getString("fechaFin")))
                                    .append("\",")

                                    .append("\"cantidadPersonas\":")
                                    .append(rs.getInt("cantidadPersonas")).append(",")

                                    .append("\"estado\":\"")
                                    .append(JavaApiServer.jsonEscape(rs.getString("estado")))
                                    .append("\",")

                                    .append("\"motivoCancelacion\":\"")
                                    .append(JavaApiServer.jsonEscape(rs.getString("motivoCancelacion")))
                                    .append("\",")

                                    .append("\"total\":")
                                    .append(total == null ? "0.00" : total.toPlainString())
                                    .append(",")

                                    .append("\"totalPagado\":")
                                    .append(totalPagado == null
                                            ? "0.00" : totalPagado.toPlainString())
                                    .append(",")

                                    .append("\"estadoPago\":\"")
                                    .append(JavaApiServer.jsonEscape(estadoPago))
                                    .append("\"")
                                    .append("}");
                        }
                    }
                }

                json.append("]}");
                JavaApiServer.sendJsonResponse(exchange, 200, json.toString());

            } catch (Exception e) {
                e.printStackTrace();
                JavaApiServer.sendJsonResponse(exchange, 500,
                        "{\"status\":\"error\",\"message\":\"Error al consultar las reservas\"}");
            }
        }

        private void procesarPostReserva(HttpExchange exchange) throws IOException {
            String body = JavaApiServer.readRequestBody(exchange);
            Map<String, Object> params = JavaApiServer.parseJsonOrFormParams(body);
            String action = String.valueOf(params.getOrDefault("action", "")).trim().toLowerCase();
            int idReserva = 0;
            try {
                idReserva = Integer.parseInt(String.valueOf(params.getOrDefault("idReserva", "0")));
            } catch (Exception ignored) {}
            int idAgencia = 1;
            try {
                idAgencia = Integer.parseInt(String.valueOf(params.getOrDefault("idAgencia", "1")));
            } catch (Exception ignored) {}

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
                    try (PreparedStatement psUp = con.prepareStatement("UPDATE Reserva SET estado = 'CANCELADA' WHERE idReserva = ? AND idAgencia = ?")) {
                        psUp.setInt(1, idReserva);
                        psUp.setInt(2, idAgencia);
                        psUp.executeUpdate();
                    }
                    try (PreparedStatement psP = con.prepareStatement("UPDATE Pago SET estado = 'RECHAZADO' WHERE idReserva = ?")) {
                        psP.setInt(1, idReserva);
                        psP.executeUpdate();
                    }
                    JavaApiServer.sendJsonResponse(exchange, 200, "{\"status\":\"success\",\"message\":\"Reserva cancelada exitosamente\"}");
                    return;
                }
                JavaApiServer.sendJsonResponse(exchange, 400, "{\"status\":\"error\",\"message\":\"Acción no válida\"}");
            } catch (Exception e) {
                e.printStackTrace();
                JavaApiServer.sendJsonResponse(exchange, 500, "{\"status\":\"error\",\"message\":\"Error al procesar reserva: " + JavaApiServer.jsonEscape(e.getMessage()) + "\"}");
            }
        }
    }

