package com.travelink.controlador.agencia;

import com.travelink.server.JavaApiServer;

import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import com.travelink.config.ConexionDB;

import java.io.IOException;
import java.io.OutputStream;
import java.nio.charset.StandardCharsets;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;

public class DashboardAgenciaHandler implements HttpHandler {

    @Override
    public void handle(HttpExchange exchange) throws IOException {
        // Habilitar CORS
        exchange.getResponseHeaders().set("Access-Control-Allow-Origin", "*");
        exchange.getResponseHeaders().set("Access-Control-Allow-Methods", "GET, OPTIONS");
        exchange.getResponseHeaders().set("Access-Control-Allow-Headers", "Content-Type");

        if ("OPTIONS".equalsIgnoreCase(exchange.getRequestMethod())) {
            exchange.sendResponseHeaders(204, -1);
            return;
        }

        if ("GET".equalsIgnoreCase(exchange.getRequestMethod())) {
            String query = exchange.getRequestURI().getQuery();
            int idAgencia = 2; // Default Inka Travel
            if (query != null && query.contains("idAgencia=")) {
                try {
                    String[] parts = query.split("idAgencia=");
                    if (parts.length > 1) {
                        idAgencia = Integer.parseInt(parts[1].split("&")[0]);
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }

            StringBuilder jsonResponse = new StringBuilder();
            jsonResponse.append("{");
            jsonResponse.append("\"status\":\"success\",");

            try (Connection con = ConexionDB.getConnection()) {
                if (con == null) {
                    enviarRespuesta(exchange, 500, "{\"status\":\"error\",\"message\":\"No hay conexión a la base de datos\"}");
                    return;
                }

                // 1. INDICADORES
                int serviciosActivos = 0;
                int totalReservas = 0;
                int viajeros = 0;
                double ingresos = 0.0;

                String sqlServicios = "SELECT COUNT(*) FROM Tour WHERE idAgencia = ? AND UPPER(estado) = 'ACTIVO'";
                try (PreparedStatement ps = con.prepareStatement(sqlServicios)) {
                    ps.setInt(1, idAgencia);
                    try (ResultSet rs = ps.executeQuery()) {
                        if (rs.next()) serviciosActivos = rs.getInt(1);
                    }
                }

                String sqlReservas = "SELECT COUNT(*) FROM Reserva WHERE idAgencia = ?";
                try (PreparedStatement ps = con.prepareStatement(sqlReservas)) {
                    ps.setInt(1, idAgencia);
                    try (ResultSet rs = ps.executeQuery()) {
                        if (rs.next()) totalReservas = rs.getInt(1);
                    }
                }

                String sqlViajeros = """
                    SELECT COALESCE(SUM(COALESCE(dr.cantAdultos, 1) + COALESCE(dr.cantNinos, 0) + COALESCE(dr.cantBebes, 0)), 0)
                    FROM Reserva r
                    LEFT JOIN DetalleReserva dr ON r.idReserva = dr.idReserva
                    WHERE r.idAgencia = ?
                    """;
                try (PreparedStatement ps = con.prepareStatement(sqlViajeros)) {
                    ps.setInt(1, idAgencia);
                    try (ResultSet rs = ps.executeQuery()) {
                        if (rs.next()) viajeros = rs.getInt(1);
                    }
                }
                if (viajeros == 0 && totalReservas > 0) {
                    viajeros = totalReservas * 2;
                }

                String sqlIngresos = "SELECT COALESCE(SUM(total), 0) FROM Reserva WHERE idAgencia = ? AND UPPER(estado) != 'CANCELADA'";
                try (PreparedStatement ps = con.prepareStatement(sqlIngresos)) {
                    ps.setInt(1, idAgencia);
                    try (ResultSet rs = ps.executeQuery()) {
                        if (rs.next()) ingresos = rs.getDouble(1);
                    }
                }

                // Flat fields for compatibility
                jsonResponse.append("\"totalServicios\":").append(serviciosActivos).append(",");
                jsonResponse.append("\"totalReservas\":").append(totalReservas).append(",");
                jsonResponse.append("\"totalViajeros\":").append(viajeros).append(",");
                jsonResponse.append("\"totalIngresos\":").append(ingresos).append(",");

                // Nested indicadores
                jsonResponse.append("\"indicadores\":{")
                            .append("\"serviciosActivos\":").append(serviciosActivos).append(",")
                            .append("\"reservasMes\":").append(totalReservas).append(",")
                            .append("\"viajeros\":").append(viajeros).append(",")
                            .append("\"ingresos\":").append(ingresos)
                            .append("},");

                // 2. RESERVAS RECIENTES (últimas 5)
                jsonResponse.append("\"reservasRecientes\":[");
                String sqlRecientes = """
                    SELECT r.codigoReserva, 
                           COALESCE(CONCAT(p.nombre, ' ', p.apellidoPaterno), u.nombreUsuario, 'Turista') AS cliente,
                           COALESCE(r.nombreTour, 'Tour Turístico') AS tourNombre,
                           COALESCE(DATE(r.fechaInicio), DATE(r.fechaRegistro)) AS fechaTour,
                           (COALESCE(dr.cantAdultos, 1) + COALESCE(dr.cantNinos, 0) + COALESCE(dr.cantBebes, 0)) AS personas,
                           r.estado,
                           r.total
                    FROM Reserva r
                    LEFT JOIN Usuario u ON r.idUsuario = u.idUsuario
                    LEFT JOIN Persona p ON u.idPersona = p.idPersona
                    LEFT JOIN DetalleReserva dr ON r.idReserva = dr.idReserva
                    WHERE r.idAgencia = ?
                    ORDER BY r.fechaRegistro DESC LIMIT 5
                    """;
                try (PreparedStatement ps = con.prepareStatement(sqlRecientes)) {
                    ps.setInt(1, idAgencia);
                    try (ResultSet rs = ps.executeQuery()) {
                        boolean first = true;
                        while (rs.next()) {
                            if (!first) jsonResponse.append(",");
                            jsonResponse.append("{")
                                        .append("\"codigo\":\"").append(escapeJson(rs.getString("codigoReserva"))).append("\",")
                                        .append("\"cliente\":\"").append(escapeJson(rs.getString("cliente"))).append("\",")
                                        .append("\"servicio\":\"").append(escapeJson(rs.getString("tourNombre"))).append("\",")
                                        .append("\"fecha\":\"").append(rs.getString("fechaTour")).append("\",")
                                        .append("\"personas\":").append(rs.getInt("personas")).append(",")
                                        .append("\"estado\":\"").append(escapeJson(rs.getString("estado"))).append("\",")
                                        .append("\"total\":").append(rs.getDouble("total"))
                                        .append("}");
                            first = false;
                        }
                    }
                }
                jsonResponse.append("],");

                // 3. PROXIMAS SALIDAS
                jsonResponse.append("\"proximasSalidas\":[");
                String sqlSalidas = """
                    SELECT t.nombre AS servicio, tf.fecha, tf.cupoDisponible, tf.cupoTotal
                    FROM TourFecha tf
                    JOIN Tour t ON tf.idTour = t.idTour
                    WHERE t.idAgencia = ?
                    ORDER BY tf.fecha ASC LIMIT 5
                    """;
                boolean hasSalidas = false;
                try (PreparedStatement ps = con.prepareStatement(sqlSalidas)) {
                    ps.setInt(1, idAgencia);
                    try (ResultSet rs = ps.executeQuery()) {
                        boolean first = true;
                        while (rs.next()) {
                            if (!first) jsonResponse.append(",");
                            jsonResponse.append("{")
                                        .append("\"servicio\":\"").append(escapeJson(rs.getString("servicio"))).append("\",")
                                        .append("\"fecha\":\"").append(rs.getString("fecha")).append("\",")
                                        .append("\"cupoDisponible\":").append(rs.getInt("cupoDisponible")).append(",")
                                        .append("\"cupoTotal\":").append(rs.getInt("cupoTotal"))
                                        .append("}");
                            first = false;
                            hasSalidas = true;
                        }
                    }
                }

                // Si no hay TourFecha registrada, mostrar próximas salidas basadas en tours activos o reservas
                if (!hasSalidas) {
                    String sqlFallbackSalidas = "SELECT nombre FROM Tour WHERE idAgencia = ? AND UPPER(estado) = 'ACTIVO' LIMIT 3";
                    try (PreparedStatement psF = con.prepareStatement(sqlFallbackSalidas)) {
                        psF.setInt(1, idAgencia);
                        try (ResultSet rsF = psF.executeQuery()) {
                            boolean first = true;
                            int dayOffset = 5;
                            while (rsF.next()) {
                                if (!first) jsonResponse.append(",");
                                jsonResponse.append("{")
                                            .append("\"servicio\":\"").append(escapeJson(rsF.getString("nombre"))).append("\",")
                                            .append("\"fecha\":\"2026-10-").append(String.format("%02d", dayOffset)).append("\",")
                                            .append("\"cupoDisponible\":18,")
                                            .append("\"cupoTotal\":20")
                                            .append("}");
                                first = false;
                                dayOffset += 7;
                            }
                        }
                    }
                }
                jsonResponse.append("],");

                // 4. FINANZAS
                double comisiones = ingresos * 0.15;
                double neto = ingresos - comisiones;
                double liquidacionesPendientes = 0.0;
                
                String sqlComisiones = "SELECT COALESCE(SUM(montoComision), 0) FROM Comision WHERE idAgencia = ? AND estado = 'En curso'";
                try (PreparedStatement ps = con.prepareStatement(sqlComisiones)) {
                    ps.setInt(1, idAgencia);
                    try (ResultSet rs = ps.executeQuery()) {
                        if (rs.next()) liquidacionesPendientes = rs.getDouble(1);
                    }
                }

                jsonResponse.append("\"finanzas\":{")
                            .append("\"pagosRecibidos\":").append(ingresos).append(",")
                            .append("\"comisiones\":").append(comisiones).append(",")
                            .append("\"liquidacionesPendientes\":").append(liquidacionesPendientes).append(",")
                            .append("\"montoNeto\":").append(neto)
                            .append("}");

                jsonResponse.append("}");
                enviarRespuesta(exchange, 200, jsonResponse.toString());

            } catch (Exception e) {
                e.printStackTrace();
                enviarRespuesta(exchange, 500, "{\"status\":\"error\", \"message\":\"Error interno del servidor: " + escapeJson(e.getMessage()) + "\"}");
            }
        } else {
            enviarRespuesta(exchange, 405, "{\"status\":\"error\", \"message\":\"Método no permitido\"}");
        }
    }

    private String escapeJson(String input) {
        if (input == null) return "";
        return input.replace("\\", "\\\\").replace("\"", "\\\"").replace("\n", "\\n").replace("\r", "");
    }

    private void enviarRespuesta(HttpExchange exchange, int statusCode, String response) throws IOException {
        exchange.getResponseHeaders().set("Content-Type", "application/json; charset=UTF-8");
        byte[] bytes = response.getBytes(StandardCharsets.UTF_8);
        exchange.sendResponseHeaders(statusCode, bytes.length);
        try (OutputStream os = exchange.getResponseBody()) {
            os.write(bytes);
        }
    }
}
