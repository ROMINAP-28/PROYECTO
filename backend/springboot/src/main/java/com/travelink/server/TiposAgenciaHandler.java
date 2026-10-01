package com.travelink.server;

import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import com.travelink.config.ConexionDB;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.charset.StandardCharsets;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.HashMap;
import java.util.Map;

public class TiposAgenciaHandler implements HttpHandler {

    @Override
    public void handle(HttpExchange exchange) throws IOException {
        enableCORS(exchange);

        if ("OPTIONS".equalsIgnoreCase(exchange.getRequestMethod())) {
            exchange.sendResponseHeaders(200, -1);
            return;
        }

        if ("GET".equalsIgnoreCase(exchange.getRequestMethod())) {
            listarTipos(exchange);
            return;
        }

        if ("POST".equalsIgnoreCase(exchange.getRequestMethod())) {
            procesarPostTipo(exchange);
            return;
        }

        sendJsonResponse(exchange, 405, "{\"status\":\"error\",\"message\":\"Método no permitido\"}");
    }

    private void listarTipos(HttpExchange exchange) throws IOException {
        StringBuilder json = new StringBuilder("{\"status\":\"success\",\"tipos\":[");
        String sql = "SELECT idTipo, nombre, descripcion, estado FROM TipoServicio WHERE estado != 'ELIMINADO' ORDER BY idTipo ASC";

        try (Connection con = ConexionDB.getConnection();
             PreparedStatement ps = con.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {

            boolean primero = true;
            while (rs.next()) {
                if (!primero) json.append(",");
                primero = false;
                json.append("{")
                        .append("\"idTipo\":").append(rs.getInt("idTipo")).append(",")
                        .append("\"nombre\":\"").append(jsonEscape(rs.getString("nombre"))).append("\",")
                        .append("\"descripcion\":\"").append(jsonEscape(rs.getString("descripcion"))).append("\",")
                        .append("\"estado\":\"").append(jsonEscape(rs.getString("estado"))).append("\"")
                        .append("}");
            }
            json.append("]}");
            sendJsonResponse(exchange, 200, json.toString());

        } catch (Exception e) {
            e.printStackTrace();
            sendJsonResponse(exchange, 500, "{\"status\":\"error\",\"message\":\"Error al listar tipos: " + jsonEscape(e.getMessage()) + "\"}");
        }
    }

    private void procesarPostTipo(HttpExchange exchange) throws IOException {
        String body = readRequestBody(exchange);
        Map<String, Object> params = parseJsonOrFormParams(body);
        String action = String.valueOf(params.getOrDefault("action", "crear")).trim().toLowerCase();

        if ("eliminar".equals(action)) {
            eliminarTipo(exchange, params);
            return;
        }
        if ("editar".equals(action)) {
            editarTipo(exchange, params);
            return;
        }
        crearTipo(exchange, params);
    }

    private void crearTipo(HttpExchange exchange, Map<String, Object> params) throws IOException {
        String nombre = String.valueOf(params.getOrDefault("nombre", "")).trim();
        String descripcion = String.valueOf(params.getOrDefault("descripcion", "")).trim();

        if (nombre.isEmpty()) {
            sendJsonResponse(exchange, 400, "{\"status\":\"error\",\"message\":\"El nombre del tipo es obligatorio\"}");
            return;
        }

        String sql = "INSERT INTO TipoServicio (nombre, descripcion, estado) VALUES (?, ?, 'ACTIVO') " +
                     "ON DUPLICATE KEY UPDATE descripcion = VALUES(descripcion), estado = 'ACTIVO'";

        try (Connection con = ConexionDB.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setString(1, nombre);
            ps.setString(2, descripcion);
            ps.executeUpdate();
            sendJsonResponse(exchange, 200, "{\"status\":\"success\",\"message\":\"Tipo guardado exitosamente\"}");
        } catch (Exception e) {
            e.printStackTrace();
            sendJsonResponse(exchange, 500, "{\"status\":\"error\",\"message\":\"Error al crear tipo: " + jsonEscape(e.getMessage()) + "\"}");
        }
    }

    private void editarTipo(HttpExchange exchange, Map<String, Object> params) throws IOException {
        int idTipo = 0;
        try { idTipo = Integer.parseInt(String.valueOf(params.getOrDefault("idTipo", "0"))); } catch (Exception ignored) {}
        String nombre = String.valueOf(params.getOrDefault("nombre", "")).trim();
        String descripcion = String.valueOf(params.getOrDefault("descripcion", "")).trim();

        if (idTipo <= 0 || nombre.isEmpty()) {
            sendJsonResponse(exchange, 400, "{\"status\":\"error\",\"message\":\"Datos insuficientes para editar\"}");
            return;
        }

        String sql = "UPDATE TipoServicio SET nombre = ?, descripcion = ? WHERE idTipo = ?";
        try (Connection con = ConexionDB.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setString(1, nombre);
            ps.setString(2, descripcion);
            ps.setInt(3, idTipo);
            ps.executeUpdate();
            sendJsonResponse(exchange, 200, "{\"status\":\"success\",\"message\":\"Tipo actualizado exitosamente\"}");
        } catch (Exception e) {
            e.printStackTrace();
            sendJsonResponse(exchange, 500, "{\"status\":\"error\",\"message\":\"Error al actualizar tipo: " + jsonEscape(e.getMessage()) + "\"}");
        }
    }

    private void eliminarTipo(HttpExchange exchange, Map<String, Object> params) throws IOException {
        int idTipo = 0;
        try { idTipo = Integer.parseInt(String.valueOf(params.getOrDefault("idTipo", "0"))); } catch (Exception ignored) {}

        if (idTipo <= 0) {
            sendJsonResponse(exchange, 400, "{\"status\":\"error\",\"message\":\"ID de tipo inválido\"}");
            return;
        }

        String sql = "UPDATE TipoServicio SET estado = 'ELIMINADO' WHERE idTipo = ?";
        try (Connection con = ConexionDB.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setInt(1, idTipo);
            ps.executeUpdate();
            sendJsonResponse(exchange, 200, "{\"status\":\"success\",\"message\":\"Tipo eliminado exitosamente\"}");
        } catch (Exception e) {
            e.printStackTrace();
            sendJsonResponse(exchange, 500, "{\"status\":\"error\",\"message\":\"Error al eliminar tipo: " + jsonEscape(e.getMessage()) + "\"}");
        }
    }

    private static void enableCORS(HttpExchange exchange) {
        exchange.getResponseHeaders().set("Access-Control-Allow-Origin", "*");
        exchange.getResponseHeaders().set("Access-Control-Allow-Methods", "GET, POST, OPTIONS");
        exchange.getResponseHeaders().set("Access-Control-Allow-Headers", "Content-Type, Authorization");
    }

    private static void sendJsonResponse(HttpExchange exchange, int statusCode, String jsonResponse) throws IOException {
        enableCORS(exchange);
        byte[] bytes = jsonResponse.getBytes(StandardCharsets.UTF_8);
        exchange.getResponseHeaders().set("Content-Type", "application/json; charset=UTF-8");
        exchange.sendResponseHeaders(statusCode, bytes.length);
        try (OutputStream os = exchange.getResponseBody()) {
            os.write(bytes);
        }
    }

    private static String readRequestBody(HttpExchange exchange) throws IOException {
        InputStream is = exchange.getRequestBody();
        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        byte[] buffer = new byte[1024];
        int len;
        while ((len = is.read(buffer)) != -1) {
            bos.write(buffer, 0, len);
        }
        return bos.toString(StandardCharsets.UTF_8);
    }

    private static Map<String, Object> parseJsonOrFormParams(String body) {
        Map<String, Object> map = new HashMap<>();
        if (body == null || body.trim().isEmpty()) return map;
        body = body.trim();
        if (body.startsWith("{")) {
            String inside = body.substring(1, body.length() - 1).trim();
            for (String part : inside.split(",(?=(?:[^\"]*\"[^\"]*\")*[^\"]*$)")) {
                String[] kv = part.split(":", 2);
                if (kv.length == 2) {
                    String k = kv[0].trim().replace("\"", "");
                    String v = kv[1].trim().replace("\"", "");
                    map.put(k, v);
                }
            }
            return map;
        }
        for (String param : body.split("&")) {
            String[] pair = param.split("=");
            if (pair.length > 1) {
                try {
                    map.put(pair[0], java.net.URLDecoder.decode(pair[1], StandardCharsets.UTF_8.name()));
                } catch (Exception e) {
                    map.put(pair[0], pair[1]);
                }
            }
        }
        return map;
    }

    private static String jsonEscape(String s) {
        if (s == null) return "";
        return s.replace("\\", "\\\\").replace("\"", "\\\"").replace("\n", "\\n").replace("\r", "\\r");
    }
}
