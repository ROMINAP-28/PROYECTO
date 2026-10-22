package com.travelink.controlador.admin;

import java.io.IOException;
import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import com.travelink.config.ConexionDB;
import com.travelink.server.JavaApiServer;

/**
 * TRAVELINK - Handlers Públicos y de Consulta de Tours
 * FASE 6: Catálogo dinámico para Turistas (Index, Experiencias, Ofertas, Detalle)
 */
public class ToursAdminHandler implements HttpHandler {

    @Override
    public void handle(HttpExchange exchange) throws IOException {
        JavaApiServer.enableCORS(exchange);
        if ("OPTIONS".equalsIgnoreCase(exchange.getRequestMethod())) {
            exchange.sendResponseHeaders(204, -1);
            return;
        }

        if ("POST".equalsIgnoreCase(exchange.getRequestMethod())) {
            String body = JavaApiServer.readRequestBody(exchange);
            Map<String, Object> params = JavaApiServer.parseJsonOrFormParams(body);
            int id = 0;
            try {
                if (params.containsKey("id")) id = Integer.parseInt(String.valueOf(params.get("id")));
                else if (params.containsKey("idTour")) id = Integer.parseInt(String.valueOf(params.get("idTour")));
            } catch (Exception ignored) {}

            String nuevoEstado = String.valueOf(params.getOrDefault("estado", "")).toUpperCase().trim();
            if (id > 0 && !nuevoEstado.isEmpty()) {
                try (Connection con = ConexionDB.getConnection();
                     PreparedStatement ps = con.prepareStatement("UPDATE Tour SET estado = ? WHERE idTour = ?")) {
                    ps.setString(1, nuevoEstado);
                    ps.setInt(2, id);
                    ps.executeUpdate();
                    JavaApiServer.sendJsonResponse(exchange, 200, "{\"status\":\"success\",\"message\":\"Tour actualizado a " + nuevoEstado + "\"}");
                    return;
                } catch (Exception e) {
                    e.printStackTrace();
                    JavaApiServer.sendJsonResponse(exchange, 500, "{\"status\":\"error\",\"message\":\"Error al actualizar tour: " + JavaApiServer.jsonEscape(e.getMessage()) + "\"}");
                    return;
                }
            }
        }

        Map<String, String> qParams = JavaApiServer.parseQueryParams(exchange.getRequestURI().getQuery());
        int idTour = 0;
        if (qParams.containsKey("id")) {
            try { idTour = Integer.parseInt(qParams.get("id")); } catch (Exception ignored) {}
        } else if (qParams.containsKey("idTour")) {
            try { idTour = Integer.parseInt(qParams.get("idTour")); } catch (Exception ignored) {}
        } else if (qParams.containsKey("tour")) {
            try { idTour = Integer.parseInt(qParams.get("tour")); } catch (Exception ignored) {}
        } else if (qParams.containsKey("idPaquete")) {
            try { idTour = Integer.parseInt(qParams.get("idPaquete")); } catch (Exception ignored) {}
        }

        if (idTour <= 0) {
            String reqPath = exchange.getRequestURI().getPath();
            if (reqPath != null && reqPath.matches(".*/api/tours/\\d+/?$")) {
                String cleanPath = reqPath.endsWith("/") ? reqPath.substring(0, reqPath.length() - 1) : reqPath;
                String[] parts = cleanPath.split("/");
                try {
                    idTour = Integer.parseInt(parts[parts.length - 1]);
                } catch (Exception ignored) {}
            }
        }

        // Si se solicita un tour específico -> vista de detalle
        if (idTour > 0) {
            retornarDetalleTour(exchange, idTour, qParams);
            return;
        }

        // Listar tours (para Index, Experiencias, Ofertas)
        listarTours(exchange, qParams);
    }

    private void listarTours(HttpExchange exchange, Map<String, String> qParams) throws IOException {
        boolean esAdmin = "true".equalsIgnoreCase(qParams.getOrDefault("admin", "false"));
        boolean esOfertas = "true".equalsIgnoreCase(qParams.getOrDefault("ofertas", "false"));
        int idAgencia = 0;
        try { idAgencia = Integer.parseInt(qParams.getOrDefault("idAgencia", "0")); } catch (Exception ignored) {}

        String destino = qParams.getOrDefault("destino", "").trim();
        String categoria = qParams.getOrDefault("categoria", "").trim();
        String query = qParams.getOrDefault("q", qParams.getOrDefault("busqueda", "")).trim().toLowerCase();

        BigDecimal precioMax = null;
        if (qParams.containsKey("precioMax") && !qParams.get("precioMax").isBlank()) {
            try { precioMax = new BigDecimal(qParams.get("precioMax")); } catch (Exception ignored) {}
        }

        BigDecimal califMin = null;
        if (qParams.containsKey("calificacionMin") && !qParams.get("calificacionMin").isBlank()) {
            try { califMin = new BigDecimal(qParams.get("calificacionMin")); } catch (Exception ignored) {}
        }

        StringBuilder sql = new StringBuilder("""
            SELECT t.idTour, t.idAgencia, t.idDestino, t.slug, t.nombre, t.descripcionCorta,
                   t.precioAdulto, t.precioNino, t.precioBebe, t.aceptaNinos, t.edadMaxNino,
                   t.edadMaxBebe, t.descuento, t.minAsientosOferta, t.duracion, t.ubicacion,
                   t.categoria, t.estado, t.aceptaBebes, t.dias, t.horas,
                   COALESCE(NULLIF(a.nombreComercial, ''), a.razonSocial, 'Agencia Travelink') AS agenciaNombre,
                   d.nombre AS destinoNombre,
                   COALESCE(ti.url, 'img/machupicchu.png') AS imagenPrincipal,
                   COALESCE(t.calificacionPromedio, a.promedioCalificacion, 0.0) AS califProm,
                   COALESCE((SELECT COUNT(*) FROM Calificacion c WHERE c.idAgencia = t.idAgencia AND (c.eliminadoPorAdmin IS NULL OR c.eliminadoPorAdmin = 0)), 0) AS totalCalif,
                   COALESCE(tfAgg.cuposMinimos, 0) AS cuposMinimos,
                   COALESCE(tfAgg.cupoTotalMax, 0) AS cupoTotalMax,
                   COALESCE(tfAgg.totalSalidasFuturas, 0) AS totalSalidasFuturas,
                   tfAgg.proximaFecha
            FROM Tour t
            JOIN Agencia a ON t.idAgencia = a.idAgencia
            LEFT JOIN Destino d ON t.idDestino = d.idDestino
            LEFT JOIN TourImagen ti ON t.idTour = ti.idTour AND ti.esPrincipal = 1
            LEFT JOIN (
                SELECT idTour,
                       MIN(cupoDisponible) AS cuposMinimos,
                       MAX(cupoTotal) AS cupoTotalMax,
                       COUNT(*) AS totalSalidasFuturas,
                       MIN(fecha) AS proximaFecha
                FROM TourFecha
                WHERE fecha >= CURDATE() AND UPPER(estado) NOT IN ('CANCELADO', 'PAUSADO')
                GROUP BY idTour
            ) tfAgg ON t.idTour = tfAgg.idTour
            WHERE 1=1
        """);

        List<Object> params = new ArrayList<>();

        if (!esAdmin) {
            // Regla 4: Todo desde Tour publicado con salidas futuras
            sql.append(" AND (UPPER(t.estado) = 'PUBLICADO' OR UPPER(t.estado) = 'ACTIVO') ");
            sql.append(" AND EXISTS (SELECT 1 FROM TourFecha tf WHERE tf.idTour = t.idTour AND tf.fecha >= CURDATE() AND UPPER(tf.estado) != 'CANCELADO' AND UPPER(tf.estado) != 'PAUSADO') ");
        }

        if (esOfertas) {
            // Regla 5: Salidas de TourFecha con cupoDisponible <= minAsientosOferta del tour O descuento > 0
            sql.append("""
                AND (
                    (t.descuento IS NOT NULL AND t.descuento > 0)
                    OR EXISTS (
                        SELECT 1 FROM TourFecha tf
                        WHERE tf.idTour = t.idTour
                          AND tf.fecha >= CURDATE()
                          AND UPPER(tf.estado) != 'CANCELADO'
                          AND UPPER(tf.estado) != 'PAUSADO'
                          AND tf.cupoDisponible <= COALESCE(t.minAsientosOferta, 5)
                    )
                )
            """);
        }

        if (idAgencia > 0) {
            sql.append(" AND t.idAgencia = ? ");
            params.add(idAgencia);
        }

        if (!destino.isEmpty()) {
            sql.append(" AND (LOWER(d.nombre) LIKE ? OR LOWER(t.ubicacion) LIKE ?) ");
            params.add("%" + destino.toLowerCase() + "%");
            params.add("%" + destino.toLowerCase() + "%");
        }

        if (!categoria.isEmpty()) {
            sql.append(" AND LOWER(t.categoria) LIKE ? ");
            params.add("%" + categoria.toLowerCase() + "%");
        }

        if (precioMax != null && precioMax.compareTo(BigDecimal.ZERO) > 0) {
            sql.append(" AND t.precioAdulto <= ? ");
            params.add(precioMax);
        }

        if (califMin != null && califMin.compareTo(BigDecimal.ZERO) > 0) {
            sql.append(" AND (SELECT AVG(c.estrellas) FROM Calificacion c WHERE c.idAgencia = t.idAgencia AND (c.eliminadoPorAdmin IS NULL OR c.eliminadoPorAdmin = 0)) >= ? ");
            params.add(califMin.doubleValue());
        }

        if (!query.isEmpty()) {
            sql.append(" AND (LOWER(t.nombre) LIKE ? OR LOWER(t.descripcion) LIKE ? OR LOWER(t.descripcionCorta) LIKE ? OR LOWER(a.nombreComercial) LIKE ? OR LOWER(a.razonSocial) LIKE ?) ");
            String qLike = "%" + query + "%";
            params.add(qLike);
            params.add(qLike);
            params.add(qLike);
            params.add(qLike);
            params.add(qLike);
        }

        sql.append(" ORDER BY t.idTour DESC ");

        StringBuilder json = new StringBuilder("{\"status\":\"success\",\"data\":[");
        boolean primero = true;

        try (Connection con = ConexionDB.getConnection();
             PreparedStatement ps = con.prepareStatement(sql.toString())) {

            for (int i = 0; i < params.size(); i++) {
                ps.setObject(i + 1, params.get(i));
            }

            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    if (!primero) json.append(",");
                    primero = false;

                    int id = rs.getInt("idTour");
                    String nombre = rs.getString("nombre");
                    String descCorta = rs.getString("descripcionCorta") != null ? rs.getString("descripcionCorta") : "";
                    String descCompleta = descCorta;
                    String agNombre = rs.getString("agenciaNombre");
                    String destNombre = rs.getString("destinoNombre") != null ? rs.getString("destinoNombre") : rs.getString("ubicacion");
                    String duracion = rs.getString("duracion") != null ? rs.getString("duracion") : "1 día";
                    BigDecimal pAdulto = rs.getBigDecimal("precioAdulto");
                    if (pAdulto == null) pAdulto = BigDecimal.ZERO;
                    BigDecimal pNino = rs.getBigDecimal("precioNino");
                    if (pNino == null) pNino = BigDecimal.ZERO;
                    BigDecimal pBebe = rs.getBigDecimal("precioBebe");
                    if (pBebe == null) pBebe = BigDecimal.ZERO;
                    BigDecimal desc = rs.getBigDecimal("descuento");
                    if (desc == null) desc = BigDecimal.ZERO;
                    double calif = rs.getDouble("califProm");
                    int totalC = rs.getInt("totalCalif");
                    String est = rs.getString("estado");
                    String cat = rs.getString("categoria") != null ? rs.getString("categoria") : "Cultural";
                    String img = rs.getString("imagenPrincipal");
                    int cuposMin = rs.getInt("cuposMinimos");
                    int cupoTotMax = rs.getInt("cupoTotalMax");
                    int totalSalidas = rs.getInt("totalSalidasFuturas");
                    int minOferta = rs.getInt("minAsientosOferta");
                    String proximaFecha = rs.getString("proximaFecha") != null ? rs.getString("proximaFecha") : "";

                    boolean esOfertaItem = desc.compareTo(BigDecimal.ZERO) > 0 || (cuposMin <= (minOferta > 0 ? minOferta : 5));

                    json.append("{")
                        .append("\"id\":").append(id).append(",")
                        .append("\"idTour\":").append(id).append(",")
                        .append("\"idPaquete\":").append(id).append(",")
                        .append("\"idAgencia\":").append(rs.getInt("idAgencia")).append(",")
                        .append("\"nombre\":\"").append(JavaApiServer.jsonEscape(nombre)).append("\",")
                        .append("\"descripcionCorta\":\"").append(JavaApiServer.jsonEscape(descCorta)).append("\",")
                        .append("\"descripcion\":\"").append(JavaApiServer.jsonEscape(descCompleta)).append("\",")
                        .append("\"agencia\":\"").append(JavaApiServer.jsonEscape(agNombre)).append("\",")
                        .append("\"destino\":\"").append(JavaApiServer.jsonEscape(destNombre != null ? destNombre : "Perú")).append("\",")
                        .append("\"ubicacion\":\"").append(JavaApiServer.jsonEscape(destNombre != null ? destNombre : "Perú")).append("\",")
                        .append("\"categoria\":\"").append(JavaApiServer.jsonEscape(cat)).append("\",")
                        .append("\"duracion\":\"").append(JavaApiServer.jsonEscape(duracion)).append("\",")
                        .append("\"precio\":").append(pAdulto).append(",")
                        .append("\"precioAdulto\":").append(pAdulto).append(",")
                        .append("\"precioNino\":").append(pNino).append(",")
                        .append("\"precioBebe\":").append(pBebe).append(",")
                        .append("\"aceptaNinos\":").append(rs.getBoolean("aceptaNinos")).append(",")
                        .append("\"aceptaBebes\":").append(rs.getBoolean("aceptaBebes")).append(",")
                        .append("\"edadMaxNino\":").append(rs.getInt("edadMaxNino")).append(",")
                        .append("\"edadMaxBebe\":").append(rs.getInt("edadMaxBebe")).append(",")
                        .append("\"descuento\":").append(desc).append(",")
                        .append("\"esOferta\":").append(esOfertaItem).append(",")
                        .append("\"minAsientosOferta\":").append(minOferta).append(",")
                        .append("\"calificacion\":").append(calif).append(",")
                        .append("\"calificacionPromedio\":").append(calif).append(",")
                        .append("\"totalCalificaciones\":").append(totalC).append(",")
                        .append("\"estado\":\"").append(JavaApiServer.jsonEscape(est)).append("\",")
                        .append("\"imagen\":\"").append(JavaApiServer.jsonEscape(img)).append("\",")
                        .append("\"imagenPrincipal\":\"").append(JavaApiServer.jsonEscape(img)).append("\",")
                        .append("\"cuposDisponibles\":").append(cuposMin).append(",")
                        .append("\"cupoTotal\":").append(cupoTotMax).append(",")
                        .append("\"totalSalidasFuturas\":").append(totalSalidas).append(",")
                        .append("\"proximaFecha\":\"").append(JavaApiServer.jsonEscape(proximaFecha)).append("\"")
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

    private void retornarDetalleTour(HttpExchange exchange, int idTour, Map<String, String> qParams) throws IOException {
        boolean esAdmin = "true".equalsIgnoreCase(qParams.getOrDefault("admin", "false"));

        String sqlTour = """
            SELECT t.idTour AS id, t.nombre, t.descripcion, t.descripcionCorta, t.itinerario,
                   t.precioAdulto, t.precioNino, t.precioBebe, t.aceptaNinos, t.edadMaxNino, t.edadMaxBebe,
                   t.descuento, t.condiciones, t.minAsientosOferta,
                   t.duracion, COALESCE(t.ubicacion, d.nombre, 'Perú') AS ubicacion,
                   COALESCE(t.categoria, 'Cultural') AS categoria,
                   COALESCE(
                       (SELECT ROUND(AVG(c.estrellas), 1) FROM Calificacion c JOIN Reserva r ON c.idReserva = r.idReserva WHERE r.idTour = t.idTour AND (c.eliminadoPorAdmin IS NULL OR c.eliminadoPorAdmin = 0)),
                       t.calificacionPromedio,
                       (SELECT ROUND(AVG(c.estrellas), 1) FROM Calificacion c WHERE c.idAgencia = a.idAgencia AND (c.eliminadoPorAdmin IS NULL OR c.eliminadoPorAdmin = 0)),
                       0.0
                   ) AS calificacionPromedio,
                   COALESCE(
                       (SELECT COUNT(*) FROM Calificacion c JOIN Reserva r ON c.idReserva = r.idReserva WHERE r.idTour = t.idTour AND (c.eliminadoPorAdmin IS NULL OR c.eliminadoPorAdmin = 0)),
                       (SELECT COUNT(*) FROM Calificacion c WHERE c.idAgencia = a.idAgencia AND (c.eliminadoPorAdmin IS NULL OR c.eliminadoPorAdmin = 0)),
                       0
                   ) AS totalCalificaciones,
                   t.estado, COALESCE(t.queIncluye, '') AS incluye, COALESCE(t.queNoIncluye, '') AS noIncluye,
                   t.aceptaBebes, a.idAgencia,
                   COALESCE(NULLIF(a.nombreComercial, ''), a.razonSocial, 'Agencia Travelink') AS agencia,
                   COALESCE((SELECT ROUND(AVG(c.estrellas), 1) FROM Calificacion c WHERE c.idAgencia = a.idAgencia AND (c.eliminadoPorAdmin IS NULL OR c.eliminadoPorAdmin = 0)), a.promedioCalificacion, 0.0) AS agenciaRating
            FROM Tour t
            JOIN Agencia a ON t.idAgencia = a.idAgencia
            LEFT JOIN Destino d ON t.idDestino = d.idDestino
            WHERE t.idTour = ?
            """;

        try (Connection con = ConexionDB.getConnection()) {
            boolean encontrado = false;
            StringBuilder json = new StringBuilder("{\"status\":\"success\",\"tour\":{");

            int idRealTour = idTour;
            int idAgencia = 1;
            String nombre = "";
            String descripcion = "";
            String descripcionCorta = "";
            String itinerario = "";
            BigDecimal pAdulto = BigDecimal.ZERO;
            BigDecimal pNino = BigDecimal.ZERO;
            BigDecimal pBebe = BigDecimal.ZERO;
            boolean aceptaNinos = true;
            int edadMaxNino = 11;
            int edadMaxBebe = 2;
            BigDecimal descuento = BigDecimal.ZERO;
            String condiciones = "";
            int minAsientosOferta = 5;
            String duracion = "1 día";
            String ubicacion = "Perú";
            String categoria = "Cultural";
            BigDecimal calif = BigDecimal.ZERO;
            String estado = "PUBLICADO";
            String incluye = "";
            String noIncluye = "";
            boolean aceptaBebes = true;
            String agencia = "";
            BigDecimal agenciaRating = BigDecimal.ZERO;
            int totalCalificaciones = 0;

            try (PreparedStatement ps = con.prepareStatement(sqlTour)) {
                ps.setInt(1, idTour);
                try (ResultSet rs = ps.executeQuery()) {
                    if (rs.next()) {
                        encontrado = true;
                        idRealTour = rs.getInt("id");
                        nombre = rs.getString("nombre");
                        descripcion = rs.getString("descripcion");
                        descripcionCorta = rs.getString("descripcionCorta");
                        itinerario = rs.getString("itinerario");
                        pAdulto = rs.getBigDecimal("precioAdulto");
                        pNino = rs.getBigDecimal("precioNino");
                        pBebe = rs.getBigDecimal("precioBebe");
                        aceptaNinos = rs.getBoolean("aceptaNinos");
                        edadMaxNino = rs.getInt("edadMaxNino");
                        edadMaxBebe = rs.getInt("edadMaxBebe");
                        descuento = rs.getBigDecimal("descuento");
                        condiciones = rs.getString("condiciones");
                        minAsientosOferta = rs.getInt("minAsientosOferta");
                        duracion = rs.getString("duracion");
                        ubicacion = rs.getString("ubicacion");
                        categoria = rs.getString("categoria");
                        calif = rs.getBigDecimal("calificacionPromedio");
                        estado = rs.getString("estado");
                        incluye = rs.getString("incluye");
                        noIncluye = rs.getString("noIncluye");
                        aceptaBebes = rs.getBoolean("aceptaBebes");
                        idAgencia = rs.getInt("idAgencia");
                        agencia = rs.getString("agencia");
                        agenciaRating = rs.getBigDecimal("agenciaRating");
                        totalCalificaciones = rs.getInt("totalCalificaciones");
                    }
                }
            }

            // Regla (1): Tour inexistente o pausado -> "Paquete no disponible"
            if (!encontrado) {
                JavaApiServer.sendJsonResponse(exchange, 404, "{\"status\":\"error\",\"code\":\"PAQUETE_NO_DISPONIBLE\",\"message\":\"Paquete no disponible\"}");
                return;
            }

            if (!esAdmin) {
                String estUp = (estado != null ? estado : "").toUpperCase();
                if (!"PUBLICADO".equals(estUp) && !"ACTIVO".equals(estUp)) {
                    JavaApiServer.sendJsonResponse(exchange, 404, "{\"status\":\"error\",\"code\":\"PAQUETE_NO_DISPONIBLE\",\"message\":\"Paquete no disponible\"}");
                    return;
                }
            }

            json.append("\"id\":").append(idRealTour).append(",")
                .append("\"idTour\":").append(idRealTour).append(",")
                .append("\"idPaquete\":").append(idRealTour).append(",")
                .append("\"nombre\":\"").append(JavaApiServer.jsonEscape(nombre != null ? nombre : "")).append("\",")
                .append("\"descripcion\":\"").append(JavaApiServer.jsonEscape(descripcion != null ? descripcion : "")).append("\",")
                .append("\"descripcionCorta\":\"").append(JavaApiServer.jsonEscape(descripcionCorta != null ? descripcionCorta : "")).append("\",")
                .append("\"itinerario\":\"").append(JavaApiServer.jsonEscape(itinerario != null ? itinerario : "")).append("\",")
                .append("\"aceptaNinos\":").append(aceptaNinos).append(",")
                .append("\"edadMaxNino\":").append(edadMaxNino).append(",")
                .append("\"edadMaxBebe\":").append(edadMaxBebe).append(",")
                .append("\"descuento\":").append(descuento != null ? descuento : 0).append(",")
                .append("\"condiciones\":\"").append(JavaApiServer.jsonEscape(condiciones != null ? condiciones : "")).append("\",")
                .append("\"minAsientosOferta\":").append(minAsientosOferta).append(",")
                .append("\"precioAdulto\":").append(pAdulto != null ? pAdulto : 0).append(",")
                .append("\"precio\":").append(pAdulto != null ? pAdulto : 0).append(",")
                .append("\"precioNino\":").append(pNino != null ? pNino : 0).append(",")
                .append("\"precioBebe\":").append(pBebe != null ? pBebe : 0).append(",")
                .append("\"duracion\":\"").append(JavaApiServer.jsonEscape(duracion != null ? duracion : "1 día")).append("\",")
                .append("\"ubicacion\":\"").append(JavaApiServer.jsonEscape(ubicacion != null ? ubicacion : "Perú")).append("\",")
                .append("\"destino\":\"").append(JavaApiServer.jsonEscape(ubicacion != null ? ubicacion : "Perú")).append("\",")
                .append("\"categoria\":\"").append(JavaApiServer.jsonEscape(categoria != null ? categoria : "Cultural")).append("\",")
                .append("\"calificacionPromedio\":").append(calif != null ? calif : 0.0).append(",")
                .append("\"totalCalificaciones\":").append(totalCalificaciones).append(",")
                .append("\"estado\":\"").append(JavaApiServer.jsonEscape(estado != null ? estado : "PUBLICADO")).append("\",")
                .append("\"incluye\":\"").append(JavaApiServer.jsonEscape(incluye != null ? incluye : "")).append("\",")
                .append("\"queIncluye\":\"").append(JavaApiServer.jsonEscape(incluye != null ? incluye : "")).append("\",")
                .append("\"noIncluye\":\"").append(JavaApiServer.jsonEscape(noIncluye != null ? noIncluye : "")).append("\",")
                .append("\"queNoIncluye\":\"").append(JavaApiServer.jsonEscape(noIncluye != null ? noIncluye : "")).append("\",")
                .append("\"aceptaBebes\":").append(aceptaBebes).append(",")
                .append("\"idAgencia\":").append(idAgencia).append(",")
                .append("\"agencia\":\"").append(JavaApiServer.jsonEscape(agencia)).append("\",")
                .append("\"agenciaRating\":").append(agenciaRating != null ? agenciaRating : 0.0).append(",");

            // Galería de imágenes (TourImagen)
            List<String> imagenes = new ArrayList<>();
            String sqlImg = "SELECT url, esPrincipal FROM TourImagen WHERE idTour = ? ORDER BY esPrincipal DESC, orden ASC, idImagen ASC";
            try (PreparedStatement psI = con.prepareStatement(sqlImg)) {
                psI.setInt(1, idRealTour);
                try (ResultSet rsI = psI.executeQuery()) {
                    while (rsI.next()) {
                        String url = rsI.getString("url");
                        if (url != null && !url.isBlank()) imagenes.add(url);
                    }
                }
            }
            if (imagenes.isEmpty()) {
                imagenes.add("img/machupicchu.png");
            }

            json.append("\"imagenPrincipal\":\"").append(JavaApiServer.jsonEscape(imagenes.get(0))).append("\",");
            json.append("\"imagenes\":[");
            for (int i = 0; i < imagenes.size(); i++) {
                if (i > 0) json.append(",");
                json.append("\"").append(JavaApiServer.jsonEscape(imagenes.get(i))).append("\"");
            }
            json.append("],");

            // Fechas y horarios disponibles (TourFecha): solo salidas futuras activas
            json.append("\"fechas\":[");
            boolean primF = true;
            String sqlFechas = """
                SELECT idTourFecha, fecha, horaInicio, cupoTotal, cupoDisponible, estado
                FROM TourFecha
                WHERE idTour = ?
                  AND fecha >= CURDATE()
                  AND UPPER(estado) != 'CANCELADO'
                  AND UPPER(estado) != 'PAUSADO'
                ORDER BY fecha ASC, horaInicio ASC
            """;
            try (PreparedStatement psF = con.prepareStatement(sqlFechas)) {
                psF.setInt(1, idRealTour);
                try (ResultSet rsF = psF.executeQuery()) {
                    while (rsF.next()) {
                        if (!primF) json.append(",");
                        primF = false;
                        json.append("{")
                            .append("\"idTourFecha\":").append(rsF.getInt("idTourFecha")).append(",")
                            .append("\"fecha\":\"").append(rsF.getString("fecha")).append("\",")
                            .append("\"horaInicio\":\"").append(rsF.getString("horaInicio")).append("\",")
                            .append("\"cupoTotal\":").append(rsF.getInt("cupoTotal")).append(",")
                            .append("\"cupoDisponible\":").append(rsF.getInt("cupoDisponible")).append(",")
                            .append("\"estado\":\"").append(JavaApiServer.jsonEscape(rsF.getString("estado"))).append("\"")
                            .append("}");
                    }
                }
            }
            json.append("]");

            json.append("}}");
            JavaApiServer.sendJsonResponse(exchange, 200, json.toString());

        } catch (Exception e) {
            e.printStackTrace();
            JavaApiServer.sendJsonResponse(exchange, 500, "{\"status\":\"error\",\"message\":\"Error en servidor: " + JavaApiServer.jsonEscape(e.getMessage()) + "\"}");
        }
    }
}
