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

    public class ToursAdminHandler implements HttpHandler {
        @Override
        public void handle(HttpExchange exchange) throws IOException {
            JavaApiServer.enableCORS(exchange);
            if ("OPTIONS".equalsIgnoreCase(exchange.getRequestMethod())) {
                exchange.sendResponseHeaders(200, -1);
                return;
            }
            StringBuilder json = new StringBuilder("{\"status\":\"success\",\"data\":[");
            boolean primero = true;
            String sql = "SELECT t.idTour AS id, t.nombre, a.razonSocial AS agencia, d.nombre AS destino, t.duracion, t.precioAdulto AS precio, t.calificacionPromedio AS calificacion, t.estado, COALESCE(ti.url, '../../img/lima.jpg') AS imagen FROM Tour t JOIN Agencia a ON t.idAgencia = a.idAgencia JOIN Destino d ON t.idDestino = d.idDestino LEFT JOIN TourImagen ti ON t.idTour = ti.idTour AND ti.esPrincipal = 1 ORDER BY t.idTour DESC";
            try (Connection con = ConexionDB.getConnection();
                 PreparedStatement ps = con.prepareStatement(sql);
                 ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    if (!primero) json.append(",");
                    primero = false;
                    json.append("{")
                        .append("\"id\":").append(rs.getInt("id")).append(",")
                        .append("\"nombre\":\"").append(JavaApiServer.jsonEscape(rs.getString("nombre"))).append("\",")
                        .append("\"agencia\":\"").append(JavaApiServer.jsonEscape(rs.getString("agencia"))).append("\",")
                        .append("\"destino\":\"").append(JavaApiServer.jsonEscape(rs.getString("destino"))).append("\",")
                        .append("\"duracion\":\"").append(JavaApiServer.jsonEscape(rs.getString("duracion"))).append("\",")
                        .append("\"precio\":").append(rs.getBigDecimal("precio")).append(",")
                        .append("\"calificacion\":").append(rs.getBigDecimal("calificacion")).append(",")
                        .append("\"estado\":\"").append(JavaApiServer.jsonEscape(rs.getString("estado"))).append("\",")
                        .append("\"imagen\":\"").append(JavaApiServer.jsonEscape(rs.getString("imagen"))).append("\"")
                        .append("}");
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
            json.append("]}");
            JavaApiServer.sendJsonResponse(exchange, 200, json.toString());
        }
    }

