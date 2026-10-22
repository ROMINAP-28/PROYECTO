package com.travelink.controlador.agencia;

import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import com.travelink.config.ConexionDB;
import com.travelink.persistencia.TourPersistencia;
import com.travelink.server.JavaApiServer;

import java.io.IOException;
import java.math.BigDecimal;
import java.sql.*;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.*;

public class PaquetesAgenciaHandler implements HttpHandler {

    @Override
    public void handle(HttpExchange exchange) throws IOException {
        JavaApiServer.enableCORS(exchange);

        if ("OPTIONS".equalsIgnoreCase(exchange.getRequestMethod())) {
            exchange.sendResponseHeaders(200, -1);
            return;
        }

        if ("GET".equalsIgnoreCase(exchange.getRequestMethod())) {
            listarPaquetes(exchange);
            return;
        }

        if ("POST".equalsIgnoreCase(exchange.getRequestMethod())) {
            procesarPostPaquete(exchange);
            return;
        }

        JavaApiServer.sendJsonResponse(exchange, 405, "{\"status\":\"error\",\"message\":\"Método no permitido\"}");
    }

    private void listarPaquetes(HttpExchange exchange) throws IOException {
        int idAgencia = 0;
        Object sessAgencia = exchange.getAttribute("ID_AGENCIA");
        if (sessAgencia instanceof Integer) {
            idAgencia = (Integer) sessAgencia;
        }

        Map<String, String> query = JavaApiServer.parseQueryParams(exchange.getRequestURI().getQuery());
        if (idAgencia <= 0) {
            try {
                if (query.containsKey("idAgencia")) {
                    idAgencia = Integer.parseInt(query.get("idAgencia"));
                }
            } catch (Exception ignored) {}
        }

        boolean isDashboard = "true".equalsIgnoreCase(query.getOrDefault("dashboard", "false"));

        StringBuilder json = new StringBuilder("{\"status\":\"success\",\"paquetes\":[");
        boolean primero = true;

        StringBuilder sql = new StringBuilder("""
            SELECT t.idTour, t.idAgencia, t.idDestino, t.slug, t.nombre, t.descripcion, t.descripcionCorta,
                   t.itinerario, t.precioAdulto, t.precioNino, t.precioBebe, t.aceptaNinos, t.edadMaxNino,
                   t.edadMaxBebe, t.descuento, t.condiciones, t.minAsientosOferta, t.duracion, t.ubicacion,
                   t.categoria, t.calificacionPromedio, t.estado, t.aceptaBebes, t.queIncluye, t.queNoIncluye,
                   t.dias, t.horas,
                   d.nombre AS destinoNombre,
                   COALESCE(NULLIF(a.nombreComercial, ''), a.razonSocial, 'Agencia Travelink') AS agenciaNombre,
                   COALESCE(
                       (SELECT ROUND(AVG(c.estrellas), 1) FROM Calificacion c JOIN Reserva r ON c.idReserva = r.idReserva WHERE r.idTour = t.idTour AND (c.eliminadoPorAdmin IS NULL OR c.eliminadoPorAdmin = 0)),
                       t.calificacionPromedio,
                       (SELECT ROUND(AVG(c.estrellas), 1) FROM Calificacion c WHERE c.idAgencia = t.idAgencia AND (c.eliminadoPorAdmin IS NULL OR c.eliminadoPorAdmin = 0)),
                       0.0
                   ) AS califProm,
                   COALESCE(
                       (SELECT COUNT(*) FROM Calificacion c JOIN Reserva r ON c.idReserva = r.idReserva WHERE r.idTour = t.idTour AND (c.eliminadoPorAdmin IS NULL OR c.eliminadoPorAdmin = 0)),
                       (SELECT COUNT(*) FROM Calificacion c WHERE c.idAgencia = t.idAgencia AND (c.eliminadoPorAdmin IS NULL OR c.eliminadoPorAdmin = 0)),
                       0
                   ) AS totalCalif,
                   (SELECT COUNT(*) FROM Reserva r WHERE r.idTour = t.idTour) AS totalReservas
            FROM Tour t
            LEFT JOIN Agencia a ON t.idAgencia = a.idAgencia
            LEFT JOIN Destino d ON t.idDestino = d.idDestino
            WHERE 1=1
        """);

        if (idAgencia > 0) {
            sql.append(" AND t.idAgencia = ? ");
        } else if (!isDashboard) {
            sql.append(" AND (t.estado = 'PUBLICADO' OR t.estado = 'ACTIVO') ");
        }
        sql.append(" ORDER BY t.idTour DESC");

        try (Connection con = ConexionDB.getConnection();
             PreparedStatement ps = con.prepareStatement(sql.toString())) {

            if (idAgencia > 0) {
                ps.setInt(1, idAgencia);
            }

            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    if (!primero) json.append(",");
                    primero = false;

                    int idTour = rs.getInt("idTour");
                    int idAg = rs.getInt("idAgencia");
                    int idDestino = rs.getInt("idDestino");
                    String nombre = rs.getString("nombre") != null ? rs.getString("nombre") : "";
                    String descripcion = rs.getString("descripcion") != null ? rs.getString("descripcion") : "";
                    String descCorta = rs.getString("descripcionCorta") != null ? rs.getString("descripcionCorta") : "";
                    String itinerario = rs.getString("itinerario") != null ? rs.getString("itinerario") : "";
                    BigDecimal precioAdulto = rs.getBigDecimal("precioAdulto");
                    if (precioAdulto == null) precioAdulto = BigDecimal.ZERO;
                    BigDecimal precioNino = rs.getBigDecimal("precioNino");
                    if (precioNino == null) precioNino = BigDecimal.ZERO;
                    BigDecimal precioBebe = rs.getBigDecimal("precioBebe");
                    if (precioBebe == null) precioBebe = BigDecimal.ZERO;

                    boolean aceptaNinos = rs.getBoolean("aceptaNinos");
                    int edadMaxNino = rs.getInt("edadMaxNino");
                    int edadMaxBebe = rs.getInt("edadMaxBebe");
                    boolean aceptaBebes = rs.getBoolean("aceptaBebes");
                    BigDecimal descuento = rs.getBigDecimal("descuento");
                    if (descuento == null) descuento = BigDecimal.ZERO;
                    String condiciones = rs.getString("condiciones") != null ? rs.getString("condiciones") : "";
                    int minAsientosOferta = rs.getInt("minAsientosOferta");
                    String duracion = rs.getString("duracion") != null ? rs.getString("duracion") : "";
                    String ubicacion = rs.getString("ubicacion") != null ? rs.getString("ubicacion") : "";
                    String destino = rs.getString("destinoNombre") != null ? rs.getString("destinoNombre") : ubicacion;
                    String categoria = rs.getString("categoria") != null ? rs.getString("categoria") : "Cultura";
                    String estado = rs.getString("estado") != null ? rs.getString("estado") : "PUBLICADO";
                    String queIncluye = rs.getString("queIncluye") != null ? rs.getString("queIncluye") : "";
                    String queNoIncluye = rs.getString("queNoIncluye") != null ? rs.getString("queNoIncluye") : "";
                    int dias = rs.getInt("dias");
                    int horas = rs.getInt("horas");
                    String agenciaNombre = rs.getString("agenciaNombre");
                    double califProm = rs.getDouble("califProm");
                    int totalCalif = rs.getInt("totalCalif");
                    int totalReservas = rs.getInt("totalReservas");

                    // Imagen principal y galería desde TourImagen
                    String imagenPrincipal = "img/machupicchu.png";
                    StringBuilder galeriaSb = new StringBuilder("[");
                    boolean primImg = true;
                    try (PreparedStatement psImg = con.prepareStatement(
                            "SELECT url, esPrincipal FROM TourImagen WHERE idTour = ? ORDER BY esPrincipal DESC, orden ASC, idImagen ASC")) {
                        psImg.setInt(1, idTour);
                        try (ResultSet rsImg = psImg.executeQuery()) {
                            while (rsImg.next()) {
                                String u = rsImg.getString("url");
                                if (u != null && !u.isBlank()) {
                                    if (rsImg.getBoolean("esPrincipal")) {
                                        imagenPrincipal = u;
                                    }
                                    if (!primImg) galeriaSb.append(",");
                                    primImg = false;
                                    galeriaSb.append("\"").append(JavaApiServer.jsonEscape(u)).append("\"");
                                }
                            }
                        }
                    }
                    galeriaSb.append("]");

                    // Salidas y cupos desde TourFecha
                    int cupoTotal = 0;
                    int cuposDisponibles = 0;
                    int totalSalidasFuturas = 0;
                    StringBuilder salidasSb = new StringBuilder("[");
                    boolean primSalida = true;
                    try (PreparedStatement psF = con.prepareStatement(
                            "SELECT tf.idTourFecha, tf.fecha, tf.horaInicio, tf.cupoTotal, tf.cupoDisponible, tf.estado, " +
                            "       (SELECT COUNT(*) FROM Reserva r WHERE r.idTourFecha = tf.idTourFecha) AS reservasCount " +
                            "FROM TourFecha tf WHERE tf.idTour = ? AND UPPER(tf.estado) != 'CANCELADO' " +
                            "ORDER BY tf.fecha ASC, tf.horaInicio ASC")) {
                        psF.setInt(1, idTour);
                        try (ResultSet rsF = psF.executeQuery()) {
                            while (rsF.next()) {
                                int cTot = rsF.getInt("cupoTotal");
                                int cDisp = rsF.getInt("cupoDisponible");
                                String fechaStr = String.valueOf(rsF.getDate("fecha"));
                                String stSalida = rsF.getString("estado") != null ? rsF.getString("estado") : "DISPONIBLE";
                                int resCount = rsF.getInt("reservasCount");

                                cupoTotal += cTot;
                                cuposDisponibles += cDisp;

                                try {
                                    if (LocalDate.parse(fechaStr).compareTo(LocalDate.now()) >= 0 && !"PAUSADO".equalsIgnoreCase(stSalida)) {
                                        totalSalidasFuturas++;
                                    }
                                } catch (Exception ignored) {}

                                if (!primSalida) salidasSb.append(",");
                                primSalida = false;
                                salidasSb.append("{")
                                        .append("\"idTourFecha\":").append(rsF.getInt("idTourFecha")).append(",")
                                        .append("\"fecha\":\"").append(fechaStr).append("\",")
                                        .append("\"horaInicio\":\"").append(rsF.getString("horaInicio")).append("\",")
                                        .append("\"cupoTotal\":").append(cTot).append(",")
                                        .append("\"cupoDisponible\":").append(cDisp).append(",")
                                        .append("\"reservasCount\":").append(resCount).append(",")
                                        .append("\"estado\":\"").append(JavaApiServer.jsonEscape(stSalida)).append("\"")
                                        .append("}");
                            }
                        }
                    }
                    salidasSb.append("]");

                    json.append("{")
                            .append("\"idTour\":").append(idTour).append(",")
                            .append("\"idPaquete\":").append(idTour).append(",")
                            .append("\"idAgencia\":").append(idAg).append(",")
                            .append("\"agencia\":\"").append(JavaApiServer.jsonEscape(agenciaNombre)).append("\",")
                            .append("\"nombre\":\"").append(JavaApiServer.jsonEscape(nombre)).append("\",")
                            .append("\"descripcion\":\"").append(JavaApiServer.jsonEscape(descripcion)).append("\",")
                            .append("\"descripcionCorta\":\"").append(JavaApiServer.jsonEscape(descCorta)).append("\",")
                            .append("\"itinerario\":\"").append(JavaApiServer.jsonEscape(itinerario)).append("\",")
                            .append("\"idDestino\":").append(idDestino).append(",")
                            .append("\"destino\":\"").append(JavaApiServer.jsonEscape(destino)).append("\",")
                            .append("\"ubicacion\":\"").append(JavaApiServer.jsonEscape(ubicacion)).append("\",")
                            .append("\"categoria\":\"").append(JavaApiServer.jsonEscape(categoria)).append("\",")
                            .append("\"calificacionPromedio\":").append(califProm).append(",")
                            .append("\"totalCalificaciones\":").append(totalCalif).append(",")
                            .append("\"precio\":").append(precioAdulto).append(",")
                            .append("\"precioAdulto\":").append(precioAdulto).append(",")
                            .append("\"precioNino\":").append(precioNino).append(",")
                            .append("\"precioBebe\":").append(precioBebe).append(",")
                            .append("\"aceptaNinos\":").append(aceptaNinos).append(",")
                            .append("\"edadMaxNino\":").append(edadMaxNino).append(",")
                            .append("\"edadMaxBebe\":").append(edadMaxBebe).append(",")
                            .append("\"aceptaBebes\":").append(aceptaBebes).append(",")
                            .append("\"duracion\":\"").append(JavaApiServer.jsonEscape(duracion)).append("\",")
                            .append("\"dias\":").append(dias).append(",")
                            .append("\"horas\":").append(horas).append(",")
                            .append("\"condiciones\":\"").append(JavaApiServer.jsonEscape(condiciones)).append("\",")
                            .append("\"queIncluye\":\"").append(JavaApiServer.jsonEscape(queIncluye)).append("\",")
                            .append("\"queNoIncluye\":\"").append(JavaApiServer.jsonEscape(queNoIncluye)).append("\",")
                            .append("\"estado\":\"").append(JavaApiServer.jsonEscape(estado)).append("\",")
                            .append("\"descuento\":").append(descuento).append(",")
                            .append("\"imagen\":\"").append(JavaApiServer.jsonEscape(imagenPrincipal)).append("\",")
                            .append("\"imagenPrincipal\":\"").append(JavaApiServer.jsonEscape(imagenPrincipal)).append("\",")
                            .append("\"cuposDisponibles\":").append(cuposDisponibles).append(",")
                            .append("\"cupoTotal\":").append(cupoTotal).append(",")
                            .append("\"totalSalidasFuturas\":").append(totalSalidasFuturas).append(",")
                            .append("\"tieneReservas\":").append(totalReservas > 0).append(",")
                            .append("\"minAsientosOferta\":").append(minAsientosOferta).append(",")
                            .append("\"galeria\":").append(galeriaSb).append(",")
                            .append("\"salidas\":").append(salidasSb)
                            .append("}");
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
            JavaApiServer.sendJsonResponse(exchange, 500, "{\"status\":\"error\",\"message\":\"" + JavaApiServer.jsonEscape(e.getMessage()) + "\"}");
            return;
        }

        json.append("]}");
        JavaApiServer.sendJsonResponse(exchange, 200, json.toString());
    }

    private void procesarPostPaquete(HttpExchange exchange) throws IOException {
        String body = JavaApiServer.readRequestBody(exchange);
        Map<String, Object> params = JavaApiServer.parseJsonOrFormParams(body);
        String action = String.valueOf(params.getOrDefault("action", "guardar")).trim().toLowerCase();

        if ("publicar".equals(action)) {
            cambiarEstado(exchange, params, "PUBLICADO");
            return;
        }
        if ("pausar".equals(action)) {
            cambiarEstado(exchange, params, "PAUSADO");
            return;
        }
        if ("borrador".equals(action)) {
            cambiarEstado(exchange, params, "BORRADOR");
            return;
        }
        if ("eliminar".equals(action)) {
            eliminarPaquete(exchange, params);
            return;
        }
        if ("cambiar_estado_salida".equals(action)) {
            cambiarEstadoSalida(exchange, params);
            return;
        }
        if ("eliminar_salida".equals(action)) {
            eliminarSalida(exchange, params);
            return;
        }

        guardarPaquete(exchange, params);
    }

    private int resolverIdAgencia(HttpExchange exchange, Map<String, Object> params) {
        Object sessAgencia = exchange.getAttribute("ID_AGENCIA");
        if (sessAgencia instanceof Integer && (Integer) sessAgencia > 0) {
            return (Integer) sessAgencia;
        }
        try {
            int pId = Integer.parseInt(String.valueOf(params.getOrDefault("idAgencia", "0")));
            if (pId > 0) return pId;
        } catch (Exception ignored) {}
        return 1;
    }

    private void cambiarEstado(HttpExchange exchange, Map<String, Object> params, String nuevoEstado) throws IOException {
        int idTour = 0;
        try {
            idTour = Integer.parseInt(String.valueOf(params.getOrDefault("idTour", params.getOrDefault("idPaquete", "0"))));
        } catch (Exception ignored) {}
        int idAgencia = resolverIdAgencia(exchange, params);

        if (idTour <= 0) {
            JavaApiServer.sendJsonResponse(exchange, 400, "{\"status\":\"error\",\"message\":\"ID de paquete inválido\"}");
            return;
        }

        // Verificar propiedad
        try (Connection conOwner = ConexionDB.getConnection();
             PreparedStatement psOwner = conOwner.prepareStatement("SELECT idAgencia FROM Tour WHERE idTour = ?")) {
            psOwner.setInt(1, idTour);
            try (ResultSet rsOwner = psOwner.executeQuery()) {
                if (rsOwner.next() && rsOwner.getInt("idAgencia") != idAgencia) {
                    JavaApiServer.sendJsonResponse(exchange, 403, "{\"status\":\"error\",\"message\":\"Acceso denegado: este paquete pertenece a otra agencia\"}");
                    return;
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        // Regla (h): Un tour sin al menos una salida futura no puede publicarse
        if ("PUBLICADO".equalsIgnoreCase(nuevoEstado)) {
            try (Connection con = ConexionDB.getConnection();
                 PreparedStatement psCheck = con.prepareStatement(
                         "SELECT COUNT(*) FROM TourFecha WHERE idTour = ? AND fecha >= CURDATE() AND UPPER(estado) != 'CANCELADO' AND UPPER(estado) != 'PAUSADO'")) {
                psCheck.setInt(1, idTour);
                try (ResultSet rs = psCheck.executeQuery()) {
                    if (rs.next() && rs.getInt(1) == 0) {
                        JavaApiServer.sendJsonResponse(exchange, 400, "{\"status\":\"error\",\"message\":\"Un tour sin al menos una salida futura no puede publicarse. Agrega al menos una salida futura activa.\"}");
                        return;
                    }
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        String sql = "UPDATE Tour SET estado = ? WHERE idTour = ? AND idAgencia = ?";
        try (Connection con = ConexionDB.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setString(1, nuevoEstado);
            ps.setInt(2, idTour);
            ps.setInt(3, idAgencia);
            int filas = ps.executeUpdate();
            if (filas > 0) {
                JavaApiServer.sendJsonResponse(exchange, 200, "{\"status\":\"success\",\"message\":\"Estado del paquete actualizado a " + nuevoEstado + "\"}");
            } else {
                JavaApiServer.sendJsonResponse(exchange, 404, "{\"status\":\"error\",\"message\":\"Paquete no encontrado\"}");
            }
        } catch (Exception e) {
            e.printStackTrace();
            JavaApiServer.sendJsonResponse(exchange, 500, "{\"status\":\"error\",\"message\":\"Error al cambiar estado: " + JavaApiServer.jsonEscape(e.getMessage()) + "\"}");
        }
    }

    private void eliminarPaquete(HttpExchange exchange, Map<String, Object> params) throws IOException {
        int idTour = 0;
        try {
            idTour = Integer.parseInt(String.valueOf(params.getOrDefault("idTour", params.getOrDefault("idPaquete", "0"))));
        } catch (Exception ignored) {}
        int idAgencia = resolverIdAgencia(exchange, params);

        if (idTour <= 0) {
            JavaApiServer.sendJsonResponse(exchange, 400, "{\"status\":\"error\",\"message\":\"ID de tour inválido\"}");
            return;
        }

        // Verificar propiedad
        try (Connection conOwner = ConexionDB.getConnection();
             PreparedStatement psOwner = conOwner.prepareStatement("SELECT idAgencia FROM Tour WHERE idTour = ?")) {
            psOwner.setInt(1, idTour);
            try (ResultSet rsOwner = psOwner.executeQuery()) {
                if (rsOwner.next() && rsOwner.getInt("idAgencia") != idAgencia) {
                    JavaApiServer.sendJsonResponse(exchange, 403, "{\"status\":\"error\",\"message\":\"Acceso denegado: este paquete pertenece a otra agencia\"}");
                    return;
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        try (Connection con = ConexionDB.getConnection()) {
            con.setAutoCommit(false);
            try {
                // Verificar si tiene reservas asociadas
                boolean tieneReservas = false;
                try (PreparedStatement psCheck = con.prepareStatement("SELECT COUNT(*) FROM Reserva WHERE idTour = ?")) {
                    psCheck.setInt(1, idTour);
                    try (ResultSet rsCheck = psCheck.executeQuery()) {
                        if (rsCheck.next() && rsCheck.getInt(1) > 0) {
                            tieneReservas = true;
                        }
                    }
                }

                if (tieneReservas) {
                    // Si tiene reservas, no se elimina: se pausa
                    try (PreparedStatement psPause = con.prepareStatement("UPDATE Tour SET estado = 'PAUSADO' WHERE idTour = ? AND idAgencia = ?")) {
                        psPause.setInt(1, idTour);
                        psPause.setInt(2, idAgencia);
                        psPause.executeUpdate();
                    }
                    con.commit();
                    JavaApiServer.sendJsonResponse(exchange, 200, "{\"status\":\"success\",\"message\":\"El tour tiene reservas registradas, por lo que se ha pausado en lugar de eliminarse para preservar el historial.\"}");
                    return;
                }

                try (PreparedStatement psImg = con.prepareStatement("DELETE FROM TourImagen WHERE idTour = ?")) {
                    psImg.setInt(1, idTour);
                    psImg.executeUpdate();
                }
                try (PreparedStatement psF = con.prepareStatement("DELETE FROM TourFecha WHERE idTour = ?")) {
                    psF.setInt(1, idTour);
                    psF.executeUpdate();
                }
                try (PreparedStatement psT = con.prepareStatement("DELETE FROM Tour WHERE idTour = ? AND idAgencia = ?")) {
                    psT.setInt(1, idTour);
                    psT.setInt(2, idAgencia);
                    psT.executeUpdate();
                }

                con.commit();
                JavaApiServer.sendJsonResponse(exchange, 200, "{\"status\":\"success\",\"message\":\"Paquete eliminado exitosamente\"}");
            } catch (Exception e) {
                con.rollback();
                throw e;
            } finally {
                con.setAutoCommit(true);
            }
        } catch (Exception e) {
            e.printStackTrace();
            JavaApiServer.sendJsonResponse(exchange, 500, "{\"status\":\"error\",\"message\":\"Error al eliminar paquete: " + JavaApiServer.jsonEscape(e.getMessage()) + "\"}");
        }
    }

    private void cambiarEstadoSalida(HttpExchange exchange, Map<String, Object> params) throws IOException {
        int idTourFecha = 0;
        try { idTourFecha = Integer.parseInt(String.valueOf(params.getOrDefault("idTourFecha", "0"))); } catch (Exception ignored) {}
        String nuevoEstado = String.valueOf(params.getOrDefault("estado", "PAUSADO")).trim().toUpperCase();
        int idAgencia = resolverIdAgencia(exchange, params);

        if (idTourFecha <= 0) {
            JavaApiServer.sendJsonResponse(exchange, 400, "{\"status\":\"error\",\"message\":\"ID de salida inválido\"}");
            return;
        }

        // Verificar propiedad a través del tour
        try (Connection con = ConexionDB.getConnection()) {
            try (PreparedStatement psCheck = con.prepareStatement(
                    "SELECT t.idAgencia FROM TourFecha tf JOIN Tour t ON tf.idTour = t.idTour WHERE tf.idTourFecha = ?")) {
                psCheck.setInt(1, idTourFecha);
                try (ResultSet rs = psCheck.executeQuery()) {
                    if (rs.next()) {
                        if (rs.getInt("idAgencia") != idAgencia) {
                            JavaApiServer.sendJsonResponse(exchange, 403, "{\"status\":\"error\",\"message\":\"Acceso denegado: salida pertenece a otra agencia\"}");
                            return;
                        }
                    } else {
                        JavaApiServer.sendJsonResponse(exchange, 404, "{\"status\":\"error\",\"message\":\"Salida no encontrada\"}");
                        return;
                    }
                }
            }

            try (PreparedStatement psUp = con.prepareStatement("UPDATE TourFecha SET estado = ? WHERE idTourFecha = ?")) {
                psUp.setString(1, nuevoEstado);
                psUp.setInt(2, idTourFecha);
                psUp.executeUpdate();
            }

            JavaApiServer.sendJsonResponse(exchange, 200, "{\"status\":\"success\",\"message\":\"Estado de la salida actualizado a " + nuevoEstado + "\"}");
        } catch (Exception e) {
            e.printStackTrace();
            JavaApiServer.sendJsonResponse(exchange, 500, "{\"status\":\"error\",\"message\":\"" + JavaApiServer.jsonEscape(e.getMessage()) + "\"}");
        }
    }

    private void eliminarSalida(HttpExchange exchange, Map<String, Object> params) throws IOException {
        int idTourFecha = 0;
        try { idTourFecha = Integer.parseInt(String.valueOf(params.getOrDefault("idTourFecha", "0"))); } catch (Exception ignored) {}
        int idAgencia = resolverIdAgencia(exchange, params);

        if (idTourFecha <= 0) {
            JavaApiServer.sendJsonResponse(exchange, 400, "{\"status\":\"error\",\"message\":\"ID de salida inválido\"}");
            return;
        }

        try (Connection con = ConexionDB.getConnection()) {
            try (PreparedStatement psCheck = con.prepareStatement(
                    "SELECT t.idAgencia FROM TourFecha tf JOIN Tour t ON tf.idTour = t.idTour WHERE tf.idTourFecha = ?")) {
                psCheck.setInt(1, idTourFecha);
                try (ResultSet rs = psCheck.executeQuery()) {
                    if (rs.next()) {
                        if (rs.getInt("idAgencia") != idAgencia) {
                            JavaApiServer.sendJsonResponse(exchange, 403, "{\"status\":\"error\",\"message\":\"Acceso denegado: salida pertenece a otra agencia\"}");
                            return;
                        }
                    } else {
                        JavaApiServer.sendJsonResponse(exchange, 404, "{\"status\":\"error\",\"message\":\"Salida no encontrada\"}");
                        return;
                    }
                }
            }

            // Regla: "una salida con reservas no se elimina, se pausa."
            int reservasCount = 0;
            try (PreparedStatement psRes = con.prepareStatement("SELECT COUNT(*) FROM Reserva WHERE idTourFecha = ?")) {
                psRes.setInt(1, idTourFecha);
                try (ResultSet rsRes = psRes.executeQuery()) {
                    if (rsRes.next()) reservasCount = rsRes.getInt(1);
                }
            }

            if (reservasCount > 0) {
                try (PreparedStatement psPause = con.prepareStatement("UPDATE TourFecha SET estado = 'PAUSADO' WHERE idTourFecha = ?")) {
                    psPause.setInt(1, idTourFecha);
                    psPause.executeUpdate();
                }
                JavaApiServer.sendJsonResponse(exchange, 200, "{\"status\":\"success\",\"message\":\"La salida tiene reservas registradas, por lo que se ha pausado en lugar de eliminarse.\"}");
                return;
            }

            try (PreparedStatement psDel = con.prepareStatement("DELETE FROM TourFecha WHERE idTourFecha = ?")) {
                psDel.setInt(1, idTourFecha);
                psDel.executeUpdate();
            }

            JavaApiServer.sendJsonResponse(exchange, 200, "{\"status\":\"success\",\"message\":\"Salida eliminada exitosamente\"}");
        } catch (Exception e) {
            e.printStackTrace();
            JavaApiServer.sendJsonResponse(exchange, 500, "{\"status\":\"error\",\"message\":\"" + JavaApiServer.jsonEscape(e.getMessage()) + "\"}");
        }
    }

    private void guardarPaquete(HttpExchange exchange, Map<String, Object> params) throws IOException {
        int idTour = 0;
        try {
            idTour = Integer.parseInt(String.valueOf(params.getOrDefault("idTour", params.getOrDefault("idPaquete", "0"))));
        } catch (Exception ignored) {}
        int idAgencia = resolverIdAgencia(exchange, params);

        // a) Nombre, destino y categoría
        String nombre = String.valueOf(params.getOrDefault("nombre", "")).trim();
        int idDestino = 1;
        try { idDestino = Integer.parseInt(String.valueOf(params.getOrDefault("idDestino", "1"))); } catch (Exception ignored) {}
        String categoria = String.valueOf(params.getOrDefault("categoria", "Cultura")).trim();

        // b) Descripción corta y completa
        String descripcionCorta = String.valueOf(params.getOrDefault("descripcionCorta", "")).trim();
        String descripcion = String.valueOf(params.getOrDefault("descripcion", "")).trim();

        // c) Duración (días y horas) e itinerario
        int dias = 0;
        try { dias = Integer.parseInt(String.valueOf(params.getOrDefault("dias", "0")).trim()); } catch (Exception ignored) {}
        int horas = 0;
        try { horas = Integer.parseInt(String.valueOf(params.getOrDefault("horas", "0")).trim()); } catch (Exception ignored) {}
        String itinerario = String.valueOf(params.getOrDefault("itinerario", "")).trim();

        // d) Precios
        BigDecimal precioAdulto = BigDecimal.ZERO;
        try { precioAdulto = new BigDecimal(String.valueOf(params.getOrDefault("precioAdulto", params.getOrDefault("precio", "0"))).trim()); } catch (Exception ignored) {}

        boolean aceptaNinos = false;
        if (params.containsKey("aceptaNinos")) {
            aceptaNinos = "true".equalsIgnoreCase(String.valueOf(params.get("aceptaNinos"))) || "1".equals(String.valueOf(params.get("aceptaNinos")));
        }
        BigDecimal precioNino = BigDecimal.ZERO;
        try { precioNino = new BigDecimal(String.valueOf(params.getOrDefault("precioNino", "0")).trim()); } catch (Exception ignored) {}
        int edadMaxNino = 11;
        try { edadMaxNino = Integer.parseInt(String.valueOf(params.getOrDefault("edadMaxNino", "11")).trim()); } catch (Exception ignored) {}

        boolean aceptaBebes = false;
        if (params.containsKey("aceptaBebes")) {
            aceptaBebes = "true".equalsIgnoreCase(String.valueOf(params.get("aceptaBebes"))) || "1".equals(String.valueOf(params.get("aceptaBebes")));
        }
        BigDecimal precioBebe = BigDecimal.ZERO;
        try { precioBebe = new BigDecimal(String.valueOf(params.getOrDefault("precioBebe", "0")).trim()); } catch (Exception ignored) {}
        int edadMaxBebe = 2;
        try { edadMaxBebe = Integer.parseInt(String.valueOf(params.getOrDefault("edadMaxBebe", "2")).trim()); } catch (Exception ignored) {}

        // e) Qué incluye / no incluye, condiciones y descuento
        String queIncluye = String.valueOf(params.getOrDefault("queIncluye", "")).trim();
        String queNoIncluye = String.valueOf(params.getOrDefault("queNoIncluye", "")).trim();
        String condiciones = String.valueOf(params.getOrDefault("condiciones", "")).trim();
        BigDecimal descuento = BigDecimal.ZERO;
        try { descuento = new BigDecimal(String.valueOf(params.getOrDefault("descuento", "0")).trim()); } catch (Exception ignored) {}
        int minAsientosOferta = 5;
        try { minAsientosOferta = Integer.parseInt(String.valueOf(params.getOrDefault("minAsientosOferta", "5")).trim()); } catch (Exception ignored) {}

        // h) Estado
        String estado = String.valueOf(params.getOrDefault("estado", "PUBLICADO")).trim().toUpperCase();
        if (!"PUBLICADO".equalsIgnoreCase(estado) && !"PAUSADO".equalsIgnoreCase(estado) && !"BORRADOR".equalsIgnoreCase(estado)) {
            estado = "PUBLICADO";
        }

        // VALIDACIONES OBLIGATORIAS
        if (nombre.isEmpty()) {
            JavaApiServer.sendJsonResponse(exchange, 400, "{\"status\":\"error\",\"message\":\"El nombre del paquete es obligatorio\"}");
            return;
        }
        if (idDestino <= 0) {
            JavaApiServer.sendJsonResponse(exchange, 400, "{\"status\":\"error\",\"message\":\"Debes seleccionar un destino válido\"}");
            return;
        }
        if (precioAdulto.compareTo(BigDecimal.ZERO) <= 0) {
            JavaApiServer.sendJsonResponse(exchange, 400, "{\"status\":\"error\",\"message\":\"El precio de adulto debe ser mayor a 0\"}");
            return;
        }
        if (aceptaNinos) {
            if (edadMaxNino <= 0) {
                JavaApiServer.sendJsonResponse(exchange, 400, "{\"status\":\"error\",\"message\":\"La edad máxima para niños debe ser mayor a 0\"}");
                return;
            }
            if (precioNino.compareTo(BigDecimal.ZERO) < 0) {
                JavaApiServer.sendJsonResponse(exchange, 400, "{\"status\":\"error\",\"message\":\"El precio de niños no puede ser negativo\"}");
                return;
            }
        } else {
            precioNino = BigDecimal.ZERO;
        }

        if (aceptaBebes) {
            if (edadMaxBebe < 0) {
                JavaApiServer.sendJsonResponse(exchange, 400, "{\"status\":\"error\",\"message\":\"La edad máxima para bebés no puede ser negativa\"}");
                return;
            }
            if (precioBebe.compareTo(BigDecimal.ZERO) < 0) {
                JavaApiServer.sendJsonResponse(exchange, 400, "{\"status\":\"error\",\"message\":\"El precio de bebés no puede ser negativo (usa 0 para gratis)\"}");
                return;
            }
        } else {
            precioBebe = BigDecimal.ZERO;
        }

        if (aceptaNinos && aceptaBebes && edadMaxNino <= edadMaxBebe) {
            JavaApiServer.sendJsonResponse(exchange, 400, "{\"status\":\"error\",\"message\":\"La edad máxima de niños (" + edadMaxNino + ") debe ser mayor a la edad máxima de bebés (" + edadMaxBebe + ")\"}");
            return;
        }

        if (dias < 0 || horas < 0 || (dias == 0 && horas == 0)) {
            dias = 1; // Default consistente
        }
        String duracion = TourPersistencia.generarDuracion(dias, horas);

        if (descuento.compareTo(BigDecimal.ZERO) < 0 || descuento.compareTo(new BigDecimal("90")) > 0) {
            JavaApiServer.sendJsonResponse(exchange, 400, "{\"status\":\"error\",\"message\":\"El descuento debe estar entre 0% y 90%\"}");
            return;
        }

        // Parsear salidas recibidas
        List<Map<String, Object>> salidasRecibidas = new ArrayList<>();
        Object rawSalidas = params.get("salidas");
        if (rawSalidas instanceof List) {
            for (Object obj : (List<?>) rawSalidas) {
                if (obj instanceof Map) {
                    @SuppressWarnings("unchecked")
                    Map<String, Object> item = (Map<String, Object>) obj;
                    salidasRecibidas.add(item);
                }
            }
        } else if (rawSalidas instanceof String && !((String) rawSalidas).trim().isEmpty()) {
            try {
                com.fasterxml.jackson.databind.ObjectMapper mapper = new com.fasterxml.jackson.databind.ObjectMapper();
                salidasRecibidas = mapper.readValue((String) rawSalidas, new com.fasterxml.jackson.core.type.TypeReference<List<Map<String, Object>>>() {});
            } catch (Exception ignored) {}
        }

        // Si se editó paquete existente, verificar propiedad
        if (idTour > 0) {
            try (Connection conOwner = ConexionDB.getConnection();
                 PreparedStatement psOwner = conOwner.prepareStatement("SELECT idAgencia FROM Tour WHERE idTour = ?")) {
                psOwner.setInt(1, idTour);
                try (ResultSet rsOwner = psOwner.executeQuery()) {
                    if (rsOwner.next() && rsOwner.getInt("idAgencia") != idAgencia) {
                        JavaApiServer.sendJsonResponse(exchange, 403, "{\"status\":\"error\",\"message\":\"Acceso denegado: este paquete pertenece a otra agencia\"}");
                        return;
                    }
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        // Validar salidas antes de escribir en BD
        LocalDate hoy = LocalDate.now();
        int salidasFuturasActivas = 0;
        for (Map<String, Object> s : salidasRecibidas) {
            String fStr = String.valueOf(s.getOrDefault("fecha", "")).trim();
            int cupoTot = 0;
            try { cupoTot = Integer.parseInt(String.valueOf(s.getOrDefault("cupoTotal", "0"))); } catch (Exception ignored) {}
            if (fStr.isEmpty()) {
                JavaApiServer.sendJsonResponse(exchange, 400, "{\"status\":\"error\",\"message\":\"Todas las salidas deben incluir una fecha válida\"}");
                return;
            }
            try {
                LocalDate ldf = LocalDate.parse(fStr);
                if (ldf.isBefore(hoy)) {
                    JavaApiServer.sendJsonResponse(exchange, 400, "{\"status\":\"error\",\"message\":\"No se permiten salidas con fechas pasadas (" + fStr + ")\"}");
                    return;
                }
            } catch (Exception ex) {
                JavaApiServer.sendJsonResponse(exchange, 400, "{\"status\":\"error\",\"message\":\"Formato de fecha inválido en salidas: " + fStr + "\"}");
                return;
            }
            if (cupoTot <= 0) {
                JavaApiServer.sendJsonResponse(exchange, 400, "{\"status\":\"error\",\"message\":\"El cupo total de cada salida debe ser mayor a 0\"}");
                return;
            }

            String stSalida = String.valueOf(s.getOrDefault("estado", "DISPONIBLE")).trim();
            if (!"PAUSADO".equalsIgnoreCase(stSalida) && !"CANCELADO".equalsIgnoreCase(stSalida)) {
                salidasFuturasActivas++;
            }
        }

        // Regla (h): Un tour sin al menos una salida futura no puede publicarse
        if ("PUBLICADO".equalsIgnoreCase(estado)) {
            if (salidasFuturasActivas == 0 && idTour <= 0) {
                JavaApiServer.sendJsonResponse(exchange, 400, "{\"status\":\"error\",\"message\":\"Un tour sin al menos una salida futura no puede publicarse. Por favor programa al menos una salida futura activa.\"}");
                return;
            }
        }

        try (Connection con = ConexionDB.getConnection()) {
            con.setAutoCommit(false);
            try {
                String ubicacion = TourPersistencia.obtenerNombreDestino(con, idDestino);

                if (idTour > 0) {
                    String sqlUp = """
                        UPDATE Tour SET idAgencia=?, idDestino=?, nombre=?, descripcion=?, descripcionCorta=?,
                                        itinerario=?, aceptaNinos=?, edadMaxNino=?, edadMaxBebe=?, descuento=?,
                                        condiciones=?, minAsientosOferta=?, precioAdulto=?, precioNino=?,
                                        precioBebe=?, duracion=?, ubicacion=?, categoria=?, estado=?,
                                        aceptaBebes=?, queIncluye=?, queNoIncluye=?, dias=?, horas=?
                        WHERE idTour=? AND idAgencia=?
                    """;
                    try (PreparedStatement ps = con.prepareStatement(sqlUp)) {
                        ps.setInt(1, idAgencia);
                        ps.setInt(2, idDestino);
                        ps.setString(3, nombre);
                        ps.setString(4, descripcion);
                        ps.setString(5, descripcionCorta);
                        ps.setString(6, itinerario);
                        ps.setBoolean(7, aceptaNinos);
                        ps.setInt(8, edadMaxNino);
                        ps.setInt(9, edadMaxBebe);
                        ps.setBigDecimal(10, descuento);
                        ps.setString(11, condiciones);
                        ps.setInt(12, minAsientosOferta);
                        ps.setBigDecimal(13, precioAdulto);
                        ps.setBigDecimal(14, precioNino);
                        ps.setBigDecimal(15, precioBebe);
                        ps.setString(16, duracion);
                        ps.setString(17, ubicacion);
                        ps.setString(18, categoria);
                        ps.setString(19, estado);
                        ps.setBoolean(20, aceptaBebes);
                        ps.setString(21, queIncluye);
                        ps.setString(22, queNoIncluye);
                        ps.setInt(23, dias);
                        ps.setInt(24, horas);
                        ps.setInt(25, idTour);
                        ps.setInt(26, idAgencia);
                        ps.executeUpdate();
                    }
                } else {
                    String slug = TourPersistencia.generarSlug(nombre) + "-" + (System.currentTimeMillis() % 10000);
                    String sqlIns = """
                        INSERT INTO Tour (idAgencia, idDestino, slug, nombre, descripcion, descripcionCorta,
                                          itinerario, aceptaNinos, edadMaxNino, edadMaxBebe, descuento,
                                          condiciones, minAsientosOferta, precioAdulto, precioNino,
                                          precioBebe, duracion, ubicacion, categoria, estado,
                                          aceptaBebes, queIncluye, queNoIncluye, dias, horas)
                        VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)
                    """;
                    try (PreparedStatement ps = con.prepareStatement(sqlIns, Statement.RETURN_GENERATED_KEYS)) {
                        ps.setInt(1, idAgencia);
                        ps.setInt(2, idDestino);
                        ps.setString(3, slug);
                        ps.setString(4, nombre);
                        ps.setString(5, descripcion);
                        ps.setString(6, descripcionCorta);
                        ps.setString(7, itinerario);
                        ps.setBoolean(8, aceptaNinos);
                        ps.setInt(9, edadMaxNino);
                        ps.setInt(10, edadMaxBebe);
                        ps.setBigDecimal(11, descuento);
                        ps.setString(12, condiciones);
                        ps.setInt(13, minAsientosOferta);
                        ps.setBigDecimal(14, precioAdulto);
                        ps.setBigDecimal(15, precioNino);
                        ps.setBigDecimal(16, precioBebe);
                        ps.setString(17, duracion);
                        ps.setString(18, ubicacion);
                        ps.setString(19, categoria);
                        ps.setString(20, estado);
                        ps.setBoolean(21, aceptaBebes);
                        ps.setString(22, queIncluye);
                        ps.setString(23, queNoIncluye);
                        ps.setInt(24, dias);
                        ps.setInt(25, horas);
                        ps.executeUpdate();
                        try (ResultSet rsK = ps.getGeneratedKeys()) {
                            if (rsK.next()) idTour = rsK.getInt(1);
                        }
                    }
                }

                // f) Procesar imágenes (TourImagen)
                String imagenPortada = String.valueOf(params.getOrDefault("imagen", params.getOrDefault("imagenPrincipal", ""))).trim();
                List<String> galeria = new ArrayList<>();
                Object rawGaleria = params.get("galeria");
                if (rawGaleria instanceof List) {
                    for (Object g : (List<?>) rawGaleria) {
                        if (g != null && !String.valueOf(g).trim().isEmpty()) galeria.add(String.valueOf(g).trim());
                    }
                } else {
                    String g1 = String.valueOf(params.getOrDefault("imgGaleria1", "")).trim();
                    String g2 = String.valueOf(params.getOrDefault("imgGaleria2", "")).trim();
                    if (!g1.isEmpty()) galeria.add(g1);
                    if (!g2.isEmpty()) galeria.add(g2);
                }

                if (!imagenPortada.isEmpty() || !galeria.isEmpty()) {
                    try (PreparedStatement psDelImg = con.prepareStatement("DELETE FROM TourImagen WHERE idTour = ?")) {
                        psDelImg.setInt(1, idTour);
                        psDelImg.executeUpdate();
                    }
                    if (!imagenPortada.isEmpty()) {
                        try (PreparedStatement psInsPort = con.prepareStatement(
                                "INSERT INTO TourImagen (idTour, url, esPrincipal, orden) VALUES (?, ?, 1, 0)")) {
                            psInsPort.setInt(1, idTour);
                            psInsPort.setString(2, imagenPortada);
                            psInsPort.executeUpdate();
                        }
                    }
                    int orden = 1;
                    for (String gUrl : galeria) {
                        try (PreparedStatement psInsGal = con.prepareStatement(
                                "INSERT INTO TourImagen (idTour, url, esPrincipal, orden) VALUES (?, ?, 0, ?)")) {
                            psInsGal.setInt(1, idTour);
                            psInsGal.setString(2, gUrl);
                            psInsGal.setInt(3, orden++);
                            psInsGal.executeUpdate();
                        }
                    }
                }

                // g) Procesar salidas (TourFecha)
                if (!salidasRecibidas.isEmpty()) {
                    Set<Integer> idsConservados = new HashSet<>();
                    for (Map<String, Object> s : salidasRecibidas) {
                        int idTF = 0;
                        try { idTF = Integer.parseInt(String.valueOf(s.getOrDefault("idTourFecha", "0"))); } catch (Exception ignored) {}
                        String fStr = String.valueOf(s.getOrDefault("fecha", "")).trim();
                        String hStr = String.valueOf(s.getOrDefault("horaInicio", "08:00")).trim();
                        if (hStr.length() == 5) hStr += ":00";
                        int cTot = Integer.parseInt(String.valueOf(s.getOrDefault("cupoTotal", "30")));
                        String stSalida = String.valueOf(s.getOrDefault("estado", "DISPONIBLE")).trim();

                        if (idTF > 0) {
                            // Actualizar salida existente
                            String sqlUpF = "UPDATE TourFecha SET fecha = ?, horaInicio = ?, cupoTotal = ?, estado = ? WHERE idTourFecha = ? AND idTour = ?";
                            try (PreparedStatement psUpF = con.prepareStatement(sqlUpF)) {
                                psUpF.setString(1, fStr);
                                psUpF.setString(2, hStr);
                                psUpF.setInt(3, cTot);
                                psUpF.setString(4, stSalida);
                                psUpF.setInt(5, idTF);
                                psUpF.setInt(6, idTour);
                                psUpF.executeUpdate();
                            }
                            idsConservados.add(idTF);
                        } else {
                            // Insertar nueva salida
                            String sqlInsF = "INSERT INTO TourFecha (idTour, fecha, horaInicio, cupoTotal, cupoDisponible, estado) VALUES (?, ?, ?, ?, ?, ?)";
                            try (PreparedStatement psInsF = con.prepareStatement(sqlInsF, Statement.RETURN_GENERATED_KEYS)) {
                                psInsF.setInt(1, idTour);
                                psInsF.setString(2, fStr);
                                psInsF.setString(3, hStr);
                                psInsF.setInt(4, cTot);
                                psInsF.setInt(5, cTot); // cupoDisponible inicia igual a cupoTotal
                                psInsF.setString(6, stSalida);
                                psInsF.executeUpdate();
                                try (ResultSet rsK = psInsF.getGeneratedKeys()) {
                                    if (rsK.next()) idsConservados.add(rsK.getInt(1));
                                }
                            }
                        }
                    }

                    // Tratar salidas que no vinieron en la lista (eliminadas del cliente)
                    try (PreparedStatement psOld = con.prepareStatement(
                            "SELECT idTourFecha FROM TourFecha WHERE idTour = ?")) {
                        psOld.setInt(1, idTour);
                        try (ResultSet rsOld = psOld.executeQuery()) {
                            while (rsOld.next()) {
                                int oldId = rsOld.getInt("idTourFecha");
                                if (!idsConservados.contains(oldId)) {
                                    // Comprobar si tiene reservas
                                    boolean hasReservas = false;
                                    try (PreparedStatement psChkR = con.prepareStatement(
                                            "SELECT COUNT(*) FROM Reserva WHERE idTourFecha = ?")) {
                                        psChkR.setInt(1, oldId);
                                        try (ResultSet rsChkR = psChkR.executeQuery()) {
                                            if (rsChkR.next() && rsChkR.getInt(1) > 0) hasReservas = true;
                                        }
                                    }
                                    if (hasReservas) {
                                        try (PreparedStatement psPaus = con.prepareStatement(
                                                "UPDATE TourFecha SET estado = 'PAUSADO' WHERE idTourFecha = ?")) {
                                            psPaus.setInt(1, oldId);
                                            psPaus.executeUpdate();
                                        }
                                    } else {
                                        try (PreparedStatement psDelF = con.prepareStatement(
                                                "DELETE FROM TourFecha WHERE idTourFecha = ?")) {
                                            psDelF.setInt(1, oldId);
                                            psDelF.executeUpdate();
                                        }
                                    }
                                }
                            }
                        }
                    }
                }

                // Validación final de publicación (h):
                if ("PUBLICADO".equalsIgnoreCase(estado)) {
                    int futurasFinal = 0;
                    try (PreparedStatement psChkFut = con.prepareStatement(
                            "SELECT COUNT(*) FROM TourFecha WHERE idTour = ? AND fecha >= CURDATE() AND UPPER(estado) != 'CANCELADO' AND UPPER(estado) != 'PAUSADO'")) {
                        psChkFut.setInt(1, idTour);
                        try (ResultSet rsFut = psChkFut.executeQuery()) {
                            if (rsFut.next()) futurasFinal = rsFut.getInt(1);
                        }
                    }
                    if (futurasFinal == 0) {
                        con.rollback();
                        JavaApiServer.sendJsonResponse(exchange, 400, "{\"status\":\"error\",\"message\":\"Un tour sin al menos una salida futura no puede publicarse. Por favor agrega o activa al menos una salida futura.\"}");
                        return;
                    }
                }

                con.commit();
                JavaApiServer.sendJsonResponse(exchange, 200, "{\"status\":\"success\",\"message\":\"Paquete turístico guardado exitosamente\",\"idTour\":" + idTour + ",\"idPaquete\":" + idTour + "}");
            } catch (Exception e) {
                con.rollback();
                throw e;
            } finally {
                con.setAutoCommit(true);
            }
        } catch (Exception e) {
            e.printStackTrace();
            JavaApiServer.sendJsonResponse(exchange, 500, "{\"status\":\"error\",\"message\":\"Error al guardar paquete: " + JavaApiServer.jsonEscape(e.getMessage()) + "\"}");
        }
    }
}
