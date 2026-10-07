package com.travelink.controlador.admin;
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

    public class DestinosToursAdminHandler implements HttpHandler {
        @Override
        public void handle(HttpExchange exchange) throws IOException {
            JavaApiServer.enableCORS(exchange);
            if ("OPTIONS".equalsIgnoreCase(exchange.getRequestMethod())) {
                exchange.sendResponseHeaders(200, -1);
                return;
            }

            Map<String, String> queryParams = JavaApiServer.parseQueryParams(exchange.getRequestURI().getQuery());
            int idDestino = 0;
            try { idDestino = Integer.parseInt(queryParams.getOrDefault("idDestino", queryParams.getOrDefault("id", "0"))); } catch (Exception ignored) {}
            String nombreDestino = queryParams.getOrDefault("destino", queryParams.getOrDefault("nombre", "")).trim();

            StringBuilder json = new StringBuilder("{\"status\":\"success\",\"data\":[");
            boolean primero = true;

            String sql = "SELECT t.idTour AS id, t.nombre, a.razonSocial AS agencia, t.duracion, t.precioAdulto AS precio, t.calificacionPromedio AS calificacion, COALESCE(t.estado, 'ACTIVO') AS estado " +
                         "FROM Tour t " +
                         "INNER JOIN Agencia a ON t.idAgencia = a.idAgencia " +
                         "INNER JOIN Destino d ON t.idDestino = d.idDestino " +
                         "WHERE (d.idDestino = ? OR (LOWER(d.nombre) LIKE LOWER(?) AND ? != '')) " +
                         "ORDER BY t.idTour DESC";

            try (Connection con = ConexionDB.getConnection();
                 PreparedStatement ps = con.prepareStatement(sql)) {
                ps.setInt(1, idDestino);
                ps.setString(2, nombreDestino.isEmpty() ? "%" : "%" + nombreDestino + "%");
                ps.setString(3, nombreDestino);

                try (ResultSet rs = ps.executeQuery()) {
                    while (rs.next()) {
                        if (!primero) json.append(",");
                        primero = false;

                        String est = rs.getString("estado");
                        if (est != null && est.equalsIgnoreCase("ACTIVO")) est = "Activo";

                        json.append("{")
                            .append("\"id\":").append(rs.getInt("id")).append(",")
                            .append("\"nombre\":\"").append(JavaApiServer.jsonEscape(rs.getString("nombre"))).append("\",")
                            .append("\"agencia\":\"").append(JavaApiServer.jsonEscape(rs.getString("agencia"))).append("\",")
                            .append("\"duracion\":\"").append(JavaApiServer.jsonEscape(rs.getString("duracion"))).append("\",")
                            .append("\"precio\":").append(rs.getBigDecimal("precio")).append(",")
                            .append("\"calificacion\":").append(rs.getBigDecimal("calificacion")).append(",")
                            .append("\"estado\":\"").append(JavaApiServer.jsonEscape(est)).append("\"")
                            .append("}");
                    }
                }
            } catch (Exception e) {
                e.printStackTrace();
            }

            json.append("]}");
            JavaApiServer.sendJsonResponse(exchange, 200, json.toString());
        }
    }

