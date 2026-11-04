package com.travelink.controlador.turista;

import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import com.travelink.config.ConexionDB;
import com.travelink.config.SecurityUtils;
import com.travelink.persistencia.ReservaPersistencia;
import com.travelink.server.JavaApiServer;

import java.io.IOException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.Map;

public class CancelarReservaHandler implements HttpHandler {
    @Override
    public void handle(HttpExchange exchange) throws IOException {
        if ("OPTIONS".equalsIgnoreCase(exchange.getRequestMethod())) {
            JavaApiServer.enableCORS(exchange);
            exchange.sendResponseHeaders(200, -1);
            return;
        }

        String method = exchange.getRequestMethod().toUpperCase();
        if ("POST".equals(method) || "DELETE".equals(method) || "PUT".equals(method)) {
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

            String cod = String.valueOf(params.getOrDefault("codigo", params.getOrDefault("codigoReserva", queryParams.getOrDefault("codigo", "")))).trim();
            if (idReserva <= 0 && !cod.isEmpty()) {
                try (Connection con = ConexionDB.getConnection();
                     PreparedStatement ps = con.prepareStatement("SELECT idReserva FROM Reserva WHERE codigoReserva = ?")) {
                    ps.setString(1, cod);
                    try (ResultSet rs = ps.executeQuery()) {
                        if (rs.next()) idReserva = rs.getInt(1);
                    }
                } catch (Exception ignored) {}
            }

            if (idReserva <= 0) {
                JavaApiServer.sendJsonResponse(exchange, 400, "{\"status\":\"error\",\"message\":\"ID o código de reserva inválido\"}");
                return;
            }

            String motivo = String.valueOf(params.getOrDefault("motivo", queryParams.getOrDefault("motivo", ""))).trim();
            if (motivo.isEmpty()) {
                JavaApiServer.sendJsonResponse(exchange, 400, "{\"status\":\"error\",\"message\":\"El motivo de cancelación es obligatorio\"}");
                return;
            }

            // Validación estricta de propiedad si hay sesión autenticada
            Integer sessionUserId = null;
            Object sessUserObj = exchange.getAttribute("ID_USUARIO");
            if (sessUserObj instanceof Integer) {
                sessionUserId = (Integer) sessUserObj;
            } else if (SecurityUtils.getAuthenticatedIdUsuario().isPresent()) {
                sessionUserId = SecurityUtils.getAuthenticatedIdUsuario().get();
            }

            if (sessionUserId != null) {
                // Verificar si pertenece al usuario o si es rol agencia/admin
                boolean esAdmin = SecurityUtils.hasRole("ADMIN");
                boolean esAgencia = SecurityUtils.hasRole("AGENCIA");
                if (!esAdmin && !esAgencia && !JavaApiServer.reservaRepositorio.perteneceAUsuario(idReserva, sessionUserId)) {
                    JavaApiServer.sendJsonResponse(exchange, 403, "{\"status\":\"error\",\"message\":\"Acceso denegado: esta reserva no pertenece a su usuario\"}");
                    return;
                }
            }

            ReservaPersistencia.CancelarResultado resultado = JavaApiServer.reservaRepositorio.cancelarReservaConDetalle(idReserva, motivo);
            String jsonStatus = resultado.success ? "success" : "error";
            String jsonResp = "{\"status\":\"" + jsonStatus + "\",\"message\":\"" + JavaApiServer.jsonEscape(resultado.message) + "\"}";
            JavaApiServer.sendJsonResponse(exchange, resultado.statusCode, jsonResp);
        } else {
            JavaApiServer.sendJsonResponse(exchange, 405, "{\"status\":\"error\",\"message\":\"Método no permitido\"}");
        }
    }
}
