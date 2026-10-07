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

    public class DestinosAdminHandler implements HttpHandler {
        @Override
        public void handle(HttpExchange exchange) throws IOException {
            JavaApiServer.enableCORS(exchange);
            if ("OPTIONS".equalsIgnoreCase(exchange.getRequestMethod())) {
                exchange.sendResponseHeaders(200, -1);
                return;
            }

            StringBuilder json = new StringBuilder("{\"status\":\"success\",\"data\":[");
            boolean primero = true;

            String sql = "SELECT d.idDestino AS id, d.nombre, COALESCE(d.descripcion, '') AS descripcion, COALESCE(d.estado, 'ACTIVO') AS estado, " +
                         "(SELECT COUNT(*) FROM Tour t WHERE t.idDestino = d.idDestino AND UPPER(t.estado) = 'ACTIVO') AS toursActivos " +
                         "FROM Destino d ORDER BY d.idDestino ASC";

            try (Connection con = ConexionDB.getConnection();
                 PreparedStatement ps = con.prepareStatement(sql);
                 ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    if (!primero) json.append(",");
                    primero = false;

                    String nombreDest = rs.getString("nombre");
                    String lower = (nombreDest != null ? nombreDest : "").toLowerCase().replaceAll("[^a-z0-9]", "");
                    String img = "../../img/cusco.jpg";
                    if (lower.contains("lima")) img = "../../img/lima.jpg";
                    else if (lower.contains("pasco")) img = "../../img/pasco.jpg";
                    else if (lower.contains("moquegua")) img = "../../img/moquegua.jpg";
                    else if (lower.contains("ilo")) img = "../../img/ilo.jpg";
                    else if (lower.contains("madre") || lower.contains("dios")) img = "../../img/madrededios.jpg";
                    else if (lower.contains("tacna")) img = "../../img/arequipa.jpg";
                    else if (lower.contains("machu") || lower.contains("cusco")) img = "../../img/cusco.jpg";

                    String est = rs.getString("estado");
                    if (est != null && est.equalsIgnoreCase("ACTIVO")) est = "Activo";
                    else if (est == null || est.isEmpty()) est = "Activo";

                    json.append("{")
                        .append("\"id\":").append(rs.getInt("id")).append(",")
                        .append("\"nombre\":\"").append(JavaApiServer.jsonEscape(nombreDest)).append("\",")
                        .append("\"descripcion\":\"").append(JavaApiServer.jsonEscape(rs.getString("descripcion"))).append("\",")
                        .append("\"toursActivos\":").append(rs.getInt("toursActivos")).append(",")
                        .append("\"estado\":\"").append(JavaApiServer.jsonEscape(est)).append("\",")
                        .append("\"imagen\":\"").append(img).append("\"")
                        .append("}");
                }
            } catch (Exception e) {
                e.printStackTrace();
            }

            json.append("]}");
            JavaApiServer.sendJsonResponse(exchange, 200, json.toString());
        }
    }

