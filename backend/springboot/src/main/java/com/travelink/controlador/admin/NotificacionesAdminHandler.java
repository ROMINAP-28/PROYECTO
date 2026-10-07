
package com.travelink.controlador.admin;

import com.travelink.server.JavaApiServer;

import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import com.travelink.config.ConexionDB;

import java.io.IOException;
import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class NotificacionesAdminHandler implements HttpHandler {

    @Override
    public void handle(HttpExchange exchange) throws IOException {
        exchange.getResponseHeaders().set(
                "Access-Control-Allow-Origin", "*");
        exchange.getResponseHeaders().set(
                "Access-Control-Allow-Methods", "GET, POST, OPTIONS");
        exchange.getResponseHeaders().set(
                "Access-Control-Allow-Headers", "Content-Type");

        if ("OPTIONS".equalsIgnoreCase(exchange.getRequestMethod())) {
            exchange.sendResponseHeaders(204, -1);
            return;
        }

        String metodo = exchange.getRequestMethod();
        String ruta = exchange.getRequestURI().getPath();

        try {
            if ("GET".equalsIgnoreCase(metodo)) {
                listarNotificaciones(exchange);
            } else if ("POST".equalsIgnoreCase(metodo)
                    && ruta.endsWith("/marcar-leida")) {
                marcarLeida(exchange);
            } else {
                responder(exchange, 405,
                        "{\"status\":\"error\",\"message\":\"Método no permitido\"}");
            }
        } catch (Exception e) {
            e.printStackTrace();
            responder(exchange, 500,
                    "{\"status\":\"error\",\"message\":\"Error al procesar notificaciones\"}");
        }
    }

    private void listarNotificaciones(HttpExchange exchange)
            throws IOException, SQLException {

        String query = exchange.getRequestURI().getRawQuery();
        int idUsuario = obtenerParametro(query, "idUsuario", 0);
        int limite = obtenerParametro(query, "limite", 15);

        if (idUsuario <= 0) {
            responder(exchange, 400,
                    "{\"status\":\"error\",\"message\":\"Falta el idUsuario\"}");
            return;
        }

        limite = Math.max(1, Math.min(limite, 100));

        String sql = """
            SELECT idNotificacion, titulo, mensaje, tipo,
                   fechaEnvio, leida, icono, color, enlace
            FROM Notificacion
            WHERE idUsuario = ?
            ORDER BY fechaEnvio DESC
            LIMIT ?
            """;

        String sqlNoLeidas = """
            SELECT COUNT(*) AS noLeidas
            FROM Notificacion
            WHERE idUsuario = ? AND leida = 0
            """;

        try (Connection con = ConexionDB.getConnection()) {
            if (con == null) {
                throw new SQLException("No se pudo conectar a la base de datos.");
            }

            StringBuilder json = new StringBuilder();
            json.append("{\"status\":\"success\",\"data\":{\"notificaciones\":[");

            boolean primero = true;

            try (PreparedStatement ps = con.prepareStatement(sql)) {
                ps.setInt(1, idUsuario);
                ps.setInt(2, limite);

                try (ResultSet rs = ps.executeQuery()) {
                    while (rs.next()) {
                        if (!primero) json.append(",");
                        primero = false;

                        json.append("{")
                                .append("\"id\":").append(rs.getInt("idNotificacion")).append(",")
                                .append("\"titulo\":\"").append(escapar(rs.getString("titulo"))).append("\",")
                                .append("\"mensaje\":\"").append(escapar(rs.getString("mensaje"))).append("\",")
                                .append("\"tipo\":\"").append(escapar(rs.getString("tipo"))).append("\",")
                                .append("\"leida\":").append(rs.getBoolean("leida")).append(",")
                                .append("\"icono\":\"").append(escapar(rs.getString("icono"))).append("\",")
                                .append("\"color\":\"").append(escapar(rs.getString("color"))).append("\",")
                                .append("\"enlace\":\"").append(escapar(rs.getString("enlace"))).append("\",")
                                .append("\"tiempoRelativo\":\"Reciente\"")
                                .append("}");
                    }
                }
            }

            int noLeidas = 0;
            try (PreparedStatement ps = con.prepareStatement(sqlNoLeidas)) {
                ps.setInt(1, idUsuario);
                try (ResultSet rs = ps.executeQuery()) {
                    if (rs.next()) noLeidas = rs.getInt("noLeidas");
                }
            }

            json.append("],\"noLeidas\":").append(noLeidas).append("}}");
            responder(exchange, 200, json.toString());
        }
    }

    private void marcarLeida(HttpExchange exchange)
            throws IOException, SQLException {

        String cuerpo = new String(
                exchange.getRequestBody().readAllBytes(),
                StandardCharsets.UTF_8);

        int idNotificacion = obtenerNumeroJson(cuerpo, "idNotificacion");
        int idUsuario = obtenerNumeroJson(cuerpo, "idUsuario");

        if (idNotificacion <= 0 || idUsuario <= 0) {
            responder(exchange, 400,
                    "{\"status\":\"error\",\"message\":\"Faltan idNotificacion o idUsuario\"}");
            return;
        }

        String sql = """
            UPDATE Notificacion
            SET leida = 1
            WHERE idNotificacion = ? AND idUsuario = ?
            """;

        try (Connection con = ConexionDB.getConnection()) {
            if (con == null) {
                throw new SQLException("No se pudo conectar a la base de datos.");
            }

            try (PreparedStatement ps = con.prepareStatement(sql)) {
                ps.setInt(1, idNotificacion);
                ps.setInt(2, idUsuario);

                int filas = ps.executeUpdate();
                if (filas == 0) {
                    responder(exchange, 404,
                            "{\"status\":\"error\",\"message\":\"Notificación no encontrada para este usuario\"}");
                    return;
                }
            }
        }

        responder(exchange, 200,
                "{\"status\":\"success\",\"message\":\"Notificación marcada como leída\"}");
    }

    private int obtenerParametro(String query, String nombre, int defecto) {
        if (query == null) return defecto;

        for (String parte : query.split("&")) {
            String[] par = parte.split("=", 2);
            if (par.length == 2 && par[0].equals(nombre)) {
                try {
                    return Integer.parseInt(
                            URLDecoder.decode(par[1], StandardCharsets.UTF_8));
                } catch (NumberFormatException e) {
                    return defecto;
                }
            }
        }
        return defecto;
    }

    private int obtenerNumeroJson(String json, String campo) {
        Pattern patron = Pattern.compile(
                "\"" + Pattern.quote(campo) + "\"\\s*:\\s*(\\d+)");
        Matcher matcher = patron.matcher(json);
        return matcher.find() ? Integer.parseInt(matcher.group(1)) : 0;
    }

    private String escapar(String valor) {
        if (valor == null) return "";
        return valor.replace("\\", "\\\\")
                .replace("\"", "\\\"")
                .replace("\n", "\\n")
                .replace("\r", "\\r")
                .replace("\t", "\\t");
    }

    private void responder(HttpExchange exchange, int codigo, String respuesta)
            throws IOException {
        byte[] bytes = respuesta.getBytes(StandardCharsets.UTF_8);
        exchange.getResponseHeaders().set(
                "Content-Type", "application/json; charset=UTF-8");
        exchange.sendResponseHeaders(codigo, bytes.length);
        try (var salida = exchange.getResponseBody()) {
            salida.write(bytes);
        }
    }
}
