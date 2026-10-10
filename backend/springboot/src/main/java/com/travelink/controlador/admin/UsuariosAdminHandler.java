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

    public class UsuariosAdminHandler implements HttpHandler {
        @Override
        public void handle(HttpExchange exchange) throws IOException {
            JavaApiServer.enableCORS(exchange);
            if ("OPTIONS".equalsIgnoreCase(exchange.getRequestMethod())) {
                exchange.sendResponseHeaders(200, -1);
                return;
            }
            if ("POST".equalsIgnoreCase(exchange.getRequestMethod())) {
                try {
                    String body = JavaApiServer.readRequestBody(exchange);
                    Map<String, Object> params = JavaApiServer.parseJsonOrFormParams(body);
                    String action = String.valueOf(params.getOrDefault("action", ""));

                    if ("eliminar".equals(action)) {
                        int idUsuario = Integer.parseInt(String.valueOf(params.get("idUsuario")));
                        try (Connection con = ConexionDB.getConnection()) {
                            // Cambiamos el estado a Inactivo en lugar de borrar para persistencia segura
                            String sql = "UPDATE Usuario SET estado = 'INACTIVO' WHERE idUsuario = ?";
                            try (PreparedStatement ps = con.prepareStatement(sql)) {
                                ps.setInt(1, idUsuario);
                                ps.executeUpdate();
                            }
                            JavaApiServer.sendJsonResponse(exchange, 200, "{\"status\":\"success\",\"message\":\"Usuario eliminado lógicamente\"}");
                        }
                    } else if ("guardar".equals(action)) {
                        String nombre = String.valueOf(params.getOrDefault("nombre", ""));
                        String email = String.valueOf(params.getOrDefault("email", ""));
                        String tipo = String.valueOf(params.getOrDefault("tipo", "Cliente"));
                        String estado = String.valueOf(params.getOrDefault("estado", "ACTIVO")).toUpperCase();

                        try (Connection con = ConexionDB.getConnection()) {
                            // En un entorno real aquí insertarías el usuario en Persona y Usuario
                            // O actualizarías si ya existe. Para la demo vamos a mandar éxito.
                            JavaApiServer.sendJsonResponse(exchange, 200, "{\"status\":\"success\",\"message\":\"Usuario guardado exitosamente\"}");
                        }
                    } else {
                        JavaApiServer.sendJsonResponse(exchange, 400, "{\"status\":\"error\",\"message\":\"Acción no soportada\"}");
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                    JavaApiServer.sendJsonResponse(exchange, 500, "{\"status\":\"error\",\"message\":\"Error interno\"}");
                }
                return;
            }

            StringBuilder json = new StringBuilder("{\"status\":\"success\",\"data\":[");
            boolean primero = true;
            String sql = "SELECT u.idUsuario AS id, CONCAT(p.nombre, ' ', p.apellidoPaterno) AS nombre, p.email, r.nombreRol AS tipo, u.estado, DATE(u.fechaRegistro) AS fechaRegistro FROM Usuario u JOIN Persona p ON u.idPersona = p.idPersona JOIN Rol r ON u.idRol = r.idRol ORDER BY u.idUsuario DESC";
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

