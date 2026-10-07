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

    public class RegistroAgenciaHandler implements HttpHandler {

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

            String body = JavaApiServer.readRequestBody(exchange);
            Map<String, Object> params = JavaApiServer.parseJsonOrFormParams(body);

            String nombre = String.valueOf(params.getOrDefault("nombre", "")).trim().toUpperCase();
            String apellidoPaterno = String.valueOf(params.getOrDefault("apellidoPaterno", "")).trim().toUpperCase();
            String apellidoMaterno = String.valueOf(params.getOrDefault("apellidoMaterno", "")).trim().toUpperCase();
            String nroDocumento = String.valueOf(params.getOrDefault("nroDocumento", "")).trim();
            String nombreUsuario = String.valueOf(params.getOrDefault("nombreUsuario", "")).trim();
            String correo = String.valueOf(params.getOrDefault("correo", "")).trim();
            String contrasena = String.valueOf(
                    params.getOrDefault("contrasena",
                            params.getOrDefault("contraseña",
                                    params.getOrDefault("password", "")))
            );
            String telefonoResponsable = String.valueOf(params.getOrDefault("telefonoResponsable", "")).trim();
            String telefonoEmpresa = String.valueOf(params.getOrDefault("telefonoEmpresa", "")).trim();
            String razonSocial = String.valueOf(params.getOrDefault("razonSocial", "")).trim().toUpperCase();
            String nombreComercial = String.valueOf(params.getOrDefault("nombreComercial", "")).trim().toUpperCase();
            String ruc = String.valueOf(params.getOrDefault("ruc", "")).trim();
            String direccion = String.valueOf(params.getOrDefault("direccion", "")).trim();
            String descripcion = String.valueOf(params.getOrDefault("descripcion", "")).trim();

            if (nombre.isEmpty() && !nombreComercial.isEmpty()) nombre = nombreComercial;
            if (apellidoPaterno.isEmpty()) apellidoPaterno = "AGENCIA";
            if (apellidoMaterno.isEmpty()) apellidoMaterno = "SAC";
            if (nroDocumento.isEmpty() && !ruc.isEmpty()) nroDocumento = ruc.length() >= 8 ? ruc.substring(0, 8) : "12345678";
            if (nroDocumento.isEmpty()) nroDocumento = "00000000";

            if (nombreUsuario.isEmpty()
                    || correo.isEmpty()
                    || contrasena.isEmpty()
                    || razonSocial.isEmpty()
                    || nombreComercial.isEmpty()
                    || ruc.isEmpty()) {

                JavaApiServer.sendJsonResponse(exchange, 400,
                        "{\"status\":\"error\",\"message\":\"Completa los campos obligatorios.\"}");
                return;
            }

            // Validaciones de formato
            if (!ruc.matches("^[0-9]{11}$")) {
                JavaApiServer.sendJsonResponse(exchange, 400, "{\"status\":\"error\",\"message\":\"El RUC debe tener exactamente 11 dígitos numéricos.\"}");
                return;
            }

            if (!nroDocumento.matches("^[0-9]{8}$")) {
                JavaApiServer.sendJsonResponse(exchange, 400, "{\"status\":\"error\",\"message\":\"El DNI debe tener exactamente 8 dígitos numéricos.\"}");
                return;
            }

            if (!telefonoEmpresa.isEmpty() && !telefonoEmpresa.matches("^[0-9]{7,9}$")) {
                JavaApiServer.sendJsonResponse(exchange, 400, "{\"status\":\"error\",\"message\":\"El teléfono de la empresa debe tener entre 7 y 9 dígitos numéricos.\"}");
                return;
            }

            if (!telefonoResponsable.isEmpty() && !telefonoResponsable.matches("^[0-9]{7,9}$")) {
                JavaApiServer.sendJsonResponse(exchange, 400, "{\"status\":\"error\",\"message\":\"El teléfono del responsable debe tener entre 7 y 9 dígitos numéricos.\"}");
                return;
            }

            if (!correo.matches("^[\\w-\\.]+@([\\w-]+\\.)+[\\w-]{2,4}$")) {
                JavaApiServer.sendJsonResponse(exchange, 400, "{\"status\":\"error\",\"message\":\"El correo electrónico ingresado no es válido.\"}");
                return;
            }

            try (java.sql.Connection con = ConexionDB.getConnection()) {
                if (con == null) {
                    JavaApiServer.sendJsonResponse(exchange, 500,
                            "{\"status\":\"error\",\"message\":\"No se pudo conectar a la base de datos.\"}");
                    return;
                }

                // Check BD duplicados
                String errDup = JavaApiServer.validarDuplicadosAgencia(con, ruc, nroDocumento, telefonoEmpresa, telefonoResponsable, correo, nombreUsuario);
                if (errDup != null) {
                    JavaApiServer.sendJsonResponse(exchange, 400, "{\"status\":\"error\",\"message\":\"" + errDup.replace("\"", "\\\"") + "\"}");
                    return;
                }

                con.setAutoCommit(false);

                try {
                    // 1. PERSONA
                    String sqlPersona = "INSERT INTO Persona "
                            + "(nombre, apellidoPaterno, apellidoMaterno, nroDocumento, telefono, email) "
                            + "VALUES (?, ?, ?, ?, ?, ?)";

                    int idPersona;

                    try (java.sql.PreparedStatement ps = con.prepareStatement(
                            sqlPersona, java.sql.Statement.RETURN_GENERATED_KEYS)) {

                        ps.setString(1, nombre);
                        ps.setString(2, apellidoPaterno);
                        ps.setString(3, apellidoMaterno);
                        ps.setString(4, nroDocumento);
                        ps.setString(5, telefonoResponsable);
                        ps.setString(6, correo);
                        ps.executeUpdate();

                        try (java.sql.ResultSet rs = ps.getGeneratedKeys()) {
                            if (!rs.next()) {
                                throw new java.sql.SQLException(
                                        "No se pudo obtener el ID de la persona.");
                            }
                            idPersona = rs.getInt(1);
                        }
                    }

                    // 2. USUARIO - Rol 2 = Agencia
                    String sqlUsuario = "INSERT INTO Usuario "
                            + "(idPersona, idRol, nombreUsuario, contrasena, estado, fechaRegistro) "
                            + "VALUES (?, 2, ?, ?, 'ACTIVO', NOW())";

                    int idUsuario;

                    try (java.sql.PreparedStatement ps = con.prepareStatement(
                            sqlUsuario, java.sql.Statement.RETURN_GENERATED_KEYS)) {

                        ps.setInt(1, idPersona);
                        ps.setString(2, nombreUsuario);
                        ps.setString(3, contrasena);
                        ps.executeUpdate();

                        try (java.sql.ResultSet rs = ps.getGeneratedKeys()) {
                            if (!rs.next()) {
                                throw new java.sql.SQLException(
                                        "No se pudo obtener el ID del usuario.");
                            }
                            idUsuario = rs.getInt(1);
                        }
                    }

                    // 3. AGENCIA
                    String sqlAgencia = "INSERT INTO Agencia "
                            + "(idUsuario, razonSocial, nombreComercial, ruc, telefono, email, "
                            + "direccion, descripcion, estado) "
                            + "VALUES (?, ?, ?, ?, ?, ?, ?, ?, 'PENDIENTE')";

                    int idAgencia;

                    try (java.sql.PreparedStatement ps = con.prepareStatement(
                            sqlAgencia, java.sql.Statement.RETURN_GENERATED_KEYS)) {

                        ps.setInt(1, idUsuario);
                        ps.setString(2, razonSocial);
                        ps.setString(3, nombreComercial);
                        ps.setString(4, ruc);
                        ps.setString(5, telefonoEmpresa);
                        ps.setString(6, correo);
                        ps.setString(7, direccion);
                        ps.setString(8, descripcion);
                        ps.executeUpdate();

                        try (java.sql.ResultSet rs = ps.getGeneratedKeys()) {
                            if (!rs.next()) {
                                throw new java.sql.SQLException(
                                        "No se pudo obtener el ID de la agencia.");
                            }
                            idAgencia = rs.getInt(1);
                        }
                    }

                    // 4. SOLICITUD DE INCORPORACIÓN
                    // 4. SOLICITUD DE AGENCIA PARA REVISION DEL ADMINISTRADOR
                    String documentoRuc = String.valueOf(
                            params.getOrDefault("documentoRuc", "")
                    );
                    String sqlSolicitud = "INSERT INTO SolicitudAgencia "
                            + "(idUsuario, razonSocial, ruc, representanteLegal, "
                            + "telefonoContacto, correoContacto, documentoRuc, estado) "
                            + "VALUES (?, ?, ?, ?, ?, ?, ?, 'Pendiente')";

                    int idSolicitud;

                    try (java.sql.PreparedStatement ps = con.prepareStatement(
                            sqlSolicitud, java.sql.Statement.RETURN_GENERATED_KEYS)) {

                        ps.setInt(1, idUsuario);
                        ps.setString(2, razonSocial);
                        ps.setString(3, ruc);

                        String representanteLegal = nombre + " "
                                + apellidoPaterno + " " + apellidoMaterno;
                        ps.setString(4, representanteLegal);

                        ps.setString(5, telefonoEmpresa);
                        ps.setString(6, correo);

                        if (documentoRuc == null || documentoRuc.isBlank()) {
                            ps.setNull(7, java.sql.Types.VARCHAR);
                        } else {
                            ps.setString(7, documentoRuc);
                        }

                        ps.executeUpdate();

                        try (java.sql.ResultSet rs = ps.getGeneratedKeys()) {
                            if (!rs.next()) {
                                throw new java.sql.SQLException(
                                        "No se pudo obtener el ID de la solicitud.");
                            }
                            idSolicitud = rs.getInt(1);
                        }
                    }

                    con.commit();

                    String json = "{"
                            + "\"status\":\"success\","
                            + "\"message\":\"Solicitud de agencia registrada correctamente.\","
                            + "\"idPersona\":" + idPersona + ","
                            + "\"idUsuario\":" + idUsuario + ","
                            + "\"idAgencia\":" + idAgencia + ","
                            + "\"idSolicitud\":" + idSolicitud
                            + "}";

                    JavaApiServer.sendJsonResponse(exchange, 200, json);

                } catch (Exception e) {
                    con.rollback();
                    e.printStackTrace();

                    String mensaje = e.getMessage() == null
                            ? "Error al registrar la agencia."
                            : e.getMessage()
                            .replace("\\", "\\\\")
                            .replace("\"", "\\\"")
                            .replace("\r", "\\r")
                            .replace("\n", "\\n")
                            .replace("\t", "\\t");

                    JavaApiServer.sendJsonResponse(exchange, 500,
                            "{\"status\":\"error\",\"message\":\""
                                    + mensaje + "\"}");
                }

            } catch (Exception e) {
                e.printStackTrace();

                String mensaje = e.getMessage() == null
                        ? "Error de conexión."
                        : e.getMessage()
                        .replace("\\", "\\\\")
                        .replace("\"", "\\\"")
                        .replace("\r", "\\r")
                        .replace("\n", "\\n")
                        .replace("\t", "\\t");

                JavaApiServer.sendJsonResponse(exchange, 500,
                        "{\"status\":\"error\",\"message\":\""
                                + mensaje + "\"}");
            }
        }
    }

