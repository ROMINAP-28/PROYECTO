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

    public class LoginHandler implements HttpHandler {
        @Override
        public void handle(HttpExchange exchange) throws IOException {
            if ("OPTIONS".equalsIgnoreCase(exchange.getRequestMethod())) {
                JavaApiServer.enableCORS(exchange);
                exchange.sendResponseHeaders(200, -1);
                return;
            }
            if ("POST".equalsIgnoreCase(exchange.getRequestMethod())) {
                String body = JavaApiServer.readRequestBody(exchange);
                Map<String, Object> params = JavaApiServer.parseJsonOrFormParams(body);

                String usuarioCorreo = String.valueOf(params.getOrDefault("usuarioCorreo", params.getOrDefault("email", "")));
                String password = String.valueOf(params.getOrDefault("password", params.getOrDefault("contrasena", "")));

                Map<String, Object> res = JavaApiServer.usuarioControlador.login(usuarioCorreo, password);

                StringBuilder json = new StringBuilder();
                if ("success".equals(res.get("status"))) {
                    Usuario u = (Usuario) res.get("user");
                    json.append("{\"status\":\"success\",\"message\":\"Login exitoso\",\"user\":{")
                            .append("\"idUsuario\":").append(u.getIdUsuario()).append(",")
                            .append("\"nombre\":\"").append(u.getNombre()).append("\",")
                            .append("\"nombreUsuario\":\"").append(u.getNombreUsuario()).append("\",")
                            .append("\"apellidoPaterno\":\"").append(u.getApellidoPaterno()).append("\",")
                            .append("\"apellidoMaterno\":\"").append(u.getApellidoMaterno()).append("\",")
                            .append("\"correo\":\"").append(u.getCorreo()).append("\",")
                            .append("\"telefono\":\"").append(u.getTelefono()).append("\",")
                            .append("\"idRol\":").append(u.getIdRol())
                            .append("}}");
                } else {
                    json.append("{\"status\":\"error\",\"message\":\"").append(res.get("message")).append("\"}");
                }
                JavaApiServer.sendJsonResponse(exchange, 200, json.toString());
            } else {
                JavaApiServer.sendJsonResponse(exchange, 405, "{\"status\":\"error\",\"message\":\"Método no permitido\"}");
            }
        }
    }

