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

import com.travelink.controlador.admin.*;
import com.travelink.controlador.agencia.*;
import com.travelink.controlador.turista.*;


import java.io.*;
import java.net.InetSocketAddress;
import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;

public class JavaApiServer {
    public static final int PORT = 8080;
    public static final UsuarioControlador usuarioControlador = new UsuarioControlador();
    public static final ReservaRepositorio reservaRepositorio = new ReservaRepositorio();
    public static final CalificacionControlador calificacionControlador = new CalificacionControlador();
    public static final String FRONTEND_DIR = "backend/springboot/src/main/resources/static";

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

    public static void enableCORS(HttpExchange exchange) {
        exchange.getResponseHeaders().set("Access-Control-Allow-Origin", "*");
        exchange.getResponseHeaders().set("Access-Control-Allow-Methods", "GET, POST, OPTIONS");
        exchange.getResponseHeaders().set("Access-Control-Allow-Headers", "Content-Type, Authorization");
    }

    public static void sendJsonResponse(HttpExchange exchange, int statusCode, String jsonResponse) throws IOException {
        enableCORS(exchange);
        byte[] bytes = jsonResponse.getBytes(StandardCharsets.UTF_8);
        exchange.getResponseHeaders().set("Content-Type", "application/json; charset=UTF-8");
        exchange.sendResponseHeaders(statusCode, bytes.length);
        OutputStream os = exchange.getResponseBody();
        os.write(bytes);
        os.close();
    }

    public static String readRequestBody(HttpExchange exchange) throws IOException {
        InputStream is = exchange.getRequestBody();
        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        byte[] buffer = new byte[1024];
        int len;
        while ((len = is.read(buffer)) != -1) {
            bos.write(buffer, 0, len);
        }
        return bos.toString(StandardCharsets.UTF_8);
    }

    public static Map<String, String> parseQueryParams(String query) {
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

    public static Map<String, Object> parseJsonOrFormParams(String body) {
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

    public static String validarDuplicadosAgencia(Connection con, String ruc, String nroDocumento, String telefonoEmpresa, String telefonoResponsable, String correo, String nombreUsuario) throws SQLException {
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

    public static String validarDuplicadosTurista(Connection con, String nroDocumento, String telefono, String correo, String nombreUsuario) throws SQLException {
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


    // 2. REGISTRO HANDLER - TURISTA

    // LOGIN - AGENCIA


    // 3. REGISTRO HANDLER - AGENCIA






    // 3. GUARDAR RESERVA HANDLER


    // 4. OBTENER RESERVAS HANDLER


    // 5. CANCELAR RESERVA HANDLER


    // 6. CALIFICAR HANDLER



    public static String jsonEscape(String value) {

        if (value == null) {
            return "";
        }

        return value
                .replace("\\", "\\\\")
                .replace("\"", "\\\"")
                .replace("\r", "\\r")
                .replace("\n", "\\n");
    }





























    // 7. STATIC FILES HANDLER (Serves HTML, CSS, JS, Images)





    public static String obtenerJson(String json, String clave) {
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

    public static String obtenerParametroFormulario(String body, String parametro) {

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

    public static String obtenerParametro(String body, String parametro) {

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
