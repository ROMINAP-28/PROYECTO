package com.travelink.controlador.turista;

import java.io.IOException;
import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.Map;

import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import com.travelink.config.ConexionDB;
import com.travelink.server.JavaApiServer;

public class EliminarResenaHandler implements HttpHandler {

    @Override
    public void handle(HttpExchange exchange) throws IOException {
        JavaApiServer.enableCORS(exchange);
        if ("OPTIONS".equalsIgnoreCase(exchange.getRequestMethod())) {
            exchange.sendResponseHeaders(204, -1);
            return;
        }

        Map<String, String> params = JavaApiServer.parseQueryParams(exchange.getRequestURI().getQuery());
        int id = 0;
        try { id = Integer.parseInt(params.getOrDefault("id", "0")); } catch (Exception ignored) {}

        if (id > 0) {
            try (Connection con = ConexionDB.getConnection()) {
                int idAgencia = 0;
                int idTour = 0;
                try (PreparedStatement psA = con.prepareStatement(
                        "SELECT c.idAgencia, COALESCE(r.idTour, 0) FROM Calificacion c LEFT JOIN Reserva r ON c.idReserva = r.idReserva WHERE c.idCalificacion = ?")) {
                    psA.setInt(1, id);
                    try (ResultSet rsA = psA.executeQuery()) {
                        if (rsA.next()) {
                            idAgencia = rsA.getInt(1);
                            idTour = rsA.getInt(2);
                        }
                    }
                }

                try (PreparedStatement ps = con.prepareStatement("UPDATE Calificacion SET eliminadoPorAdmin = 1 WHERE idCalificacion = ?")) {
                    ps.setInt(1, id);
                    ps.executeUpdate();
                }

                // Recalcular promedio de la agencia
                if (idAgencia > 0) {
                    double nuevoPromedioAgencia = 0.0;
                    try (PreparedStatement psC = con.prepareStatement(
                            "SELECT ROUND(COALESCE(AVG(estrellas), 0.0), 1) AS prom FROM Calificacion WHERE idAgencia = ? AND (eliminadoPorAdmin IS NULL OR eliminadoPorAdmin = 0)")) {
                        psC.setInt(1, idAgencia);
                        try (ResultSet rsC = psC.executeQuery()) {
                            if (rsC.next()) nuevoPromedioAgencia = rsC.getDouble("prom");
                        }
                    }
                    try (PreparedStatement psUA = con.prepareStatement("UPDATE Agencia SET promedioCalificacion = ? WHERE idAgencia = ?")) {
                        psUA.setBigDecimal(1, BigDecimal.valueOf(nuevoPromedioAgencia));
                        psUA.setInt(2, idAgencia);
                        psUA.executeUpdate();
                    }
                }

                // Recalcular promedio del tour
                if (idTour > 0) {
                    double nuevoPromedioTour = 0.0;
                    try (PreparedStatement psT = con.prepareStatement(
                            "SELECT ROUND(COALESCE(AVG(c.estrellas), 0.0), 1) AS prom FROM Calificacion c JOIN Reserva r ON c.idReserva = r.idReserva WHERE r.idTour = ? AND (c.eliminadoPorAdmin IS NULL OR c.eliminadoPorAdmin = 0)")) {
                        psT.setInt(1, idTour);
                        try (ResultSet rsT = psT.executeQuery()) {
                            if (rsT.next()) nuevoPromedioTour = rsT.getDouble("prom");
                        }
                    }
                    try (PreparedStatement psUT = con.prepareStatement("UPDATE Tour SET calificacionPromedio = ? WHERE idTour = ?")) {
                        psUT.setBigDecimal(1, BigDecimal.valueOf(nuevoPromedioTour));
                        psUT.setInt(2, idTour);
                        psUT.executeUpdate();
                    }
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        JavaApiServer.sendJsonResponse(exchange, 200, "{\"status\":\"success\",\"message\":\"Reseña eliminada correctamente.\"}");
    }
}
