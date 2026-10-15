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

    public class RegistroHandler implements HttpHandler {
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

                String nombre = String.valueOf(params.getOrDefault("nombre", "")).trim().toUpperCase();
                String apellidoPaterno = String.valueOf(params.getOrDefault("apellidoPaterno", "")).trim().toUpperCase();
                String apellidoMaterno = String.valueOf(params.getOrDefault("apellidoMaterno", "")).trim().toUpperCase();
                String nombreUsuario = String.valueOf(
                        params.getOrDefault("usuario",
                                params.getOrDefault("nombreUsuario", ""))).trim();
                String correo = String.valueOf(
                        params.getOrDefault("correo",
                                params.getOrDefault("email", ""))).trim();
                String contrasena = String.valueOf(
                        params.getOrDefault("password",
                                params.getOrDefault("contrasena", "")));
                String telefono = String.valueOf(params.getOrDefault("telefono", "")).trim();
                String nroDocumento = String.valueOf(params.getOrDefault("nroDocumento", params.getOrDefault("nroDoc", ""))).trim();

                // Validaciones obligatorias
                if (nombre.isEmpty() || nombreUsuario.isEmpty() || correo.isEmpty() || contrasena.isEmpty()) {
                    JavaApiServer.sendJsonResponse(exchange, 400, "{\"status\":\"error\",\"message\":\"Completa los campos obligatorios.\"}");
                    return;
                }

                if (!nroDocumento.isEmpty() && !nroDocumento.matches("^[0-9]{8}$")) {
                    JavaApiServer.sendJsonResponse(exchange, 400, "{\"status\":\"error\",\"message\":\"El DNI debe contener exactamente 8 dígitos numéricos.\"}");
                    return;
                }

                if (!telefono.isEmpty() && !telefono.matches("^[0-9]{7,9}$")) {
                    JavaApiServer.sendJsonResponse(exchange, 400, "{\"status\":\"error\",\"message\":\"El teléfono debe contener entre 7 y 9 dígitos numéricos.\"}");
                    return;
                }

                if (!correo.matches("^[\\w-\\.]+@([\\w-]+\\.)+[\\w-]{2,4}$")) {
                    JavaApiServer.sendJsonResponse(exchange, 400, "{\"status\":\"error\",\"message\":\"El correo electrónico ingresado no es válido.\"}");
                    return;
                }

                // Check BD duplicados
                try (Connection con = ConexionDB.getConnection()) {
                    if (con != null) {
                        String errDup = JavaApiServer.validarDuplicadosTurista(con, nroDocumento, telefono, correo, nombreUsuario);
                        if (errDup != null) {
                            JavaApiServer.sendJsonResponse(exchange, 400, "{\"status\":\"error\",\"message\":\"" + errDup.replace("\"", "\\\"") + "\"}");
                            return;
                        }
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }

                Usuario u = new Usuario();
                u.setNombre(nombre);
                u.setApellidoPaterno(apellidoPaterno);
                u.setApellidoMaterno(apellidoMaterno);
                u.setNombreUsuario(nombreUsuario);
                u.setCorreo(correo);
                u.setContrasena(contrasena);
                u.setTelefono(telefono);
                u.setNroDocumento(nroDocumento);

                // Rol 1 = Turista
                u.setIdRol(1);

                Map<String, Object> res = JavaApiServer.usuarioControlador.registrar(u);
                StringBuilder json = new StringBuilder();

                if ("success".equals(res.get("status"))) {
                    Usuario guardado = (Usuario) res.get("user");

                    json.append("{\"status\":\"success\",\"message\":\"Registro exitoso\",\"user\":{")
                            .append("\"idUsuario\":").append(guardado.getIdUsuario()).append(",")
                            .append("\"nombre\":\"").append(guardado.getNombre()).append("\",")
                            .append("\"nombreUsuario\":\"").append(guardado.getNombreUsuario()).append("\",")
                            .append("\"apellidoPaterno\":\"").append(guardado.getApellidoPaterno()).append("\",")
                            .append("\"apellidoMaterno\":\"").append(guardado.getApellidoMaterno()).append("\",")
                            .append("\"correo\":\"").append(guardado.getCorreo()).append("\",")
                            .append("\"telefono\":\"").append(guardado.getTelefono()).append("\",")
                            .append("\"idRol\":").append(guardado.getIdRol())
                            .append("}}");
                } else {
                    json.append("{\"status\":\"error\",\"message\":\"")
                            .append(res.get("message"))
                            .append("\"}");
                }

                JavaApiServer.sendJsonResponse(exchange, 200, json.toString());
            } else {
                JavaApiServer.sendJsonResponse(exchange, 405,
                        "{\"status\":\"error\",\"message\":\"Método no permitido\"}");
            }
        }
    }

