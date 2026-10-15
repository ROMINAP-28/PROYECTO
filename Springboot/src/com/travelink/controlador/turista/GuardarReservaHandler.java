package com.travelink.controlador.turista;

import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import com.travelink.config.SecurityUtils;
import com.travelink.server.JavaApiServer;

import java.io.IOException;
import java.util.Map;

public class GuardarReservaHandler implements HttpHandler {
    @Override
    public void handle(HttpExchange exchange) throws IOException {
        JavaApiServer.enableCORS(exchange);
        if ("OPTIONS".equalsIgnoreCase(exchange.getRequestMethod())) {
            exchange.sendResponseHeaders(200, -1);
            return;
        }
        if ("POST".equalsIgnoreCase(exchange.getRequestMethod())) {
            String body = JavaApiServer.readRequestBody(exchange);
            Map<String, Object> params = JavaApiServer.parseJsonOrFormParams(body);

            // Inyectar usuario de sesión autenticada si está presente
            Object sessUserObj = exchange.getAttribute("ID_USUARIO");
            if (sessUserObj instanceof Integer) {
                params.put("sessionUserId", sessUserObj);
                params.put("idUsuario", sessUserObj);
            } else if (SecurityUtils.getAuthenticatedIdUsuario().isPresent()) {
                Integer uid = SecurityUtils.getAuthenticatedIdUsuario().get();
                params.put("sessionUserId", uid);
                params.put("idUsuario", uid);
            }

            Map<String, Object> res = JavaApiServer.reservaRepositorio.guardarReservaCompleta(params);

            StringBuilder json = new StringBuilder();
            if ("success".equals(res.get("status"))) {
                @SuppressWarnings("unchecked")
                Map<String, Object> d = (Map<String, Object>) res.get("data");
                json.append("{\"status\":\"success\",\"message\":\"Reserva guardada exitosamente\",")
                        .append("\"codigoReserva\":\"").append(d.get("codigo")).append("\",")
                        .append("\"codigo\":\"").append(d.get("codigo")).append("\",")
                        .append("\"total\":").append(d.get("total")).append(",")
                        .append("\"desglose\":\"").append(JavaApiServer.jsonEscape(String.valueOf(d.getOrDefault("desglose", "")))).append("\",")
                        .append("\"fechaSalida\":\"").append(d.getOrDefault("fechaSalida", "")).append("\",")
                        .append("\"horaSalida\":\"").append(d.getOrDefault("horaSalida", "")).append("\",")
                        .append("\"data\":{")
                        .append("\"idReserva\":").append(d.get("idReserva")).append(",")
                        .append("\"idPago\":").append(d.get("idPago")).append(",")
                        .append("\"codigo\":\"").append(d.get("codigo")).append("\",")
                        .append("\"estado\":\"").append(d.get("estado")).append("\",")
                        .append("\"precioTotal\":").append(d.get("total")).append(",")
                        .append("\"total\":").append(d.get("total")).append(",")
                        .append("\"metodoPago\":\"").append(d.get("metodoPago")).append("\",")
                        .append("\"desglose\":\"").append(JavaApiServer.jsonEscape(String.valueOf(d.getOrDefault("desglose", "")))).append("\",")
                        .append("\"fechaSalida\":\"").append(d.getOrDefault("fechaSalida", "")).append("\",")
                        .append("\"horaSalida\":\"").append(d.getOrDefault("horaSalida", "")).append("\",")
                        .append("\"cuposRestantes\":").append(d.getOrDefault("cuposRestantes", 0))
                        .append("}}");
                JavaApiServer.sendJsonResponse(exchange, 200, json.toString());
            } else {
                int httpCode = 400;
                String errCode = String.valueOf(res.getOrDefault("code", "ERROR"));
                if ("UNAUTHORIZED".equalsIgnoreCase(errCode)) httpCode = 401;

                json.append("{\"status\":\"error\",")
                    .append("\"code\":\"").append(errCode).append("\",")
                    .append("\"message\":\"").append(JavaApiServer.jsonEscape(String.valueOf(res.get("message")))).append("\"}");
                JavaApiServer.sendJsonResponse(exchange, httpCode, json.toString());
            }
        } else {
            JavaApiServer.sendJsonResponse(exchange, 405, "{\"status\":\"error\",\"message\":\"Método no permitido\"}");
        }
    }
}
