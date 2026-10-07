package com.travelink.controlador.agencia;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.io.File;
import java.io.OutputStream;
import java.nio.charset.StandardCharsets;
import com.travelink.entidades.Usuario;

import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import java.io.IOException;
import java.util.Map;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.Statement;
import com.travelink.config.ConexionDB;
import com.travelink.server.JavaApiServer;
import java.util.Locale;
import java.util.ArrayList;
import java.util.List;
import com.travelink.controlador.UsuarioControlador;
import com.travelink.controlador.CalificacionControlador;
import com.travelink.repositorio.ReservaRepositorio;

    public class DisponibilidadAgenciaHandler implements HttpHandler {

        @Override
        public void handle(HttpExchange exchange) throws IOException {

            // CORS
            if ("OPTIONS".equalsIgnoreCase(exchange.getRequestMethod())) {
                JavaApiServer.enableCORS(exchange);
                exchange.sendResponseHeaders(200, -1);
                return;
            }

            JavaApiServer.enableCORS(exchange);

            exchange.getResponseHeaders().add(
                    "Content-Type",
                    "application/json; charset=UTF-8"
            );

            try {
                if ("GET".equalsIgnoreCase(exchange.getRequestMethod())) {

                    Map<String, String> qParams = JavaApiServer.parseQueryParams(exchange.getRequestURI().getQuery());
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
                                            .append("\"fecha\":\"").append(JavaApiServer.jsonEscape(rs.getString("fecha"))).append("\",")
                                            .append("\"horaInicio\":\"").append(JavaApiServer.jsonEscape(rs.getString("horaInicio"))).append("\",")
                                            .append("\"horaFin\":\"").append(JavaApiServer.jsonEscape(rs.getString("horaFin"))).append("\",")
                                            .append("\"cupoTotal\":").append(rs.getInt("cupoTotal")).append(",")
                                            .append("\"cupoDisponible\":").append(rs.getInt("cupoDisponible")).append(",")
                                            .append("\"estado\":\"").append(JavaApiServer.jsonEscape(rs.getString("estado"))).append("\",")
                                            .append("\"idServicio\":").append(rs.getInt("idServicio")).append(",")
                                            .append("\"servicio\":\"").append(JavaApiServer.jsonEscape(rs.getString("servicio"))).append("\"")
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
                                                .append("\"servicio\":\"").append(JavaApiServer.jsonEscape(rsF.getString("servicio"))).append("\"")
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
                    JavaApiServer.sendJsonResponse(exchange, 200, json.toString());
                    return;
                }


                if ("POST".equalsIgnoreCase(exchange.getRequestMethod())) {

                    System.out.println("========== POST DISPONIBILIDAD ==========");

                    String body = new String(
                            exchange.getRequestBody().readAllBytes(),
                            StandardCharsets.UTF_8
                    );

                    System.out.println("BODY: " + body);

                    String action = JavaApiServer.obtenerParametro(body, "action").toLowerCase();
                    if ("cerrar_ventas".equals(action) || "cerrar".equals(action)) {
                        int idDisp = 0;
                        try { idDisp = Integer.parseInt(JavaApiServer.obtenerParametro(body, "idDisponibilidad")); } catch (Exception ignored) {}
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
                        try { idDisp = Integer.parseInt(JavaApiServer.obtenerParametro(body, "idDisponibilidad")); } catch (Exception ignored) {}
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
                        try { idDisp = Integer.parseInt(JavaApiServer.obtenerParametro(body, "idDisponibilidad")); } catch (Exception ignored) {}
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

                    String fecha = JavaApiServer.obtenerParametro(body, "fecha");
                    String horaInicio = JavaApiServer.obtenerParametro(body, "horaInicio");
                    String horaFin = JavaApiServer.obtenerParametro(body, "horaFin");
                    String cupoTotalTexto = JavaApiServer.obtenerParametro(body, "cupoTotal");
                    String idServicioTexto = JavaApiServer.obtenerParametro(body, "idServicio");
                    String idDisponibilidadTexto =
                            JavaApiServer.obtenerParametro(body, "idDisponibilidad");

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

                JavaApiServer.sendJsonResponse(exchange, 500,
                        "{\"status\":\"error\",\"message\":\""
                                + JavaApiServer.jsonEscape(e.getMessage()) + "\"}");
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

