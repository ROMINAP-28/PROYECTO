package com.travelink.controlador.agencia;

import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import com.travelink.config.ConexionDB;
import com.travelink.server.JavaApiServer;

import java.io.IOException;
import java.io.OutputStream;
import java.nio.charset.StandardCharsets;
import java.sql.*;
import java.util.Map;

public class DisponibilidadAgenciaHandler implements HttpHandler {

    @Override
    public void handle(HttpExchange exchange) throws IOException {
        JavaApiServer.enableCORS(exchange);

        if ("OPTIONS".equalsIgnoreCase(exchange.getRequestMethod())) {
            exchange.sendResponseHeaders(200, -1);
            return;
        }

        exchange.getResponseHeaders().add("Content-Type", "application/json; charset=UTF-8");

        try {
            if ("GET".equalsIgnoreCase(exchange.getRequestMethod())) {
                listarDisponibilidad(exchange);
                return;
            }

            if ("POST".equalsIgnoreCase(exchange.getRequestMethod())) {
                procesarPostDisponibilidad(exchange);
                return;
            }

            enviar(exchange, 405, "{\"error\":\"Método no permitido\"}");
        } catch (Exception e) {
            e.printStackTrace();
            enviar(exchange, 500, "{\"error\":\"Ocurrió un error interno en el servidor: " + JavaApiServer.jsonEscape(e.getMessage()) + "\"}");
        }
    }

    private void listarDisponibilidad(HttpExchange exchange) throws IOException {
        Map<String, String> qParams = JavaApiServer.parseQueryParams(exchange.getRequestURI().getQuery());
        int idAgencia = 1;
        try {
            idAgencia = Integer.parseInt(qParams.getOrDefault("idAgencia", "1"));
        } catch (Exception ignored) {}

        StringBuilder json = new StringBuilder("{\"status\":\"success\",\"disponibilidades\":[");
        boolean primero = true;

        try (Connection con = ConexionDB.getConnection()) {
            String sql = """
                SELECT tf.idTourFecha AS idDisponibilidad,
                       tf.idTourFecha,
                       tf.fecha,
                       tf.horaInicio,
                       tf.cupoTotal,
                       tf.cupoDisponible,
                       tf.estado,
                       tf.idTour AS idServicio,
                       t.nombre AS servicio
                FROM TourFecha tf
                INNER JOIN Tour t ON tf.idTour = t.idTour
                WHERE t.idAgencia = ?
                ORDER BY tf.fecha ASC, tf.horaInicio ASC
            """;

            try (PreparedStatement ps = con.prepareStatement(sql)) {
                ps.setInt(1, idAgencia);
                try (ResultSet rs = ps.executeQuery()) {
                    while (rs.next()) {
                        if (!primero) json.append(",");
                        primero = false;
                        json.append("{")
                                .append("\"idDisponibilidad\":").append(rs.getInt("idDisponibilidad")).append(",")
                                .append("\"idTourFecha\":").append(rs.getInt("idTourFecha")).append(",")
                                .append("\"fecha\":\"").append(rs.getString("fecha")).append("\",")
                                .append("\"horaInicio\":\"").append(rs.getString("horaInicio")).append("\",")
                                .append("\"cupoTotal\":").append(rs.getInt("cupoTotal")).append(",")
                                .append("\"cupoDisponible\":").append(rs.getInt("cupoDisponible")).append(",")
                                .append("\"estado\":\"").append(JavaApiServer.jsonEscape(rs.getString("estado"))).append("\",")
                                .append("\"idServicio\":").append(rs.getInt("idServicio")).append(",")
                                .append("\"idTour\":").append(rs.getInt("idServicio")).append(",")
                                .append("\"servicio\":\"").append(JavaApiServer.jsonEscape(rs.getString("servicio"))).append("\"")
                                .append("}");
                    }
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
            enviar(exchange, 500, "{\"error\":\"Error al consultar salidas: " + JavaApiServer.jsonEscape(e.getMessage()) + "\"}");
            return;
        }

        json.append("]}");
        enviar(exchange, 200, json.toString());
    }

    private void procesarPostDisponibilidad(HttpExchange exchange) throws IOException {
        String body = JavaApiServer.readRequestBody(exchange);
        Map<String, Object> params = JavaApiServer.parseJsonOrFormParams(body);

        String action = String.valueOf(params.getOrDefault("action", "")).trim().toLowerCase();
        int idTourFecha = 0;
        try {
            idTourFecha = Integer.parseInt(String.valueOf(params.getOrDefault("idTourFecha", params.getOrDefault("idDisponibilidad", "0"))));
        } catch (Exception ignored) {}

        if ("cerrar_ventas".equals(action) || "cerrar".equals(action)) {
            if (idTourFecha > 0) {
                try (Connection con = ConexionDB.getConnection();
                     PreparedStatement ps = con.prepareStatement("UPDATE TourFecha SET estado = 'CERRADO', cupoDisponible = 0 WHERE idTourFecha = ?")) {
                    ps.setInt(1, idTourFecha);
                    ps.executeUpdate();
                    enviar(exchange, 200, "{\"mensaje\":\"Ventas cerradas exitosamente.\"}");
                    return;
                } catch (Exception e) {
                    enviar(exchange, 500, "{\"error\":\"" + JavaApiServer.jsonEscape(e.getMessage()) + "\"}");
                    return;
                }
            }
        } else if ("reabrir".equals(action)) {
            if (idTourFecha > 0) {
                try (Connection con = ConexionDB.getConnection();
                     PreparedStatement ps = con.prepareStatement("UPDATE TourFecha SET estado = 'DISPONIBLE', cupoDisponible = cupoTotal WHERE idTourFecha = ?")) {
                    ps.setInt(1, idTourFecha);
                    ps.executeUpdate();
                    enviar(exchange, 200, "{\"mensaje\":\"Ventas reabiertas exitosamente.\"}");
                    return;
                } catch (Exception e) {
                    enviar(exchange, 500, "{\"error\":\"" + JavaApiServer.jsonEscape(e.getMessage()) + "\"}");
                    return;
                }
            }
        } else if ("eliminar".equals(action)) {
            if (idTourFecha > 0) {
                try (Connection con = ConexionDB.getConnection()) {
                    // Si tiene reservas asociadas, no se borra, se pausa/cancela
                    try (PreparedStatement psCheck = con.prepareStatement("SELECT COUNT(*) FROM Reserva WHERE idTourFecha = ?")) {
                        psCheck.setInt(1, idTourFecha);
                        try (ResultSet rsC = psCheck.executeQuery()) {
                            if (rsC.next() && rsC.getInt(1) > 0) {
                                try (PreparedStatement psPause = con.prepareStatement("UPDATE TourFecha SET estado = 'CANCELADO', cupoDisponible = 0 WHERE idTourFecha = ?")) {
                                    psPause.setInt(1, idTourFecha);
                                    psPause.executeUpdate();
                                }
                                enviar(exchange, 200, "{\"mensaje\":\"La salida tiene reservas asociadas, por lo que fue cancelada en lugar de eliminarse.\"}");
                                return;
                            }
                        }
                    }

                    try (PreparedStatement ps = con.prepareStatement("DELETE FROM TourFecha WHERE idTourFecha = ?")) {
                        ps.setInt(1, idTourFecha);
                        ps.executeUpdate();
                        enviar(exchange, 200, "{\"mensaje\":\"Salida eliminada exitosamente.\"}");
                        return;
                    }
                } catch (Exception e) {
                    enviar(exchange, 500, "{\"error\":\"" + JavaApiServer.jsonEscape(e.getMessage()) + "\"}");
                    return;
                }
            }
        }

        // Guardar o actualizar salida
        String fecha = String.valueOf(params.getOrDefault("fecha", "")).trim();
        String horaInicio = String.valueOf(params.getOrDefault("horaInicio", "08:00:00")).trim();
        int cupoTotal = 30;
        try { cupoTotal = Integer.parseInt(String.valueOf(params.getOrDefault("cupoTotal", "30"))); } catch (Exception ignored) {}
        int idTour = 0;
        try { idTour = Integer.parseInt(String.valueOf(params.getOrDefault("idTour", params.getOrDefault("idServicio", "0")))); } catch (Exception ignored) {}

        if (fecha.isEmpty() || idTour <= 0 || cupoTotal <= 0) {
            enviar(exchange, 400, "{\"error\":\"Completa los campos obligatorios (fecha, tour y cupo > 0).\"}");
            return;
        }

        try (Connection con = ConexionDB.getConnection()) {
            if (idTourFecha > 0) {
                String sqlUp = "UPDATE TourFecha SET fecha = ?, horaInicio = ?, cupoTotal = ?, cupoDisponible = LEAST(cupoDisponible, ?) WHERE idTourFecha = ?";
                try (PreparedStatement ps = con.prepareStatement(sqlUp)) {
                    ps.setString(1, fecha);
                    ps.setString(2, horaInicio);
                    ps.setInt(3, cupoTotal);
                    ps.setInt(4, cupoTotal);
                    ps.setInt(5, idTourFecha);
                    ps.executeUpdate();
                    enviar(exchange, 200, "{\"mensaje\":\"Salida actualizada correctamente.\"}");
                }
            } else {
                String sqlIns = "INSERT INTO TourFecha (idTour, fecha, horaInicio, cupoTotal, cupoDisponible, estado) VALUES (?, ?, ?, ?, ?, 'DISPONIBLE')";
                try (PreparedStatement ps = con.prepareStatement(sqlIns)) {
                    ps.setInt(1, idTour);
                    ps.setString(2, fecha);
                    ps.setString(3, horaInicio);
                    ps.setInt(4, cupoTotal);
                    ps.setInt(5, cupoTotal);
                    ps.executeUpdate();
                    enviar(exchange, 200, "{\"mensaje\":\"Salida registrada correctamente.\"}");
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
            enviar(exchange, 500, "{\"error\":\"Error al guardar salida: " + JavaApiServer.jsonEscape(e.getMessage()) + "\"}");
        }
    }

    private void enviar(HttpExchange exchange, int codigo, String json) throws IOException {
        byte[] bytes = json.getBytes(StandardCharsets.UTF_8);
        exchange.sendResponseHeaders(codigo, bytes.length);
        try (OutputStream os = exchange.getResponseBody()) {
            os.write(bytes);
        }
    }
}
