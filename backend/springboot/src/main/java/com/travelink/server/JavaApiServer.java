package com.travelink.server;

import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import com.sun.net.httpserver.HttpServer;
import com.travelink.config.ConexionDB;

import com.travelink.controlador.CalificacionControlador;
import com.travelink.controlador.UsuarioControlador;
import com.travelink.entidades.Usuario;
import com.travelink.repositorio.ReservaRepositorio;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

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
    private static final String FRONTEND_DIR = "frontend";

    public static void main(String[] args) throws IOException {
        HttpServer server = HttpServer.create(new InetSocketAddress(PORT), 0);

        System.out.println("========== TRAVELINK ==========");
        System.out.println("Servidor iniciado");
        System.out.println("FRONTEND_DIR = " + FRONTEND_DIR);

        // API Endpoints - TURISTA
        server.createContext("/api/login", new LoginHandler());
        server.createContext("/api/registro", new RegistroHandler());


        // Aquí conserva los demás endpoints existentes
        server.createContext("/api/guardar_reserva", new GuardarReservaHandler());
        server.createContext("/api/obtener_reservas", new ObtenerReservasHandler());
        server.createContext("/api/cancelar_reserva", new CancelarReservaHandler());
        server.createContext("/api/calificar", new CalificarHandler());
        server.createContext("/api/agencia/destinos", new DestinosAgenciaHandler());
        server.createContext("/api/agencia/tipos", new TiposAgenciaHandler());
        server.createContext("/api/agencia/servicios", new ServiciosAgenciaHandler());
        server.createContext("/api/agencia/registro", new RegistroAgenciaHandler());
        server.createContext("/api/agencia/disponibilidad", new DisponibilidadAgenciaHandler());
        server.createContext("/api/agencia/paquetes", new PaquetesAgenciaHandler());
        server.createContext("/api/agencia/reservas", new ReservasAgenciaHandler());
        server.createContext("/api/agencia/login", new LoginAgenciaHandler());
        server.createContext("/api/agencia/pagos", new PagosAgenciaHandler());
        server.createContext("/api/agencia/dashboard", new DashboardAgenciaHandler());

        //ADMINISTRADOR
        server.createContext("/api/admin/agencias", new AgenciasAdminHandler());
        server.createContext("/api/admin/notificaciones", new NotificacionesAdminHandler());
        server.createContext("/api/admin/destinos", new DestinosAdminHandler());
        server.createContext("/api/destinos", new DestinosAdminHandler());
        server.createContext("/api/admin/destinos/tours", new DestinosToursAdminHandler());
        server.createContext("/api/destinos/tours", new DestinosToursAdminHandler());
        server.createContext("/api/admin/tours", new ToursAdminHandler());
        server.createContext("/api/tours", new ToursAdminHandler());
        server.createContext("/api/admin/usuarios", new UsuariosAdminHandler());
        server.createContext("/api/usuarios", new UsuariosAdminHandler());
        server.createContext("/api/admin/reservas", new ReservasAdminHandler());
        server.createContext("/api/reservas", new ReservasAdminHandler());
        server.createContext("/api/admin/comisiones", new ComisionesAdminHandler());
        server.createContext("/api/comisiones", new ComisionesAdminHandler());
        server.createContext("/api/comision/liquidar", new LiquidarComisionHandler());
        server.createContext("/api/admin/calidad", new CalidadAdminHandler());
        server.createContext("/api/calidad", new CalidadAdminHandler());
        server.createContext("/api/calidad/eliminar", new EliminarResenaHandler());
        server.createContext("/api/admin/dashboard", new DashboardAdminHandler());

        // Static Files Handler (Serves frontend UI)
        server.createContext("/", new StaticFileHandler());

        server.setExecutor(null); // Default executor
        System.out.println("==================================================");
        System.out.println("Servidor Java iniciado exitosamente!");
        System.out.println("Servidor Backend API: http://localhost:" + PORT + "/api/");
        System.out.println("Aplicacion Web Frontend: http://localhost:" + PORT + "/html/index.html");
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

    private static String validarDuplicadosAgencia(Connection con, String ruc, String nroDocumento, String telefonoEmpresa, String telefonoResponsable, String correo, String nombreUsuario) throws SQLException {
        if (ruc != null && !ruc.isBlank()) {
            try (PreparedStatement ps = con.prepareStatement("SELECT COUNT(*) FROM Agencia WHERE ruc = ?")) {
                ps.setString(1, ruc);
                try (ResultSet rs = ps.executeQuery()) {
                    if (rs.next() && rs.getInt(1) > 0) return "El RUC '" + ruc + "' ya se encuentra registrado en el sistema.";
                }
            }
            try (PreparedStatement ps = con.prepareStatement("SELECT COUNT(*) FROM SolicitudAgencia WHERE ruc = ?")) {
                ps.setString(1, ruc);
                try (ResultSet rs = ps.executeQuery()) {
                    if (rs.next() && rs.getInt(1) > 0) return "El RUC '" + ruc + "' ya se encuentra registrado en una solicitud de agencia.";
                }
            }
        }

        if (nroDocumento != null && !nroDocumento.isBlank()) {
            try (PreparedStatement ps = con.prepareStatement("SELECT COUNT(*) FROM Persona WHERE nroDocumento = ?")) {
                ps.setString(1, nroDocumento);
                try (ResultSet rs = ps.executeQuery()) {
                    if (rs.next() && rs.getInt(1) > 0) return "El DNI / documento '" + nroDocumento + "' ya se encuentra registrado en el sistema.";
                }
            }
        }

        for (String tel : new String[]{telefonoEmpresa, telefonoResponsable}) {
            if (tel != null && !tel.isBlank()) {
                try (PreparedStatement ps = con.prepareStatement("SELECT COUNT(*) FROM Persona WHERE telefono = ?")) {
                    ps.setString(1, tel);
                    try (ResultSet rs = ps.executeQuery()) {
                        if (rs.next() && rs.getInt(1) > 0) return "El número de teléfono '" + tel + "' ya se encuentra registrado.";
                    }
                }
                try (PreparedStatement ps = con.prepareStatement("SELECT COUNT(*) FROM Agencia WHERE telefono = ?")) {
                    ps.setString(1, tel);
                    try (ResultSet rs = ps.executeQuery()) {
                        if (rs.next() && rs.getInt(1) > 0) return "El número de teléfono '" + tel + "' ya se encuentra registrado en una agencia.";
                    }
                }
                try (PreparedStatement ps = con.prepareStatement("SELECT COUNT(*) FROM SolicitudAgencia WHERE telefonoContacto = ?")) {
                    ps.setString(1, tel);
                    try (ResultSet rs = ps.executeQuery()) {
                        if (rs.next() && rs.getInt(1) > 0) return "El número de teléfono '" + tel + "' ya se encuentra registrado en una solicitud.";
                    }
                }
            }
        }

        if (correo != null && !correo.isBlank()) {
            try (PreparedStatement ps = con.prepareStatement("SELECT COUNT(*) FROM Persona WHERE email = ?")) {
                ps.setString(1, correo);
                try (ResultSet rs = ps.executeQuery()) {
                    if (rs.next() && rs.getInt(1) > 0) return "El correo electrónico '" + correo + "' ya se encuentra registrado.";
                }
            }
            try (PreparedStatement ps = con.prepareStatement("SELECT COUNT(*) FROM Agencia WHERE email = ?")) {
                ps.setString(1, correo);
                try (ResultSet rs = ps.executeQuery()) {
                    if (rs.next() && rs.getInt(1) > 0) return "El correo electrónico '" + correo + "' ya se encuentra registrado en una agencia.";
                }
            }
            try (PreparedStatement ps = con.prepareStatement("SELECT COUNT(*) FROM SolicitudAgencia WHERE correoContacto = ?")) {
                ps.setString(1, correo);
                try (ResultSet rs = ps.executeQuery()) {
                    if (rs.next() && rs.getInt(1) > 0) return "El correo electrónico '" + correo + "' ya se encuentra registrado en una solicitud.";
                }
            }
        }

        if (nombreUsuario != null && !nombreUsuario.isBlank()) {
            try (PreparedStatement ps = con.prepareStatement("SELECT COUNT(*) FROM Usuario WHERE nombreUsuario = ?")) {
                ps.setString(1, nombreUsuario);
                try (ResultSet rs = ps.executeQuery()) {
                    if (rs.next() && rs.getInt(1) > 0) return "El nombre de usuario '" + nombreUsuario + "' ya se encuentra registrado.";
                }
            }
        }

        return null;
    }

    private static String validarDuplicadosTurista(Connection con, String nroDocumento, String telefono, String correo, String nombreUsuario) throws SQLException {
        if (nroDocumento != null && !nroDocumento.isBlank()) {
            try (PreparedStatement ps = con.prepareStatement("SELECT COUNT(*) FROM Persona WHERE nroDocumento = ?")) {
                ps.setString(1, nroDocumento);
                try (ResultSet rs = ps.executeQuery()) {
                    if (rs.next() && rs.getInt(1) > 0) return "El DNI / documento '" + nroDocumento + "' ya se encuentra registrado.";
                }
            }
        }
        if (telefono != null && !telefono.isBlank()) {
            try (PreparedStatement ps = con.prepareStatement("SELECT COUNT(*) FROM Persona WHERE telefono = ?")) {
                ps.setString(1, telefono);
                try (ResultSet rs = ps.executeQuery()) {
                    if (rs.next() && rs.getInt(1) > 0) return "El número de teléfono '" + telefono + "' ya se encuentra registrado.";
                }
            }
        }
        if (correo != null && !correo.isBlank()) {
            try (PreparedStatement ps = con.prepareStatement("SELECT COUNT(*) FROM Persona WHERE email = ?")) {
                ps.setString(1, correo);
                try (ResultSet rs = ps.executeQuery()) {
                    if (rs.next() && rs.getInt(1) > 0) return "El correo electrónico '" + correo + "' ya se encuentra registrado.";
                }
            }
        }
        if (nombreUsuario != null && !nombreUsuario.isBlank()) {
            try (PreparedStatement ps = con.prepareStatement("SELECT COUNT(*) FROM Usuario WHERE nombreUsuario = ?")) {
                ps.setString(1, nombreUsuario);
                try (ResultSet rs = ps.executeQuery()) {
                    if (rs.next() && rs.getInt(1) > 0) return "El nombre de usuario '" + nombreUsuario + "' ya está en uso.";
                }
            }
        }
        return null;
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
                sendJsonResponse(exchange, 200, json.toString());
            } else {
                sendJsonResponse(exchange, 405, "{\"status\":\"error\",\"message\":\"Método no permitido\"}");
            }
        }
    }

    // 2. REGISTRO HANDLER - TURISTA
    static class RegistroHandler implements HttpHandler {
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
                    sendJsonResponse(exchange, 400, "{\"status\":\"error\",\"message\":\"Completa los campos obligatorios.\"}");
                    return;
                }

                if (!nroDocumento.isEmpty() && !nroDocumento.matches("^[0-9]{8}$")) {
                    sendJsonResponse(exchange, 400, "{\"status\":\"error\",\"message\":\"El DNI debe contener exactamente 8 dígitos numéricos.\"}");
                    return;
                }

                if (!telefono.isEmpty() && !telefono.matches("^[0-9]{7,9}$")) {
                    sendJsonResponse(exchange, 400, "{\"status\":\"error\",\"message\":\"El teléfono debe contener entre 7 y 9 dígitos numéricos.\"}");
                    return;
                }

                if (!correo.matches("^[\\w-\\.]+@([\\w-]+\\.)+[\\w-]{2,4}$")) {
                    sendJsonResponse(exchange, 400, "{\"status\":\"error\",\"message\":\"El correo electrónico ingresado no es válido.\"}");
                    return;
                }

                // Check BD duplicados
                try (Connection con = ConexionDB.getConnection()) {
                    if (con != null) {
                        String errDup = validarDuplicadosTurista(con, nroDocumento, telefono, correo, nombreUsuario);
                        if (errDup != null) {
                            sendJsonResponse(exchange, 400, "{\"status\":\"error\",\"message\":\"" + errDup.replace("\"", "\\\"") + "\"}");
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
                    json.append("{\"status\":\"error\",\"message\":\"")
                            .append(res.get("message"))
                            .append("\"}");
                }

                sendJsonResponse(exchange, 200, json.toString());
            } else {
                sendJsonResponse(exchange, 405,
                        "{\"status\":\"error\",\"message\":\"Método no permitido\"}");
            }
        }
    }
    // LOGIN - AGENCIA

    static class LoginAgenciaHandler implements HttpHandler {

        @Override
        public void handle(HttpExchange exchange) throws IOException {

            if ("OPTIONS".equalsIgnoreCase(exchange.getRequestMethod())) {
                enableCORS(exchange);
                exchange.sendResponseHeaders(200, -1);
                return;
            }

            if (!"POST".equalsIgnoreCase(exchange.getRequestMethod())) {
                sendJsonResponse(exchange, 405,
                        "{\"status\":\"error\",\"message\":\"Método no permitido\"}");
                return;
            }

            Map<String, Object> params =
                    parseJsonOrFormParams(readRequestBody(exchange));

            String usuario = String.valueOf(
                    params.getOrDefault("usuario", "")
            ).trim();

            String contrasena = String.valueOf(
                    params.getOrDefault("contrasena",
                            params.getOrDefault("contraseña",
                                    params.getOrDefault("password", "")))
            );

            if (usuario.isEmpty() || contrasena.isEmpty()) {
                sendJsonResponse(exchange, 400,
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
                    sendJsonResponse(exchange, 500,
                            "{\"status\":\"error\",\"message\":\"No se pudo conectar a la base de datos.\"}");
                    return;
                }

                try (PreparedStatement ps = con.prepareStatement(sql)) {

                    ps.setString(1, usuario);
                    ps.setString(2, usuario);

                    try (ResultSet rs = ps.executeQuery()) {

                        if (!rs.next()) {
                            sendJsonResponse(exchange, 401,
                                    "{\"status\":\"error\",\"message\":\"Usuario o contraseña incorrectos.\"}");
                            return;
                        }

                        String passwordBD = rs.getString("contrasena");

                        // Compatible con el registro actual, que guarda
                        // la contraseña directamente. Migrar a hash seguro.
                        if (passwordBD == null || !passwordBD.equals(contrasena)) {
                            sendJsonResponse(exchange, 401,
                                    "{\"status\":\"error\",\"message\":\"Usuario o contraseña incorrectos.\"}");
                            return;
                        }

                        String estadoUsuario = rs.getString("estadoUsuario");
                        String estadoAgencia = rs.getString("estadoAgencia");

                        if (!"ACTIVO".equalsIgnoreCase(estadoUsuario)) {
                            sendJsonResponse(exchange, 403,
                                    "{\"status\":\"error\",\"message\":\"La cuenta no está activa.\"}");
                            return;
                        }

                        if (!"ACTIVO".equalsIgnoreCase(estadoAgencia)) {
                            sendJsonResponse(exchange, 403,
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

                        sendJsonResponse(exchange, 200, json);
                    }
                }

            } catch (Exception e) {
                e.printStackTrace();
                sendJsonResponse(exchange, 500,
                        "{\"status\":\"error\",\"message\":\"Error interno al iniciar sesión.\"}");
            }
        }
    }
    // 3. REGISTRO HANDLER - AGENCIA
    static class RegistroAgenciaHandler implements HttpHandler {

        @Override
        public void handle(HttpExchange exchange) throws IOException {
            if ("OPTIONS".equalsIgnoreCase(exchange.getRequestMethod())) {
                enableCORS(exchange);
                exchange.sendResponseHeaders(200, -1);
                return;
            }

            if (!"POST".equalsIgnoreCase(exchange.getRequestMethod())) {
                sendJsonResponse(exchange, 405,
                        "{\"status\":\"error\",\"message\":\"Método no permitido\"}");
                return;
            }

            String body = readRequestBody(exchange);
            Map<String, Object> params = parseJsonOrFormParams(body);

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

                sendJsonResponse(exchange, 400,
                        "{\"status\":\"error\",\"message\":\"Completa los campos obligatorios.\"}");
                return;
            }

            // Validaciones de formato
            if (!ruc.matches("^[0-9]{11}$")) {
                sendJsonResponse(exchange, 400, "{\"status\":\"error\",\"message\":\"El RUC debe tener exactamente 11 dígitos numéricos.\"}");
                return;
            }

            if (!nroDocumento.matches("^[0-9]{8}$")) {
                sendJsonResponse(exchange, 400, "{\"status\":\"error\",\"message\":\"El DNI debe tener exactamente 8 dígitos numéricos.\"}");
                return;
            }

            if (!telefonoEmpresa.isEmpty() && !telefonoEmpresa.matches("^[0-9]{7,9}$")) {
                sendJsonResponse(exchange, 400, "{\"status\":\"error\",\"message\":\"El teléfono de la empresa debe tener entre 7 y 9 dígitos numéricos.\"}");
                return;
            }

            if (!telefonoResponsable.isEmpty() && !telefonoResponsable.matches("^[0-9]{7,9}$")) {
                sendJsonResponse(exchange, 400, "{\"status\":\"error\",\"message\":\"El teléfono del responsable debe tener entre 7 y 9 dígitos numéricos.\"}");
                return;
            }

            if (!correo.matches("^[\\w-\\.]+@([\\w-]+\\.)+[\\w-]{2,4}$")) {
                sendJsonResponse(exchange, 400, "{\"status\":\"error\",\"message\":\"El correo electrónico ingresado no es válido.\"}");
                return;
            }

            try (java.sql.Connection con = ConexionDB.getConnection()) {
                if (con == null) {
                    sendJsonResponse(exchange, 500,
                            "{\"status\":\"error\",\"message\":\"No se pudo conectar a la base de datos.\"}");
                    return;
                }

                // Check BD duplicados
                String errDup = validarDuplicadosAgencia(con, ruc, nroDocumento, telefonoEmpresa, telefonoResponsable, correo, nombreUsuario);
                if (errDup != null) {
                    sendJsonResponse(exchange, 400, "{\"status\":\"error\",\"message\":\"" + errDup.replace("\"", "\\\"") + "\"}");
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

                    sendJsonResponse(exchange, 500,
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

                sendJsonResponse(exchange, 500,
                        "{\"status\":\"error\",\"message\":\""
                                + mensaje + "\"}");
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

                            .append("\"idReserva\":").append(d.get("idReserva")).append(",")
                            .append("\"idPago\":").append(d.get("idPago")).append(",")
                            .append("\"codigo\":\"").append(d.get("codigo")).append("\",")
                            .append("\"estado\":\"").append(d.get("estado")).append("\",")
                            .append("\"precioTotal\":").append(d.get("precioTotal")).append(",")
                            .append("\"metodoPago\":\"").append(d.get("metodoPago")).append("\"")
                            .append("}}");

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
                         "SELECT idDestino, nombre FROM Destino WHERE estado = 'ACTIVO' AND nombre IN ('Lima', 'Pasco', 'Tacna', 'Moquegua', 'Ilo', 'Madre de Dios') ORDER BY idDestino");
                 ResultSet rs = ps.executeQuery()) {

                boolean primero = true;
                while (rs.next()) {
                    if (!primero) json.append(",");
                    json.append("{")
                            .append("\"idDestino\":").append(rs.getInt("idDestino")).append(",")
                            .append("\"nombre\":\"").append(jsonEscape(rs.getString("nombre"))).append("\"")
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
                procesarPostServicio(exchange);
                return;
            }

            sendJsonResponse(exchange, 405,
                    "{\"status\":\"error\",\"message\":\"Método no permitido\"}");
        }

        private void obtenerServicios(HttpExchange exchange) throws IOException {
            Map<String, String> params = parseQueryParams(exchange.getRequestURI().getQuery());
            int idAgencia = 0;
            try {
                idAgencia = Integer.parseInt(params.getOrDefault("idAgencia", "1"));
            } catch (Exception e) {
                idAgencia = 1;
            }

            String buscar = params.getOrDefault("buscar", "").trim().toLowerCase();
            String tipoFiltro = params.getOrDefault("tipo", "").trim();
            String estadoFiltro = params.getOrDefault("estado", "").trim();

            StringBuilder json = new StringBuilder();
            json.append("{\"status\":\"success\",\"servicios\":[");

            try (Connection con = ConexionDB.getConnection()) {
                StringBuilder sql = new StringBuilder(
                        "SELECT t.idTour, t.nombre, t.descripcion, t.categoria, t.precioAdulto, t.precioNino, t.precioBebe, " +
                        "t.duracion, t.estado, t.idAgencia, t.idDestino, d.nombre AS destino " +
                        "FROM Tour t " +
                        "INNER JOIN Destino d ON t.idDestino = d.idDestino " +
                        "WHERE t.idAgencia = ? "
                );

                if (!buscar.isEmpty()) {
                    sql.append("AND LOWER(t.nombre) LIKE ? ");
                }
                if (!tipoFiltro.isEmpty() && !"Todos".equalsIgnoreCase(tipoFiltro)) {
                    sql.append("AND t.categoria = ? ");
                }
                if (!estadoFiltro.isEmpty() && !"Todos".equalsIgnoreCase(estadoFiltro)) {
                    sql.append("AND t.estado = ? ");
                }
                sql.append("ORDER BY t.idTour DESC");

                try (PreparedStatement ps = con.prepareStatement(sql.toString())) {
                    int paramIdx = 1;
                    ps.setInt(paramIdx++, idAgencia);
                    if (!buscar.isEmpty()) {
                        ps.setString(paramIdx++, "%" + buscar + "%");
                    }
                    if (!tipoFiltro.isEmpty() && !"Todos".equalsIgnoreCase(tipoFiltro)) {
                        ps.setString(paramIdx++, tipoFiltro);
                    }
                    if (!estadoFiltro.isEmpty() && !"Todos".equalsIgnoreCase(estadoFiltro)) {
                        ps.setString(paramIdx++, estadoFiltro);
                    }

                    try (ResultSet rs = ps.executeQuery()) {
                        boolean primero = true;
                        while (rs.next()) {
                            int idTour = rs.getInt("idTour");
                            String nombre = rs.getString("nombre");
                            String desc = rs.getString("descripcion");
                            String cat = rs.getString("categoria");
                            double pAdulto = rs.getDouble("precioAdulto");
                            double pNino = rs.getDouble("precioNino");
                            double pBebe = rs.getDouble("precioBebe");
                            String duracion = rs.getString("duracion");
                            String estado = rs.getString("estado");
                            int idDestino = rs.getInt("idDestino");
                            String destino = rs.getString("destino");

                            // Imágenes del tour
                            StringBuilder imagenesJson = new StringBuilder("[");
                            String mainImgUrl = "";
                            try (PreparedStatement psImg = con.prepareStatement(
                                    "SELECT idImagen, url, esPrincipal, orden FROM TourImagen WHERE idTour = ? ORDER BY esPrincipal DESC, orden ASC")) {
                                psImg.setInt(1, idTour);
                                try (ResultSet rsImg = psImg.executeQuery()) {
                                    boolean primeroImg = true;
                                    while (rsImg.next()) {
                                        if (!primeroImg) imagenesJson.append(",");
                                        String url = rsImg.getString("url");
                                        boolean esPrinc = rsImg.getBoolean("esPrincipal");
                                        if (esPrinc && mainImgUrl.isEmpty()) mainImgUrl = url;
                                        if (mainImgUrl.isEmpty()) mainImgUrl = url;

                                        imagenesJson.append("{")
                                                .append("\"idImagen\":").append(rsImg.getInt("idImagen")).append(",")
                                                .append("\"url\":\"").append(jsonEscape(url)).append("\",")
                                                .append("\"esPrincipal\":").append(esPrinc).append(",")
                                                .append("\"orden\":").append(rsImg.getInt("orden"))
                                                .append("}");
                                        primeroImg = false;
                                    }
                                }
                            }
                            imagenesJson.append("]");

                            // Verificar reservas activas futuras
                            boolean tieneReservasActivas = false;
                            try (PreparedStatement psCheck = con.prepareStatement(
                                    "SELECT COUNT(*) FROM DetalleReserva dr " +
                                    "INNER JOIN TourFecha tf ON dr.idTourFecha = tf.idTourFecha " +
                                    "INNER JOIN Reserva r ON dr.idReserva = r.idReserva " +
                                    "WHERE tf.idTour = ? AND r.estado IN ('CONFIRMADA', 'PENDIENTE') AND tf.fecha >= CURRENT_DATE")) {
                                psCheck.setInt(1, idTour);
                                try (ResultSet rsC = psCheck.executeQuery()) {
                                    if (rsC.next() && rsC.getInt(1) > 0) tieneReservasActivas = true;
                                }
                            }

                            // Total de reservas históricas
                            int reservasHistoricas = 0;
                            try (PreparedStatement psHist = con.prepareStatement(
                                    "SELECT COUNT(*) FROM DetalleReserva dr " +
                                    "INNER JOIN TourFecha tf ON dr.idTourFecha = tf.idTourFecha " +
                                    "WHERE tf.idTour = ?")) {
                                psHist.setInt(1, idTour);
                                try (ResultSet rsH = psHist.executeQuery()) {
                                    if (rsH.next()) reservasHistoricas = rsH.getInt(1);
                                }
                            }

                            double comision = pAdulto * 0.15;
                            double neto = pAdulto * 0.85;

                            if (!primero) json.append(",");
                            json.append("{")
                                    .append("\"idServicio\":").append(idTour).append(",")
                                    .append("\"idTour\":").append(idTour).append(",")
                                    .append("\"nombre\":\"").append(jsonEscape(nombre)).append("\",")
                                    .append("\"descripcion\":\"").append(jsonEscape(desc)).append("\",")
                                    .append("\"tipoServicio\":\"").append(jsonEscape(cat)).append("\",")
                                    .append("\"categoria\":\"").append(jsonEscape(cat)).append("\",")
                                    .append("\"idDestino\":").append(idDestino).append(",")
                                    .append("\"destino\":\"").append(jsonEscape(destino)).append("\",")
                                    .append("\"precio\":").append(pAdulto).append(",")
                                    .append("\"precioAdulto\":").append(pAdulto).append(",")
                                    .append("\"precioNino\":").append(pNino).append(",")
                                    .append("\"precioBebe\":").append(pBebe).append(",")
                                    .append("\"comision15\":").append(String.format(Locale.US, "%.2f", comision)).append(",")
                                    .append("\"neto85\":").append(String.format(Locale.US, "%.2f", neto)).append(",")
                                    .append("\"duracion\":\"").append(jsonEscape(duracion)).append("\",")
                                    .append("\"estado\":\"").append(jsonEscape(estado)).append("\",")
                                    .append("\"imagenPrincipal\":\"").append(jsonEscape(mainImgUrl)).append("\",")
                                    .append("\"imagenes\":").append(imagenesJson.toString()).append(",")
                                    .append("\"tieneReservasActivas\":").append(tieneReservasActivas).append(",")
                                    .append("\"reservasHistoricas\":").append(reservasHistoricas)
                                    .append("}");
                            primero = false;
                        }
                    }
                }

                json.append("]}");
                sendJsonResponse(exchange, 200, json.toString());

            } catch (Exception e) {
                e.printStackTrace();
                sendJsonResponse(exchange, 500, "{\"status\":\"error\",\"message\":\"Error al obtener los servicios\"}");
            }
        }

        private void procesarPostServicio(HttpExchange exchange) throws IOException {
            String body = readRequestBody(exchange);
            Map<String, Object> params = parseJsonOrFormParams(body);

            String action = String.valueOf(params.getOrDefault("action", "")).trim();

            if ("cambiar_estado".equalsIgnoreCase(action) || "pausar".equalsIgnoreCase(action) || "activar".equalsIgnoreCase(action)) {
                cambiarEstadoServicio(exchange, params);
                return;
            }

            if ("eliminar".equalsIgnoreCase(action)) {
                eliminarServicio(exchange, params);
                return;
            }

            guardarServicio(exchange, params);
        }

        private void cambiarEstadoServicio(HttpExchange exchange, Map<String, Object> params) throws IOException {
            int idTour = 0;
            try {
                idTour = Integer.parseInt(String.valueOf(params.getOrDefault("idServicio", params.getOrDefault("idTour", "0"))));
            } catch (Exception e) {}

            int idAgencia = 0;
            try {
                idAgencia = Integer.parseInt(String.valueOf(params.getOrDefault("idAgencia", "1")));
            } catch (Exception e) { idAgencia = 1; }

            String nuevoEstado = String.valueOf(params.getOrDefault("nuevoEstado", "")).trim().toUpperCase();
            if (nuevoEstado.isEmpty()) {
                String act = String.valueOf(params.getOrDefault("action", "")).trim();
                if ("pausar".equalsIgnoreCase(act)) nuevoEstado = "PAUSADO";
                else if ("activar".equalsIgnoreCase(act)) nuevoEstado = "ACTIVO";
            }

            if (idTour <= 0) {
                sendJsonResponse(exchange, 400, "{\"status\":\"error\",\"message\":\"ID de servicio no válido\"}");
                return;
            }

            try (Connection con = ConexionDB.getConnection()) {
                // Verificar estado actual
                String estadoActual = "";
                try (PreparedStatement ps = con.prepareStatement("SELECT estado FROM Tour WHERE idTour = ? AND idAgencia = ?")) {
                    ps.setInt(1, idTour);
                    ps.setInt(2, idAgencia);
                    try (ResultSet rs = ps.executeQuery()) {
                        if (rs.next()) {
                            estadoActual = rs.getString("estado");
                        } else {
                            sendJsonResponse(exchange, 404, "{\"status\":\"error\",\"message\":\"Servicio no encontrado\"}");
                            return;
                        }
                    }
                }

                // Si fue desactivado por el administrador, la agencia NO puede modificarlo
                if ("DESACTIVADO_POR_ADMIN".equalsIgnoreCase(estadoActual)) {
                    sendJsonResponse(exchange, 403, "{\"status\":\"error\",\"message\":\"Este servicio fue desactivado por el administrador y no puede ser reactivado por la agencia.\"}");
                    return;
                }

                // Si intenta pausar, validar reservas activas futuras
                if ("PAUSADO".equalsIgnoreCase(nuevoEstado)) {
                    try (PreparedStatement psCheck = con.prepareStatement(
                            "SELECT COUNT(*) FROM DetalleReserva dr " +
                            "INNER JOIN TourFecha tf ON dr.idTourFecha = tf.idTourFecha " +
                            "INNER JOIN Reserva r ON dr.idReserva = r.idReserva " +
                            "WHERE tf.idTour = ? AND r.estado IN ('CONFIRMADA', 'PENDIENTE') AND tf.fecha >= CURRENT_DATE")) {
                        psCheck.setInt(1, idTour);
                        try (ResultSet rsC = psCheck.executeQuery()) {
                            if (rsC.next() && rsC.getInt(1) > 0) {
                                sendJsonResponse(exchange, 400, "{\"status\":\"error\",\"message\":\"No se puede pausar un servicio con reservas activas futuras (" + rsC.getInt(1) + " reserva(s) pendiente(s)).\"}");
                                return;
                            }
                        }
                    }
                }

                try (PreparedStatement psUpd = con.prepareStatement("UPDATE Tour SET estado = ? WHERE idTour = ? AND idAgencia = ?")) {
                    psUpd.setString(1, nuevoEstado);
                    psUpd.setInt(2, idTour);
                    psUpd.setInt(3, idAgencia);
                    psUpd.executeUpdate();
                }

                sendJsonResponse(exchange, 200, "{\"status\":\"success\",\"message\":\"Estado del servicio actualizado a " + nuevoEstado + "\"}");

            } catch (Exception e) {
                e.printStackTrace();
                sendJsonResponse(exchange, 500, "{\"status\":\"error\",\"message\":\"Error al cambiar el estado del servicio: " + e.getMessage() + "\"}");
            }
        }

        private void eliminarServicio(HttpExchange exchange, Map<String, Object> params) throws IOException {
            int idTour = 0;
            try {
                idTour = Integer.parseInt(String.valueOf(params.getOrDefault("idServicio", params.getOrDefault("idTour", "0"))));
            } catch (Exception e) {}

            int idAgencia = 0;
            try {
                idAgencia = Integer.parseInt(String.valueOf(params.getOrDefault("idAgencia", "1")));
            } catch (Exception e) { idAgencia = 1; }

            if (idTour <= 0) {
                sendJsonResponse(exchange, 400, "{\"status\":\"error\",\"message\":\"ID de servicio no válido\"}");
                return;
            }

            try (Connection con = ConexionDB.getConnection()) {
                // Verificar reservas históricas
                int reservasHistoricas = 0;
                try (PreparedStatement psHist = con.prepareStatement(
                        "SELECT COUNT(*) FROM DetalleReserva dr " +
                        "INNER JOIN TourFecha tf ON dr.idTourFecha = tf.idTourFecha " +
                        "WHERE tf.idTour = ?")) {
                    psHist.setInt(1, idTour);
                    try (ResultSet rsH = psHist.executeQuery()) {
                        if (rsH.next()) reservasHistoricas = rsH.getInt(1);
                    }
                }

                // Si tuvo reservas, NO se elimina: se pausa automáticamente
                if (reservasHistoricas > 0) {
                    try (PreparedStatement psPausar = con.prepareStatement("UPDATE Tour SET estado = 'PAUSADO' WHERE idTour = ? AND idAgencia = ?")) {
                        psPausar.setInt(1, idTour);
                        psPausar.setInt(2, idAgencia);
                        psPausar.executeUpdate();
                    }
                    sendJsonResponse(exchange, 400, "{\"status\":\"warning\",\"canPause\":true,\"message\":\"El servicio no se puede eliminar porque tiene historial de reservas registradas. Ha sido pausado automáticamente.\"}");
                    return;
                }

                // Si no tuvo reservas, se elimina completamente
                try (PreparedStatement psDelImg = con.prepareStatement("DELETE FROM TourImagen WHERE idTour = ?")) {
                    psDelImg.setInt(1, idTour);
                    psDelImg.executeUpdate();
                }
                try (PreparedStatement psDelFechas = con.prepareStatement("DELETE FROM TourFecha WHERE idTour = ?")) {
                    psDelFechas.setInt(1, idTour);
                    psDelFechas.executeUpdate();
                }
                try (PreparedStatement psDelTour = con.prepareStatement("DELETE FROM Tour WHERE idTour = ? AND idAgencia = ?")) {
                    psDelTour.setInt(1, idTour);
                    psDelTour.setInt(2, idAgencia);
                    psDelTour.executeUpdate();
                }

                sendJsonResponse(exchange, 200, "{\"status\":\"success\",\"message\":\"Servicio eliminado exitosamente.\"}");

            } catch (Exception e) {
                e.printStackTrace();
                sendJsonResponse(exchange, 500, "{\"status\":\"error\",\"message\":\"Error al eliminar el servicio: " + e.getMessage() + "\"}");
            }
        }

        private void guardarServicio(HttpExchange exchange, Map<String, Object> params) throws IOException {
            String nombre = String.valueOf(params.getOrDefault("nombre", "")).trim();
            String descripcion = String.valueOf(params.getOrDefault("descripcion", "")).trim();
            String tipoServicio = String.valueOf(params.getOrDefault("tipoServicio", params.getOrDefault("categoria", ""))).trim();
            String duracion = String.valueOf(params.getOrDefault("duracion", "")).trim();
            String estado = String.valueOf(params.getOrDefault("estado", "ACTIVO")).trim();

            int idAgencia = 1;
            int idDestino = 0;
            int idTour = 0;

            try {
                idAgencia = Integer.parseInt(String.valueOf(params.getOrDefault("idAgencia", "1")));
            } catch (Exception ignored) {}

            try {
                idDestino = Integer.parseInt(String.valueOf(params.getOrDefault("idDestino", "2")));
            } catch (Exception ignored) {}

            try {
                idTour = Integer.parseInt(String.valueOf(params.getOrDefault("idServicio", params.getOrDefault("idTour", "0"))));
            } catch (Exception ignored) {}

            double pAdulto = 0.0;
            double pNino = 0.0;
            double pBebe = 0.0;

            try {
                pAdulto = Double.parseDouble(String.valueOf(params.getOrDefault("precioAdulto", params.getOrDefault("precio", "0"))));
                pNino = Double.parseDouble(String.valueOf(params.getOrDefault("precioNino", "0")));
                pBebe = Double.parseDouble(String.valueOf(params.getOrDefault("precioBebe", "0")));
            } catch (Exception e) {
                sendJsonResponse(exchange, 400, "{\"status\":\"error\",\"message\":\"Los valores de precios no son números válidos\"}");
                return;
            }

            // Validaciones
            if (nombre.isEmpty() || tipoServicio.isEmpty() || duracion.isEmpty() || idDestino <= 0) {
                sendJsonResponse(exchange, 400, "{\"status\":\"error\",\"message\":\"Completa todos los campos obligatorios: Nombre, Tipo, Destino y Duración.\"}");
                return;
            }

            // Validación de precios: Adulto > 0 y Adulto >= Niño >= Bebé
            if (pAdulto <= 0) {
                sendJsonResponse(exchange, 400, "{\"status\":\"error\",\"message\":\"El precio de adulto debe ser mayor a 0.\"}");
                return;
            }

            if (pAdulto < pNino || pNino < pBebe || pBebe < 0) {
                sendJsonResponse(exchange, 400, "{\"status\":\"error\",\"message\":\"Los precios deben cumplir la regla de jerarquía: Adulto ≥ Niño ≥ Bebé.\"}");
                return;
            }

            try (Connection con = ConexionDB.getConnection()) {
                // Validación: Nombre único dentro de la misma agencia
                try (PreparedStatement psCheckName = con.prepareStatement(
                        "SELECT idTour FROM Tour WHERE idAgencia = ? AND LOWER(TRIM(nombre)) = LOWER(TRIM(?)) AND idTour != ?")) {
                    psCheckName.setInt(1, idAgencia);
                    psCheckName.setString(2, nombre);
                    psCheckName.setInt(3, idTour);
                    try (ResultSet rsN = psCheckName.executeQuery()) {
                        if (rsN.next()) {
                            sendJsonResponse(exchange, 400, "{\"status\":\"error\",\"message\":\"Ya existe un servicio con el nombre '" + nombre + "' registrado en tu agencia.\"}");
                            return;
                        }
                    }
                }

                if (idTour > 0) {
                    // Actualizar
                    // Verificar que no esté desactivado por admin
                    try (PreparedStatement psCheckAdmin = con.prepareStatement("SELECT estado FROM Tour WHERE idTour = ? AND idAgencia = ?")) {
                        psCheckAdmin.setInt(1, idTour);
                        psCheckAdmin.setInt(2, idAgencia);
                        try (ResultSet rsA = psCheckAdmin.executeQuery()) {
                            if (rsA.next() && "DESACTIVADO_POR_ADMIN".equalsIgnoreCase(rsA.getString("estado"))) {
                                sendJsonResponse(exchange, 403, "{\"status\":\"error\",\"message\":\"No se puede editar un servicio desactivado por el administrador.\"}");
                                return;
                            }
                        }
                    }

                    String sqlUpd = "UPDATE Tour SET nombre = ?, descripcion = ?, categoria = ?, precioAdulto = ?, precioNino = ?, precioBebe = ?, duracion = ?, estado = ?, idDestino = ? WHERE idTour = ? AND idAgencia = ?";
                    try (PreparedStatement ps = con.prepareStatement(sqlUpd)) {
                        ps.setString(1, nombre);
                        ps.setString(2, descripcion);
                        ps.setString(3, tipoServicio);
                        ps.setDouble(4, pAdulto);
                        ps.setDouble(5, pNino);
                        ps.setDouble(6, pBebe);
                        ps.setString(7, duracion);
                        ps.setString(8, estado);
                        ps.setInt(9, idDestino);
                        ps.setInt(10, idTour);
                        ps.setInt(11, idAgencia);
                        ps.executeUpdate();
                    }
                } else {
                    // Crear nuevo
                    String slug = nombre.toLowerCase().replaceAll("[^a-z0-9]+", "-") + "-" + (System.currentTimeMillis() % 100000);
                    String sqlIns = "INSERT INTO Tour (idAgencia, idDestino, slug, nombre, descripcion, precioAdulto, precioNino, precioBebe, duracion, categoria, estado) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";
                    try (PreparedStatement ps = con.prepareStatement(sqlIns, Statement.RETURN_GENERATED_KEYS)) {
                        ps.setInt(1, idAgencia);
                        ps.setInt(2, idDestino);
                        ps.setString(3, slug);
                        ps.setString(4, nombre);
                        ps.setString(5, descripcion);
                        ps.setDouble(6, pAdulto);
                        ps.setDouble(7, pNino);
                        ps.setDouble(8, pBebe);
                        ps.setString(9, duracion);
                        ps.setString(10, tipoServicio);
                        ps.setString(11, estado);
                        ps.executeUpdate();
                        try (ResultSet rk = ps.getGeneratedKeys()) {
                            if (rk.next()) idTour = rk.getInt(1);
                        }
                    }
                }

                // Procesar imágenes si se enviaron (hasta 5)
                String imagenesStr = String.valueOf(params.getOrDefault("imagenes", "")).trim();
                String imagenPrincipal = String.valueOf(params.getOrDefault("imagenPrincipal", "")).trim();

                if (!imagenesStr.isEmpty() || !imagenPrincipal.isEmpty()) {
                    List<String> urls = new ArrayList<>();
                    if (!imagenPrincipal.isEmpty()) urls.add(imagenPrincipal);

                    if (!imagenesStr.isEmpty()) {
                        String[] parts = imagenesStr.split(",");
                        for (String p : parts) {
                            String clean = p.trim().replaceAll("[\"\\[\\]]", "");
                            if (!clean.isEmpty() && !urls.contains(clean) && urls.size() < 5) {
                                urls.add(clean);
                            }
                        }
                    }

                    if (!urls.isEmpty()) {
                        try (PreparedStatement psDelImg = con.prepareStatement("DELETE FROM TourImagen WHERE idTour = ?")) {
                            psDelImg.setInt(1, idTour);
                            psDelImg.executeUpdate();
                        }
                        for (int i = 0; i < urls.size(); i++) {
                            try (PreparedStatement psInsImg = con.prepareStatement("INSERT INTO TourImagen (idTour, url, esPrincipal, orden) VALUES (?, ?, ?, ?)")) {
                                psInsImg.setInt(1, idTour);
                                psInsImg.setString(2, urls.get(i));
                                psInsImg.setBoolean(3, i == 0);
                                psInsImg.setInt(4, i + 1);
                                psInsImg.executeUpdate();
                            }
                        }
                    }
                }

                sendJsonResponse(exchange, 200, "{\"status\":\"success\",\"message\":\"Servicio guardado exitosamente\",\"idServicio\":" + idTour + "}");

            } catch (Exception e) {
                e.printStackTrace();
                sendJsonResponse(exchange, 500, "{\"status\":\"error\",\"message\":\"Error al procesar el servicio: " + e.getMessage() + "\"}");
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

            if ("POST".equalsIgnoreCase(exchange.getRequestMethod())) {
                procesarPostReserva(exchange);
                return;
            }

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
                        params.getOrDefault("idAgencia", "0"));
            } catch (NumberFormatException e) {
                idAgencia = 0;
            }

            if (idAgencia <= 0) {
                sendJsonResponse(exchange, 400,
                        "{\"status\":\"error\",\"message\":\"ID de agencia inválido\"}");
                return;
            }

            String sql = """
            SELECT
                r.idReserva,
                r.codigoReserva,
                r.idUsuario,
                r.idAgencia,
                r.nombreTour,
                r.fechaRegistro,
                r.fechaInicio,
                r.fechaFin,
                r.estado,
                r.motivoCancelacion,
                r.total,
                u.nombreUsuario,
                COALESCE(NULLIF((SELECT COUNT(*) FROM Pasajero p WHERE p.idReserva = r.idReserva), 0), 2) AS cantidadPersonas,
                (
                    SELECT COALESCE(SUM(p.monto), 0)
                    FROM Pago p
                    WHERE p.idReserva = r.idReserva
                ) AS totalPagado,
                (
                    SELECT p.estado
                    FROM Pago p
                    WHERE p.idReserva = r.idReserva
                    ORDER BY p.fechaPago DESC, p.idPago DESC
                    LIMIT 1
                ) AS estadoPago
            FROM Reserva r
            LEFT JOIN Usuario u
                ON u.idUsuario = r.idUsuario
            WHERE r.idAgencia = ?
            ORDER BY r.fechaRegistro DESC
            """;

            StringBuilder json = new StringBuilder();
            json.append("{\"status\":\"success\",\"reservas\":[");

            try (Connection con = ConexionDB.getConnection()) {

                if (con == null) {
                    sendJsonResponse(exchange, 500,
                            "{\"status\":\"error\",\"message\":\"No se pudo conectar a la base de datos\"}");
                    return;
                }

                try (PreparedStatement ps = con.prepareStatement(sql)) {
                    ps.setInt(1, idAgencia);

                    try (ResultSet rs = ps.executeQuery()) {
                        boolean primero = true;

                        while (rs.next()) {
                            if (!primero) {
                                json.append(",");
                            }
                            primero = false;

                            String estadoPago = rs.getString("estadoPago");
                            java.math.BigDecimal total =
                                    rs.getBigDecimal("total");
                            java.math.BigDecimal totalPagado =
                                    rs.getBigDecimal("totalPagado");

                            json.append("{")
                                    .append("\"idReserva\":")
                                    .append(rs.getInt("idReserva")).append(",")

                                    .append("\"codigoReserva\":\"")
                                    .append(jsonEscape(rs.getString("codigoReserva")))
                                    .append("\",")

                                    .append("\"idUsuario\":")
                                    .append(rs.getInt("idUsuario")).append(",")

                                    .append("\"idAgencia\":")
                                    .append(rs.getInt("idAgencia")).append(",")

                                    .append("\"nombreCliente\":\"")
                                    .append(jsonEscape(rs.getString("nombreUsuario")))
                                    .append("\",")

                                    .append("\"nombreTour\":\"")
                                    .append(jsonEscape(rs.getString("nombreTour")))
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

                                    .append("\"cantidadPersonas\":")
                                    .append(rs.getInt("cantidadPersonas")).append(",")

                                    .append("\"estado\":\"")
                                    .append(jsonEscape(rs.getString("estado")))
                                    .append("\",")

                                    .append("\"motivoCancelacion\":\"")
                                    .append(jsonEscape(rs.getString("motivoCancelacion")))
                                    .append("\",")

                                    .append("\"total\":")
                                    .append(total == null ? "0.00" : total.toPlainString())
                                    .append(",")

                                    .append("\"totalPagado\":")
                                    .append(totalPagado == null
                                            ? "0.00" : totalPagado.toPlainString())
                                    .append(",")

                                    .append("\"estadoPago\":\"")
                                    .append(jsonEscape(estadoPago))
                                    .append("\"")
                                    .append("}");
                        }
                    }
                }

                json.append("]}");
                sendJsonResponse(exchange, 200, json.toString());

            } catch (Exception e) {
                e.printStackTrace();
                sendJsonResponse(exchange, 500,
                        "{\"status\":\"error\",\"message\":\"Error al consultar las reservas\"}");
            }
        }

        private void procesarPostReserva(HttpExchange exchange) throws IOException {
            String body = readRequestBody(exchange);
            Map<String, Object> params = parseJsonOrFormParams(body);
            String action = String.valueOf(params.getOrDefault("action", "")).trim().toLowerCase();
            int idReserva = 0;
            try {
                idReserva = Integer.parseInt(String.valueOf(params.getOrDefault("idReserva", "0")));
            } catch (Exception ignored) {}
            int idAgencia = 1;
            try {
                idAgencia = Integer.parseInt(String.valueOf(params.getOrDefault("idAgencia", "1")));
            } catch (Exception ignored) {}

            if (idReserva <= 0) {
                sendJsonResponse(exchange, 400, "{\"status\":\"error\",\"message\":\"ID de reserva inválido\"}");
                return;
            }

            try (Connection con = ConexionDB.getConnection()) {
                if ("confirmar".equals(action)) {
                    String sqlCheck = "SELECT estado FROM Reserva WHERE idReserva = ? AND idAgencia = ?";
                    try (PreparedStatement psC = con.prepareStatement(sqlCheck)) {
                        psC.setInt(1, idReserva);
                        psC.setInt(2, idAgencia);
                        try (ResultSet rs = psC.executeQuery()) {
                            if (!rs.next()) {
                                sendJsonResponse(exchange, 404, "{\"status\":\"error\",\"message\":\"Reserva no encontrada\"}");
                                return;
                            }
                            String estado = rs.getString("estado");
                            if (!"PENDIENTE".equalsIgnoreCase(estado)) {
                                sendJsonResponse(exchange, 400, "{\"status\":\"error\",\"message\":\"Solo se pueden confirmar reservas pendientes (estado actual: " + estado + ")\"}");
                                return;
                            }
                        }
                    }
                    try (PreparedStatement psUp = con.prepareStatement("UPDATE Reserva SET estado = 'CONFIRMADA' WHERE idReserva = ? AND idAgencia = ?")) {
                        psUp.setInt(1, idReserva);
                        psUp.setInt(2, idAgencia);
                        psUp.executeUpdate();
                    }
                    try (PreparedStatement psP = con.prepareStatement("UPDATE Pago SET estado = 'COMPLETADO' WHERE idReserva = ?")) {
                        psP.setInt(1, idReserva);
                        psP.executeUpdate();
                    }
                    sendJsonResponse(exchange, 200, "{\"status\":\"success\",\"message\":\"Reserva confirmada exitosamente\"}");
                    return;
                } else if ("cancelar".equals(action)) {
                    try (PreparedStatement psUp = con.prepareStatement("UPDATE Reserva SET estado = 'CANCELADA' WHERE idReserva = ? AND idAgencia = ?")) {
                        psUp.setInt(1, idReserva);
                        psUp.setInt(2, idAgencia);
                        psUp.executeUpdate();
                    }
                    try (PreparedStatement psP = con.prepareStatement("UPDATE Pago SET estado = 'RECHAZADO' WHERE idReserva = ?")) {
                        psP.setInt(1, idReserva);
                        psP.executeUpdate();
                    }
                    sendJsonResponse(exchange, 200, "{\"status\":\"success\",\"message\":\"Reserva cancelada exitosamente\"}");
                    return;
                }
                sendJsonResponse(exchange, 400, "{\"status\":\"error\",\"message\":\"Acción no válida\"}");
            } catch (Exception e) {
                e.printStackTrace();
                sendJsonResponse(exchange, 500, "{\"status\":\"error\",\"message\":\"Error al procesar reserva: " + jsonEscape(e.getMessage()) + "\"}");
            }
        }
    }

    static class ToursAdminHandler implements HttpHandler {
        @Override
        public void handle(HttpExchange exchange) throws IOException {
            enableCORS(exchange);
            if ("OPTIONS".equalsIgnoreCase(exchange.getRequestMethod())) {
                exchange.sendResponseHeaders(200, -1);
                return;
            }
            StringBuilder json = new StringBuilder("{\"status\":\"success\",\"data\":[");
            boolean primero = true;
            String sql = "SELECT t.idTour AS id, t.nombre, a.razonSocial AS agencia, d.nombre AS destino, t.duracion, t.precioAdulto AS precio, t.calificacionPromedio AS calificacion, t.estado, COALESCE(ti.url, '../../img/lima.jpg') AS imagen FROM Tour t JOIN Agencia a ON t.idAgencia = a.idAgencia JOIN Destino d ON t.idDestino = d.idDestino LEFT JOIN TourImagen ti ON t.idTour = ti.idTour AND ti.esPrincipal = 1 ORDER BY t.idTour DESC";
            try (Connection con = ConexionDB.getConnection();
                 PreparedStatement ps = con.prepareStatement(sql);
                 ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    if (!primero) json.append(",");
                    primero = false;
                    json.append("{")
                        .append("\"id\":").append(rs.getInt("id")).append(",")
                        .append("\"nombre\":\"").append(jsonEscape(rs.getString("nombre"))).append("\",")
                        .append("\"agencia\":\"").append(jsonEscape(rs.getString("agencia"))).append("\",")
                        .append("\"destino\":\"").append(jsonEscape(rs.getString("destino"))).append("\",")
                        .append("\"duracion\":\"").append(jsonEscape(rs.getString("duracion"))).append("\",")
                        .append("\"precio\":").append(rs.getBigDecimal("precio")).append(",")
                        .append("\"calificacion\":").append(rs.getBigDecimal("calificacion")).append(",")
                        .append("\"estado\":\"").append(jsonEscape(rs.getString("estado"))).append("\",")
                        .append("\"imagen\":\"").append(jsonEscape(rs.getString("imagen"))).append("\"")
                        .append("}");
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
            json.append("]}");
            sendJsonResponse(exchange, 200, json.toString());
        }
    }

    static class DestinosAdminHandler implements HttpHandler {
        @Override
        public void handle(HttpExchange exchange) throws IOException {
            enableCORS(exchange);
            if ("OPTIONS".equalsIgnoreCase(exchange.getRequestMethod())) {
                exchange.sendResponseHeaders(200, -1);
                return;
            }

            StringBuilder json = new StringBuilder("{\"status\":\"success\",\"data\":[");
            boolean primero = true;

            String sql = "SELECT d.idDestino AS id, d.nombre, COALESCE(d.descripcion, '') AS descripcion, COALESCE(d.estado, 'ACTIVO') AS estado, " +
                         "(SELECT COUNT(*) FROM Tour t WHERE t.idDestino = d.idDestino AND UPPER(t.estado) = 'ACTIVO') AS toursActivos " +
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
                        .append("\"nombre\":\"").append(jsonEscape(nombreDest)).append("\",")
                        .append("\"descripcion\":\"").append(jsonEscape(rs.getString("descripcion"))).append("\",")
                        .append("\"toursActivos\":").append(rs.getInt("toursActivos")).append(",")
                        .append("\"estado\":\"").append(jsonEscape(est)).append("\",")
                        .append("\"imagen\":\"").append(img).append("\"")
                        .append("}");
                }
            } catch (Exception e) {
                e.printStackTrace();
            }

            json.append("]}");
            sendJsonResponse(exchange, 200, json.toString());
        }
    }

    static class DestinosToursAdminHandler implements HttpHandler {
        @Override
        public void handle(HttpExchange exchange) throws IOException {
            enableCORS(exchange);
            if ("OPTIONS".equalsIgnoreCase(exchange.getRequestMethod())) {
                exchange.sendResponseHeaders(200, -1);
                return;
            }

            Map<String, String> queryParams = parseQueryParams(exchange.getRequestURI().getQuery());
            int idDestino = 0;
            try { idDestino = Integer.parseInt(queryParams.getOrDefault("idDestino", queryParams.getOrDefault("id", "0"))); } catch (Exception ignored) {}
            String nombreDestino = queryParams.getOrDefault("destino", queryParams.getOrDefault("nombre", "")).trim();

            StringBuilder json = new StringBuilder("{\"status\":\"success\",\"data\":[");
            boolean primero = true;

            String sql = "SELECT t.idTour AS id, t.nombre, a.razonSocial AS agencia, t.duracion, t.precioAdulto AS precio, t.calificacionPromedio AS calificacion, COALESCE(t.estado, 'ACTIVO') AS estado " +
                         "FROM Tour t " +
                         "INNER JOIN Agencia a ON t.idAgencia = a.idAgencia " +
                         "INNER JOIN Destino d ON t.idDestino = d.idDestino " +
                         "WHERE (d.idDestino = ? OR (LOWER(d.nombre) LIKE LOWER(?) AND ? != '')) " +
                         "ORDER BY t.idTour DESC";

            try (Connection con = ConexionDB.getConnection();
                 PreparedStatement ps = con.prepareStatement(sql)) {
                ps.setInt(1, idDestino);
                ps.setString(2, nombreDestino.isEmpty() ? "%" : "%" + nombreDestino + "%");
                ps.setString(3, nombreDestino);

                try (ResultSet rs = ps.executeQuery()) {
                    while (rs.next()) {
                        if (!primero) json.append(",");
                        primero = false;

                        String est = rs.getString("estado");
                        if (est != null && est.equalsIgnoreCase("ACTIVO")) est = "Activo";

                        json.append("{")
                            .append("\"id\":").append(rs.getInt("id")).append(",")
                            .append("\"nombre\":\"").append(jsonEscape(rs.getString("nombre"))).append("\",")
                            .append("\"agencia\":\"").append(jsonEscape(rs.getString("agencia"))).append("\",")
                            .append("\"duracion\":\"").append(jsonEscape(rs.getString("duracion"))).append("\",")
                            .append("\"precio\":").append(rs.getBigDecimal("precio")).append(",")
                            .append("\"calificacion\":").append(rs.getBigDecimal("calificacion")).append(",")
                            .append("\"estado\":\"").append(jsonEscape(est)).append("\"")
                            .append("}");
                    }
                }
            } catch (Exception e) {
                e.printStackTrace();
            }

            json.append("]}");
            sendJsonResponse(exchange, 200, json.toString());
        }
    }

    static class UsuariosAdminHandler implements HttpHandler {
        @Override
        public void handle(HttpExchange exchange) throws IOException {
            enableCORS(exchange);
            if ("OPTIONS".equalsIgnoreCase(exchange.getRequestMethod())) {
                exchange.sendResponseHeaders(200, -1);
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
                        .append("\"nombre\":\"").append(jsonEscape(rs.getString("nombre"))).append("\",")
                        .append("\"email\":\"").append(jsonEscape(rs.getString("email"))).append("\",")
                        .append("\"tipo\":\"").append(jsonEscape(rs.getString("tipo"))).append("\",")
                        .append("\"estado\":\"").append(jsonEscape(rs.getString("estado"))).append("\",")
                        .append("\"fechaRegistro\":\"").append(rs.getString("fechaRegistro")).append("\"")
                        .append("}");
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
            json.append("]}");
            sendJsonResponse(exchange, 200, json.toString());
        }
    }

    static class ReservasAdminHandler implements HttpHandler {
        @Override
        public void handle(HttpExchange exchange) throws IOException {
            enableCORS(exchange);
            if ("OPTIONS".equalsIgnoreCase(exchange.getRequestMethod())) {
                exchange.sendResponseHeaders(200, -1);
                return;
            }
            StringBuilder json = new StringBuilder("{\"status\":\"success\",\"data\":[");
            boolean primero = true;
            String sql = "SELECT r.idReserva AS id, r.codigoReserva AS codigo, CONCAT(p.nombre, ' ', p.apellidoPaterno) AS turista, p.email, COALESCE(r.nombreTour, 'Tour Turístico') AS tour, a.razonSocial AS agencia, r.fechaInicio AS fechaTour, (COALESCE(dr.cantAdultos, 1) + COALESCE(dr.cantNinos, 0)) AS cupos, r.total, r.estado, DATE(r.fechaRegistro) AS fechaReserva FROM Reserva r JOIN Usuario u ON r.idUsuario = u.idUsuario JOIN Persona p ON u.idPersona = p.idPersona JOIN Agencia a ON r.idAgencia = a.idAgencia LEFT JOIN DetalleReserva dr ON r.idReserva = dr.idReserva ORDER BY r.idReserva DESC";
            try (Connection con = ConexionDB.getConnection();
                 PreparedStatement ps = con.prepareStatement(sql);
                 ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    if (!primero) json.append(",");
                    primero = false;
                    json.append("{")
                        .append("\"id\":").append(rs.getInt("id")).append(",")
                        .append("\"codigo\":\"").append(jsonEscape(rs.getString("codigo"))).append("\",")
                        .append("\"turista\":\"").append(jsonEscape(rs.getString("turista"))).append("\",")
                        .append("\"email\":\"").append(jsonEscape(rs.getString("email"))).append("\",")
                        .append("\"tour\":\"").append(jsonEscape(rs.getString("tour"))).append("\",")
                        .append("\"agencia\":\"").append(jsonEscape(rs.getString("agencia"))).append("\",")
                        .append("\"fechaTour\":\"").append(rs.getString("fechaTour")).append("\",")
                        .append("\"cupos\":").append(rs.getInt("cupos")).append(",")
                        .append("\"total\":").append(rs.getBigDecimal("total")).append(",")
                        .append("\"estado\":\"").append(jsonEscape(rs.getString("estado"))).append("\",")
                        .append("\"fechaReserva\":\"").append(rs.getString("fechaReserva")).append("\"")
                        .append("}");
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
            json.append("]}");
            sendJsonResponse(exchange, 200, json.toString());
        }
    }

    static class ComisionesAdminHandler implements HttpHandler {
        @Override
        public void handle(HttpExchange exchange) throws IOException {
            enableCORS(exchange);
            if ("OPTIONS".equalsIgnoreCase(exchange.getRequestMethod())) {
                exchange.sendResponseHeaders(200, -1);
                return;
            }
            StringBuilder json = new StringBuilder("{\"status\":\"success\",\"comisiones\":[");
            boolean primero = true;
            String sql = """
                SELECT 
                    a.idAgencia AS id, 
                    COALESCE(NULLIF(a.razonSocial, ''), a.nombreComercial, 'Agencia Travelink') AS agencia, 
                    COALESCE(a.ruc, '20556677889') AS ruc,
                    'Octubre 2026' AS periodo,
                    COALESCE(SUM(r.total), 0) AS ventasTotales,
                    15.00 AS comisionPct,
                    COALESCE(SUM(r.total * 0.15), 0) AS montoComision,
                    'Pendiente' AS estado
                FROM Agencia a 
                LEFT JOIN Reserva r ON a.idAgencia = r.idAgencia AND UPPER(r.estado) IN ('CONFIRMADA', 'ACTIVA', 'PENDIENTE', 'FINALIZADA') 
                WHERE a.estado IN ('ACTIVO', 'Aceptado') 
                GROUP BY a.idAgencia, a.razonSocial, a.nombreComercial, a.ruc 
                ORDER BY a.idAgencia ASC
            """;
            try (Connection con = ConexionDB.getConnection();
                 PreparedStatement ps = con.prepareStatement(sql);
                 ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    if (!primero) json.append(",");
                    primero = false;
                    json.append("{")
                        .append("\"id\":").append(rs.getInt("id")).append(",")
                        .append("\"agencia\":\"").append(jsonEscape(rs.getString("agencia"))).append("\",")
                        .append("\"ruc\":\"").append(jsonEscape(rs.getString("ruc"))).append("\",")
                        .append("\"periodo\":\"").append(jsonEscape(rs.getString("periodo"))).append("\",")
                        .append("\"ventasTotales\":").append(rs.getBigDecimal("ventasTotales")).append(",")
                        .append("\"comisionPct\":15.00,")
                        .append("\"montoComision\":").append(rs.getBigDecimal("montoComision")).append(",")
                        .append("\"estado\":\"").append(jsonEscape(rs.getString("estado"))).append("\"")
                        .append("}");
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
            json.append("]}");
            sendJsonResponse(exchange, 200, json.toString());
        }
    }

    static class LiquidarComisionHandler implements HttpHandler {
        @Override
        public void handle(HttpExchange exchange) throws IOException {
            enableCORS(exchange);
            if ("OPTIONS".equalsIgnoreCase(exchange.getRequestMethod())) {
                exchange.sendResponseHeaders(200, -1);
                return;
            }
            Map<String, String> params = parseQueryParams(exchange.getRequestURI().getQuery());
            int id = 0;
            try { id = Integer.parseInt(params.getOrDefault("id", "0")); } catch (Exception ignored) {}

            if (id > 0) {
                try (Connection con = ConexionDB.getConnection();
                     PreparedStatement ps = con.prepareStatement("UPDATE Comision SET estado = 'Liquidado' WHERE idAgencia = ? OR idComision = ?")) {
                    ps.setInt(1, id);
                    ps.setInt(2, id);
                    ps.executeUpdate();
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
            sendJsonResponse(exchange, 200, "{\"status\":\"success\",\"message\":\"Comisión liquidada exitosamente.\"}");
        }
    }

    static class CalidadAdminHandler implements HttpHandler {
        @Override
        public void handle(HttpExchange exchange) throws IOException {
            enableCORS(exchange);
            if ("OPTIONS".equalsIgnoreCase(exchange.getRequestMethod())) {
                exchange.sendResponseHeaders(200, -1);
                return;
            }

            StringBuilder json = new StringBuilder("{\"status\":\"success\",\"data\":{");
            
            // 1. REVIEWS
            json.append("\"reviews\":[");
            boolean primeroRev = true;
            String sqlRev = """
                SELECT 
                    c.idCalificacion AS id,
                    CONCAT(p.nombre, ' ', p.apellidoPaterno) AS usuario,
                    p.email,
                    COALESCE(NULLIF(a.nombreComercial, ''), a.razonSocial, 'Agencia Travelink') AS agencia,
                    COALESCE(t.nombre, r.nombreTour, 'Tour Turístico') AS tour,
                    c.estrellas,
                    COALESCE(c.comentario, 'Sin comentario') AS comentario,
                    DATE_FORMAT(c.fechaCalificacion, '%d %b. %Y - %H:%i') AS fecha,
                    IF(c.eliminadoPorAdmin = 1, 'Eliminado', 'Pendiente') AS estado
                FROM Calificacion c
                JOIN Usuario u ON c.idUsuario = u.idUsuario
                JOIN Persona p ON u.idPersona = p.idPersona
                JOIN Agencia a ON c.idAgencia = a.idAgencia
                LEFT JOIN Reserva r ON c.idReserva = r.idReserva
                LEFT JOIN Tour t ON r.idTour = t.idTour
                ORDER BY c.idCalificacion DESC
            """;

            try (Connection con = ConexionDB.getConnection();
                 PreparedStatement ps = con.prepareStatement(sqlRev);
                 ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    if (!primeroRev) json.append(",");
                    primeroRev = false;
                    json.append("{")
                        .append("\"id\":").append(rs.getInt("id")).append(",")
                        .append("\"usuario\":\"").append(jsonEscape(rs.getString("usuario"))).append("\",")
                        .append("\"email\":\"").append(jsonEscape(rs.getString("email"))).append("\",")
                        .append("\"agencia\":\"").append(jsonEscape(rs.getString("agencia"))).append("\",")
                        .append("\"tour\":\"").append(jsonEscape(rs.getString("tour"))).append("\",")
                        .append("\"estrellas\":").append(rs.getDouble("estrellas")).append(",")
                        .append("\"comentario\":\"").append(jsonEscape(rs.getString("comentario"))).append("\",")
                        .append("\"fecha\":\"").append(jsonEscape(rs.getString("fecha"))).append("\",")
                        .append("\"estado\":\"").append(jsonEscape(rs.getString("estado"))).append("\"")
                        .append("}");
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
            json.append("],");

            // 2. TOURS EN REVISION
            json.append("\"toursEnRevision\":[");
            boolean primeroTour = true;
            String sqlTours = """
                SELECT 
                    t.idTour AS id,
                    t.nombre AS tour,
                    t.duracion,
                    COALESCE(NULLIF(a.nombreComercial, ''), a.razonSocial) AS agencia,
                    a.ruc,
                    d.nombre AS destino,
                    COALESCE(t.calificacionPromedio, 4.5) AS califTour,
                    COALESCE(a.promedioCalificacion, 4.8) AS califAgencia,
                    t.precioAdulto AS precio,
                    t.estado,
                    'Supervisión rutinaria de calidad e itinerario' AS motivo,
                    COALESCE(ti.url, '../../img/lima.jpg') AS imagen
                FROM Tour t
                JOIN Agencia a ON t.idAgencia = a.idAgencia
                JOIN Destino d ON t.idDestino = d.idDestino
                LEFT JOIN TourImagen ti ON t.idTour = ti.idTour AND ti.esPrincipal = 1
                ORDER BY t.idTour DESC
            """;

            try (Connection con = ConexionDB.getConnection();
                 PreparedStatement ps = con.prepareStatement(sqlTours);
                 ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    if (!primeroTour) json.append(",");
                    primeroTour = false;
                    json.append("{")
                        .append("\"id\":").append(rs.getInt("id")).append(",")
                        .append("\"tour\":\"").append(jsonEscape(rs.getString("tour"))).append("\",")
                        .append("\"duracion\":\"").append(jsonEscape(rs.getString("duracion"))).append("\",")
                        .append("\"agencia\":\"").append(jsonEscape(rs.getString("agencia"))).append("\",")
                        .append("\"ruc\":\"").append(jsonEscape(rs.getString("ruc"))).append("\",")
                        .append("\"destino\":\"").append(jsonEscape(rs.getString("destino"))).append("\",")
                        .append("\"califTour\":").append(rs.getDouble("califTour")).append(",")
                        .append("\"califAgencia\":").append(rs.getDouble("califAgencia")).append(",")
                        .append("\"precio\":").append(rs.getBigDecimal("precio")).append(",")
                        .append("\"estado\":\"").append(jsonEscape(rs.getString("estado"))).append("\",")
                        .append("\"motivo\":\"").append(jsonEscape(rs.getString("motivo"))).append("\",")
                        .append("\"imagen\":\"").append(jsonEscape(rs.getString("imagen"))).append("\"")
                        .append("}");
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
            json.append("]}}");

            sendJsonResponse(exchange, 200, json.toString());
        }
    }

    static class EliminarResenaHandler implements HttpHandler {
        @Override
        public void handle(HttpExchange exchange) throws IOException {
            enableCORS(exchange);
            if ("OPTIONS".equalsIgnoreCase(exchange.getRequestMethod())) {
                exchange.sendResponseHeaders(200, -1);
                return;
            }
            Map<String, String> params = parseQueryParams(exchange.getRequestURI().getQuery());
            int id = 0;
            try { id = Integer.parseInt(params.getOrDefault("id", "0")); } catch (Exception ignored) {}

            if (id > 0) {
                try (Connection con = ConexionDB.getConnection();
                     PreparedStatement ps = con.prepareStatement("UPDATE Calificacion SET eliminadoPorAdmin = 1 WHERE idCalificacion = ?")) {
                    ps.setInt(1, id);
                    ps.executeUpdate();
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
            sendJsonResponse(exchange, 200, "{\"status\":\"success\",\"message\":\"Reseña eliminada correctamente.\"}");
        }
    }

    static class DashboardAdminHandler implements HttpHandler {
        @Override
        public void handle(HttpExchange exchange) throws IOException {
            enableCORS(exchange);
            if ("OPTIONS".equalsIgnoreCase(exchange.getRequestMethod())) {
                exchange.sendResponseHeaders(200, -1);
                return;
            }
            double ventasTotales = 0.0;
            int reservasRealizadas = 0;
            int agenciasActivas = 0;
            double comisionGenerada = 0.0;
            StringBuilder detalleVentasJson = new StringBuilder();

            try (Connection con = ConexionDB.getConnection()) {
                if (con != null) {
                    try (Statement stmt = con.createStatement();
                         ResultSet rs = stmt.executeQuery("SELECT COALESCE(SUM(total), 0) AS total, COUNT(*) AS cant FROM Reserva WHERE estado = 'CONFIRMADA'")) {
                        if (rs.next()) {
                            ventasTotales = rs.getDouble("total");
                            reservasRealizadas = rs.getInt("cant");
                            comisionGenerada = ventasTotales * 0.15;
                        }
                    }
                    try (Statement stmt = con.createStatement();
                         ResultSet rs = stmt.executeQuery("SELECT COUNT(*) FROM Agencia WHERE estado = 'ACTIVO'")) {
                        if (rs.next()) {
                            agenciasActivas = rs.getInt(1);
                        }
                    }

                    // Detalle de ventas completo
                    String sqlDetalle = """
                        SELECT r.idReserva, r.codigoReserva, DATE(r.fechaRegistro) AS fecha,
                               COALESCE(CONCAT(p.nombre, ' ', p.apellidoPaterno), u.nombreUsuario, 'Turista') AS cliente,
                               COALESCE(d.nombre, 'Cusco') AS destino,
                               COALESCE(r.nombreTour, 'Tour Turístico') AS tour,
                               COALESCE(a.razonSocial, a.nombreComercial, 'Agencia Travelink') AS agencia,
                               r.total, (r.total * 0.15) AS comision, r.estado
                        FROM Reserva r
                        JOIN Usuario u ON r.idUsuario = u.idUsuario
                        JOIN Persona p ON u.idPersona = p.idPersona
                        LEFT JOIN Agencia a ON r.idAgencia = a.idAgencia
                        LEFT JOIN DetalleReserva dr ON r.idReserva = dr.idReserva
                        LEFT JOIN TourFecha tf ON dr.idTourFecha = tf.idTourFecha
                        LEFT JOIN Tour t ON tf.idTour = t.idTour
                        LEFT JOIN Destino d ON t.idDestino = d.idDestino
                        ORDER BY r.idReserva DESC
                        """;
                    try (Statement stmt = con.createStatement();
                         ResultSet rsV = stmt.executeQuery(sqlDetalle)) {
                        boolean primero = true;
                        while (rsV.next()) {
                            if (!primero) detalleVentasJson.append(",");
                            primero = false;
                            detalleVentasJson.append("{")
                                .append("\"id\":").append(rsV.getInt("idReserva")).append(",")
                                .append("\"fecha\":\"").append(rsV.getString("fecha")).append("\",")
                                .append("\"nroReserva\":\"").append(jsonEscape(rsV.getString("codigoReserva"))).append("\",")
                                .append("\"cliente\":\"").append(jsonEscape(rsV.getString("cliente"))).append("\",")
                                .append("\"destino\":\"").append(jsonEscape(rsV.getString("destino"))).append("\",")
                                .append("\"tour\":\"").append(jsonEscape(rsV.getString("tour"))).append("\",")
                                .append("\"agencia\":\"").append(jsonEscape(rsV.getString("agencia"))).append("\",")
                                .append("\"monto\":").append(rsV.getDouble("total")).append(",")
                                .append("\"comision\":").append(rsV.getDouble("comision")).append(",")
                                .append("\"estado\":\"").append(jsonEscape(rsV.getString("estado"))).append("\"")
                                .append("}");
                        }
                    }
                }
            } catch (Exception e) {
                e.printStackTrace();
            }

            StringBuilder json = new StringBuilder();
            json.append("{")
                .append("\"status\":\"success\",")
                .append("\"data\":{")
                .append("\"ventasTotales\":").append(ventasTotales).append(",")
                .append("\"reservasRealizadas\":").append(reservasRealizadas).append(",")
                .append("\"agenciasActivas\":").append(agenciasActivas).append(",")
                .append("\"comisionGenerada\":").append(comisionGenerada).append(",")
                .append("\"chartEvolucionLabels\":[\"Ene\",\"Feb\",\"Mar\",\"Abr\",\"May\",\"Jun\",\"Jul\",\"Ago\",\"Set\",\"Oct\"],")
                .append("\"chartEvolucionValues\":[1200,1800,2400,3100,4200,5100,6300,7800,8900,").append(ventasTotales).append("],")
                .append("\"destinosLabels\":[\"Cusco\",\"Lima\",\"Ilo\",\"Tacna\",\"Moquegua\",\"Pasco\"],")
                .append("\"destinosPercentages\":[35,20,15,12,10,8],")
                .append("\"detalleVentas\":[").append(detalleVentasJson.toString()).append("]")
                .append("}}");

            sendJsonResponse(exchange, 200, json.toString());
        }
    }

    // 7. STATIC FILES HANDLER (Serves HTML, CSS, JS, Images)
    static class StaticFileHandler implements HttpHandler {
        @Override
        public void handle(HttpExchange exchange) throws IOException {
            String path = exchange.getRequestURI().getPath();
            if (path.equals("/") || path.equals("/index.html")) {
                path = "/html/index.html";
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

                    Map<String, String> qParams = parseQueryParams(exchange.getRequestURI().getQuery());
                    int idAgencia = 1;
                    try {
                        idAgencia = Integer.parseInt(qParams.getOrDefault("idAgencia", "1"));
                    } catch (Exception ignored) {}

                    StringBuilder json = new StringBuilder("{\"status\":\"success\",\"disponibilidades\":[");
                    boolean primero = true;

                    try (Connection con = ConexionDB.getConnection()) {
                        String sql = """
                        SELECT tf.idTourFecha AS idDisponibilidad,
                               tf.fecha,
                               '08:00:00' AS horaInicio,
                               '18:00:00' AS horaFin,
                               tf.cupoTotal,
                               tf.cupoDisponible,
                               tf.estado,
                               tf.idTour AS idServicio,
                               t.nombre AS servicio
                        FROM TourFecha tf
                        INNER JOIN Tour t ON tf.idTour = t.idTour
                        WHERE t.idAgencia = ?
                        ORDER BY tf.fecha ASC
                        """;

                        try (PreparedStatement ps = con.prepareStatement(sql)) {
                            ps.setInt(1, idAgencia);
                            try (ResultSet rs = ps.executeQuery()) {
                                while (rs.next()) {
                                    if (!primero) json.append(",");
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
                                    primero = false;
                                }
                            }
                        }

                        if (primero) {
                            // Fallback using Tour table with 30 default cupos
                            String sqlF = "SELECT idTour AS idServicio, nombre AS servicio FROM Tour WHERE idAgencia = ? AND estado = 'ACTIVO'";
                            try (PreparedStatement psF = con.prepareStatement(sqlF)) {
                                psF.setInt(1, idAgencia);
                                try (ResultSet rsF = psF.executeQuery()) {
                                    int count = 1;
                                    while (rsF.next()) {
                                        if (!primero) json.append(",");
                                        json.append("{")
                                                .append("\"idDisponibilidad\":").append(count++).append(",")
                                                .append("\"fecha\":\"2026-10-15\",")
                                                .append("\"horaInicio\":\"08:00:00\",")
                                                .append("\"horaFin\":\"18:00:00\",")
                                                .append("\"cupoTotal\":30,")
                                                .append("\"cupoDisponible\":28,")
                                                .append("\"estado\":\"DISPONIBLE\",")
                                                .append("\"idServicio\":").append(rsF.getInt("idServicio")).append(",")
                                                .append("\"servicio\":\"").append(jsonEscape(rsF.getString("servicio"))).append("\"")
                                                .append("}");
                                        primero = false;
                                    }
                                }
                            }
                        }
                    } catch (Exception e) {
                        e.printStackTrace();
                    }

                    json.append("]}");
                    sendJsonResponse(exchange, 200, json.toString());
                    return;
                }


                if ("POST".equalsIgnoreCase(exchange.getRequestMethod())) {

                    System.out.println("========== POST DISPONIBILIDAD ==========");

                    String body = new String(
                            exchange.getRequestBody().readAllBytes(),
                            StandardCharsets.UTF_8
                    );

                    System.out.println("BODY: " + body);

                    String action = obtenerParametro(body, "action").toLowerCase();
                    if ("cerrar_ventas".equals(action) || "cerrar".equals(action)) {
                        int idDisp = 0;
                        try { idDisp = Integer.parseInt(obtenerParametro(body, "idDisponibilidad")); } catch (Exception ignored) {}
                        if (idDisp > 0) {
                            try (Connection con = ConexionDB.getConnection();
                                 PreparedStatement ps = con.prepareStatement("UPDATE Disponibilidad SET estado = 'CERRADO', cupoDisponible = 0 WHERE idDisponibilidad = ?")) {
                                ps.setInt(1, idDisp);
                                ps.executeUpdate();
                                enviar(exchange, 200, "{\"mensaje\":\"Ventas cerradas exitosamente.\"}");
                                return;
                            }
                        }
                    } else if ("reabrir".equals(action)) {
                        int idDisp = 0;
                        try { idDisp = Integer.parseInt(obtenerParametro(body, "idDisponibilidad")); } catch (Exception ignored) {}
                        if (idDisp > 0) {
                            try (Connection con = ConexionDB.getConnection();
                                 PreparedStatement ps = con.prepareStatement("UPDATE Disponibilidad SET estado = 'DISPONIBLE', cupoDisponible = cupoTotal WHERE idDisponibilidad = ?")) {
                                ps.setInt(1, idDisp);
                                ps.executeUpdate();
                                enviar(exchange, 200, "{\"mensaje\":\"Ventas reabiertas exitosamente.\"}");
                                return;
                            }
                        }
                    } else if ("eliminar".equals(action)) {
                        int idDisp = 0;
                        try { idDisp = Integer.parseInt(obtenerParametro(body, "idDisponibilidad")); } catch (Exception ignored) {}
                        if (idDisp > 0) {
                            try (Connection con = ConexionDB.getConnection();
                                 PreparedStatement ps = con.prepareStatement("DELETE FROM Disponibilidad WHERE idDisponibilidad = ?")) {
                                ps.setInt(1, idDisp);
                                ps.executeUpdate();
                                enviar(exchange, 200, "{\"mensaje\":\"Disponibilidad eliminada exitosamente.\"}");
                                return;
                            }
                        }
                    }

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

}
