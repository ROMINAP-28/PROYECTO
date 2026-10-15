package com.travelink.controlador.admin;

import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import com.travelink.config.ConexionDB;
import com.travelink.server.JavaApiServer;

import java.io.IOException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.Map;

public class UsuariosAdminHandler implements HttpHandler {
    @Override
    public void handle(HttpExchange exchange) throws IOException {
        JavaApiServer.enableCORS(exchange);
        if ("OPTIONS".equalsIgnoreCase(exchange.getRequestMethod())) {
            exchange.sendResponseHeaders(200, -1);
            return;
        }

        String path = exchange.getRequestURI().getPath();
        String method = exchange.getRequestMethod();

        if ("POST".equalsIgnoreCase(method) || "DELETE".equalsIgnoreCase(method)) {
            try {
                String body = JavaApiServer.readRequestBody(exchange);
                Map<String, Object> params = JavaApiServer.parseJsonOrFormParams(body);
                Map<String, String> queryParams = JavaApiServer.parseQueryParams(exchange.getRequestURI().getQuery());

                String action = String.valueOf(params.getOrDefault("action", params.getOrDefault("accion", "")));
                int idUsuario = 0;
                try {
                    if (params.containsKey("idUsuario")) {
                        idUsuario = Integer.parseInt(String.valueOf(params.get("idUsuario")));
                    } else if (params.containsKey("id")) {
                        idUsuario = Integer.parseInt(String.valueOf(params.get("id")));
                    } else if (queryParams.containsKey("idUsuario")) {
                        idUsuario = Integer.parseInt(queryParams.get("idUsuario"));
                    } else if (queryParams.containsKey("id")) {
                        idUsuario = Integer.parseInt(queryParams.get("id"));
                    }
                } catch (Exception ignored) {}

                if (idUsuario <= 0) {
                    JavaApiServer.sendJsonResponse(exchange, 400, "{\"status\":\"error\",\"message\":\"Falta el parámetro idUsuario\"}");
                    return;
                }

                // ACCION: ELIMINAR / DESACTIVAR USUARIO (se refleja en Usuario y Persona)
                if ("eliminar".equalsIgnoreCase(action) || path.endsWith("/eliminar") || "DELETE".equalsIgnoreCase(method)) {
                    try (Connection con = ConexionDB.getConnection()) {
                        String sqlU = "UPDATE Usuario SET estado = 'INACTIVO' WHERE idUsuario = ?";
                        try (PreparedStatement psU = con.prepareStatement(sqlU)) {
                            psU.setInt(1, idUsuario);
                            psU.executeUpdate();
                        }
                        String sqlP = "UPDATE Persona SET estado = 'INACTIVO' WHERE idPersona = (SELECT idPersona FROM Usuario WHERE idUsuario = ?)";
                        try (PreparedStatement psP = con.prepareStatement(sqlP)) {
                            psP.setInt(1, idUsuario);
                            psP.executeUpdate();
                        }
                        JavaApiServer.sendJsonResponse(exchange, 200, "{\"status\":\"success\",\"message\":\"Usuario desactivado lógicamente en Usuario y Persona\"}");
                        return;
                    }
                }

                // ACCION: GUARDAR / CAMBIAR ESTADO
                if ("guardar".equalsIgnoreCase(action) || path.endsWith("/guardar")) {
                    String nuevoEstado = String.valueOf(params.getOrDefault("estado", "ACTIVO")).toUpperCase();
                    if (!"ACTIVO".equals(nuevoEstado) && !"INACTIVO".equals(nuevoEstado) && !"PENDIENTE".equals(nuevoEstado)) {
                        nuevoEstado = "ACTIVO";
                    }

                    try (Connection con = ConexionDB.getConnection()) {
                        String sqlU = "UPDATE Usuario SET estado = ? WHERE idUsuario = ?";
                        try (PreparedStatement psU = con.prepareStatement(sqlU)) {
                            psU.setString(1, nuevoEstado);
                            psU.setInt(2, idUsuario);
                            psU.executeUpdate();
                        }
                        String sqlP = "UPDATE Persona SET estado = ? WHERE idPersona = (SELECT idPersona FROM Usuario WHERE idUsuario = ?)";
                        try (PreparedStatement psP = con.prepareStatement(sqlP)) {
                            psP.setString(1, nuevoEstado);
                            psP.setInt(2, idUsuario);
                            psP.executeUpdate();
                        }
                        JavaApiServer.sendJsonResponse(exchange, 200, "{\"status\":\"success\",\"message\":\"Usuario actualizado a " + nuevoEstado + "\"}");
                        return;
                    }
                }

                JavaApiServer.sendJsonResponse(exchange, 400, "{\"status\":\"error\",\"message\":\"Acción no soportada\"}");
                return;
            } catch (Exception e) {
                e.printStackTrace();
                JavaApiServer.sendJsonResponse(exchange, 500, "{\"status\":\"error\",\"message\":\"Error interno: " + JavaApiServer.jsonEscape(e.getMessage()) + "\"}");
                return;
            }
        }

        // LISTAR USUARIOS
        StringBuilder json = new StringBuilder("{\"status\":\"success\",\"data\":[");
        boolean primero = true;
        String sql = """
            SELECT u.idUsuario AS id,
                   COALESCE(NULLIF(TRIM(CONCAT(p.nombre, ' ', p.apellidoPaterno)), ''), u.nombreUsuario) AS nombre,
                   COALESCE(p.email, '') AS email,
                   COALESCE(r.nombreRol, 'Turista') AS tipo,
                   COALESCE(u.estado, 'ACTIVO') AS estado,
                   DATE(COALESCE(u.fechaRegistro, NOW())) AS fechaRegistro
            FROM Usuario u
            LEFT JOIN Persona p ON u.idPersona = p.idPersona
            LEFT JOIN Rol r ON u.idRol = r.idRol
            ORDER BY u.idUsuario DESC
        """;
        try (Connection con = ConexionDB.getConnection();
             PreparedStatement ps = con.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                if (!primero) json.append(",");
                primero = false;
                json.append("{")
                    .append("\"id\":").append(rs.getInt("id")).append(",")
                    .append("\"nombre\":\"").append(JavaApiServer.jsonEscape(rs.getString("nombre"))).append("\",")
                    .append("\"email\":\"").append(JavaApiServer.jsonEscape(rs.getString("email"))).append("\",")
                    .append("\"tipo\":\"").append(JavaApiServer.jsonEscape(rs.getString("tipo"))).append("\",")
                    .append("\"estado\":\"").append(JavaApiServer.jsonEscape(rs.getString("estado"))).append("\",")
                    .append("\"fechaRegistro\":\"").append(rs.getString("fechaRegistro")).append("\"")
                    .append("}");
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        json.append("]}");
        JavaApiServer.sendJsonResponse(exchange, 200, json.toString());
    }
}
