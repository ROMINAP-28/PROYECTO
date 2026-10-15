package com.travelink.controlador.agencia;

import com.travelink.server.JavaApiServer;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import com.travelink.config.ConexionDB;

import java.io.IOException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.Map;

public class PagosAgenciaHandler implements HttpHandler {

    @Override
    public void handle(HttpExchange exchange) throws IOException {
        JavaApiServer.enableCORS(exchange);

        if ("OPTIONS".equalsIgnoreCase(exchange.getRequestMethod())) {
            exchange.sendResponseHeaders(204, -1);
            return;
        }

        if (!"GET".equalsIgnoreCase(exchange.getRequestMethod())) {
            JavaApiServer.sendJsonResponse(exchange, 405, "{\"status\":\"error\",\"message\":\"Método no permitido\"}");
            return;
        }

        int idAgencia = 0;
        Object sessAgencia = exchange.getAttribute("ID_AGENCIA");
        if (sessAgencia instanceof Integer) {
            idAgencia = (Integer) sessAgencia;
        }
        if (idAgencia <= 0) {
            Map<String, String> params = JavaApiServer.parseQueryParams(exchange.getRequestURI().getQuery());
            try {
                if (params.containsKey("idAgencia")) {
                    idAgencia = Integer.parseInt(params.get("idAgencia"));
                }
            } catch (Exception ignored) {}
        }
        if (idAgencia <= 0) idAgencia = 1;

        StringBuilder json = new StringBuilder("{\"status\":\"success\",");

        try (Connection con = ConexionDB.getConnection()) {
            if (con == null) {
                JavaApiServer.sendJsonResponse(exchange, 500, "{\"status\":\"error\",\"message\":\"No se pudo conectar a la base de datos\"}");
                return;
            }

            // 1. PAGOS (Consultar Pago y MetodoPago)
            json.append("\"pagos\":[");
            boolean primeroPago = true;
            String sqlPagos = """
                SELECT
                    p.idPago,
                    COALESCE(p.numeroOperacion, r.codigoReserva, '-') AS numeroOperacion,
                    p.idReserva,
                    r.codigoReserva,
                    COALESCE(r.nombreTour, 'Tour Turístico') AS nombreTour,
                    p.fechaPago,
                    p.metodoPago,
                    p.monto,
                    p.estado
                FROM Pago p
                INNER JOIN Reserva r ON r.idReserva = p.idReserva
                WHERE r.idAgencia = ?
                UNION
                SELECT
                    mp.idMetodoPago AS idPago,
                    COALESCE(mp.numeroOperacion, r.codigoReserva, '-') AS numeroOperacion,
                    mp.idReserva,
                    r.codigoReserva,
                    COALESCE(r.nombreTour, 'Tour Turístico') AS nombreTour,
                    mp.fechaPago,
                    mp.tipoPago AS metodoPago,
                    mp.monto,
                    mp.estadoPago AS estado
                FROM MetodoPago mp
                INNER JOIN Reserva r ON r.idReserva = mp.idReserva
                WHERE r.idAgencia = ? AND mp.idReserva NOT IN (SELECT p2.idReserva FROM Pago p2 WHERE p2.idReserva IS NOT NULL)
                ORDER BY fechaPago DESC
                """;

            try (PreparedStatement psP = con.prepareStatement(sqlPagos)) {
                psP.setInt(1, idAgencia);
                psP.setInt(2, idAgencia);
                try (ResultSet rsP = psP.executeQuery()) {
                    while (rsP.next()) {
                        if (!primeroPago) json.append(",");
                        primeroPago = false;
                        json.append("{")
                            .append("\"idPago\":").append(rsP.getInt("idPago")).append(",")
                            .append("\"numeroOperacion\":\"").append(JavaApiServer.jsonEscape(rsP.getString("numeroOperacion"))).append("\",")
                            .append("\"idReserva\":").append(rsP.getInt("idReserva")).append(",")
                            .append("\"codigoReserva\":\"").append(JavaApiServer.jsonEscape(rsP.getString("codigoReserva"))).append("\",")
                            .append("\"nombreTour\":\"").append(JavaApiServer.jsonEscape(rsP.getString("nombreTour"))).append("\",")
                            .append("\"fechaPago\":\"").append(rsP.getString("fechaPago") != null ? rsP.getString("fechaPago").replace("T", " ") : "").append("\",")
                            .append("\"metodoPago\":\"").append(JavaApiServer.jsonEscape(rsP.getString("metodoPago"))).append("\",")
                            .append("\"monto\":").append(rsP.getDouble("monto")).append(",")
                            .append("\"estado\":\"").append(JavaApiServer.jsonEscape(rsP.getString("estado"))).append("\"")
                            .append("}");
                    }
                }
            }
            json.append("],");

            // 2. LIQUIDACIONES (Desde tabla Comision)
            json.append("\"liquidaciones\":[");
            boolean primeroLiq = true;
            String sqlLiq = """
                SELECT
                    c.idComision AS idLiquidacion,
                    c.idComision,
                    c.subtotal AS montoBruto,
                    c.montoComision,
                    c.montoAgencia AS montoNeto,
                    c.fechaCalculo AS fechaGeneracion,
                    c.fechaLiquidacion,
                    COALESCE(c.estadoLiquidacion, c.estado, 'Pendiente') AS estado,
                    c.idAgencia,
                    c.idReserva,
                    COALESCE(r.codigoReserva, CONCAT('TRK-', c.idReserva)) AS codigoReserva
                FROM Comision c
                LEFT JOIN Reserva r ON c.idReserva = r.idReserva
                WHERE c.idAgencia = ?
                ORDER BY c.fechaCalculo DESC, c.idComision DESC
                """;

            try (PreparedStatement psL = con.prepareStatement(sqlLiq)) {
                psL.setInt(1, idAgencia);
                try (ResultSet rsL = psL.executeQuery()) {
                    while (rsL.next()) {
                        if (!primeroLiq) json.append(",");
                        primeroLiq = false;
                        json.append("{")
                            .append("\"idLiquidacion\":").append(rsL.getInt("idLiquidacion")).append(",")
                            .append("\"idComision\":").append(rsL.getInt("idComision")).append(",")
                            .append("\"idReserva\":").append(rsL.getInt("idReserva")).append(",")
                            .append("\"codigoReserva\":\"").append(JavaApiServer.jsonEscape(rsL.getString("codigoReserva"))).append("\",")
                            .append("\"montoBruto\":").append(rsL.getDouble("montoBruto")).append(",")
                            .append("\"montoComision\":").append(rsL.getDouble("montoComision")).append(",")
                            .append("\"montoNeto\":").append(rsL.getDouble("montoNeto")).append(",")
                            .append("\"fechaGeneracion\":\"").append(rsL.getString("fechaGeneracion") != null ? rsL.getString("fechaGeneracion").replace("T", " ") : "").append("\",")
                            .append("\"fechaLiquidacion\":\"").append(rsL.getString("fechaLiquidacion") != null ? rsL.getString("fechaLiquidacion").replace("T", " ") : "-").append("\",")
                            .append("\"estado\":\"").append(JavaApiServer.jsonEscape(rsL.getString("estado"))).append("\",")
                            .append("\"idAgencia\":").append(rsL.getInt("idAgencia"))
                            .append("}");
                    }
                }
            }
            json.append("]}");

            JavaApiServer.sendJsonResponse(exchange, 200, json.toString());

        } catch (Exception e) {
            e.printStackTrace();
            JavaApiServer.sendJsonResponse(exchange, 500, "{\"status\":\"error\",\"message\":\"Error interno: " + e.getMessage() + "\"}");
        }
    }
}
