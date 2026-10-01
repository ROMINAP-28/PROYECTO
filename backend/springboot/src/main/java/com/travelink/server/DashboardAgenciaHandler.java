package com.travelink.server;

import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import com.travelink.config.ConexionDB;

import java.io.IOException;
import java.io.OutputStream;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;

public class DashboardAgenciaHandler implements HttpHandler {

    @Override
    public void handle(HttpExchange exchange) throws IOException {
        // Habilitar CORS
        exchange.getResponseHeaders().add("Access-Control-Allow-Origin", "*");
        exchange.getResponseHeaders().add("Access-Control-Allow-Methods", "GET, OPTIONS");
        exchange.getResponseHeaders().add("Access-Control-Allow-Headers", "Content-Type");

        if ("OPTIONS".equalsIgnoreCase(exchange.getRequestMethod())) {
            exchange.sendResponseHeaders(204, -1);
            return;
        }

        if ("GET".equalsIgnoreCase(exchange.getRequestMethod())) {
            String query = exchange.getRequestURI().getQuery();
            int idAgencia = -1;
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

            if (idAgencia == -1) {
                enviarRespuesta(exchange, 400, "{\"status\":\"error\", \"message\":\"Falta idAgencia\"}");
                return;
            }

            StringBuilder jsonResponse = new StringBuilder();
            jsonResponse.append("{");
            jsonResponse.append("\"status\":\"success\",");

            try (Connection con = ConexionDB.getConnection()) {
                
                // INDICADORES
                int serviciosActivos = 0;
                int reservasMes = 0;
                int viajeros = 0;
                double ingresos = 0.0;

                String sqlServicios = "SELECT COUNT(*) FROM Tour WHERE idAgencia = ? AND estado = 'ACTIVO'";
                try (PreparedStatement ps = con.prepareStatement(sqlServicios)) {
                    ps.setInt(1, idAgencia);
                    try (ResultSet rs = ps.executeQuery()) {
                        if (rs.next()) serviciosActivos = rs.getInt(1);
                    }
                }

                String sqlReservas = "SELECT COUNT(DISTINCT r.idReserva) FROM Reserva r " +
                                     "JOIN DetalleReserva dr ON r.idReserva = dr.idReserva " +
                                     "JOIN TourFecha tf ON dr.idTourFecha = tf.idTourFecha " +
                                     "JOIN Tour t ON tf.idTour = t.idTour " +
                                     "WHERE t.idAgencia = ? AND MONTH(r.fechaRegistro) = MONTH(CURRENT_DATE()) AND YEAR(r.fechaRegistro) = YEAR(CURRENT_DATE())";
                try (PreparedStatement ps = con.prepareStatement(sqlReservas)) {
                    ps.setInt(1, idAgencia);
                    try (ResultSet rs = ps.executeQuery()) {
                        if (rs.next()) reservasMes = rs.getInt(1);
                    }
                }

                String sqlViajeros = "SELECT COALESCE(SUM(dr.cantAdultos + dr.cantNinos + dr.cantBebes), 0) FROM DetalleReserva dr " +
                                     "JOIN Reserva r ON dr.idReserva = r.idReserva " +
                                     "JOIN TourFecha tf ON dr.idTourFecha = tf.idTourFecha " +
                                     "JOIN Tour t ON tf.idTour = t.idTour " +
                                     "WHERE t.idAgencia = ?";
                try (PreparedStatement ps = con.prepareStatement(sqlViajeros)) {
                    ps.setInt(1, idAgencia);
                    try (ResultSet rs = ps.executeQuery()) {
                        if (rs.next()) viajeros = rs.getInt(1);
                    }
                }

                String sqlIngresos = "SELECT COALESCE(SUM(dr.subtotal), 0) FROM DetalleReserva dr " +
                                     "JOIN Reserva r ON dr.idReserva = r.idReserva " +
                                     "JOIN TourFecha tf ON dr.idTourFecha = tf.idTourFecha " +
                                     "JOIN Tour t ON tf.idTour = t.idTour " +
                                     "WHERE t.idAgencia = ? AND r.estado = 'Confirmada' AND MONTH(r.fechaRegistro) = MONTH(CURRENT_DATE()) AND YEAR(r.fechaRegistro) = YEAR(CURRENT_DATE())";
                try (PreparedStatement ps = con.prepareStatement(sqlIngresos)) {
                    ps.setInt(1, idAgencia);
                    try (ResultSet rs = ps.executeQuery()) {
                        if (rs.next()) ingresos = rs.getDouble(1);
                    }
                }

                jsonResponse.append("\"indicadores\":{")
                            .append("\"serviciosActivos\":").append(serviciosActivos).append(",")
                            .append("\"reservasMes\":").append(reservasMes).append(",")
                            .append("\"viajeros\":").append(viajeros).append(",")
                            .append("\"ingresos\":").append(ingresos)
                            .append("},");

                // RESERVAS RECIENTES (últimas 4)
                jsonResponse.append("\"reservasRecientes\":[");
                String sqlRecientes = "SELECT r.codigoReserva, p.nombre, p.apellidoPaterno, p.apellidoMaterno, t.nombre AS tourNombre, tf.fecha, " +
                                      "(dr.cantAdultos + dr.cantNinos + dr.cantBebes) AS personas, r.estado, dr.subtotal " +
                                      "FROM Reserva r " +
                                      "JOIN DetalleReserva dr ON r.idReserva = dr.idReserva " +
                                      "JOIN TourFecha tf ON dr.idTourFecha = tf.idTourFecha " +
                                      "JOIN Tour t ON tf.idTour = t.idTour " +
                                      "JOIN Usuario u ON r.idUsuario = u.idUsuario " +
                                      "JOIN Persona p ON u.idPersona = p.idPersona " +
                                      "WHERE t.idAgencia = ? " +
                                      "ORDER BY r.fechaRegistro DESC LIMIT 4";
                try (PreparedStatement ps = con.prepareStatement(sqlRecientes)) {
                    ps.setInt(1, idAgencia);
                    try (ResultSet rs = ps.executeQuery()) {
                        boolean first = true;
                        while (rs.next()) {
                            if (!first) jsonResponse.append(",");
                            jsonResponse.append("{")
                                        .append("\"codigo\":\"").append(escapeJson(rs.getString("codigoReserva"))).append("\",")
                                        .append("\"cliente\":\"").append(escapeJson(rs.getString("nombre") + " " + rs.getString("apellidoPaterno") + " " + rs.getString("apellidoMaterno"))).append("\",")
                                        .append("\"servicio\":\"").append(escapeJson(rs.getString("tourNombre"))).append("\",")
                                        .append("\"fecha\":\"").append(rs.getString("fecha")).append("\",")
                                        .append("\"personas\":").append(rs.getInt("personas")).append(",")
                                        .append("\"estado\":\"").append(escapeJson(rs.getString("estado"))).append("\",")
                                        .append("\"total\":").append(rs.getDouble("subtotal"))
                                        .append("}");
                            first = false;
                        }
                    }
                }
                jsonResponse.append("],");

                // PROXIMAS SALIDAS (próximas 4)
                jsonResponse.append("\"proximasSalidas\":[");
                String sqlSalidas = "SELECT t.nombre, tf.fecha, tf.cupoDisponible " +
                                    "FROM TourFecha tf " +
                                    "JOIN Tour t ON tf.idTour = t.idTour " +
                                    "WHERE t.idAgencia = ? AND tf.fecha >= CURRENT_DATE() " +
                                    "ORDER BY tf.fecha ASC LIMIT 4";
                try (PreparedStatement ps = con.prepareStatement(sqlSalidas)) {
                    ps.setInt(1, idAgencia);
                    try (ResultSet rs = ps.executeQuery()) {
                        boolean first = true;
                        while (rs.next()) {
                            if (!first) jsonResponse.append(",");
                            jsonResponse.append("{")
                                        .append("\"servicio\":\"").append(escapeJson(rs.getString("nombre"))).append("\",")
                                        .append("\"fecha\":\"").append(rs.getString("fecha")).append("\",")
                                        .append("\"cupoLibre\":").append(rs.getInt("cupoDisponible"))
                                        .append("}");
                            first = false;
                        }
                    }
                }
                jsonResponse.append("],");

                // FINANZAS
                double comisiones = ingresos * 0.15;
                double neto = ingresos - comisiones;
                double liquidacionesPendientes = 0; // Se podria calcular de Comision, pero por ahora lo dejo simulado o le sumo de la tabla.
                
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
                enviarRespuesta(exchange, 500, "{\"status\":\"error\", \"message\":\"Error interno del servidor\"}");
            }
        } else {
            enviarRespuesta(exchange, 405, "{\"status\":\"error\", \"message\":\"Método no permitido\"}");
        }
    }

    private String escapeJson(String input) {
        if (input == null) return "";
        return input.replace("\"", "\\\"").replace("\n", "\\n");
    }

    private void enviarRespuesta(HttpExchange exchange, int statusCode, String response) throws IOException {
        exchange.getResponseHeaders().set("Content-Type", "application/json; charset=UTF-8");
        byte[] bytes = response.getBytes("UTF-8");
        exchange.sendResponseHeaders(statusCode, bytes.length);
        try (OutputStream os = exchange.getResponseBody()) {
            os.write(bytes);
        }
    }
}
