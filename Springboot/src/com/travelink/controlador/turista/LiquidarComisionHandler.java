package com.travelink.controlador.turista;

import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import com.travelink.config.ConexionDB;
import com.travelink.server.JavaApiServer;

import java.io.IOException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.util.Map;

public class LiquidarComisionHandler implements HttpHandler {
    @Override
    public void handle(HttpExchange exchange) throws IOException {
        JavaApiServer.enableCORS(exchange);
        if ("OPTIONS".equalsIgnoreCase(exchange.getRequestMethod())) {
            exchange.sendResponseHeaders(200, -1);
            return;
        }

        String method = exchange.getRequestMethod().toUpperCase();
        Map<String, String> queryParams = JavaApiServer.parseQueryParams(exchange.getRequestURI().getQuery());
        String body = JavaApiServer.readRequestBody(exchange);
        Map<String, Object> bodyParams = JavaApiServer.parseJsonOrFormParams(body);

        int id = 0;
        try {
            if (bodyParams.containsKey("idAgencia")) id = Integer.parseInt(String.valueOf(bodyParams.get("idAgencia")));
            else if (bodyParams.containsKey("id")) id = Integer.parseInt(String.valueOf(bodyParams.get("id")));
            else if (queryParams.containsKey("idAgencia")) id = Integer.parseInt(queryParams.get("idAgencia"));
            else if (queryParams.containsKey("id")) id = Integer.parseInt(queryParams.get("id"));
        } catch (Exception ignored) {}

        if (id <= 0) {
            JavaApiServer.sendJsonResponse(exchange, 400, "{\"status\":\"error\",\"message\":\"ID de agencia o comisión requerido\"}");
            return;
        }

        if ("DELETE".equals(method)) {
            // Anular / Revertir liquidación
            try (Connection con = ConexionDB.getConnection();
                 PreparedStatement ps = con.prepareStatement("UPDATE Comision SET estado = 'Pendiente' WHERE idAgencia = ? OR idComision = ?")) {
                ps.setInt(1, id);
                ps.setInt(2, id);
                ps.executeUpdate();
                JavaApiServer.sendJsonResponse(exchange, 200, "{\"status\":\"success\",\"message\":\"Liquidación revertida a pendiente exitosamente.\"}");
                return;
            } catch (Exception e) {
                e.printStackTrace();
                JavaApiServer.sendJsonResponse(exchange, 500, "{\"status\":\"error\",\"message\":\"Error al revertir liquidación: " + JavaApiServer.jsonEscape(e.getMessage()) + "\"}");
                return;
            }
        }

        // POST o PUT: Liquidar comisión
        try (Connection con = ConexionDB.getConnection();
             PreparedStatement ps = con.prepareStatement("UPDATE Comision SET estado = 'Liquidado' WHERE idAgencia = ? OR idComision = ?")) {
            ps.setInt(1, id);
            ps.setInt(2, id);
            ps.executeUpdate();
            JavaApiServer.sendJsonResponse(exchange, 200, "{\"status\":\"success\",\"message\":\"Comisión liquidada exitosamente.\"}");
        } catch (Exception e) {
            e.printStackTrace();
            JavaApiServer.sendJsonResponse(exchange, 500, "{\"status\":\"error\",\"message\":\"Error al liquidar comisión: " + JavaApiServer.jsonEscape(e.getMessage()) + "\"}");
        }
    }
}
