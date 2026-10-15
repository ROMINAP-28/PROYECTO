package com.travelink.controlador.turista;

import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import com.travelink.config.ConexionDB;
import com.travelink.config.SecurityUtils;
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
        if ("POST".equalsIgnoreCase(exchange.getRequestMethod())) {
            String body = JavaApiServer.readRequestBody(exchange);
            Map<String, Object> params = JavaApiServer.parseJsonOrFormParams(body);

            int idReserva = 0;
            try { idReserva = Integer.parseInt(String.valueOf(params.getOrDefault("idReserva", params.getOrDefault("id", "0")))); } catch (Exception ignored) {}

            if (idReserva <= 0) {
                String cod = String.valueOf(params.getOrDefault("codigo", params.getOrDefault("codigoReserva", ""))).trim();
                if (!cod.isEmpty()) {
                    try (Connection con = ConexionDB.getConnection();
                         PreparedStatement ps = con.prepareStatement("SELECT idReserva FROM Reserva WHERE codigoReserva = ?")) {
                        ps.setString(1, cod);
                        try (ResultSet rs = ps.executeQuery()) {
                            if (rs.next()) idReserva = rs.getInt(1);
                        }
                    } catch (Exception ignored) {}
                }
            }

            if (idReserva <= 0) {
                JavaApiServer.sendJsonResponse(exchange, 400, "{\"status\":\"error\",\"message\":\"ID o código de reserva inválido\"}");
                return;
            }

            String motivo = String.valueOf(params.getOrDefault("motivo", "Cancelación solicitada por el usuario"));

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

            boolean ok = JavaApiServer.reservaRepositorio.cancelarReserva(idReserva, motivo);
            if (ok) {
                JavaApiServer.sendJsonResponse(exchange, 200, "{\"status\":\"success\",\"message\":\"Reserva cancelada correctamente y cupos restaurados\"}");
            } else {
                JavaApiServer.sendJsonResponse(exchange, 400, "{\"status\":\"error\",\"message\":\"No se pudo cancelar la reserva\"}");
            }
        } else {
            JavaApiServer.sendJsonResponse(exchange, 405, "{\"status\":\"error\",\"message\":\"Método no permitido\"}");
        }
    }
}
