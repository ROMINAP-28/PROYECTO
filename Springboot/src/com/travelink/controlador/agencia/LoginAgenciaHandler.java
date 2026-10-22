package com.travelink.controlador.agencia;
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

    public class LoginAgenciaHandler implements HttpHandler {

        @Override
        public void handle(HttpExchange exchange) throws IOException {

            if ("OPTIONS".equalsIgnoreCase(exchange.getRequestMethod())) {
                JavaApiServer.enableCORS(exchange);
                exchange.sendResponseHeaders(200, -1);
                return;
            }

            if (!"POST".equalsIgnoreCase(exchange.getRequestMethod())) {
                JavaApiServer.sendJsonResponse(exchange, 405,
                        "{\"status\":\"error\",\"message\":\"Método no permitido\"}");
                return;
            }

            Map<String, Object> params =
                    JavaApiServer.parseJsonOrFormParams(JavaApiServer.readRequestBody(exchange));

            String usuario = String.valueOf(
                    params.getOrDefault("usuario", "")
            ).trim();

            String contrasena = String.valueOf(
                    params.getOrDefault("contrasena",
                            params.getOrDefault("contraseña",
                                    params.getOrDefault("password", "")))
            );

            if (usuario.isEmpty() || contrasena.isEmpty()) {
                JavaApiServer.sendJsonResponse(exchange, 400,
                        "{\"status\":\"error\",\"message\":\"Completa el usuario y la contraseña.\"}");
                return;
            }

            String sql =
                    "SELECT u.idUsuario, u.nombreUsuario, u.contrasena, " +
                            "u.estado AS estadoUsuario, u.idRol, " +
                            "a.idAgencia, a.nombreComercial, a.estado AS estadoAgencia " +
                            "FROM Usuario u " +
                            "INNER JOIN Agencia a ON a.idUsuario = u.idUsuario " +
                            "WHERE (u.nombreUsuario = ? OR u.idPersona IN " +
                            "(SELECT p.idPersona FROM Persona p WHERE p.email = ?)) " +
                            "AND u.idRol = 2 LIMIT 1";

            try (Connection con = ConexionDB.getConnection()) {

                if (con == null) {
                    JavaApiServer.sendJsonResponse(exchange, 500,
                            "{\"status\":\"error\",\"message\":\"No se pudo conectar a la base de datos.\"}");
                    return;
                }

                try (PreparedStatement ps = con.prepareStatement(sql)) {

                    ps.setString(1, usuario);
                    ps.setString(2, usuario);

                    try (ResultSet rs = ps.executeQuery()) {

                        if (!rs.next()) {
                            JavaApiServer.sendJsonResponse(exchange, 401,
                                    "{\"status\":\"error\",\"message\":\"Usuario o contraseña incorrectos.\"}");
                            return;
                        }

                        String passwordBD = rs.getString("contrasena");

                        // Compatible con el registro actual, que guarda
                        // la contraseña directamente. Migrar a hash seguro.
                        if (passwordBD == null || !passwordBD.equals(contrasena)) {
                            JavaApiServer.sendJsonResponse(exchange, 401,
                                    "{\"status\":\"error\",\"message\":\"Usuario o contraseña incorrectos.\"}");
                            return;
                        }

                        String estadoUsuario = rs.getString("estadoUsuario");
                        String estadoAgencia = rs.getString("estadoAgencia");

                        if (!"ACTIVO".equalsIgnoreCase(estadoUsuario)) {
                            JavaApiServer.sendJsonResponse(exchange, 403,
                                    "{\"status\":\"error\",\"message\":\"La cuenta no está activa.\"}");
                            return;
                        }

                        if (!"ACTIVO".equalsIgnoreCase(estadoAgencia)) {
                            JavaApiServer.sendJsonResponse(exchange, 403,
                                    "{\"status\":\"error\",\"message\":\"La agencia aún está pendiente de aprobación.\"}");
                            return;
                        }

                        String nombreComercial = rs.getString("nombreComercial")
                                .replace("\\", "\\\\")
                                .replace("\"", "\\\"");

                        String json =
                                "{\"status\":\"success\"," +
                                        "\"message\":\"Inicio de sesión correcto.\"," +
                                        "\"agencia\":{" +
                                        "\"idUsuario\":" + rs.getInt("idUsuario") + "," +
                                        "\"idAgencia\":" + rs.getInt("idAgencia") + "," +
                                        "\"nombreUsuario\":\"" +
                                        rs.getString("nombreUsuario").replace("\"", "\\\"") + "\"," +
                                        "\"nombreComercial\":\"" + nombreComercial + "\"," +
                                        "\"idRol\":" + rs.getInt("idRol") +
                                        "}}";

                        JavaApiServer.sendJsonResponse(exchange, 200, json);
                    }
                }

            } catch (Exception e) {
                e.printStackTrace();
                JavaApiServer.sendJsonResponse(exchange, 500,
                        "{\"status\":\"error\",\"message\":\"Error interno al iniciar sesión.\"}");
            }
        }
    }

