package com.travelink.server;

import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import com.sun.net.httpserver.HttpServer;
<<<<<<< HEAD
=======
import com.travelink.config.ConexionDB;
>>>>>>> daac32cd486230b773d5739284598de3e2ff6164
import com.travelink.controlador.CalificacionControlador;
import com.travelink.controlador.UsuarioControlador;
import com.travelink.entidades.Usuario;
import com.travelink.repositorio.ReservaRepositorio;
<<<<<<< HEAD
=======
import com.travelink.config.ConexionDB;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
>>>>>>> daac32cd486230b773d5739284598de3e2ff6164

import java.io.*;
import java.net.InetSocketAddress;
import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;

public class JavaApiServer {
    private static final int PORT = 8080;
    private static final UsuarioControlador usuarioControlador = new UsuarioControlador();
    private static final ReservaRepositorio reservaRepositorio = new ReservaRepositorio();
    private static final CalificacionControlador calificacionControlador = new CalificacionControlador();
<<<<<<< HEAD
    private static final String FRONTEND_DIR = "d:/ProyectosU/PresentaciónTravelink/frontend";

    public static void main(String[] args) throws IOException {
        HttpServer server = HttpServer.create(new InetSocketAddress(PORT), 0);

        // API Endpoints
        server.createContext("/api/login", new LoginHandler());
        server.createContext("/api/registro", new RegistroHandler());
=======
    private static final String FRONTEND_DIR =
            "C:/Users/romin/OneDrive/Documentos/Travelink/DESCARGA NUEVA/DESCARGA NUEVO/frontend";

    public static void main(String[] args) throws IOException {
        HttpServer server = HttpServer.create(new InetSocketAddress(PORT), 0);
        System.out.println("========== VERSION AGENCIA 001 ==========");
        System.out.println("ENDPOINT AGENCIA REGISTRADO");

        System.out.println("FRONTEND_DIR = " + FRONTEND_DIR);

        // aquí continúa el resto de tu código...
        // API Endpoints
        server.createContext("/api/login", new LoginHandler());
        server.createContext("/api/registro", new RegistroAgenciaHandler());
>>>>>>> daac32cd486230b773d5739284598de3e2ff6164
        server.createContext("/api/guardar_reserva", new GuardarReservaHandler());
        server.createContext("/api/obtener_reservas", new ObtenerReservasHandler());
        server.createContext("/api/cancelar_reserva", new CancelarReservaHandler());
        server.createContext("/api/calificar", new CalificarHandler());
<<<<<<< HEAD
=======
        server.createContext("/api/agencia/destinos", new DestinosAgenciaHandler());
        server.createContext("/api/agencia/servicios", new ServiciosAgenciaHandler());
        server.createContext("/api/agencia/registro", new RegistroAgenciaHandler());
        server.createContext("/api/agencia/disponibilidad", new DisponibilidadAgenciaHandler());
        server.createContext("/api/agencia/reservas", new ReservasAgenciaHandler());
>>>>>>> daac32cd486230b773d5739284598de3e2ff6164

        // Static Files Handler (Serves frontend UI)
        server.createContext("/", new StaticFileHandler());

        server.setExecutor(null); // Default executor
        System.out.println("==================================================");
        System.out.println("Servidor Java iniciado exitosamente!");
        System.out.println("Servidor Backend API: http://localhost:" + PORT + "/api/");
        System.out.println("Aplicacion Web Frontend: http://localhost:" + PORT + "/html/turista/index.html");
        System.out.println("==================================================");
        server.start();
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
        OutputStream os = exchange.getResponseBody();
        os.write(bytes);
        os.close();
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
                map.put(URLDecoder.decode(pair[0], StandardCharsets.UTF_8), URLDecoder.decode(pair[1], StandardCharsets.UTF_8));
            } else if (pair.length == 1) {
                map.put(URLDecoder.decode(pair[0], StandardCharsets.UTF_8), "");
            }
        }
        return map;
    }

    private static Map<String, Object> parseJsonOrFormParams(String body) {
        Map<String, Object> map = new HashMap<>();
        if (body == null || body.trim().isEmpty()) return map;

        body = body.trim();
        if (body.startsWith("{") && body.endsWith("}")) {
            body = body.substring(1, body.length() - 1);
            String[] tokens = body.split(",(?=([^\"]*\"[^\"]*\")*[^\"]*$)");
            for (String token : tokens) {
                String[] kv = token.split(":", 2);
                if (kv.length == 2) {
                    String key = kv[0].trim().replace("\"", "");
                    String val = kv[1].trim().replace("\"", "");
                    map.put(key, val);
                }
            }
        } else {
            for (String param : body.split("&")) {
                String[] pair = param.split("=");
                if (pair.length > 1) {
                    map.put(URLDecoder.decode(pair[0], StandardCharsets.UTF_8), URLDecoder.decode(pair[1], StandardCharsets.UTF_8));
                }
            }
        }
        return map;
    }

    // 1. LOGIN HANDLER
    static class LoginHandler implements HttpHandler {
        @Override
        public void handle(HttpExchange exchange) throws IOException {
            if ("OPTIONS".equalsIgnoreCase(exchange.getRequestMethod())) {
                enableCORS(exchange);
                exchange.sendResponseHeaders(200, -1);
                return;
            }
            if ("POST".equalsIgnoreCase(exchange.getRequestMethod())) {
                String body = readRequestBody(exchange);
                Map<String, Object> params = parseJsonOrFormParams(body);

                String usuarioCorreo = String.valueOf(params.getOrDefault("usuarioCorreo", params.getOrDefault("email", "")));
                String password = String.valueOf(params.getOrDefault("password", params.getOrDefault("contrasena", "")));

                Map<String, Object> res = usuarioControlador.login(usuarioCorreo, password);

                StringBuilder json = new StringBuilder();
                if ("success".equals(res.get("status"))) {
                    Usuario u = (Usuario) res.get("user");
                    json.append("{\"status\":\"success\",\"message\":\"Login exitoso\",\"user\":{")
<<<<<<< HEAD
                        .append("\"idUsuario\":").append(u.getIdUsuario()).append(",")
                        .append("\"nombre\":\"").append(u.getNombre()).append("\",")
                        .append("\"nombreUsuario\":\"").append(u.getNombreUsuario()).append("\",")
                        .append("\"apellidoPaterno\":\"").append(u.getApellidoPaterno()).append("\",")
                        .append("\"apellidoMaterno\":\"").append(u.getApellidoMaterno()).append("\",")
                        .append("\"correo\":\"").append(u.getCorreo()).append("\",")
                        .append("\"telefono\":\"").append(u.getTelefono()).append("\",")
                        .append("\"idRol\":").append(u.getIdRol())
                        .append("}}");
=======
                            .append("\"idUsuario\":").append(u.getIdUsuario()).append(",")
                            .append("\"nombre\":\"").append(u.getNombre()).append("\",")
                            .append("\"nombreUsuario\":\"").append(u.getNombreUsuario()).append("\",")
                            .append("\"apellidoPaterno\":\"").append(u.getApellidoPaterno()).append("\",")
                            .append("\"apellidoMaterno\":\"").append(u.getApellidoMaterno()).append("\",")
                            .append("\"correo\":\"").append(u.getCorreo()).append("\",")
                            .append("\"telefono\":\"").append(u.getTelefono()).append("\",")
                            .append("\"idRol\":").append(u.getIdRol())
                            .append("}}");
>>>>>>> daac32cd486230b773d5739284598de3e2ff6164
                } else {
                    json.append("{\"status\":\"error\",\"message\":\"").append(res.get("message")).append("\"}");
                }
                sendJsonResponse(exchange, 200, json.toString());
            } else {
                sendJsonResponse(exchange, 405, "{\"status\":\"error\",\"message\":\"Método no permitido\"}");
            }
        }
    }

    // 2. REGISTRO HANDLER
<<<<<<< HEAD
    static class RegistroHandler implements HttpHandler {
        @Override
        public void handle(HttpExchange exchange) throws IOException {
=======
    static class RegistroAgenciaHandler implements HttpHandler {

        @Override
        public void handle(HttpExchange exchange) throws IOException {

>>>>>>> daac32cd486230b773d5739284598de3e2ff6164
            if ("OPTIONS".equalsIgnoreCase(exchange.getRequestMethod())) {
                enableCORS(exchange);
                exchange.sendResponseHeaders(200, -1);
                return;
            }
<<<<<<< HEAD
            if ("POST".equalsIgnoreCase(exchange.getRequestMethod())) {
                String body = readRequestBody(exchange);
                Map<String, Object> params = parseJsonOrFormParams(body);

                Usuario u = new Usuario();
                u.setNombre(String.valueOf(params.getOrDefault("nombre", "")));
                u.setApellidoPaterno(String.valueOf(params.getOrDefault("apellidoPaterno", "")));
                u.setApellidoMaterno(String.valueOf(params.getOrDefault("apellidoMaterno", "")));
                u.setNombreUsuario(String.valueOf(params.getOrDefault("usuario", params.getOrDefault("nombreUsuario", ""))));
                u.setCorreo(String.valueOf(params.getOrDefault("correo", params.getOrDefault("email", ""))));
                u.setContrasena(String.valueOf(params.getOrDefault("password", params.getOrDefault("contrasena", ""))));
                u.setTelefono(String.valueOf(params.getOrDefault("telefono", "")));
                u.setIdRol(2);

                Map<String, Object> res = usuarioControlador.registrar(u);
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
                    json.append("{\"status\":\"error\",\"message\":\"").append(res.get("message")).append("\"}");
                }
                sendJsonResponse(exchange, 200, json.toString());
            } else {
                sendJsonResponse(exchange, 405, "{\"status\":\"error\",\"message\":\"Método no permitido\"}");
=======

            if (!"POST".equalsIgnoreCase(exchange.getRequestMethod())) {
                sendJsonResponse(
                        exchange,
                        405,
                        "{\"status\":\"error\",\"message\":\"Método no permitido\"}"
                );
                return;
            }

            String body = readRequestBody(exchange);
            Map<String, Object> params = parseJsonOrFormParams(body);

            String nombre = String.valueOf(params.getOrDefault("nombre", ""));
            String apellidoPaterno = String.valueOf(params.getOrDefault("apellidoPaterno", ""));
            String apellidoMaterno = String.valueOf(params.getOrDefault("apellidoMaterno", ""));
            String nroDocumento = String.valueOf(params.getOrDefault("nroDocumento", ""));
            String nombreUsuario = String.valueOf(params.getOrDefault("nombreUsuario", ""));
            String correo = String.valueOf(params.getOrDefault("correo", ""));
            String contrasena = String.valueOf(params.getOrDefault("contrasena", ""));

            String telefonoResponsable =
                    String.valueOf(params.getOrDefault("telefonoResponsable", ""));

            String telefonoEmpresa =
                    String.valueOf(params.getOrDefault("telefonoEmpresa", ""));

            String razonSocial = String.valueOf(params.getOrDefault("razonSocial", ""));
            String nombreComercial = String.valueOf(params.getOrDefault("nombreComercial", ""));
            String ruc = String.valueOf(params.getOrDefault("ruc", ""));
            String direccion = String.valueOf(params.getOrDefault("direccion", ""));
            String descripcion = String.valueOf(params.getOrDefault("descripcion", ""));

            if (nombre.isEmpty()
                    || apellidoPaterno.isEmpty()
                    || apellidoMaterno.isEmpty()
                    || nroDocumento.isEmpty()
                    || nombreUsuario.isEmpty()
                    || correo.isEmpty()
                    || contrasena.isEmpty()
                    || razonSocial.isEmpty()
                    || nombreComercial.isEmpty()
                    || ruc.isEmpty()) {

                sendJsonResponse(
                        exchange,
                        400,
                        "{\"status\":\"error\",\"message\":\"Completa los campos obligatorios.\"}"
                );
                return;
            }

            try (java.sql.Connection con = ConexionDB.getConnection()) {

                if (con == null) {
                    sendJsonResponse(
                            exchange,
                            500,
                            "{\"status\":\"error\",\"message\":\"No se pudo conectar a la base de datos.\"}"
                    );
                    return;
                }

                con.setAutoCommit(false);

                try {

                    // 1. PERSONA
                    String sqlPersona =
                            "INSERT INTO Persona " +
                                    "(nombre, apellidoPaterno, apellidoMaterno, nroDocumento, telefono, email) " +
                                    "VALUES (?, ?, ?, ?, ?, ?)";

                    int idPersona;

                    try (java.sql.PreparedStatement ps =
                                 con.prepareStatement(
                                         sqlPersona,
                                         java.sql.Statement.RETURN_GENERATED_KEYS)) {

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
                                        "No se pudo obtener el ID de la persona."
                                );
                            }

                            idPersona = rs.getInt(1);
                        }
                    }

                    // 2. USUARIO - Agencia = ID ROL 2
                    String sqlUsuario =
                            "INSERT INTO Usuario " +
                                    "(idPersona, idRol, nombreUsuario, contrasena, estado, fechaRegistro) " +
                                    "VALUES (?, 2, ?, ?, 'ACTIVO', NOW())";

                    int idUsuario;

                    try (java.sql.PreparedStatement ps =
                                 con.prepareStatement(
                                         sqlUsuario,
                                         java.sql.Statement.RETURN_GENERATED_KEYS)) {

                        ps.setInt(1, idPersona);
                        ps.setString(2, nombreUsuario);
                        ps.setString(3, contrasena);

                        ps.executeUpdate();

                        try (java.sql.ResultSet rs = ps.getGeneratedKeys()) {
                            if (!rs.next()) {
                                throw new java.sql.SQLException(
                                        "No se pudo obtener el ID del usuario."
                                );
                            }

                            idUsuario = rs.getInt(1);
                        }
                    }

                    // 3. AGENCIA
                    String sqlAgencia =
                            "INSERT INTO Agencia " +
                                    "(idUsuario, razonSocial, nombreComercial, ruc, telefono, email, " +
                                    "direccion, descripcion, estado) " +
                                    "VALUES (?, ?, ?, ?, ?, ?, ?, ?, 'PENDIENTE')";

                    int idAgencia;

                    try (java.sql.PreparedStatement ps =
                                 con.prepareStatement(
                                         sqlAgencia,
                                         java.sql.Statement.RETURN_GENERATED_KEYS)) {

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
                                        "No se pudo obtener el ID de la agencia."
                                );
                            }

                            idAgencia = rs.getInt(1);
                        }
                    }

                    // 4. SOLICITUD DE INCORPORACIÓN
                    String sqlSolicitud =
                            "INSERT INTO SolicitudIncorporacion " +
                                    "(fechaSolicitud, estado, observaciones, fechaRespuesta, idAgencia) " +
                                    "VALUES (NOW(), 'PENDIENTE', ?, NULL, ?)";

                    int idSolicitud;

                    try (java.sql.PreparedStatement ps =
                                 con.prepareStatement(
                                         sqlSolicitud,
                                         java.sql.Statement.RETURN_GENERATED_KEYS)) {

                        ps.setString(
                                1,
                                "Solicitud de incorporación enviada por la agencia."
                        );

                        ps.setInt(2, idAgencia);

                        ps.executeUpdate();

                        try (java.sql.ResultSet rs = ps.getGeneratedKeys()) {
                            if (!rs.next()) {
                                throw new java.sql.SQLException(
                                        "No se pudo obtener el ID de la solicitud."
                                );
                            }

                            idSolicitud = rs.getInt(1);
                        }
                    }

                    con.commit();

                    String json =
                            "{"
                                    + "\"status\":\"success\","
                                    + "\"message\":\"Solicitud de agencia registrada correctamente.\","
                                    + "\"idPersona\":" + idPersona + ","
                                    + "\"idUsuario\":" + idUsuario + ","
                                    + "\"idAgencia\":" + idAgencia + ","
                                    + "\"idSolicitud\":" + idSolicitud
                                    + "}";

                    sendJsonResponse(exchange, 200, json);

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

                    sendJsonResponse(
                            exchange,
                            500,
                            "{\"status\":\"error\",\"message\":\""
                                    + mensaje
                                    + "\"}"
                    );
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

                sendJsonResponse(
                        exchange,
                        500,
                        "{\"status\":\"error\",\"message\":\""
                                + mensaje
                                + "\"}"
                );
>>>>>>> daac32cd486230b773d5739284598de3e2ff6164
            }
        }
    }

    // 3. GUARDAR RESERVA HANDLER
    static class GuardarReservaHandler implements HttpHandler {
        @Override
        public void handle(HttpExchange exchange) throws IOException {
            if ("OPTIONS".equalsIgnoreCase(exchange.getRequestMethod())) {
                enableCORS(exchange);
                exchange.sendResponseHeaders(200, -1);
                return;
            }
            if ("POST".equalsIgnoreCase(exchange.getRequestMethod())) {
                String body = readRequestBody(exchange);
                Map<String, Object> params = parseJsonOrFormParams(body);
                Map<String, Object> res = reservaRepositorio.guardarReservaCompleta(params);

                StringBuilder json = new StringBuilder();
                if ("success".equals(res.get("status"))) {
                    @SuppressWarnings("unchecked")
                    Map<String, Object> d = (Map<String, Object>) res.get("data");
                    json.append("{\"status\":\"success\",\"message\":\"Reserva guardada exitosamente\",\"data\":{")
<<<<<<< HEAD
                        .append("\"idReserva\":").append(d.get("idReserva")).append(",")
                        .append("\"idPago\":").append(d.get("idPago")).append(",")
                        .append("\"codigo\":\"").append(d.get("codigo")).append("\",")
                        .append("\"estado\":\"").append(d.get("estado")).append("\",")
                        .append("\"precioTotal\":").append(d.get("precioTotal")).append(",")
                        .append("\"metodoPago\":\"").append(d.get("metodoPago")).append("\"")
                        .append("}}");
=======
                            .append("\"idReserva\":").append(d.get("idReserva")).append(",")
                            .append("\"idPago\":").append(d.get("idPago")).append(",")
                            .append("\"codigo\":\"").append(d.get("codigo")).append("\",")
                            .append("\"estado\":\"").append(d.get("estado")).append("\",")
                            .append("\"precioTotal\":").append(d.get("precioTotal")).append(",")
                            .append("\"metodoPago\":\"").append(d.get("metodoPago")).append("\"")
                            .append("}}");
>>>>>>> daac32cd486230b773d5739284598de3e2ff6164
                } else {
                    json.append("{\"status\":\"error\",\"message\":\"").append(res.get("message")).append("\"}");
                }
                sendJsonResponse(exchange, 200, json.toString());
            } else {
                sendJsonResponse(exchange, 405, "{\"status\":\"error\",\"message\":\"Método no permitido\"}");
            }
        }
    }

    // 4. OBTENER RESERVAS HANDLER
    static class ObtenerReservasHandler implements HttpHandler {
        @Override
        public void handle(HttpExchange exchange) throws IOException {
            if ("OPTIONS".equalsIgnoreCase(exchange.getRequestMethod())) {
                enableCORS(exchange);
                exchange.sendResponseHeaders(200, -1);
                return;
            }
            Map<String, String> queryParams = parseQueryParams(exchange.getRequestURI().getQuery());
            String correo = queryParams.getOrDefault("correo", queryParams.getOrDefault("email", ""));
            int idUsuario = 0;
            try { idUsuario = Integer.parseInt(queryParams.getOrDefault("idUsuario", "0")); } catch (Exception ignored) {}

            Map<String, Object> res = reservaRepositorio.obtenerReservasPorUsuario(correo, idUsuario);
            StringBuilder json = new StringBuilder();
            json.append("{\"status\":\"").append(res.get("status")).append("\",\"reservas\":[");

            @SuppressWarnings("unchecked")
            List<Map<String, Object>> lista = (List<Map<String, Object>>) res.get("reservas");
            if (lista != null) {
                for (int i = 0; i < lista.size(); i++) {
                    Map<String, Object> r = lista.get(i);
                    json.append("{")
<<<<<<< HEAD
                        .append("\"id\":").append(r.get("id")).append(",")
                        .append("\"codigo\":\"").append(r.get("codigo")).append("\",")
                        .append("\"titulo\":\"").append(r.get("titulo")).append("\",")
                        .append("\"ubicacion\":\"").append(r.get("ubicacion")).append("\",")
                        .append("\"fechas\":\"").append(r.get("fechas")).append("\",")
                        .append("\"personas\":\"").append(r.get("personas")).append("\",")
                        .append("\"agencia\":\"").append(r.get("agencia")).append("\",")
                        .append("\"estado\":\"").append(r.get("estado")).append("\",")
                        .append("\"total\":").append(r.get("total")).append(",")
                        .append("\"metodoPago\":\"").append(r.get("metodoPago")).append("\",")
                        .append("\"imagen\":\"").append(r.get("imagen")).append("\",")
                        .append("\"fechaRegistro\":\"").append(r.get("fechaRegistro")).append("\"")
                        .append("}");
=======
                            .append("\"id\":").append(r.get("id")).append(",")
                            .append("\"codigo\":\"").append(r.get("codigo")).append("\",")
                            .append("\"titulo\":\"").append(r.get("titulo")).append("\",")
                            .append("\"ubicacion\":\"").append(r.get("ubicacion")).append("\",")
                            .append("\"fechas\":\"").append(r.get("fechas")).append("\",")
                            .append("\"personas\":\"").append(r.get("personas")).append("\",")
                            .append("\"agencia\":\"").append(r.get("agencia")).append("\",")
                            .append("\"estado\":\"").append(r.get("estado")).append("\",")
                            .append("\"total\":").append(r.get("total")).append(",")
                            .append("\"metodoPago\":\"").append(r.get("metodoPago")).append("\",")
                            .append("\"imagen\":\"").append(r.get("imagen")).append("\",")
                            .append("\"fechaRegistro\":\"").append(r.get("fechaRegistro")).append("\"")
                            .append("}");
>>>>>>> daac32cd486230b773d5739284598de3e2ff6164
                    if (i < lista.size() - 1) json.append(",");
                }
            }
            json.append("]}");
            sendJsonResponse(exchange, 200, json.toString());
        }
    }

    // 5. CANCELAR RESERVA HANDLER
    static class CancelarReservaHandler implements HttpHandler {
        @Override
        public void handle(HttpExchange exchange) throws IOException {
            if ("OPTIONS".equalsIgnoreCase(exchange.getRequestMethod())) {
                enableCORS(exchange);
                exchange.sendResponseHeaders(200, -1);
                return;
            }
            if ("POST".equalsIgnoreCase(exchange.getRequestMethod())) {
                String body = readRequestBody(exchange);
                Map<String, Object> params = parseJsonOrFormParams(body);

                int idReserva = 0;
                try { idReserva = Integer.parseInt(String.valueOf(params.get("idReserva"))); } catch (Exception ignored) {}
                String motivo = String.valueOf(params.getOrDefault("motivo", "Cancelación solicitada por el usuario"));

                boolean ok = reservaRepositorio.cancelarReserva(idReserva, motivo);
                if (ok) {
                    sendJsonResponse(exchange, 200, "{\"status\":\"success\",\"message\":\"Reserva cancelada correctamente\"}");
                } else {
                    sendJsonResponse(exchange, 200, "{\"status\":\"error\",\"message\":\"No se pudo cancelar la reserva\"}");
                }
            } else {
                sendJsonResponse(exchange, 405, "{\"status\":\"error\",\"message\":\"Método no permitido\"}");
            }
        }
    }

    // 6. CALIFICAR HANDLER
    static class CalificarHandler implements HttpHandler {
        @Override
        public void handle(HttpExchange exchange) throws IOException {
            if ("OPTIONS".equalsIgnoreCase(exchange.getRequestMethod())) {
                enableCORS(exchange);
                exchange.sendResponseHeaders(200, -1);
                return;
            }
            if ("POST".equalsIgnoreCase(exchange.getRequestMethod())) {
                String body = readRequestBody(exchange);
                Map<String, Object> params = parseJsonOrFormParams(body);

                com.travelink.entidades.Calificacion c = new com.travelink.entidades.Calificacion();
                try { c.setIdUsuario(Integer.parseInt(String.valueOf(params.getOrDefault("idUsuario", "1")))); } catch (Exception ignored) {}
                try { c.setIdAgencia(Integer.parseInt(String.valueOf(params.getOrDefault("idAgencia", "1")))); } catch (Exception ignored) {}
                try { c.setIdReserva(Integer.parseInt(String.valueOf(params.getOrDefault("idReserva", "1")))); } catch (Exception ignored) {}
                try { c.setEstrellas(Integer.parseInt(String.valueOf(params.getOrDefault("estrellas", "5")))); } catch (Exception ignored) {}
                c.setComentario(String.valueOf(params.getOrDefault("comentario", "")));

                Map<String, Object> res = calificacionControlador.guardarCalificacion(c);
                if ("success".equals(res.get("status"))) {
                    sendJsonResponse(exchange, 200, "{\"status\":\"success\",\"message\":\"Calificación registrada exitosamente\"}");
                } else {
                    sendJsonResponse(exchange, 200, "{\"status\":\"error\",\"message\":\"" + res.get("message") + "\"}");
                }
            } else {
                sendJsonResponse(exchange, 405, "{\"status\":\"error\",\"message\":\"Método no permitido\"}");
            }
        }
    }

<<<<<<< HEAD
=======
    private static String jsonEscape(String value) {

        if (value == null) {
            return "";
        }

        return value
                .replace("\\", "\\\\")
                .replace("\"", "\\\"")
                .replace("\r", "\\r")
                .replace("\n", "\\n");
    }

    static class DestinosAgenciaHandler implements HttpHandler {

        @Override
        public void handle(HttpExchange exchange) throws IOException {

            if ("OPTIONS".equalsIgnoreCase(exchange.getRequestMethod())) {
                enableCORS(exchange);
                exchange.sendResponseHeaders(200, -1);
                return;
            }

            if (!"GET".equalsIgnoreCase(exchange.getRequestMethod())) {
                sendJsonResponse(exchange, 405,
                        "{\"status\":\"error\",\"message\":\"Método no permitido\"}");
                return;
            }

            StringBuilder json = new StringBuilder();
            json.append("{\"status\":\"success\",\"destinos\":[");

            try (Connection con = ConexionDB.getConnection();
                 PreparedStatement ps = con.prepareStatement(
                         "SELECT idDestino, nombre, ubicacion " +
                                 "FROM Destino ORDER BY nombre");
                 ResultSet rs = ps.executeQuery()) {

                boolean primero = true;

                while (rs.next()) {

                    if (!primero) {
                        json.append(",");
                    }

                    json.append("{")
                            .append("\"idDestino\":").append(rs.getInt("idDestino")).append(",")
                            .append("\"nombre\":\"").append(jsonEscape(rs.getString("nombreTour"))).append("\",")
                            .append("\"ubicacion\":\"")
                            .append(jsonEscape(rs.getString("ubicacion")))
                            .append("\"")
                            .append("}");

                    primero = false;
                }

                json.append("]}");

                sendJsonResponse(exchange, 200, json.toString());

            } catch (Exception e) {

                e.printStackTrace();

                sendJsonResponse(exchange, 500,
                        "{\"status\":\"error\",\"message\":\"Error al obtener los destinos\"}");
            }
        }
    }


    static class ServiciosAgenciaHandler implements HttpHandler {

        @Override
        public void handle(HttpExchange exchange) throws IOException {

            if ("OPTIONS".equalsIgnoreCase(exchange.getRequestMethod())) {
                enableCORS(exchange);
                exchange.sendResponseHeaders(200, -1);
                return;
            }

            String metodo = exchange.getRequestMethod();

            if ("GET".equalsIgnoreCase(metodo)) {
                obtenerServicios(exchange);
                return;
            }

            if ("POST".equalsIgnoreCase(metodo)) {
                guardarServicio(exchange);
                return;
            }

            sendJsonResponse(exchange, 405,
                    "{\"status\":\"error\",\"message\":\"Método no permitido\"}");
        }

        private void obtenerServicios(HttpExchange exchange) throws IOException {

            Map<String, String> params =
                    parseQueryParams(exchange.getRequestURI().getQuery());

            int idAgencia;

            try {
                idAgencia = Integer.parseInt(
                        params.getOrDefault("idAgencia", "0")
                );
            } catch (Exception e) {
                idAgencia = 0;
            }

            if (idAgencia <= 0) {
                sendJsonResponse(exchange, 400,
                        "{\"status\":\"error\",\"message\":\"Agencia no especificada\"}");
                return;
            }

            StringBuilder json = new StringBuilder();

            json.append("{\"status\":\"success\",\"servicios\":[");

            try (Connection con = ConexionDB.getConnection();
                 PreparedStatement ps = con.prepareStatement(
                         "SELECT s.idServicio, s.nombre, s.descripcion, " +
                                 "s.tipoServicio, s.precio, s.duracion, " +
                                 "s.condiciones, s.estado, s.fechaRegistro, " +
                                 "s.idAgencia, s.idDestino, d.nombre AS destino " +
                                 "FROM ServicioTuristico s " +
                                 "INNER JOIN Destino d ON s.idDestino = d.idDestino " +
                                 "WHERE s.idAgencia = ? " +
                                 "ORDER BY s.idServicio DESC")) {

                ps.setInt(1, idAgencia);

                try (ResultSet rs = ps.executeQuery()) {

                    boolean primero = true;

                    while (rs.next()) {

                        if (!primero) {
                            json.append(",");
                        }

                        json.append("{")
                                .append("\"idServicio\":")
                                .append(rs.getInt("idServicio")).append(",")

                                .append("\"nombre\":\"")
                                .append(jsonEscape(rs.getString("nombre")))
                                .append("\",")

                                .append("\"descripcion\":\"")
                                .append(jsonEscape(rs.getString("descripcion")))
                                .append("\",")

                                .append("\"tipoServicio\":\"")
                                .append(jsonEscape(rs.getString("tipoServicio")))
                                .append("\",")

                                .append("\"precio\":")
                                .append(rs.getBigDecimal("precio")).append(",")

                                .append("\"duracion\":\"")
                                .append(jsonEscape(rs.getString("duracion")))
                                .append("\",")

                                .append("\"condiciones\":\"")
                                .append(jsonEscape(rs.getString("condiciones")))
                                .append("\",")

                                .append("\"estado\":\"")
                                .append(jsonEscape(rs.getString("estado")))
                                .append("\",")

                                .append("\"idDestino\":")
                                .append(rs.getInt("idDestino")).append(",")

                                .append("\"destino\":\"")
                                .append(jsonEscape(rs.getString("destino")))
                                .append("\"")

                                .append("}");

                        primero = false;
                    }
                }

                json.append("]}");

                sendJsonResponse(exchange, 200, json.toString());

            } catch (Exception e) {

                e.printStackTrace();

                sendJsonResponse(exchange, 500,
                        "{\"status\":\"error\",\"message\":\"Error al obtener los servicios\"}");
            }
        }

        private void guardarServicio(HttpExchange exchange) throws IOException {

            String body = readRequestBody(exchange);

            Map<String, Object> params =
                    parseJsonOrFormParams(body);

            String nombre = String.valueOf(
                    params.getOrDefault("nombre", "")
            );

            String descripcion = String.valueOf(
                    params.getOrDefault("descripcion", "")
            );

            String tipoServicio = String.valueOf(
                    params.getOrDefault("tipoServicio", "")
            );

            String precioTexto = String.valueOf(
                    params.getOrDefault("precio", "0")
            );

            String duracion = String.valueOf(
                    params.getOrDefault("duracion", "")
            );

            String condiciones = String.valueOf(
                    params.getOrDefault("condiciones", "")
            );

            String estado = String.valueOf(
                    params.getOrDefault("estado", "ACTIVO")
            );

            int idAgencia = 0;
            int idDestino = 0;
            int idServicio = 0;


// Conserva aquí la declaración original de idServicio
// si ya existe en otra parte del método.

            Object idServicioObj = params.get("idServicio");

            if (idServicioObj != null) {
                String idServicioTexto =
                        String.valueOf(idServicioObj).trim();

                if (!idServicioTexto.isEmpty()) {
                    idServicio = Integer.parseInt(idServicioTexto);
                }
            }
            try {
                idAgencia = Integer.parseInt(
                        String.valueOf(params.getOrDefault("idAgencia", "0"))
                );

                idDestino = Integer.parseInt(
                        String.valueOf(params.getOrDefault("idDestino", "0"))
                );

            } catch (Exception e) {

                sendJsonResponse(exchange, 400,
                        "{\"status\":\"error\",\"message\":\"Agencia o destino inválido\"}");
                return;
            }

            if (nombre.trim().isEmpty() ||
                    tipoServicio.trim().isEmpty() ||
                    duracion.trim().isEmpty() ||
                    idAgencia <= 0 ||
                    idDestino <= 0) {

                sendJsonResponse(exchange, 400,
                        "{\"status\":\"error\",\"message\":\"Completa los campos obligatorios\"}");
                return;
            }

            try {

                double precio = Double.parseDouble(precioTexto);

                if (idServicio > 0) {
                    String sqlUpdate =
                            "UPDATE ServicioTuristico SET " +
                                    "nombre = ?, descripcion = ?, tipoServicio = ?, precio = ?, " +
                                    "duracion = ?, condiciones = ?, estado = ?, idDestino = ? " +
                                    "WHERE idServicio = ? AND idAgencia = ?";

                    try (Connection con = ConexionDB.getConnection();
                         PreparedStatement ps = con.prepareStatement(sqlUpdate)) {

                        ps.setString(1, nombre);
                        ps.setString(2, descripcion);
                        ps.setString(3, tipoServicio);
                        ps.setDouble(4, precio);
                        ps.setString(5, duracion);
                        ps.setString(6, condiciones);
                        ps.setString(7, estado);
                        ps.setInt(8, idDestino);
                        ps.setInt(9, idServicio);
                        ps.setInt(10, idAgencia);

                        int filas = ps.executeUpdate();

                        if (filas == 0) {
                            sendJsonResponse(exchange, 404,
                                    "{\"status\":\"error\","
                                            + "\"message\":\"No se encontró el servicio "
                                            + "o no pertenece a esta agencia\"}");
                            return;
                        }

                        sendJsonResponse(exchange, 200,
                                "{\"status\":\"success\","
                                        + "\"message\":\"Servicio actualizado correctamente\","
                                        + "\"idServicio\":" + idServicio + "}");
                        return;
                    }
                }

                try (Connection con = ConexionDB.getConnection();
                     PreparedStatement ps = con.prepareStatement(
                             "INSERT INTO ServicioTuristico " +
                                     "(nombre, descripcion, tipoServicio, precio, " +
                                     "duracion, condiciones, estado, idAgencia, idDestino) " +
                                     "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?)",
                             PreparedStatement.RETURN_GENERATED_KEYS)) {

                    ps.setString(1, nombre);
                    ps.setString(2, descripcion);
                    ps.setString(3, tipoServicio);
                    ps.setDouble(4, precio);
                    ps.setString(5, duracion);
                    ps.setString(6, condiciones);
                    ps.setString(7, estado);
                    ps.setInt(8, idAgencia);
                    ps.setInt(9, idDestino);

                    int filas = ps.executeUpdate();

                    if (filas == 0) {
                        sendJsonResponse(exchange, 500,
                                "{\"status\":\"error\",\"message\":\"No se pudo registrar el servicio\"}");
                        return;
                    }

                    try (ResultSet keys = ps.getGeneratedKeys()) {


                        if (keys.next()) {
                            idServicio = keys.getInt(1);
                        }

                        sendJsonResponse(exchange, 200,
                                "{\"status\":\"success\"," +
                                        "\"message\":\"Servicio registrado correctamente\"," +
                                        "\"idServicio\":" + idServicio +
                                        "}");
                    }
                }

            } catch (NumberFormatException e) {

                sendJsonResponse(exchange, 400,
                        "{\"status\":\"error\",\"message\":\"El precio no es válido\"}");

            } catch (Exception e) {

                e.printStackTrace();

                sendJsonResponse(exchange, 500,
                        "{\"status\":\"error\",\"message\":\"Error al guardar el servicio\"}");
            }
        }
    }


    static class ReservasAgenciaHandler implements HttpHandler {

        @Override
        public void handle(HttpExchange exchange) throws IOException {

            if ("OPTIONS".equalsIgnoreCase(exchange.getRequestMethod())) {
                enableCORS(exchange);
                exchange.sendResponseHeaders(200, -1);
                return;
            }

            enableCORS(exchange);

            if (!"GET".equalsIgnoreCase(exchange.getRequestMethod())) {
                sendJsonResponse(exchange, 405,
                        "{\"status\":\"error\",\"message\":\"Método no permitido\"}");
                return;
            }

            Map<String, String> params =
                    parseQueryParams(exchange.getRequestURI().getQuery());

            int idAgencia;

            try {
                idAgencia = Integer.parseInt(
                        params.getOrDefault("idAgencia", "0")
                );
            } catch (NumberFormatException e) {
                idAgencia = 0;
            }

            if (idAgencia <= 0) {
                sendJsonResponse(exchange, 400,
                        "{\"status\":\"error\",\"message\":\"Agencia no especificada\"}");
                return;
            }

            String sql = """
                    SELECT
                        idReserva,
                        idUsuario,
                        idAgencia,
                        nombreTour,
                        codigoReserva,
                        fechaRegistro,
                        fechaInicio,
                        fechaFin,
                        estado,
                        motivoCancelacion,
                        total
                    FROM Reserva
                    WHERE idAgencia = ?
                    ORDER BY fechaRegistro DESC
                    """;

            StringBuilder json = new StringBuilder();
            json.append("{\"status\":\"success\",\"reservas\":[");

            try (Connection con = ConexionDB.getConnection();
                 PreparedStatement ps = con.prepareStatement(sql)) {

                ps.setInt(1, idAgencia);

                try (ResultSet rs = ps.executeQuery()) {

                    boolean primero = true;

                    while (rs.next()) {

                        if (!primero) {
                            json.append(",");
                        }
                        primero = false;

                        String nombreCliente =
                                rs.getString("nombreTour");


                        json.append("{")
                                .append("\"idReserva\":")
                                .append(rs.getInt("idReserva")).append(",")

                                .append("\"idUsuario\":")
                                .append(rs.getInt("idUsuario")).append(",")

                                .append("\"idAgencia\":")
                                .append(rs.getInt("idAgencia")).append(",")

                                .append("\"nombreTour\":\"")
                                .append(jsonEscape(rs.getString("nombreTour")))
                                .append("\",")

                                .append("\"codigoReserva\":\"")
                                .append(jsonEscape(rs.getString("codigoReserva")))
                                .append("\",")

                                .append("\"fechaRegistro\":\"")
                                .append(jsonEscape(rs.getString("fechaRegistro")))
                                .append("\",")

                                .append("\"fechaInicio\":\"")
                                .append(jsonEscape(rs.getString("fechaInicio")))
                                .append("\",")

                                .append("\"fechaFin\":\"")
                                .append(jsonEscape(rs.getString("fechaFin")))
                                .append("\",")

                                .append("\"estado\":\"")
                                .append(jsonEscape(rs.getString("estado")))
                                .append("\",")

                                .append("\"motivoCancelacion\":\"")
                                .append(jsonEscape(rs.getString("motivoCancelacion")))
                                .append("\",")

                                .append("\"total\":")
                                .append(rs.getBigDecimal("total"))
                                .append("}");
                    }
                }

                json.append("]}");
                sendJsonResponse(exchange, 200, json.toString());

            } catch (Exception e) {
                e.printStackTrace();

                sendJsonResponse(
                        exchange,
                        500,
                        "{\"status\":\"error\",\"message\":\""
                                + jsonEscape(e.getMessage())
                                + "\"}"
                );
            }
        }
    }

>>>>>>> daac32cd486230b773d5739284598de3e2ff6164
    // 7. STATIC FILES HANDLER (Serves HTML, CSS, JS, Images)
    static class StaticFileHandler implements HttpHandler {
        @Override
        public void handle(HttpExchange exchange) throws IOException {
            String path = exchange.getRequestURI().getPath();
            if (path.equals("/") || path.equals("/index.html")) {
                path = "/html/turista/index.html";
            }

            Path filePath = Paths.get(FRONTEND_DIR, path);
            File file = filePath.toFile();

            if (!file.exists() || file.isDirectory()) {
                String notFound = "<h1>404 File Not Found</h1><p>" + path + "</p>";
                exchange.sendResponseHeaders(404, notFound.length());
                OutputStream os = exchange.getResponseBody();
                os.write(notFound.getBytes(StandardCharsets.UTF_8));
                os.close();
                return;
            }

            String contentType = getContentType(file.getName());
            exchange.getResponseHeaders().set("Content-Type", contentType);
            exchange.sendResponseHeaders(200, file.length());

            OutputStream os = exchange.getResponseBody();
            Files.copy(file.toPath(), os);
            os.close();
        }

        private String getContentType(String filename) {
            if (filename.endsWith(".html")) return "text/html; charset=UTF-8";
            if (filename.endsWith(".css")) return "text/css; charset=UTF-8";
            if (filename.endsWith(".js")) return "application/javascript; charset=UTF-8";
            if (filename.endsWith(".jpg") || filename.endsWith(".jpeg")) return "image/jpeg";
            if (filename.endsWith(".png")) return "image/png";
            if (filename.endsWith(".svg")) return "image/svg+xml";
            if (filename.endsWith(".ico")) return "image/x-icon";
            return "application/octet-stream";
        }
    }
<<<<<<< HEAD
=======

    static class DisponibilidadAgenciaHandler implements HttpHandler {

        @Override
        public void handle(HttpExchange exchange) throws IOException {

            // CORS
            if ("OPTIONS".equalsIgnoreCase(exchange.getRequestMethod())) {
                enableCORS(exchange);
                exchange.sendResponseHeaders(200, -1);
                return;
            }

            enableCORS(exchange);

            exchange.getResponseHeaders().add(
                    "Content-Type",
                    "application/json; charset=UTF-8"
            );

            try {
                if ("GET".equalsIgnoreCase(exchange.getRequestMethod())) {

                    String query = exchange.getRequestURI().getQuery();
                    String idAgencia = null;

                    if (query != null && query.startsWith("idAgencia=")) {
                        idAgencia = query.substring("idAgencia=".length());
                    }

                    if (idAgencia == null || idAgencia.isEmpty()) {
                        enviar(exchange, 400, "{\"error\":\"Falta idAgencia\"}");
                        return;
                    }

                    String sql = """
                    SELECT d.idDisponibilidad,
                           d.fecha,
                           d.horaInicio,
                           d.horaFin,
                           d.cupoTotal,
                           d.cupoDisponible,
                           d.estado,
                           d.idServicio,
                           s.nombre AS servicio
                    FROM Disponibilidad d
                    INNER JOIN ServicioTuristico s
                        ON d.idServicio = s.idServicio
                    WHERE s.idAgencia = ?
                    ORDER BY d.fecha ASC, d.horaInicio ASC
                    """;

                    try (Connection con = ConexionDB.getConnection();
                         PreparedStatement ps = con.prepareStatement(sql)) {

                        ps.setInt(1, Integer.parseInt(idAgencia));

                        ResultSet rs = ps.executeQuery();

                        StringBuilder json = new StringBuilder("[");
                        boolean primero = true;

                        while (rs.next()) {

                            if (!primero) json.append(",");
                            primero = false;

                            json.append("{")
                                    .append("\"idDisponibilidad\":").append(rs.getInt("idDisponibilidad")).append(",")
                                    .append("\"fecha\":\"").append(jsonEscape(rs.getString("fecha"))).append("\",")
                                    .append("\"horaInicio\":\"").append(jsonEscape(rs.getString("horaInicio"))).append("\",")
                                    .append("\"horaFin\":\"").append(jsonEscape(rs.getString("horaFin"))).append("\",")
                                    .append("\"cupoTotal\":").append(rs.getInt("cupoTotal")).append(",")
                                    .append("\"cupoDisponible\":").append(rs.getInt("cupoDisponible")).append(",")
                                    .append("\"estado\":\"").append(jsonEscape(rs.getString("estado"))).append("\",")
                                    .append("\"idServicio\":").append(rs.getInt("idServicio")).append(",")
                                    .append("\"servicio\":\"").append(jsonEscape(rs.getString("servicio"))).append("\"")
                                    .append("}");
                        }

                        json.append("]");

                        enviar(exchange, 200, json.toString());
                    }

                    return;
                }


                if ("POST".equalsIgnoreCase(exchange.getRequestMethod())) {

                    System.out.println("========== POST DISPONIBILIDAD ==========");

                    String body = new String(
                            exchange.getRequestBody().readAllBytes(),
                            StandardCharsets.UTF_8
                    );

                    System.out.println("BODY: " + body);

                    String fecha = obtenerParametro(body, "fecha");
                    String horaInicio = obtenerParametro(body, "horaInicio");
                    String horaFin = obtenerParametro(body, "horaFin");
                    String cupoTotalTexto = obtenerParametro(body, "cupoTotal");
                    String idServicioTexto = obtenerParametro(body, "idServicio");
                    String idDisponibilidadTexto =
                            obtenerParametro(body, "idDisponibilidad");

                    if (fecha.isEmpty()
                            || cupoTotalTexto.isEmpty()
                            || idServicioTexto.isEmpty()) {

                        enviar(exchange, 400,
                                "{\"error\":\"Completa los campos obligatorios.\"}");
                        return;
                    }

                    int cupos;
                    int idServicio;
                    int idDisponibilidad = 0;

                    try {
                        cupos = Integer.parseInt(cupoTotalTexto);
                        idServicio = Integer.parseInt(idServicioTexto);

                        if (!idDisponibilidadTexto.isEmpty()) {
                            idDisponibilidad =
                                    Integer.parseInt(idDisponibilidadTexto);
                        }

                        if (cupos <= 0 || idServicio <= 0
                                || idDisponibilidad < 0) {
                            throw new NumberFormatException();
                        }

                    } catch (NumberFormatException e) {
                        enviar(exchange, 400,
                                "{\"error\":\"Los identificadores y cupos deben ser válidos.\"}");
                        return;
                    }

                    // ==========================================
                    // ACTUALIZAR UNA DISPONIBILIDAD EXISTENTE
                    // ==========================================

                    if (idDisponibilidad > 0) {

                        String sqlBuscar = """
            SELECT cupoTotal, cupoDisponible
            FROM Disponibilidad
            WHERE idDisponibilidad = ?
              AND idServicio = ?
            FOR UPDATE
            """;

                        String sqlActualizar = """
            UPDATE Disponibilidad
            SET fecha = ?,
                horaInicio = ?,
                horaFin = ?,
                cupoTotal = ?,
                cupoDisponible = ?
            WHERE idDisponibilidad = ?
              AND idServicio = ?
            """;

                        try (Connection con = ConexionDB.getConnection()) {

                            con.setAutoCommit(false);

                            try {
                                int cupoAnterior;
                                int cupoDisponibleAnterior;

                                try (PreparedStatement ps =
                                             con.prepareStatement(sqlBuscar)) {

                                    ps.setInt(1, idDisponibilidad);
                                    ps.setInt(2, idServicio);

                                    try (ResultSet rs = ps.executeQuery()) {

                                        if (!rs.next()) {
                                            con.rollback();

                                            enviar(exchange, 404,
                                                    "{\"error\":\"No se encontró la disponibilidad.\"}");
                                            return;
                                        }

                                        cupoAnterior = rs.getInt("cupoTotal");
                                        cupoDisponibleAnterior =
                                                rs.getInt("cupoDisponible");
                                    }
                                }

                                // Conservar los cupos ya ocupados
                                int cuposOcupados =
                                        cupoAnterior - cupoDisponibleAnterior;

                                if (cuposOcupados < 0) {
                                    con.rollback();

                                    enviar(exchange, 409,
                                            "{\"error\":\"Los datos actuales de cupos son inconsistentes.\"}");
                                    return;
                                }

                                if (cupos < cuposOcupados) {
                                    con.rollback();

                                    enviar(exchange, 409,
                                            "{\"error\":\"El nuevo cupo total no puede ser menor que los cupos ya ocupados.\"}");
                                    return;
                                }

                                int nuevosCuposDisponibles =
                                        cupos - cuposOcupados;

                                try (PreparedStatement ps =
                                             con.prepareStatement(sqlActualizar)) {

                                    ps.setString(1, fecha);
                                    ps.setString(2,
                                            horaInicio.isEmpty() ? null : horaInicio);
                                    ps.setString(3,
                                            horaFin.isEmpty() ? null : horaFin);
                                    ps.setInt(4, cupos);
                                    ps.setInt(5, nuevosCuposDisponibles);
                                    ps.setInt(6, idDisponibilidad);
                                    ps.setInt(7, idServicio);

                                    int filas = ps.executeUpdate();

                                    if (filas == 0) {
                                        con.rollback();

                                        enviar(exchange, 404,
                                                "{\"error\":\"No se pudo actualizar la disponibilidad.\"}");
                                        return;
                                    }
                                }

                                con.commit();

                                enviar(exchange, 200,
                                        "{\"mensaje\":\"Disponibilidad actualizada correctamente.\"}");

                            } catch (Exception e) {
                                con.rollback();
                                throw e;
                            } finally {
                                con.setAutoCommit(true);
                            }

                        }

                        return;
                    }

                    // ==========================================
                    // REGISTRAR UNA NUEVA DISPONIBILIDAD
                    // ==========================================

                    String sqlInsertar = """
        INSERT INTO Disponibilidad
        (fecha, horaInicio, horaFin, cupoTotal,
         cupoDisponible, estado, idServicio)
        VALUES (?, ?, ?, ?, ?, 'DISPONIBLE', ?)
        """;

                    try (Connection con = ConexionDB.getConnection();
                         PreparedStatement ps =
                                 con.prepareStatement(sqlInsertar)) {

                        ps.setString(1, fecha);
                        ps.setString(2,
                                horaInicio.isEmpty() ? null : horaInicio);
                        ps.setString(3,
                                horaFin.isEmpty() ? null : horaFin);
                        ps.setInt(4, cupos);
                        ps.setInt(5, cupos);
                        ps.setInt(6, idServicio);

                        ps.executeUpdate();

                        enviar(exchange, 200,
                                "{\"mensaje\":\"Disponibilidad registrada correctamente.\"}");
                    }

                    return;
                }

                enviar(exchange, 405, "{\"error\":\"Método no permitido.\"}");

            } catch (Exception e) {
                e.printStackTrace();

                sendJsonResponse(exchange, 500,
                        "{\"status\":\"error\",\"message\":\""
                                + jsonEscape(e.getMessage()) + "\"}");
            }
        }

        private static void enviar(HttpExchange exchange, int codigo, String respuesta)
                throws IOException {

            byte[] bytes = respuesta.getBytes(StandardCharsets.UTF_8);

            exchange.sendResponseHeaders(codigo, bytes.length);

            try (OutputStream os = exchange.getResponseBody()) {
                os.write(bytes);
            }
        }
    }

    private static String obtenerJson(String json, String clave) {
        String buscar = "\"" + clave + "\"";
        int inicio = json.indexOf(buscar);

        if (inicio == -1) return "";

        inicio = json.indexOf(":", inicio);

        if (inicio == -1) return "";

        inicio++;

        while (inicio < json.length() &&
                Character.isWhitespace(json.charAt(inicio))) {
            inicio++;
        }

        if (inicio < json.length() && json.charAt(inicio) == '"') {

            inicio++;

            int fin = json.indexOf("\"", inicio);

            if (fin == -1) return "";

            return json.substring(inicio, fin);
        }

        int fin = json.indexOf(",", inicio);

        if (fin == -1) {
            fin = json.indexOf("}", inicio);
        }

        if (fin == -1) return "";

        return json.substring(inicio, fin).trim();
    }

    private static String obtenerParametroFormulario(String body, String parametro) {

        if (body == null || body.isEmpty()) {
            return "";
        }

        String[] partes = body.split("&");

        for (String parte : partes) {

            String[] claveValor = parte.split("=", 2);

            if (claveValor.length == 2 && claveValor[0].equals(parametro)) {

                try {
                    return java.net.URLDecoder.decode(
                            claveValor[1],
                            java.nio.charset.StandardCharsets.UTF_8
                    );
                } catch (Exception e) {
                    return claveValor[1];
                }
            }
        }

        return "";
    }

    private static String obtenerParametro(String body, String parametro) {

        if (body == null || body.trim().isEmpty()) {
            return "";
        }

        body = body.trim();

        // Si viene como JSON
        if (body.startsWith("{")) {
            return obtenerJson(body, parametro);
        }

        // Si viene como formulario
        return obtenerParametroFormulario(body, parametro);
    }
>>>>>>> daac32cd486230b773d5739284598de3e2ff6164
}
