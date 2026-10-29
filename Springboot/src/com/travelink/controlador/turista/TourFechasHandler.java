package com.travelink.controlador.turista;

import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import com.travelink.config.ConexionDB;
import com.travelink.server.JavaApiServer;
import java.io.IOException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.Map;

/** Devuelve las fechas futuras con cupo disponible de un tour real. */
public class TourFechasHandler implements HttpHandler {
    @Override
    public void handle(HttpExchange exchange) throws IOException {
        JavaApiServer.enableCORS(exchange);
        if ("OPTIONS".equalsIgnoreCase(exchange.getRequestMethod())) {
            exchange.sendResponseHeaders(200, -1);
            return;
        }
        if (!"GET".equalsIgnoreCase(exchange.getRequestMethod())) {
            JavaApiServer.sendJsonResponse(exchange, 405, "{\"status\":\"error\",\"message\":\"Método no permitido\"}");
            return;
        }
        Map<String, String> params = JavaApiServer.parseQueryParams(exchange.getRequestURI().getQuery());
        int idTour;
        try { idTour = Integer.parseInt(params.getOrDefault("idTour", "0")); }
        catch (Exception e) { idTour = 0; }
        if (idTour <= 0) {
            JavaApiServer.sendJsonResponse(exchange, 400, "{\"status\":\"error\",\"message\":\"Falta un idTour válido\"}");
            return;
        }
        StringBuilder json = new StringBuilder("{\"status\":\"success\",\"data\":[");
        boolean first = true;
        String sql = "SELECT idTourFecha, idTour, fecha, horaInicio, cupoDisponible FROM TourFecha " +
                     "WHERE idTour = ? AND fecha >= CURDATE() AND UPPER(estado) = 'DISPONIBLE' " +
                     "AND cupoDisponible > 0 ORDER BY fecha, horaInicio";
        try (Connection con = ConexionDB.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setInt(1, idTour);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    if (!first) json.append(',');
                    first = false;
                    json.append("{\"idTourFecha\":").append(rs.getInt("idTourFecha"))
                        .append(",\"idTour\":").append(rs.getInt("idTour"))
                        .append(",\"fecha\":\"").append(rs.getDate("fecha").toString()).append("\"")
                        .append(",\"horaInicio\":\"").append(rs.getTime("horaInicio").toString()).append("\"")
                        .append(",\"cupoDisponible\":").append(rs.getInt("cupoDisponible")).append('}');
                }
            }
        } catch (Exception e) {
            JavaApiServer.sendJsonResponse(exchange, 500, "{\"status\":\"error\",\"message\":\"No se pudieron consultar las fechas disponibles\"}");
            return;
        }
        json.append("]}");
        JavaApiServer.sendJsonResponse(exchange, 200, json.toString());
    }
}
