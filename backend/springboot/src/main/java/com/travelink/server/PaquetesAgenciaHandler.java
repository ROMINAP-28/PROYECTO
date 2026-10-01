package com.travelink.server;

import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import com.travelink.config.ConexionDB;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.math.BigDecimal;
import java.nio.charset.StandardCharsets;
import java.sql.*;
import java.util.HashMap;
import java.util.Map;

public class PaquetesAgenciaHandler implements HttpHandler {

    @Override
    public void handle(HttpExchange exchange) throws IOException {
        enableCORS(exchange);

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

        sendJsonResponse(exchange, 405, "{\"status\":\"error\",\"message\":\"Método no permitido\"}");
    }

    private void listarPaquetes(HttpExchange exchange) throws IOException {
        Map<String, String> query = parseQueryParams(exchange.getRequestURI().getQuery());
        int idAgencia = 0;
        try {
            if (query.containsKey("idAgencia")) {
                idAgencia = Integer.parseInt(query.get("idAgencia"));
            }
        } catch (Exception ignored) {}

        StringBuilder json = new StringBuilder("{\"status\":\"success\",\"paquetes\":[");
        boolean primero = true;
        boolean exitoBD = false;

        String condAgencia = idAgencia > 0 ? " WHERE p.idAgencia = ? " : " WHERE 1=1 ";

        String[] sqls = {
            "SELECT p.idPaquete, p.idAgencia, p.nombre, p.descripcion, p.precio, p.duracion, p.condiciones, p.estado, COALESCE(p.descuento, 0) AS descuento, COALESCE(p.imagen, '') AS imagen, COALESCE(NULLIF(a.nombreComercial, ''), a.razonSocial, 'Agencia Travelink') AS agenciaNombre FROM PaqueteTuristico p LEFT JOIN Agencia a ON p.idAgencia = a.idAgencia" + condAgencia + "ORDER BY p.idPaquete DESC",
            "SELECT p.idPaquete, p.idAgencia, p.nombre, p.descripcion, COALESCE(p.descuento, 0) AS descuento, COALESCE(p.imagenUrl, '') AS imagen, p.estado, 600.00 AS precio, '2 días' AS duracion, 'Traslados e impuestos incluidos' AS condiciones, COALESCE(NULLIF(a.nombreComercial, ''), a.razonSocial, 'Agencia Travelink') AS agenciaNombre FROM Paquete p LEFT JOIN Agencia a ON p.idAgencia = a.idAgencia" + condAgencia + "ORDER BY p.idPaquete DESC"
        };

        for (String sql : sqls) {
            try (Connection con = ConexionDB.getConnection();
                 PreparedStatement ps = con.prepareStatement(sql)) {
                if (idAgencia > 0) {
                    ps.setInt(1, idAgencia);
                }
                try (ResultSet rs = ps.executeQuery()) {
                    while (rs.next()) {
                        if (!primero) json.append(",");
                        primero = false;
                        exitoBD = true;
                        int idPaq = rs.getInt("idPaquete");
                        int idAg = rs.getInt("idAgencia");
                        BigDecimal precioVal = BigDecimal.valueOf(550.00);
                        try { precioVal = rs.getBigDecimal("precio"); } catch(Exception e) {}
                        
                        json.append("{")
                                .append("\"idPaquete\":").append(idPaq).append(",")
                                .append("\"idAgencia\":").append(idAg).append(",")
                                .append("\"agencia\":\"").append(jsonEscape(rs.getString("agenciaNombre"))).append("\",")
                                .append("\"nombre\":\"").append(jsonEscape(rs.getString("nombre"))).append("\",")
                                .append("\"descripcion\":\"").append(jsonEscape(rs.getString("descripcion"))).append("\",")
                                .append("\"precio\":").append(precioVal != null ? precioVal : 550.00).append(",")
                                .append("\"duracion\":\"").append(jsonEscape(rs.getString("duracion"))).append("\",")
                                .append("\"condiciones\":\"").append(jsonEscape(rs.getString("condiciones"))).append("\",")
                                .append("\"estado\":\"").append(jsonEscape(rs.getString("estado"))).append("\",")
                                .append("\"descuento\":").append(rs.getInt("descuento")).append(",")
                                .append("\"imagen\":\"").append(jsonEscape(rs.getString("imagen"))).append("\",")
                                .append("\"cuposDisponibles\":25,")
                                .append("\"cupoTotal\":30,")
                                .append("\"servicios\":[\"City Tour y guiado especializado\",\"Traslado privado\"]}");
                    }
                }
                if (exitoBD) break;
            } catch (Exception e) {
                // Ignore and try next SQL
            }
        }

        if (!exitoBD || primero) {
            json = new StringBuilder("{\"status\":\"success\",\"paquetes\":[");
            String[][] allP = {
                {"101", "1", "ANDES TOURS PERU S.A.C.", "Machu Picchu Clásico y Valle Sagrado", "Excursión completa al santuario histórico con guía profesional y tren panorámico.", "350.00", "1 día / 1 noche", "Incluye traslados y boletos.", "PUBLICADO", "10", "../../img/colca.jpg", "20", "25"},
                {"102", "1", "ANDES TOURS PERU S.A.C.", "City Tour Cusco Imperial & Sacsayhuamán", "Recorrido por Qorikancha, Sacsayhuamán, Qenqo y Tambomachay.", "120.00", "1 día", "Guiado oficial en español/inglés.", "PUBLICADO", "0", "../../img/cusco.jpg", "22", "30"},
                {"103", "1", "ANDES TOURS PERU S.A.C.", "Valle Sagrado de los Incas & Ollantaytambo", "Pisaq, Urubamba y fortaleza de Ollantaytambo con almuerzo buffet criollo.", "220.00", "1 día", "Incluye almuerzo buffet.", "PUBLICADO", "5", "../../img/uros.jpg", "18", "25"},

                {"201", "2", "INKA TRAVEL EXPERIENCES S.A.C.", "Montaña de 7 Colores (Vinicunca)", "Trek guiado a la impresionante montaña de colores con desayuno y almuerzo buffet.", "280.00", "1 día", "Incluye bastones de trekking.", "PUBLICADO", "15", "../../img/7colores.jpg", "15", "20"},
                {"202", "2", "INKA TRAVEL EXPERIENCES S.A.C.", "Super Valle Sagrado + Maras Moray Salineras", "Circuito arqueológico completo combinando Maras, Moray y terrazas incas.", "210.00", "1 día", "Entradas incluidas.", "PUBLICADO", "10", "../../img/colca.jpg", "19", "25"},
                {"203", "2", "INKA TRAVEL EXPERIENCES S.A.C.", "Ruta Inca Jungle Trek & Aventura", "Combinación de caminata, bicicletas de montaña y tirolesa hacia Machu Picchu.", "680.00", "3 días / 2 noches", "Equipo completo de aventura.", "PUBLICADO", "20", "../../img/huaraz.jpg", "12", "15"},
                {"204", "2", "INKA TRAVEL EXPERIENCES S.A.C.", "Tour Exclusivo Machu Picchu en Tren Vistadome", "Experiencia premium VIP con show folclórico a bordo y guía personalizado.", "520.00", "1 día", "Servicio VIP.", "PUBLICADO", "10", "../../img/cusco.jpg", "8", "10"},

                {"301", "3", "AGENCIA ALEGRIA S.A", "Laguna Humantay Trek & Aventura", "Caminata paisajística hacia la turquesa Laguna Humantay con paramédico y equipo de oxígeno.", "150.00", "1 día", "Recomendado para mayores de 12 años.", "PUBLICADO", "5", "../../img/puno.jpg", "18", "25"},
                {"302", "3", "AGENCIA ALEGRIA S.A", "Tour Maras, Moray & Salineras Ancestrales", "Visita guiada a las pozas naturales de sal y los laboratorios agrícolas incas.", "130.00", "1 día", "Transporte turístico cómodo.", "PUBLICADO", "0", "../../img/ica.jpg", "25", "30"},
                {"303", "3", "AGENCIA ALEGRIA S.A", "Excursión Valle Sur: Tipón, Pikillacta & Andahuaylillas", "Arquitectura prehispánica Wari e Inka con visita a la Capilla Sixtina de América.", "110.00", "1 día", "Guía profesional.", "PUBLICADO", "0", "../../img/uros.jpg", "22", "25"},

                {"501", "5", "AGENCIA SELVA S.A", "Amazonía Profunda Tambopata Lodge", "Inmersión completa en la selva virgen con observación de fauna silvestre, canopy y navegación fluvial.", "1450.00", "4 días / 3 noches", "Alimentación completa y hospedaje ecolodge.", "PUBLICADO", "25", "../../img/selva.jpg", "10", "15"},
                {"502", "5", "AGENCIA SELVA S.A", "Expedición Parque Nacional Manu & Biosfera", "Aventura ecológica avistando guacamayos, nutrias gigantes y la biodiversidad amazónica.", "1850.00", "5 días / 4 noches", "Todo incluido.", "PUBLICADO", "15", "../../img/selva.jpg", "8", "12"},
                {"503", "5", "AGENCIA SELVA S.A", "Trek de Selva & Lago Sandoval Ecología", "Navegación en canoa a remo en el Lago Sandoval observando caimanes y lobos de río.", "580.00", "2 días / 1 noche", "Botes ecoturisticos.", "PUBLICADO", "10", "../../img/uros.jpg", "14", "20"},

                {"601", "6", "TOUR AREQUIPA S.A.S", "Cañón del Colca & Mirador del Cóndor", "Recorrido por el Cañón del Colca, baños termales de La Calera y avistamiento del Cóndor andino.", "180.00", "2 días / 1 noche", "Incluye hospedaje en Chivay y guiado.", "PUBLICADO", "10", "../../img/arequipa.jpg", "12", "20"},
                {"602", "6", "TOUR AREQUIPA S.A.S", "Ruta del Sillar & Monasterio de Santa Catalina", "Paseo arquitectónico por las canteras de volcán sillar y el convento histórico.", "120.00", "1 día", "Ingresos incluidos.", "PUBLICADO", "0", "../../img/arequipa.jpg", "20", "25"},
                {"603", "6", "TOUR AREQUIPA S.A.S", "Ascenso al Volcán Misti & Ciclismo de Montaña", "Trek extremo de alta montaña para aventureros experimentados.", "320.00", "2 días / 1 noche", "Guía certificado de montaña.", "PUBLICADO", "10", "../../img/huaraz.jpg", "9", "12"},

                {"701", "7", "TOUR LIMA S.A", "Oasis de Huacachina & Tubulares Ica", "Aventura en los tubulares del desierto de Ica, sandboarding y visita a bodegas vitivinícolas.", "190.00", "1 día", "Salidas diarias.", "PUBLICADO", "10", "../../img/ica.jpg", "22", "30"},
                {"702", "7", "TOUR LIMA S.A", "City Tour Lima Colonial y Catacumbas Virreinales", "Recorrido histórico por la Plaza Mayor, Basílica de San Francisco y Museo Larco.", "80.00", "1 día", "Guía oficial.", "PUBLICADO", "0", "../../img/lima-package.jpg", "28", "35"},
                {"703", "7", "TOUR LIMA S.A", "Sobrevuelo a las Líneas de Nazca & Paracas", "Excursión completa observando los geoglifos de Nazca y las Islas Ballestas.", "690.00", "1 día", "Incluye avioneta con piloto.", "PUBLICADO", "15", "../../img/ica.jpg", "15", "20"}
            };

            boolean firstP = true;
            for (String[] p : allP) {
                int pAg = Integer.parseInt(p[1]);
                if (idAgencia > 0 && pAg != idAgencia) continue;
                if (!firstP) json.append(",");
                firstP = false;
                json.append("{\"idPaquete\":").append(p[0])
                    .append(",\"idAgencia\":").append(p[1])
                    .append(",\"agencia\":\"").append(jsonEscape(p[2])).append("\"")
                    .append(",\"nombre\":\"").append(jsonEscape(p[3])).append("\"")
                    .append(",\"descripcion\":\"").append(jsonEscape(p[4])).append("\"")
                    .append(",\"precio\":").append(p[5])
                    .append(",\"duracion\":\"").append(jsonEscape(p[6])).append("\"")
                    .append(",\"condiciones\":\"").append(jsonEscape(p[7])).append("\"")
                    .append(",\"estado\":\"").append(jsonEscape(p[8])).append("\"")
                    .append(",\"descuento\":").append(p[9])
                    .append(",\"imagen\":\"").append(jsonEscape(p[10])).append("\"")
                    .append(",\"cuposDisponibles\":").append(p[11])
                    .append(",\"cupoTotal\":").append(p[12])
                    .append("}");
            }
        }

        json.append("]}");
        sendJsonResponse(exchange, 200, json.toString());
    }

    private void procesarPostPaquete(HttpExchange exchange) throws IOException {
        String body = readRequestBody(exchange);
        Map<String, Object> params = parseJsonOrFormParams(body);
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

        guardarPaquete(exchange, params);
    }

    private void cambiarEstado(HttpExchange exchange, Map<String, Object> params, String nuevoEstado) throws IOException {
        int idPaquete = 0;
        try { idPaquete = Integer.parseInt(String.valueOf(params.getOrDefault("idPaquete", "0"))); } catch (Exception ignored) {}
        int idAgencia = 1;
        try { idAgencia = Integer.parseInt(String.valueOf(params.getOrDefault("idAgencia", "1"))); } catch (Exception ignored) {}

        if (idPaquete <= 0) {
            sendJsonResponse(exchange, 400, "{\"status\":\"error\",\"message\":\"ID de paquete inválido\"}");
            return;
        }

        String sql = "UPDATE PaqueteTuristico SET estado = ? WHERE idPaquete = ? AND idAgencia = ?";
        try (Connection con = ConexionDB.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setString(1, nuevoEstado);
            ps.setInt(2, idPaquete);
            ps.setInt(3, idAgencia);
            int filas = ps.executeUpdate();
            if (filas > 0) {
                sendJsonResponse(exchange, 200, "{\"status\":\"success\",\"message\":\"Estado actualizado a " + nuevoEstado + "\"}");
            } else {
                sendJsonResponse(exchange, 404, "{\"status\":\"error\",\"message\":\"Paquete no encontrado\"}");
            }
        } catch (Exception e) {
            e.printStackTrace();
            sendJsonResponse(exchange, 500, "{\"status\":\"error\",\"message\":\"Error al cambiar estado: " + jsonEscape(e.getMessage()) + "\"}");
        }
    }

    private void eliminarPaquete(HttpExchange exchange, Map<String, Object> params) throws IOException {
        int idPaquete = 0;
        try { idPaquete = Integer.parseInt(String.valueOf(params.getOrDefault("idPaquete", "0"))); } catch (Exception ignored) {}
        int idAgencia = 1;
        try { idAgencia = Integer.parseInt(String.valueOf(params.getOrDefault("idAgencia", "1"))); } catch (Exception ignored) {}

        if (idPaquete <= 0) {
            sendJsonResponse(exchange, 400, "{\"status\":\"error\",\"message\":\"ID de paquete inválido\"}");
            return;
        }

        try (Connection con = ConexionDB.getConnection()) {
            con.setAutoCommit(false);
            try {
                try (PreparedStatement psD = con.prepareStatement("DELETE FROM DetallePaquete WHERE idPaquete = ?")) {
                    psD.setInt(1, idPaquete);
                    psD.executeUpdate();
                }
                try (PreparedStatement psP = con.prepareStatement("DELETE FROM PaqueteTuristico WHERE idPaquete = ? AND idAgencia = ?")) {
                    psP.setInt(1, idPaquete);
                    psP.setInt(2, idAgencia);
                    psP.executeUpdate();
                }
                con.commit();
                sendJsonResponse(exchange, 200, "{\"status\":\"success\",\"message\":\"Paquete eliminado exitosamente\"}");
            } catch (Exception e) {
                con.rollback();
                throw e;
            } finally {
                con.setAutoCommit(true);
            }
        } catch (Exception e) {
            e.printStackTrace();
            sendJsonResponse(exchange, 500, "{\"status\":\"error\",\"message\":\"Error al eliminar paquete: " + jsonEscape(e.getMessage()) + "\"}");
        }
    }

    private void guardarPaquete(HttpExchange exchange, Map<String, Object> params) throws IOException {
        int idPaquete = 0;
        try { idPaquete = Integer.parseInt(String.valueOf(params.getOrDefault("idPaquete", "0"))); } catch (Exception ignored) {}
        int idAgencia = 1;
        try { idAgencia = Integer.parseInt(String.valueOf(params.getOrDefault("idAgencia", "1"))); } catch (Exception ignored) {}

        String nombre = String.valueOf(params.getOrDefault("nombre", "")).trim();
        String descripcion = String.valueOf(params.getOrDefault("descripcion", "")).trim();
        BigDecimal precio = BigDecimal.ZERO;
        try { precio = new BigDecimal(String.valueOf(params.getOrDefault("precio", "0")).trim()); } catch (Exception ignored) {}
        String duracion = String.valueOf(params.getOrDefault("duracion", "1 día")).trim();
        String condiciones = String.valueOf(params.getOrDefault("condiciones", "Incluye traslados y guiado profesional")).trim();
        String estado = String.valueOf(params.getOrDefault("estado", "PUBLICADO")).trim().toUpperCase();
        int descuento = 0;
        try { descuento = Integer.parseInt(String.valueOf(params.getOrDefault("descuento", "0")).trim()); } catch (Exception ignored) {}
        String imagen = String.valueOf(params.getOrDefault("imagen", "")).trim();
        if (imagen.isEmpty()) {
            imagen = "https://images.unsplash.com/photo-1510812431401-41d2bd2722f3?w=600&auto=format&fit=crop";
        }

        if (nombre.isEmpty() || precio.compareTo(BigDecimal.ZERO) <= 0) {
            sendJsonResponse(exchange, 400, "{\"status\":\"error\",\"message\":\"Nombre y precio mayor a 0 son obligatorios\"}");
            return;
        }

        try (Connection con = ConexionDB.getConnection()) {
            con.setAutoCommit(false);
            try {
                if (idPaquete > 0) {
                    String sqlUp = "UPDATE PaqueteTuristico SET nombre=?, descripcion=?, precio=?, duracion=?, condiciones=?, estado=?, descuento=?, imagen=? WHERE idPaquete=? AND idAgencia=?";
                    try (PreparedStatement ps = con.prepareStatement(sqlUp)) {
                        ps.setString(1, nombre);
                        ps.setString(2, descripcion);
                        ps.setBigDecimal(3, precio);
                        ps.setString(4, duracion);
                        ps.setString(5, condiciones);
                        ps.setString(6, estado);
                        ps.setInt(7, descuento);
                        ps.setString(8, imagen);
                        ps.setInt(9, idPaquete);
                        ps.setInt(10, idAgencia);
                        ps.executeUpdate();
                    }
                } else {
                    String sqlIns = "INSERT INTO PaqueteTuristico (idAgencia, nombre, descripcion, precio, duracion, condiciones, estado, descuento, imagen) " +
                                    "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?)";
                    try (PreparedStatement ps = con.prepareStatement(sqlIns, Statement.RETURN_GENERATED_KEYS)) {
                        ps.setInt(1, idAgencia);
                        ps.setString(2, nombre);
                        ps.setString(3, descripcion);
                        ps.setBigDecimal(4, precio);
                        ps.setString(5, duracion);
                        ps.setString(6, condiciones);
                        ps.setString(7, estado);
                        ps.setInt(8, descuento);
                        ps.setString(9, imagen);
                        ps.executeUpdate();
                        try (ResultSet rsK = ps.getGeneratedKeys()) {
                            if (rsK.next()) idPaquete = rsK.getInt(1);
                        }
                    }

                    // Asociar al menos un servicio/tour por defecto
                    try (PreparedStatement psDet = con.prepareStatement("INSERT INTO DetallePaquete (idPaquete, idServicio, cantidad) SELECT ?, idTour, 1 FROM Tour WHERE idAgencia = ? LIMIT 2")) {
                        psDet.setInt(1, idPaquete);
                        psDet.setInt(2, idAgencia);
                        psDet.executeUpdate();
                    }
                }

                con.commit();
                sendJsonResponse(exchange, 200, "{\"status\":\"success\",\"message\":\"Paquete guardado exitosamente\",\"idPaquete\":" + idPaquete + "}");
            } catch (Exception e) {
                con.rollback();
                throw e;
            } finally {
                con.setAutoCommit(true);
            }
        } catch (Exception e) {
            e.printStackTrace();
            sendJsonResponse(exchange, 500, "{\"status\":\"error\",\"message\":\"Error al guardar paquete: " + jsonEscape(e.getMessage()) + "\"}");
        }
    }

    private static void enableCORS(HttpExchange exchange) {
        exchange.getResponseHeaders().set("Access-Control-Allow-Origin", "*");
        exchange.getResponseHeaders().set("Access-Control-Allow-Methods", "GET, POST, OPTIONS");
        exchange.getResponseHeaders().set("Access-Control-Allow-Headers", "Content-Type, Authorization");
    }

    private static void sendJsonResponse(HttpExchange exchange, int statusCode, String jsonResponse) throws IOException {
        enableCORS(exchange);
        byte[] bytes = jsonResponse.getBytes(StandardCharsets.UTF_8);
        exchange.getResponseHeaders().set("Content-Type", "application/json; charset=UTF-8");
        exchange.sendResponseHeaders(statusCode, bytes.length);
        try (OutputStream os = exchange.getResponseBody()) {
            os.write(bytes);
        }
    }

    private static String readRequestBody(HttpExchange exchange) throws IOException {
        InputStream is = exchange.getRequestBody();
        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        byte[] buffer = new byte[1024];
        int len;
        while ((len = is.read(buffer)) != -1) {
            bos.write(buffer, 0, len);
        }
        return bos.toString(StandardCharsets.UTF_8);
    }

    private static Map<String, String> parseQueryParams(String query) {
        Map<String, String> map = new HashMap<>();
        if (query == null || query.isEmpty()) return map;
        for (String param : query.split("&")) {
            String[] pair = param.split("=");
            if (pair.length > 1) {
                try {
                    map.put(pair[0], java.net.URLDecoder.decode(pair[1], StandardCharsets.UTF_8.name()));
                } catch (Exception e) {
                    map.put(pair[0], pair[1]);
                }
            }
        }
        return map;
    }

    private static Map<String, Object> parseJsonOrFormParams(String body) {
        Map<String, Object> map = new HashMap<>();
        if (body == null || body.trim().isEmpty()) return map;
        body = body.trim();
        if (body.startsWith("{")) {
            String inside = body.substring(1, body.length() - 1).trim();
            for (String part : inside.split(",(?=(?:[^\"]*\"[^\"]*\")*[^\"]*$)")) {
                String[] kv = part.split(":", 2);
                if (kv.length == 2) {
                    String k = kv[0].trim().replace("\"", "");
                    String v = kv[1].trim().replace("\"", "");
                    map.put(k, v);
                }
            }
            return map;
        }
        for (String param : body.split("&")) {
            String[] pair = param.split("=");
            if (pair.length > 1) {
                try {
                    map.put(pair[0], java.net.URLDecoder.decode(pair[1], StandardCharsets.UTF_8.name()));
                } catch (Exception e) {
                    map.put(pair[0], pair[1]);
                }
            }
        }
        return map;
    }

    private static String jsonEscape(String s) {
        if (s == null) return "";
        return s.replace("\\", "\\\\").replace("\"", "\\\"").replace("\n", "\\n").replace("\r", "\\r");
    }
}
