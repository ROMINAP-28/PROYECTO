package com.travelink.controlador.turista;

import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import com.travelink.config.ConexionDB;
import com.travelink.config.SecurityUtils;
import com.travelink.server.JavaApiServer;

import java.io.IOException;
import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.Locale;
import java.util.Map;

public class CalificarHandler implements HttpHandler {

    @Override
    public void handle(HttpExchange exchange) throws IOException {
        JavaApiServer.enableCORS(exchange);

        if ("OPTIONS".equalsIgnoreCase(exchange.getRequestMethod())) {
            exchange.sendResponseHeaders(204, -1);
            return;
        }

        if ("GET".equalsIgnoreCase(exchange.getRequestMethod())) {
            obtenerCalificaciones(exchange);
            return;
        }

        if ("POST".equalsIgnoreCase(exchange.getRequestMethod())) {
            guardarCalificacion(exchange);
            return;
        }

        JavaApiServer.sendJsonResponse(exchange, 405, "{\"status\":\"error\",\"message\":\"Método no permitido\"}");
    }

    private void obtenerCalificaciones(HttpExchange exchange) throws IOException {
        Map<String, String> params = JavaApiServer.parseQueryParams(exchange.getRequestURI().getQuery());
        int idAgencia = 0;
        int idTour = 0;

        try {
            if (params.containsKey("idAgencia")) idAgencia = Integer.parseInt(params.get("idAgencia"));
            if (params.containsKey("agencia")) idAgencia = Integer.parseInt(params.get("agencia"));
            if (params.containsKey("idTour")) idTour = Integer.parseInt(params.get("idTour"));
            if (params.containsKey("tour")) idTour = Integer.parseInt(params.get("tour"));
        } catch (Exception ignored) {}

        try (Connection con = ConexionDB.getConnection()) {
            if (idAgencia <= 0 && idTour > 0) {
                try (PreparedStatement psT = con.prepareStatement("SELECT idAgencia FROM Tour WHERE idTour = ?")) {
                    psT.setInt(1, idTour);
                    try (ResultSet rsT = psT.executeQuery()) {
                        if (rsT.next()) idAgencia = rsT.getInt(1);
                    }
                }
            }

            double promedio = 0.0;
            int total = 0;

            if (idTour > 0) {
                String sqlTourStats = """
                    SELECT ROUND(AVG(c.estrellas), 1) AS prom, COUNT(*) AS cnt
                    FROM Calificacion c
                    JOIN Reserva r ON c.idReserva = r.idReserva
                    WHERE r.idTour = ? AND (c.eliminadoPorAdmin IS NULL OR c.eliminadoPorAdmin = 0)
                """;
                try (PreparedStatement psS = con.prepareStatement(sqlTourStats)) {
                    psS.setInt(1, idTour);
                    try (ResultSet rsS = psS.executeQuery()) {
                        if (rsS.next()) {
                            promedio = rsS.getDouble("prom");
                            total = rsS.getInt("cnt");
                        }
                    }
                }
            }

            if (total == 0 && idAgencia > 0) {
                String sqlAgStats = "SELECT ROUND(AVG(estrellas), 1) AS prom, COUNT(*) AS cnt FROM Calificacion WHERE idAgencia = ? AND (eliminadoPorAdmin IS NULL OR eliminadoPorAdmin = 0)";
                try (PreparedStatement psS = con.prepareStatement(sqlAgStats)) {
                    psS.setInt(1, idAgencia);
                    try (ResultSet rsS = psS.executeQuery()) {
                        if (rsS.next()) {
                            promedio = rsS.getDouble("prom");
                            total = rsS.getInt("cnt");
                        }
                    }
                }
            }

            StringBuilder json = new StringBuilder("{");
            json.append("\"status\":\"success\",")
                .append("\"idAgencia\":").append(idAgencia).append(",")
                .append("\"idTour\":").append(idTour).append(",")
                .append(String.format(Locale.US, "\"promedio\":%.1f,", total > 0 ? promedio : 0.0))
                .append("\"total\":").append(total).append(",")
                .append("\"calificaciones\":[");

            String sqlList = """
                SELECT c.idCalificacion, c.estrellas, c.comentario, 
                       DATE_FORMAT(c.fechaCalificacion, '%d/%m/%Y') AS fecha,
                       COALESCE(CONCAT(p.nombre, ' ', p.apellidoPaterno), u.nombreUsuario, 'Turista') AS turista,
                       COALESCE(t.nombre, r.nombreTour, 'Tour Turístico') AS tour
                FROM Calificacion c
                JOIN Usuario u ON c.idUsuario = u.idUsuario
                JOIN Persona p ON u.idPersona = p.idPersona
                LEFT JOIN Reserva r ON c.idReserva = r.idReserva
                LEFT JOIN Tour t ON r.idTour = t.idTour
                WHERE (c.eliminadoPorAdmin IS NULL OR c.eliminadoPorAdmin = 0)
                  AND (? = 0 OR r.idTour = ?)
                  AND (? = 0 OR c.idAgencia = ?)
                ORDER BY c.fechaCalificacion DESC
                """;

            boolean primero = true;
            try (PreparedStatement psL = con.prepareStatement(sqlList)) {
                psL.setInt(1, idTour);
                psL.setInt(2, idTour);
                psL.setInt(3, idAgencia);
                psL.setInt(4, idAgencia);
                try (ResultSet rsL = psL.executeQuery()) {
                    while (rsL.next()) {
                        if (!primero) json.append(",");
                        primero = false;
                        json.append("{")
                            .append("\"id\":").append(rsL.getInt("idCalificacion")).append(",")
                            .append("\"turista\":\"").append(JavaApiServer.jsonEscape(rsL.getString("turista"))).append("\",")
                            .append("\"tour\":\"").append(JavaApiServer.jsonEscape(rsL.getString("tour"))).append("\",")
                            .append("\"estrellas\":").append(rsL.getInt("estrellas")).append(",")
                            .append("\"comentario\":\"").append(JavaApiServer.jsonEscape(rsL.getString("comentario"))).append("\",")
                            .append("\"fecha\":\"").append(rsL.getString("fecha")).append("\"")
                            .append("}");
                    }
                }
            }

            json.append("]}");
            JavaApiServer.sendJsonResponse(exchange, 200, json.toString());

        } catch (Exception e) {
            e.printStackTrace();
            JavaApiServer.sendJsonResponse(exchange, 500, "{\"status\":\"error\",\"message\":\"" + e.getMessage() + "\"}");
        }
    }

    private void guardarCalificacion(HttpExchange exchange) throws IOException {
        String body = JavaApiServer.readRequestBody(exchange);
        Map<String, Object> params = JavaApiServer.parseJsonOrFormParams(body);

        // 1. Obtener usuario de la sesión autenticada con fallback a parámetro
        Integer sessionUserId = null;
        Object sessUserObj = exchange.getAttribute("ID_USUARIO");
        if (sessUserObj instanceof Integer) {
            sessionUserId = (Integer) sessUserObj;
        } else if (SecurityUtils.getAuthenticatedIdUsuario().isPresent()) {
            sessionUserId = SecurityUtils.getAuthenticatedIdUsuario().get();
        }

        if (sessionUserId == null && params.containsKey("idUsuario")) {
            try { sessionUserId = Integer.parseInt(String.valueOf(params.get("idUsuario"))); } catch (Exception ignored) {}
        }

        if (sessionUserId == null || sessionUserId <= 0) {
            JavaApiServer.sendJsonResponse(exchange, 401,
                    "{\"status\":\"error\",\"message\":\"Debe iniciar sesión para calificar una reserva.\"}");
            return;
        }

        // 2. Resolver reserva por ID o código
        int idReserva = 0;
        try { idReserva = Integer.parseInt(String.valueOf(params.getOrDefault("idReserva", params.getOrDefault("id", "0")))); } catch (Exception ignored) {}

        String codigo = String.valueOf(params.getOrDefault("codigo", params.getOrDefault("codigoReserva", ""))).trim();

        try (Connection con = ConexionDB.getConnection()) {
            if (idReserva <= 0 && !codigo.isEmpty()) {
                try (PreparedStatement psCod = con.prepareStatement("SELECT idReserva FROM Reserva WHERE codigoReserva = ?")) {
                    psCod.setString(1, codigo);
                    try (ResultSet rsCod = psCod.executeQuery()) {
                        if (rsCod.next()) idReserva = rsCod.getInt(1);
                    }
                }
            }

            if (idReserva <= 0) {
                JavaApiServer.sendJsonResponse(exchange, 400,
                        "{\"status\":\"error\",\"message\":\"ID o código de reserva inválido.\"}");
                return;
            }

            // 3. Verificar reserva y pertenencia
            int resUserId = 0;
            int idAgencia = 0;
            int idTour = 0;
            String estadoReserva = "";

            try (PreparedStatement psR = con.prepareStatement(
                    "SELECT idUsuario, idAgencia, idTour, estado FROM Reserva WHERE idReserva = ?")) {
                psR.setInt(1, idReserva);
                try (ResultSet rsR = psR.executeQuery()) {
                    if (rsR.next()) {
                        resUserId = rsR.getInt("idUsuario");
                        idAgencia = rsR.getInt("idAgencia");
                        idTour = rsR.getInt("idTour");
                        estadoReserva = rsR.getString("estado");
                    } else {
                        JavaApiServer.sendJsonResponse(exchange, 404,
                                "{\"status\":\"error\",\"message\":\"Reserva no encontrada.\"}");
                        return;
                    }
                }
            }

            boolean esAdmin = SecurityUtils.hasRole("ADMIN");
            if (!esAdmin && sessionUserId.intValue() != resUserId) {
                JavaApiServer.sendJsonResponse(exchange, 403,
                        "{\"status\":\"error\",\"message\":\"Solo puedes calificar una reserva propia.\"}");
                return;
            }

            if (estadoReserva != null && "CANCELADA".equalsIgnoreCase(estadoReserva.trim())) {
                JavaApiServer.sendJsonResponse(exchange, 400,
                        "{\"status\":\"error\",\"message\":\"No se puede calificar una reserva cancelada.\"}");
                return;
            }

            // 4. Verificar si ya fue calificada previamente
            try (PreparedStatement psCheck = con.prepareStatement(
                    "SELECT idCalificacion FROM Calificacion WHERE idReserva = ? AND (eliminadoPorAdmin IS NULL OR eliminadoPorAdmin = 0)")) {
                psCheck.setInt(1, idReserva);
                try (ResultSet rsCheck = psCheck.executeQuery()) {
                    if (rsCheck.next()) {
                        JavaApiServer.sendJsonResponse(exchange, 400,
                                "{\"status\":\"error\",\"message\":\"Esta reserva ya fue calificada previamente.\"}");
                        return;
                    }
                }
            }

            // 5. Validar estrellas y comentario
            int estrellas = 5;
            try { estrellas = Integer.parseInt(String.valueOf(params.getOrDefault("estrellas", "5"))); } catch (Exception ignored) {}
            if (estrellas < 1) estrellas = 1;
            if (estrellas > 5) estrellas = 5;

            String comentario = String.valueOf(params.getOrDefault("comentario", "")).trim();
            if (comentario.length() > 500) {
                comentario = comentario.substring(0, 500);
            }

            // 6. Insertar en Calificacion
            String sqlInsert = "INSERT INTO Calificacion (idUsuario, idAgencia, idReserva, estrellas, comentario, fechaCalificacion, eliminadoPorAdmin, estadoRevision) VALUES (?, ?, ?, ?, ?, NOW(), 0, 'Aprobada')";
            try (PreparedStatement psI = con.prepareStatement(sqlInsert)) {
                psI.setInt(1, resUserId);
                psI.setInt(2, idAgencia);
                psI.setInt(3, idReserva);
                psI.setInt(4, estrellas);
                psI.setString(5, comentario);
                psI.executeUpdate();
            }

            // 7. Recalcular promedio y total del TOUR
            double nuevoPromedioTour = estrellas;
            int nuevoTotalTour = 1;
            if (idTour > 0) {
                String sqlTourRecalc = """
                    SELECT ROUND(AVG(c.estrellas), 1) AS prom, COUNT(*) AS cnt
                    FROM Calificacion c
                    JOIN Reserva r ON c.idReserva = r.idReserva
                    WHERE r.idTour = ? AND (c.eliminadoPorAdmin IS NULL OR c.eliminadoPorAdmin = 0)
                """;
                try (PreparedStatement psTR = con.prepareStatement(sqlTourRecalc)) {
                    psTR.setInt(1, idTour);
                    try (ResultSet rsTR = psTR.executeQuery()) {
                        if (rsTR.next() && rsTR.getInt("cnt") > 0) {
                            nuevoPromedioTour = rsTR.getDouble("prom");
                            nuevoTotalTour = rsTR.getInt("cnt");
                        }
                    }
                }
                try (PreparedStatement psUT = con.prepareStatement("UPDATE Tour SET calificacionPromedio = ? WHERE idTour = ?")) {
                    psUT.setBigDecimal(1, BigDecimal.valueOf(nuevoPromedioTour));
                    psUT.setInt(2, idTour);
                    psUT.executeUpdate();
                }
            }

            // 8. Recalcular promedio y total de la AGENCIA
            double nuevoPromedioAgencia = estrellas;
            int nuevoTotalAgencia = 1;
            if (idAgencia > 0) {
                String sqlAgRecalc = """
                    SELECT ROUND(AVG(c.estrellas), 1) AS prom, COUNT(*) AS cnt
                    FROM Calificacion c
                    WHERE c.idAgencia = ? AND (c.eliminadoPorAdmin IS NULL OR c.eliminadoPorAdmin = 0)
                """;
                try (PreparedStatement psAR = con.prepareStatement(sqlAgRecalc)) {
                    psAR.setInt(1, idAgencia);
                    try (ResultSet rsAR = psAR.executeQuery()) {
                        if (rsAR.next() && rsAR.getInt("cnt") > 0) {
                            nuevoPromedioAgencia = rsAR.getDouble("prom");
                            nuevoTotalAgencia = rsAR.getInt("cnt");
                        }
                    }
                }
                try (PreparedStatement psUA = con.prepareStatement("UPDATE Agencia SET promedioCalificacion = ? WHERE idAgencia = ?")) {
                    psUA.setBigDecimal(1, BigDecimal.valueOf(nuevoPromedioAgencia));
                    psUA.setInt(2, idAgencia);
                    psUA.executeUpdate();
                }
            }

            String respJson = String.format(Locale.US,
                    "{\"status\":\"success\",\"message\":\"Calificación registrada exitosamente\",\"promedio\":%.1f,\"total\":%d,\"promedioTour\":%.1f,\"totalTour\":%d,\"promedioAgencia\":%.1f,\"totalAgencia\":%d}",
                    nuevoPromedioTour, nuevoTotalTour, nuevoPromedioTour, nuevoTotalTour, nuevoPromedioAgencia, nuevoTotalAgencia);
            JavaApiServer.sendJsonResponse(exchange, 200, respJson);

        } catch (Exception e) {
            e.printStackTrace();
            JavaApiServer.sendJsonResponse(exchange, 500, "{\"status\":\"error\",\"message\":\"Error al guardar calificación: " + JavaApiServer.jsonEscape(e.getMessage()) + "\"}");
        }
    }
}
