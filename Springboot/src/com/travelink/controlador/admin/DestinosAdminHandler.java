package com.travelink.controlador.admin;

import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import com.travelink.config.ConexionDB;
import com.travelink.server.JavaApiServer;

import java.io.IOException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.Statement;
import java.util.Map;

public class DestinosAdminHandler implements HttpHandler {
    @Override
    public void handle(HttpExchange exchange) throws IOException {
        JavaApiServer.enableCORS(exchange);
        if ("OPTIONS".equalsIgnoreCase(exchange.getRequestMethod())) {
            exchange.sendResponseHeaders(200, -1);
            return;
        }

        String method = exchange.getRequestMethod().toUpperCase();

        // 1. POST: Crear destino
        if ("POST".equals(method)) {
            String body = JavaApiServer.readRequestBody(exchange);
            Map<String, Object> params = JavaApiServer.parseJsonOrFormParams(body);
            String nombre = String.valueOf(params.getOrDefault("nombre", "")).trim();
            String descripcion = String.valueOf(params.getOrDefault("descripcion", "")).trim();

            if (nombre.isEmpty()) {
                JavaApiServer.sendJsonResponse(exchange, 400, "{\"status\":\"error\",\"message\":\"El nombre del destino es obligatorio\"}");
                return;
            }

            try (Connection con = ConexionDB.getConnection();
                 PreparedStatement ps = con.prepareStatement("INSERT INTO Destino (nombre, descripcion, estado) VALUES (?, ?, 'ACTIVO')", Statement.RETURN_GENERATED_KEYS)) {
                ps.setString(1, nombre);
                ps.setString(2, descripcion);
                ps.executeUpdate();
                int newId = 0;
                try (ResultSet rs = ps.getGeneratedKeys()) {
                    if (rs.next()) newId = rs.getInt(1);
                }
                JavaApiServer.sendJsonResponse(exchange, 201, "{\"status\":\"success\",\"message\":\"Destino creado exitosamente\",\"idDestino\":" + newId + "}");
                return;
            } catch (Exception e) {
                e.printStackTrace();
                JavaApiServer.sendJsonResponse(exchange, 500, "{\"status\":\"error\",\"message\":\"Error al crear destino: " + JavaApiServer.jsonEscape(e.getMessage()) + "\"}");
                return;
            }
        }

        // 2. PUT: Editar destino
        if ("PUT".equals(method)) {
            String body = JavaApiServer.readRequestBody(exchange);
            Map<String, Object> params = JavaApiServer.parseJsonOrFormParams(body);
            Map<String, String> queryParams = JavaApiServer.parseQueryParams(exchange.getRequestURI().getQuery());

            int idDestino = 0;
            try {
                if (params.containsKey("idDestino")) idDestino = Integer.parseInt(String.valueOf(params.get("idDestino")));
                else if (params.containsKey("id")) idDestino = Integer.parseInt(String.valueOf(params.get("id")));
                else if (queryParams.containsKey("idDestino")) idDestino = Integer.parseInt(queryParams.get("idDestino"));
                else if (queryParams.containsKey("id")) idDestino = Integer.parseInt(queryParams.get("id"));
            } catch (Exception ignored) {}

            String nombre = String.valueOf(params.getOrDefault("nombre", "")).trim();
            String descripcion = String.valueOf(params.getOrDefault("descripcion", "")).trim();
            String estado = String.valueOf(params.getOrDefault("estado", "ACTIVO")).trim().toUpperCase();

            if (idDestino <= 0) {
                JavaApiServer.sendJsonResponse(exchange, 400, "{\"status\":\"error\",\"message\":\"ID de destino inválido\"}");
                return;
            }

            try (Connection con = ConexionDB.getConnection();
                 PreparedStatement ps = con.prepareStatement("UPDATE Destino SET nombre = COALESCE(NULLIF(?, ''), nombre), descripcion = ?, estado = ? WHERE idDestino = ?")) {
                ps.setString(1, nombre);
                ps.setString(2, descripcion);
                ps.setString(3, estado.isEmpty() ? "ACTIVO" : estado);
                ps.setInt(4, idDestino);
                int rows = ps.executeUpdate();
                if (rows > 0) {
                    JavaApiServer.sendJsonResponse(exchange, 200, "{\"status\":\"success\",\"message\":\"Destino actualizado exitosamente\"}");
                } else {
                    JavaApiServer.sendJsonResponse(exchange, 404, "{\"status\":\"error\",\"message\":\"Destino no encontrado\"}");
                }
                return;
            } catch (Exception e) {
                e.printStackTrace();
                JavaApiServer.sendJsonResponse(exchange, 500, "{\"status\":\"error\",\"message\":\"Error al actualizar destino: " + JavaApiServer.jsonEscape(e.getMessage()) + "\"}");
                return;
            }
        }

        // 3. DELETE: Eliminar o desactivar destino
        if ("DELETE".equals(method)) {
            String body = JavaApiServer.readRequestBody(exchange);
            Map<String, Object> params = JavaApiServer.parseJsonOrFormParams(body);
            Map<String, String> queryParams = JavaApiServer.parseQueryParams(exchange.getRequestURI().getQuery());

            int idDestino = 0;
            try {
                if (params.containsKey("idDestino")) idDestino = Integer.parseInt(String.valueOf(params.get("idDestino")));
                else if (params.containsKey("id")) idDestino = Integer.parseInt(String.valueOf(params.get("id")));
                else if (queryParams.containsKey("idDestino")) idDestino = Integer.parseInt(queryParams.get("idDestino"));
                else if (queryParams.containsKey("id")) idDestino = Integer.parseInt(queryParams.get("id"));
            } catch (Exception ignored) {}

            if (idDestino <= 0) {
                JavaApiServer.sendJsonResponse(exchange, 400, "{\"status\":\"error\",\"message\":\"ID de destino inválido\"}");
                return;
            }

            try (Connection con = ConexionDB.getConnection()) {
                // Si tiene tours asociados, dar de baja lógica
                boolean tieneTours = false;
                try (PreparedStatement psCheck = con.prepareStatement("SELECT COUNT(*) FROM Tour WHERE idDestino = ?")) {
                    psCheck.setInt(1, idDestino);
                    try (ResultSet rs = psCheck.executeQuery()) {
                        if (rs.next() && rs.getInt(1) > 0) tieneTours = true;
                    }
                }

                if (tieneTours) {
                    try (PreparedStatement psUp = con.prepareStatement("UPDATE Destino SET estado = 'INACTIVO' WHERE idDestino = ?")) {
                        psUp.setInt(1, idDestino);
                        psUp.executeUpdate();
                    }
                    JavaApiServer.sendJsonResponse(exchange, 200, "{\"status\":\"success\",\"message\":\"Destino desactivado lógicamente por tener tours asociados\"}");
                } else {
                    try (PreparedStatement psDel = con.prepareStatement("DELETE FROM Destino WHERE idDestino = ?")) {
                        psDel.setInt(1, idDestino);
                        psDel.executeUpdate();
                    }
                    JavaApiServer.sendJsonResponse(exchange, 200, "{\"status\":\"success\",\"message\":\"Destino eliminado exitosamente\"}");
                }
                return;
            } catch (Exception e) {
                e.printStackTrace();
                JavaApiServer.sendJsonResponse(exchange, 500, "{\"status\":\"error\",\"message\":\"Error al eliminar destino: " + JavaApiServer.jsonEscape(e.getMessage()) + "\"}");
                return;
            }
        }

        // 4. GET: Listar destinos
        StringBuilder json = new StringBuilder("{\"status\":\"success\",\"data\":[");
        boolean primero = true;

        String sql = "SELECT d.idDestino AS id, d.nombre, COALESCE(d.descripcion, '') AS descripcion, COALESCE(d.estado, 'ACTIVO') AS estado, " +
                     "(SELECT COUNT(*) FROM Tour t WHERE t.idDestino = d.idDestino AND (UPPER(t.estado) = 'ACTIVO' OR UPPER(t.estado) = 'PUBLICADO')) AS toursActivos " +
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
