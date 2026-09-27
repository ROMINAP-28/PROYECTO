
package com.travelink.server;

import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import com.travelink.config.ConexionDB;

import java.io.IOException;
import java.io.OutputStream;
import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.HashMap;
import java.util.Map;

public class PagosAgenciaHandler implements HttpHandler {

    @Override
    public void handle(HttpExchange exchange) throws IOException {

        enableCORS(exchange);

        if ("OPTIONS".equalsIgnoreCase(exchange.getRequestMethod())) {
            exchange.sendResponseHeaders(200, -1);
            exchange.close();
            return;
        }

        if (!"GET".equalsIgnoreCase(exchange.getRequestMethod())) {
            sendJsonResponse(exchange, 405,
                    "{\"status\":\"error\",\"message\":\"Método no permitido\"}");
            return;
        }

        Map<String, String> params =
                parseQueryParams(exchange.getRequestURI().getRawQuery());

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

        String sqlPagos = """
            SELECT
                p.idPago,
                p.numeroOperacion,
                p.idReserva,
                r.codigoReserva,
                r.nombreTour,
                p.fechaPago,
                p.metodoPago,
                p.monto,
                p.estado
            FROM Pago p
            INNER JOIN Reserva r
                ON r.idReserva = p.idReserva
            WHERE r.idAgencia = ?
            ORDER BY p.fechaPago DESC, p.idPago DESC
            """;

        String sqlLiquidaciones = """
            SELECT
                idLiquidacion,
                montoBruto,
                montoComision,
                montoNeto,
                fechaGeneracion,
                fechaLiquidacion,
                estado,
                idAgencia
            FROM Liquidacion
            WHERE idAgencia = ?
            ORDER BY fechaGeneracion DESC, idLiquidacion DESC
            """;

        StringBuilder json = new StringBuilder();
        json.append("{\"status\":\"success\",\"pagos\":[");

        try (Connection con = ConexionDB.getConnection()) {

            if (con == null) {
                sendJsonResponse(exchange, 500,
                        "{\"status\":\"error\",\"message\":\"No se pudo conectar a la base de datos\"}");
                return;
            }

            // Consultar pagos de las reservas de la agencia
            try (PreparedStatement ps = con.prepareStatement(sqlPagos)) {
                ps.setInt(1, idAgencia);

                try (ResultSet rs = ps.executeQuery()) {
                    boolean primero = true;

                    while (rs.next()) {
                        if (!primero) {
                            json.append(",");
                        }
                        primero = false;

                        json.append("{")
                                .append("\"idPago\":")
                                .append(rs.getInt("idPago")).append(",")

                                .append("\"numeroOperacion\":\"")
                                .append(jsonEscape(rs.getString("numeroOperacion")))
                                .append("\",")

                                .append("\"idReserva\":")
                                .append(rs.getInt("idReserva")).append(",")

                                .append("\"codigoReserva\":\"")
                                .append(jsonEscape(rs.getString("codigoReserva")))
                                .append("\",")

                                .append("\"nombreTour\":\"")
                                .append(jsonEscape(rs.getString("nombreTour")))
                                .append("\",")

                                .append("\"fechaPago\":\"")
                                .append(jsonEscape(rs.getString("fechaPago")))
                                .append("\",")

                                .append("\"metodoPago\":\"")
                                .append(jsonEscape(rs.getString("metodoPago")))
                                .append("\",")

                                .append("\"monto\":")
                                .append(decimal(rs.getBigDecimal("monto")))
                                .append(",")

                                .append("\"estado\":\"")
                                .append(jsonEscape(rs.getString("estado")))
                                .append("\"")
                                .append("}");
                    }
                }
            }

            json.append("],\"liquidaciones\":[");

            // Consultar liquidaciones de la agencia
            try (PreparedStatement ps =
                         con.prepareStatement(sqlLiquidaciones)) {
                ps.setInt(1, idAgencia);

                try (ResultSet rs = ps.executeQuery()) {
                    boolean primero = true;

                    while (rs.next()) {
                        if (!primero) {
                            json.append(",");
                        }
                        primero = false;

                        json.append("{")
                                .append("\"idLiquidacion\":")
                                .append(rs.getInt("idLiquidacion")).append(",")

                                .append("\"montoBruto\":")
                                .append(decimal(rs.getBigDecimal("montoBruto")))
                                .append(",")

                                .append("\"montoComision\":")
                                .append(decimal(rs.getBigDecimal("montoComision")))
                                .append(",")

                                .append("\"montoNeto\":")
                                .append(decimal(rs.getBigDecimal("montoNeto")))
                                .append(",")

                                .append("\"fechaGeneracion\":\"")
                                .append(jsonEscape(rs.getString("fechaGeneracion")))
                                .append("\",")

                                .append("\"fechaLiquidacion\":\"")
                                .append(jsonEscape(rs.getString("fechaLiquidacion")))
                                .append("\",")

                                .append("\"estado\":\"")
                                .append(jsonEscape(rs.getString("estado")))
                                .append("\",")

                                .append("\"idAgencia\":")
                                .append(rs.getInt("idAgencia"))
                                .append("}");
                    }
                }
            }

            json.append("]}");
            sendJsonResponse(exchange, 200, json.toString());

        } catch (Exception e) {
            e.printStackTrace();
            sendJsonResponse(exchange, 500,
                    "{\"status\":\"error\",\"message\":\"Error al consultar pagos y liquidaciones\"}");
        }
    }

    private static Map<String, String> parseQueryParams(String query) {
        Map<String, String> params = new HashMap<>();

        if (query == null || query.isBlank()) {
            return params;
        }

        for (String parte : query.split("&")) {
            String[] par = parte.split("=", 2);

            String clave = URLDecoder.decode(
                    par[0], StandardCharsets.UTF_8);

            String valor = par.length > 1
                    ? URLDecoder.decode(par[1], StandardCharsets.UTF_8)
                    : "";

            params.put(clave, valor);
        }

        return params;
    }

    private static String jsonEscape(String valor) {
        if (valor == null) {
            return "";
        }

        return valor
                .replace("\\", "\\\\")
                .replace("\"", "\\\"")
                .replace("\n", "\\n")
                .replace("\r", "\\r")
                .replace("\t", "\\t");
    }

    private static String decimal(java.math.BigDecimal valor) {
        return valor == null ? "0.00" : valor.toPlainString();
    }

    private static void enableCORS(HttpExchange exchange) {
        exchange.getResponseHeaders().set(
                "Access-Control-Allow-Origin", "*");
        exchange.getResponseHeaders().set(
                "Access-Control-Allow-Methods", "GET, OPTIONS");
        exchange.getResponseHeaders().set(
                "Access-Control-Allow-Headers", "Content-Type");
        exchange.getResponseHeaders().set(
                "Content-Type", "application/json; charset=UTF-8");
    }

    private static void sendJsonResponse(
            HttpExchange exchange, int status, String respuesta)
            throws IOException {

        byte[] datos = respuesta.getBytes(StandardCharsets.UTF_8);

        exchange.getResponseHeaders().set(
                "Content-Type", "application/json; charset=UTF-8");

        exchange.sendResponseHeaders(status, datos.length);

        try (OutputStream salida = exchange.getResponseBody()) {
            salida.write(datos);
        }
    }
}