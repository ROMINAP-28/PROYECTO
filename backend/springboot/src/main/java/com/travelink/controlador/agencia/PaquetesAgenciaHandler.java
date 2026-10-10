package com.travelink.controlador.agencia;

import com.travelink.server.JavaApiServer;

import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import com.travelink.config.ConexionDB;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.math.BigDecimal;
import java.nio.charset.StandardCharsets;
import java.sql.*;
import java.util.HashMap;
import java.util.Map;

public class PaquetesAgenciaHandler implements HttpHandler {

    @Override
    public void handle(HttpExchange exchange) throws IOException {
        JavaApiServer.enableCORS(exchange);

        if ("OPTIONS".equalsIgnoreCase(exchange.getRequestMethod())) {
            exchange.sendResponseHeaders(200, -1);
            return;
        }

        if ("GET".equalsIgnoreCase(exchange.getRequestMethod())) {
            listarPaquetes(exchange);
            return;
        }

        if ("POST".equalsIgnoreCase(exchange.getRequestMethod())) {
            procesarPostPaquete(exchange);
            return;
        }

        JavaApiServer.sendJsonResponse(exchange, 405, "{\"status\":\"error\",\"message\":\"Método no permitido\"}");
    }

    private void listarPaquetes(HttpExchange exchange) throws IOException {
        Map<String, String> query = JavaApiServer.parseQueryParams(exchange.getRequestURI().getQuery());
        int idAgencia = 0;
        try {
            if (query.containsKey("idAgencia")) {
                idAgencia = Integer.parseInt(query.get("idAgencia"));
            }
        } catch (Exception ignored) {}
        
        boolean isDashboard = "true".equalsIgnoreCase(query.getOrDefault("dashboard", "false"));

        StringBuilder json = new StringBuilder("{\"status\":\"success\",\"paquetes\":[");
        boolean primero = true;
        boolean exitoBD = false;

        String condAgencia = " WHERE 1=1 ";
        if (idAgencia > 0) {
            condAgencia += " AND p.idAgencia = ? ";
        }
        if (!isDashboard) {
            condAgencia += " AND (p.estado = 'PUBLICADO' OR p.estado IS NULL) ";
        }

        String[] sqls = {
            "SELECT p.idPaquete, p.idAgencia, p.nombre, p.descripcion, p.precio, p.duracion, p.condiciones, p.estado, COALESCE(p.descuento, 0) AS descuento, COALESCE(p.imagen, '') AS imagen, COALESCE(NULLIF(a.nombreComercial, ''), a.razonSocial, 'Agencia Travelink') AS agenciaNombre, COALESCE(p.cupoTotal, 30) AS cupoTotal, COALESCE(p.cuposDisponibles, 30) AS cuposDisponibles, COALESCE(p.minAsientosOferta, 5) AS minAsientosOferta, COALESCE(p.servicios, 'City Tour y guiado especializado,Traslado privado') AS servicios, COALESCE(p.galeria, '[]') AS galeria FROM PaqueteTuristico p LEFT JOIN Agencia a ON p.idAgencia = a.idAgencia" + condAgencia + "ORDER BY p.idPaquete DESC",
            "SELECT p.idPaquete, p.idAgencia, p.nombre, p.descripcion, COALESCE(p.descuento, 0) AS descuento, COALESCE(p.imagenUrl, '') AS imagen, p.estado, 600.00 AS precio, '2 días' AS duracion, 'Traslados e impuestos incluidos' AS condiciones, COALESCE(NULLIF(a.nombreComercial, ''), a.razonSocial, 'Agencia Travelink') AS agenciaNombre, 30 AS cupoTotal, 30 AS cuposDisponibles, 5 AS minAsientosOferta, 'City Tour y guiado especializado,Traslado privado' AS servicios, '[]' AS galeria FROM Paquete p LEFT JOIN Agencia a ON p.idAgencia = a.idAgencia" + condAgencia + "ORDER BY p.idPaquete DESC"
        };

        for (String sql : sqls) {
            try (Connection con = ConexionDB.getConnection();
                 PreparedStatement ps = con.prepareStatement(sql)) {
                if (idAgencia > 0) {
                    ps.setInt(1, idAgencia);
                }
                try (ResultSet rs = ps.executeQuery()) {
                    while (rs.next()) {
                        if (!primero) json.append(",");
                        primero = false;
                        exitoBD = true;
                        int idPaq = rs.getInt("idPaquete");
                        int idAg = rs.getInt("idAgencia");
                        BigDecimal precioVal = BigDecimal.valueOf(550.00);
                        try { precioVal = rs.getBigDecimal("precio"); } catch(Exception e) {}
                        
                        json.append("{")
                                .append("\"idPaquete\":").append(idPaq).append(",")
                                .append("\"idAgencia\":").append(idAg).append(",")
                                .append("\"agencia\":\"").append(JavaApiServer.jsonEscape(rs.getString("agenciaNombre"))).append("\",")
                                .append("\"nombre\":\"").append(JavaApiServer.jsonEscape(rs.getString("nombre"))).append("\",")
                                .append("\"descripcion\":\"").append(JavaApiServer.jsonEscape(rs.getString("descripcion"))).append("\",")
                                .append("\"precio\":").append(precioVal != null ? precioVal : 550.00).append(",")
                                .append("\"duracion\":\"").append(JavaApiServer.jsonEscape(rs.getString("duracion"))).append("\",")
                                .append("\"condiciones\":\"").append(JavaApiServer.jsonEscape(rs.getString("condiciones"))).append("\",")
                                .append("\"estado\":\"").append(JavaApiServer.jsonEscape(rs.getString("estado"))).append("\",")
                                .append("\"descuento\":").append(rs.getInt("descuento")).append(",")
                                .append("\"imagen\":\"").append(JavaApiServer.jsonEscape(rs.getString("imagen"))).append("\",")
                                .append("\"cuposDisponibles\":").append(rs.getInt("cuposDisponibles")).append(",")
                                .append("\"cupoTotal\":").append(rs.getInt("cupoTotal")).append(",")
                                .append("\"minAsientosOferta\":").append(rs.getInt("minAsientosOferta")).append(",")
                                .append("\"servicios\":[\"").append(JavaApiServer.jsonEscape(rs.getString("servicios")).replace(",", "\",\"")).append("\"],")
                                .append("\"galeria\":").append(rs.getString("galeria") != null && rs.getString("galeria").startsWith("[") ? rs.getString("galeria") : "[]").append("}");
                    }
                }
                if (exitoBD) break;
            } catch (Exception e) {
                // Ignore and try next SQL
            }
        }



        json.append("]}");
        JavaApiServer.sendJsonResponse(exchange, 200, json.toString());
    }

    private void procesarPostPaquete(HttpExchange exchange) throws IOException {
        String body = JavaApiServer.readRequestBody(exchange);
        Map<String, Object> params = JavaApiServer.parseJsonOrFormParams(body);
        String action = String.valueOf(params.getOrDefault("action", "guardar")).trim().toLowerCase();

        if ("publicar".equals(action)) {
            cambiarEstado(exchange, params, "PUBLICADO");
            return;
        }
        if ("pausar".equals(action)) {
            cambiarEstado(exchange, params, "PAUSADO");
            return;
        }
        if ("borrador".equals(action)) {
            cambiarEstado(exchange, params, "BORRADOR");
            return;
        }
        if ("eliminar".equals(action)) {
            eliminarPaquete(exchange, params);
            return;
        }

        guardarPaquete(exchange, params);
    }

    private void cambiarEstado(HttpExchange exchange, Map<String, Object> params, String nuevoEstado) throws IOException {
        int idPaquete = 0;
        try { idPaquete = Integer.parseInt(String.valueOf(params.getOrDefault("idPaquete", "0"))); } catch (Exception ignored) {}
        int idAgencia = 1;
        try { idAgencia = Integer.parseInt(String.valueOf(params.getOrDefault("idAgencia", "1"))); } catch (Exception ignored) {}

        if (idPaquete <= 0) {
            JavaApiServer.sendJsonResponse(exchange, 400, "{\"status\":\"error\",\"message\":\"ID de paquete inválido\"}");
            return;
        }

        String sql = "UPDATE PaqueteTuristico SET estado = ? WHERE idPaquete = ? AND idAgencia = ?";
        try (Connection con = ConexionDB.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setString(1, nuevoEstado);
            ps.setInt(2, idPaquete);
            ps.setInt(3, idAgencia);
            int filas = ps.executeUpdate();
            if (filas > 0) {
                JavaApiServer.sendJsonResponse(exchange, 200, "{\"status\":\"success\",\"message\":\"Estado actualizado a " + nuevoEstado + "\"}");
            } else {
                JavaApiServer.sendJsonResponse(exchange, 404, "{\"status\":\"error\",\"message\":\"Paquete no encontrado\"}");
            }
        } catch (Exception e) {
            e.printStackTrace();
            JavaApiServer.sendJsonResponse(exchange, 500, "{\"status\":\"error\",\"message\":\"Error al cambiar estado: " + JavaApiServer.jsonEscape(e.getMessage()) + "\"}");
        }
    }

    private void eliminarPaquete(HttpExchange exchange, Map<String, Object> params) throws IOException {
        int idPaquete = 0;
        try { idPaquete = Integer.parseInt(String.valueOf(params.getOrDefault("idPaquete", "0"))); } catch (Exception ignored) {}
        int idAgencia = 1;
        try { idAgencia = Integer.parseInt(String.valueOf(params.getOrDefault("idAgencia", "1"))); } catch (Exception ignored) {}

        if (idPaquete <= 0) {
            JavaApiServer.sendJsonResponse(exchange, 400, "{\"status\":\"error\",\"message\":\"ID de paquete inválido\"}");
            return;
        }

        try (Connection con = ConexionDB.getConnection()) {
            con.setAutoCommit(false);
            try {
                try (PreparedStatement psD = con.prepareStatement("DELETE FROM DetallePaquete WHERE idPaquete = ?")) {
                    psD.setInt(1, idPaquete);
                    psD.executeUpdate();
                }
                try (PreparedStatement psP = con.prepareStatement("DELETE FROM PaqueteTuristico WHERE idPaquete = ? AND idAgencia = ?")) {
                    psP.setInt(1, idPaquete);
                    psP.setInt(2, idAgencia);
                    psP.executeUpdate();
                }
                con.commit();
                JavaApiServer.sendJsonResponse(exchange, 200, "{\"status\":\"success\",\"message\":\"Paquete eliminado exitosamente\"}");
            } catch (Exception e) {
                con.rollback();
                throw e;
            } finally {
                con.setAutoCommit(true);
            }
        } catch (Exception e) {
            e.printStackTrace();
            JavaApiServer.sendJsonResponse(exchange, 500, "{\"status\":\"error\",\"message\":\"Error al eliminar paquete: " + JavaApiServer.jsonEscape(e.getMessage()) + "\"}");
        }
    }

    private void guardarPaquete(HttpExchange exchange, Map<String, Object> params) throws IOException {
        int idPaquete = 0;
        try { idPaquete = Integer.parseInt(String.valueOf(params.getOrDefault("idPaquete", "0"))); } catch (Exception ignored) {}
        int idAgencia = 1;
        try { idAgencia = Integer.parseInt(String.valueOf(params.getOrDefault("idAgencia", "1"))); } catch (Exception ignored) {}

        String nombre = String.valueOf(params.getOrDefault("nombre", "")).trim();
        String descripcion = String.valueOf(params.getOrDefault("descripcion", "")).trim();
        BigDecimal precio = BigDecimal.ZERO;
        try { precio = new BigDecimal(String.valueOf(params.getOrDefault("precio", "0")).trim()); } catch (Exception ignored) {}
        String duracion = String.valueOf(params.getOrDefault("duracion", "1 día")).trim();
        String condiciones = String.valueOf(params.getOrDefault("condiciones", "Incluye traslados y guiado profesional")).trim();
        String estado = String.valueOf(params.getOrDefault("estado", "PUBLICADO")).trim().toUpperCase();
        int descuento = 0;
        try { descuento = Integer.parseInt(String.valueOf(params.getOrDefault("descuento", "0")).trim()); } catch (Exception ignored) {}
        String imagen = String.valueOf(params.getOrDefault("imagen", "")).trim();
        if (imagen.isEmpty()) {
            imagen = "https://images.unsplash.com/photo-1510812431401-41d2bd2722f3?w=600&auto=format&fit=crop";
        }
        
        int cupoTotal = 30;
        try { cupoTotal = Integer.parseInt(String.valueOf(params.getOrDefault("cupoTotal", "30")).trim()); } catch (Exception ignored) {}
        int minAsientosOferta = 5;
        try { minAsientosOferta = Integer.parseInt(String.valueOf(params.getOrDefault("minAsientosOferta", "5")).trim()); } catch (Exception ignored) {}
        
        int cuposDisponibles = cupoTotal; // initially
        try { 
            if (params.containsKey("cuposDisponibles")) {
                cuposDisponibles = Integer.parseInt(String.valueOf(params.get("cuposDisponibles")).trim()); 
            }
        } catch (Exception ignored) {}
        
        String servicios = String.valueOf(params.getOrDefault("servicios", "City Tour y guiado especializado,Traslado privado")).trim();
        
        String galeria = "[]";
        if (params.containsKey("galeria") && params.get("galeria") instanceof java.util.List) {
            java.util.List<?> list = (java.util.List<?>) params.get("galeria");
            StringBuilder sb = new StringBuilder("[");
            for (int i=0; i<list.size(); i++) {
                if (i>0) sb.append(",");
                sb.append("\"").append(JavaApiServer.jsonEscape(String.valueOf(list.get(i)))).append("\"");
            }
            sb.append("]");
            galeria = sb.toString();
        }

        if (nombre.isEmpty() || precio.compareTo(BigDecimal.ZERO) <= 0) {
            JavaApiServer.sendJsonResponse(exchange, 400, "{\"status\":\"error\",\"message\":\"Nombre y precio mayor a 0 son obligatorios\"}");
            return;
        }

        try (Connection con = ConexionDB.getConnection()) {
            con.setAutoCommit(false);
            try {
                if (idPaquete > 0) {
                    String sqlUp = "UPDATE PaqueteTuristico SET nombre=?, descripcion=?, precio=?, duracion=?, condiciones=?, estado=?, descuento=?, imagen=?, cupoTotal=?, cuposDisponibles=?, minAsientosOferta=?, servicios=?, galeria=? WHERE idPaquete=? AND idAgencia=?";
                    try (PreparedStatement ps = con.prepareStatement(sqlUp)) {
                        ps.setString(1, nombre);
                        ps.setString(2, descripcion);
                        ps.setBigDecimal(3, precio);
                        ps.setString(4, duracion);
                        ps.setString(5, condiciones);
                        ps.setString(6, estado);
                        ps.setInt(7, descuento);
                        ps.setString(8, imagen);
                        ps.setInt(9, cupoTotal);
                        ps.setInt(10, cuposDisponibles);
                        ps.setInt(11, minAsientosOferta);
                        ps.setString(12, servicios);
                        ps.setString(13, galeria);
                        ps.setInt(14, idPaquete);
                        ps.setInt(15, idAgencia);
                        ps.executeUpdate();
                    }
                } else {
                    String sqlIns = "INSERT INTO PaqueteTuristico (idAgencia, nombre, descripcion, precio, duracion, condiciones, estado, descuento, imagen, cupoTotal, cuposDisponibles, minAsientosOferta, servicios, galeria) " +
                                    "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";
                    try (PreparedStatement ps = con.prepareStatement(sqlIns, Statement.RETURN_GENERATED_KEYS)) {
                        ps.setInt(1, idAgencia);
                        ps.setString(2, nombre);
                        ps.setString(3, descripcion);
                        ps.setBigDecimal(4, precio);
                        ps.setString(5, duracion);
                        ps.setString(6, condiciones);
                        ps.setString(7, estado);
                        ps.setInt(8, descuento);
                        ps.setString(9, imagen);
                        ps.setInt(10, cupoTotal);
                        ps.setInt(11, cuposDisponibles);
                        ps.setInt(12, minAsientosOferta);
                        ps.setString(13, servicios);
                        ps.setString(14, galeria);
                        ps.executeUpdate();
                        try (ResultSet rsK = ps.getGeneratedKeys()) {
                            if (rsK.next()) idPaquete = rsK.getInt(1);
                        }
                    }

                }

                con.commit();
                JavaApiServer.sendJsonResponse(exchange, 200, "{\"status\":\"success\",\"message\":\"Paquete guardado exitosamente\",\"idPaquete\":" + idPaquete + "}");
            } catch (Exception e) {
                con.rollback();
                throw e;
            } finally {
                con.setAutoCommit(true);
            }
        } catch (Exception e) {
            e.printStackTrace();
            JavaApiServer.sendJsonResponse(exchange, 500, "{\"status\":\"error\",\"message\":\"Error al guardar paquete: " + JavaApiServer.jsonEscape(e.getMessage()) + "\"}");
        }
    }

    private static void enableCORS(HttpExchange exchange) {
        exchange.getResponseHeaders().set("Access-Control-Allow-Origin", "*");
        exchange.getResponseHeaders().set("Access-Control-Allow-Methods", "GET, POST, OPTIONS");
        exchange.getResponseHeaders().set("Access-Control-Allow-Headers", "Content-Type, Authorization");
    }

    private static void sendJsonResponse(HttpExchange exchange, int statusCode, String jsonResponse) throws IOException {
        JavaApiServer.enableCORS(exchange);
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

    private static Map<String, String> parseQueryParams(String query) {
        Map<String, String> map = new HashMap<>();
        if (query == null || query.isEmpty()) return map;
        for (String param : query.split("&")) {
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
