package com.travelink.controlador.turista;
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
                            .append("\"titulo\":\"").append(r.get("titulo")).append("\",")
                            .append("\"ubicacion\":\"").append(r.get("ubicacion")).append("\",")
                            .append("\"fechas\":\"").append(r.get("fechas")).append("\",")
                            .append("\"personas\":\"").append(r.get("personas")).append("\",")
                            .append("\"agencia\":\"").append(r.get("agencia")).append("\",")
                            .append("\"estado\":\"").append(r.get("estado")).append("\",")
                            .append("\"total\":").append(r.get("total")).append(",")
                            .append("\"metodoPago\":\"").append(r.get("metodoPago")).append("\",")
                            .append("\"imagen\":\"").append(r.get("imagen")).append("\",")
                            .append("\"fechaRegistro\":\"").append(r.get("fechaRegistro")).append("\"")
                            .append("}");

                    if (i < lista.size() - 1) json.append(",");
                }
            }
            json.append("]}");
            JavaApiServer.sendJsonResponse(exchange, 200, json.toString());
        }
    }

