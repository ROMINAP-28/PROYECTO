package com.travelink.controlador.turista;

import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import com.travelink.config.SecurityUtils;
import com.travelink.server.JavaApiServer;

import java.io.IOException;
import java.util.List;
import java.util.Map;

public class ObtenerReservasHandler implements HttpHandler {
    @Override
    public void handle(HttpExchange exchange) throws IOException {
        if ("OPTIONS".equalsIgnoreCase(exchange.getRequestMethod())) {
            JavaApiServer.enableCORS(exchange);
            exchange.sendResponseHeaders(200, -1);
            return;
        }
        Map<String, String> queryParams = JavaApiServer.parseQueryParams(exchange.getRequestURI().getQuery());
        String correo = queryParams.getOrDefault("correo", queryParams.getOrDefault("email", ""));
        int idUsuario = 0;
        try { idUsuario = Integer.parseInt(queryParams.getOrDefault("idUsuario", "0")); } catch (Exception ignored) {}

        // Si no vino idUsuario explícito, intentar tomar de la sesión autenticada
        if (idUsuario <= 0) {
            Object sessUserObj = exchange.getAttribute("ID_USUARIO");
            if (sessUserObj instanceof Integer) {
                idUsuario = (Integer) sessUserObj;
            } else if (SecurityUtils.getAuthenticatedIdUsuario().isPresent()) {
                idUsuario = SecurityUtils.getAuthenticatedIdUsuario().get();
            }
        }

        Map<String, Object> res = JavaApiServer.reservaRepositorio.obtenerReservasPorUsuario(correo, idUsuario);
        StringBuilder json = new StringBuilder();
        json.append("{\"status\":\"").append(res.get("status")).append("\",\"reservas\":[");

        @SuppressWarnings("unchecked")
        List<Map<String, Object>> lista = (List<Map<String, Object>>) res.get("reservas");
        if (lista != null) {
            for (int i = 0; i < lista.size(); i++) {
                Map<String, Object> r = lista.get(i);
                json.append("{")
                        .append("\"id\":").append(r.get("id")).append(",")
                        .append("\"codigo\":\"").append(r.get("codigo")).append("\",")
                        .append("\"titulo\":\"").append(JavaApiServer.jsonEscape(String.valueOf(r.getOrDefault("titulo", "")))).append("\",")
                        .append("\"ubicacion\":\"").append(JavaApiServer.jsonEscape(String.valueOf(r.getOrDefault("ubicacion", "")))).append("\",")
                        .append("\"fechas\":\"").append(JavaApiServer.jsonEscape(String.valueOf(r.getOrDefault("fechas", "")))).append("\",")
                        .append("\"fechaSalida\":\"").append(r.getOrDefault("fechaSalida", "")).append("\",")
                        .append("\"horaSalida\":\"").append(r.getOrDefault("horaSalida", "")).append("\",")
                        .append("\"personas\":\"").append(JavaApiServer.jsonEscape(String.valueOf(r.getOrDefault("personas", "")))).append("\",")
                        .append("\"cantAdultos\":").append(r.getOrDefault("cantAdultos", 1)).append(",")
                        .append("\"cantNinos\":").append(r.getOrDefault("cantNinos", 0)).append(",")
                        .append("\"cantBebes\":").append(r.getOrDefault("cantBebes", 0)).append(",")
                        .append("\"desglose\":\"").append(JavaApiServer.jsonEscape(String.valueOf(r.getOrDefault("desglose", "")))).append("\",")
                        .append("\"agencia\":\"").append(JavaApiServer.jsonEscape(String.valueOf(r.getOrDefault("agencia", "")))).append("\",")
                        .append("\"estado\":\"").append(r.get("estado")).append("\",")
                        .append("\"total\":").append(r.get("total")).append(",")
                        .append("\"metodoPago\":\"").append(JavaApiServer.jsonEscape(String.valueOf(r.getOrDefault("metodoPago", "")))).append("\",")
                        .append("\"marcaTarjeta\":\"").append(JavaApiServer.jsonEscape(String.valueOf(r.getOrDefault("marcaTarjeta", "")))).append("\",")
                        .append("\"ultimosDigitos\":\"").append(JavaApiServer.jsonEscape(String.valueOf(r.getOrDefault("ultimosDigitos", "")))).append("\",")
                        .append("\"imagen\":\"").append(r.get("imagen")).append("\",")
                        .append("\"idTour\":").append(r.getOrDefault("idTour", 0)).append(",")
                        .append("\"idAgencia\":").append(r.getOrDefault("idAgencia", 0)).append(",")
                        .append("\"calificada\":").append(Boolean.TRUE.equals(r.get("calificada"))).append(",")
                        .append("\"fechaRegistro\":\"").append(r.get("fechaRegistro")).append("\"")
                        .append("}");

                if (i < lista.size() - 1) json.append(",");
            }
        }
        json.append("]}");
        JavaApiServer.sendJsonResponse(exchange, 200, json.toString());
    }
}
