package com.travelink.server;

import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import com.sun.net.httpserver.HttpServer;
import com.travelink.controlador.CalificacionControlador;
import com.travelink.controlador.UsuarioControlador;
import com.travelink.entidades.Usuario;
import com.travelink.repositorio.ReservaRepositorio;

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
    private static final String FRONTEND_DIR = "d:/ProyectosU/PresentaciónTravelink/frontend";

    public static void main(String[] args) throws IOException {
        HttpServer server = HttpServer.create(new InetSocketAddress(PORT), 0);

        // API Endpoints
        server.createContext("/api/login", new LoginHandler());
        server.createContext("/api/registro", new RegistroHandler());
        server.createContext("/api/guardar_reserva", new GuardarReservaHandler());
        server.createContext("/api/obtener_reservas", new ObtenerReservasHandler());
        server.createContext("/api/cancelar_reserva", new CancelarReservaHandler());
        server.createContext("/api/calificar", new CalificarHandler());

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

    // 2. REGISTRO HANDLER
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
}
