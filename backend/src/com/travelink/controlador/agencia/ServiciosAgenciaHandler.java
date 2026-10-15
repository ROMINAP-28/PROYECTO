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

    public class ServiciosAgenciaHandler implements HttpHandler {

        @Override
        public void handle(HttpExchange exchange) throws IOException {

            if ("OPTIONS".equalsIgnoreCase(exchange.getRequestMethod())) {
                JavaApiServer.enableCORS(exchange);
                exchange.sendResponseHeaders(200, -1);
                return;
            }

            String metodo = exchange.getRequestMethod();

            if ("GET".equalsIgnoreCase(metodo)) {
                obtenerServicios(exchange);
                return;
            }

            if ("POST".equalsIgnoreCase(metodo)) {
                procesarPostServicio(exchange);
                return;
            }

            JavaApiServer.sendJsonResponse(exchange, 405,
                    "{\"status\":\"error\",\"message\":\"Método no permitido\"}");
        }

        private void obtenerServicios(HttpExchange exchange) throws IOException {
            Map<String, String> params = JavaApiServer.parseQueryParams(exchange.getRequestURI().getQuery());
            int idAgencia = 0;
            try {
                idAgencia = Integer.parseInt(params.getOrDefault("idAgencia", "1"));
            } catch (Exception e) {
                idAgencia = 1;
            }

            String buscar = params.getOrDefault("buscar", "").trim().toLowerCase();
            String tipoFiltro = params.getOrDefault("tipo", "").trim();
            String estadoFiltro = params.getOrDefault("estado", "").trim();

            StringBuilder json = new StringBuilder();
            json.append("{\"status\":\"success\",\"servicios\":[");

            try (Connection con = ConexionDB.getConnection()) {
                StringBuilder sql = new StringBuilder(
                        "SELECT t.idTour, t.nombre, t.descripcion, t.categoria, t.precioAdulto, t.precioNino, t.precioBebe, " +
                        "t.duracion, t.estado, t.idAgencia, t.idDestino, t.aceptaBebes, t.queIncluye, t.queNoIncluye, t.dias, t.horas, t.ubicacion, d.nombre AS destino " +
                        "FROM Tour t " +
                        "INNER JOIN Destino d ON t.idDestino = d.idDestino " +
                        "WHERE t.idAgencia = ? "
                );

                if (!buscar.isEmpty()) {
                    sql.append("AND LOWER(t.nombre) LIKE ? ");
                }
                if (!tipoFiltro.isEmpty() && !"Todos".equalsIgnoreCase(tipoFiltro)) {
                    sql.append("AND t.categoria = ? ");
                }
                if (!estadoFiltro.isEmpty() && !"Todos".equalsIgnoreCase(estadoFiltro)) {
                    sql.append("AND t.estado = ? ");
                }
                sql.append("ORDER BY t.idTour DESC");

                try (PreparedStatement ps = con.prepareStatement(sql.toString())) {
                    int paramIdx = 1;
                    ps.setInt(paramIdx++, idAgencia);
                    if (!buscar.isEmpty()) {
                        ps.setString(paramIdx++, "%" + buscar + "%");
                    }
                    if (!tipoFiltro.isEmpty() && !"Todos".equalsIgnoreCase(tipoFiltro)) {
                        ps.setString(paramIdx++, tipoFiltro);
                    }
                    if (!estadoFiltro.isEmpty() && !"Todos".equalsIgnoreCase(estadoFiltro)) {
                        ps.setString(paramIdx++, estadoFiltro);
                    }

                    try (ResultSet rs = ps.executeQuery()) {
                        boolean primero = true;
                        while (rs.next()) {
                            int idTour = rs.getInt("idTour");
                            String nombre = rs.getString("nombre");
                            String desc = rs.getString("descripcion");
                            String cat = rs.getString("categoria");
                            double pAdulto = rs.getDouble("precioAdulto");
                            double pNino = rs.getDouble("precioNino");
                            double pBebe = rs.getDouble("precioBebe");
                            String duracion = rs.getString("duracion");
                            String estado = rs.getString("estado");
                            int idDestino = rs.getInt("idDestino");
                            String destino = rs.getString("destino");
                            boolean aceptaBebes = rs.getBoolean("aceptaBebes");
                            String queIncluye = rs.getString("queIncluye");
                            String queNoIncluye = rs.getString("queNoIncluye");
                            int dias = rs.getInt("dias");
                            int horas = rs.getInt("horas");
                            String ubicacion = rs.getString("ubicacion");

                            // Imágenes del tour
                            StringBuilder imagenesJson = new StringBuilder("[");
                            String mainImgUrl = "";
                            try (PreparedStatement psImg = con.prepareStatement(
                                    "SELECT idImagen, url, esPrincipal, orden FROM TourImagen WHERE idTour = ? ORDER BY esPrincipal DESC, orden ASC")) {
                                psImg.setInt(1, idTour);
                                try (ResultSet rsImg = psImg.executeQuery()) {
                                    boolean primeroImg = true;
                                    while (rsImg.next()) {
                                        if (!primeroImg) imagenesJson.append(",");
                                        String url = rsImg.getString("url");
                                        boolean esPrinc = rsImg.getBoolean("esPrincipal");
                                        if (esPrinc && mainImgUrl.isEmpty()) mainImgUrl = url;
                                        if (mainImgUrl.isEmpty()) mainImgUrl = url;

                                        imagenesJson.append("{")
                                                .append("\"idImagen\":").append(rsImg.getInt("idImagen")).append(",")
                                                .append("\"url\":\"").append(JavaApiServer.jsonEscape(url)).append("\",")
                                                .append("\"esPrincipal\":").append(esPrinc).append(",")
                                                .append("\"orden\":").append(rsImg.getInt("orden"))
                                                .append("}");
                                        primeroImg = false;
                                    }
                                }
                            }
                            imagenesJson.append("]");

                            // Verificar reservas activas futuras
                            boolean tieneReservasActivas = false;
                            try (PreparedStatement psCheck = con.prepareStatement(
                                    "SELECT COUNT(*) FROM DetalleReserva dr " +
                                    "INNER JOIN TourFecha tf ON dr.idTourFecha = tf.idTourFecha " +
                                    "INNER JOIN Reserva r ON dr.idReserva = r.idReserva " +
                                    "WHERE tf.idTour = ? AND r.estado IN ('CONFIRMADA', 'PENDIENTE') AND tf.fecha >= CURRENT_DATE")) {
                                psCheck.setInt(1, idTour);
                                try (ResultSet rsC = psCheck.executeQuery()) {
                                    if (rsC.next() && rsC.getInt(1) > 0) tieneReservasActivas = true;
                                }
                            }

                            // Total de reservas históricas
                            int reservasHistoricas = 0;
                            try (PreparedStatement psHist = con.prepareStatement(
                                    "SELECT COUNT(*) FROM DetalleReserva dr " +
                                    "INNER JOIN TourFecha tf ON dr.idTourFecha = tf.idTourFecha " +
                                    "WHERE tf.idTour = ?")) {
                                psHist.setInt(1, idTour);
                                try (ResultSet rsH = psHist.executeQuery()) {
                                    if (rsH.next()) reservasHistoricas = rsH.getInt(1);
                                }
                            }

                            double comision = pAdulto * 0.15;
                            double neto = pAdulto * 0.85;

                            if (!primero) json.append(",");
                            json.append("{")
                                    .append("\"idServicio\":").append(idTour).append(",")
                                    .append("\"idTour\":").append(idTour).append(",")
                                    .append("\"nombre\":\"").append(JavaApiServer.jsonEscape(nombre)).append("\",")
                                    .append("\"descripcion\":\"").append(JavaApiServer.jsonEscape(desc)).append("\",")
                                    .append("\"tipoServicio\":\"").append(JavaApiServer.jsonEscape(cat)).append("\",")
                                    .append("\"categoria\":\"").append(JavaApiServer.jsonEscape(cat)).append("\",")
                                    .append("\"idDestino\":").append(idDestino).append(",")
                                    .append("\"destino\":\"").append(JavaApiServer.jsonEscape(destino)).append("\",")
                                    .append("\"precio\":").append(pAdulto).append(",")
                                    .append("\"precioAdulto\":").append(pAdulto).append(",")
                                    .append("\"precioNino\":").append(pNino).append(",")
                                    .append("\"precioBebe\":").append(pBebe).append(",")
                                    .append("\"comision15\":").append(String.format(Locale.US, "%.2f", comision)).append(",")
                                    .append("\"neto85\":").append(String.format(Locale.US, "%.2f", neto)).append(",")
                                    .append("\"duracion\":\"").append(JavaApiServer.jsonEscape(duracion)).append("\",")
                                    .append("\"dias\":").append(dias).append(",")
                                    .append("\"horas\":").append(horas).append(",")
                                    .append("\"aceptaBebes\":").append(aceptaBebes).append(",")
                                    .append("\"queIncluye\":\"").append(JavaApiServer.jsonEscape(queIncluye != null ? queIncluye : "")).append("\",")
                                    .append("\"queNoIncluye\":\"").append(JavaApiServer.jsonEscape(queNoIncluye != null ? queNoIncluye : "")).append("\",")
                                    .append("\"ubicacion\":\"").append(JavaApiServer.jsonEscape(ubicacion != null ? ubicacion : "")).append("\",")
                                    .append("\"estado\":\"").append(JavaApiServer.jsonEscape(estado)).append("\",")
                                    .append("\"imagenPrincipal\":\"").append(JavaApiServer.jsonEscape(mainImgUrl)).append("\",")
                                    .append("\"imagenes\":").append(imagenesJson.toString()).append(",")
                                    .append("\"tieneReservasActivas\":").append(tieneReservasActivas).append(",")
                                    .append("\"reservasHistoricas\":").append(reservasHistoricas)
                                    .append("}");
                            primero = false;
                        }
                    }
                }

                json.append("]}");
                JavaApiServer.sendJsonResponse(exchange, 200, json.toString());

            } catch (Exception e) {
                e.printStackTrace();
                JavaApiServer.sendJsonResponse(exchange, 500, "{\"status\":\"error\",\"message\":\"Error al obtener los servicios\"}");
            }
        }

        private void procesarPostServicio(HttpExchange exchange) throws IOException {
            String body = JavaApiServer.readRequestBody(exchange);
            Map<String, Object> params = JavaApiServer.parseJsonOrFormParams(body);

            String action = String.valueOf(params.getOrDefault("action", "")).trim();

            if ("cambiar_estado".equalsIgnoreCase(action) || "pausar".equalsIgnoreCase(action) || "activar".equalsIgnoreCase(action)) {
                cambiarEstadoServicio(exchange, params);
                return;
            }

            if ("eliminar".equalsIgnoreCase(action)) {
                eliminarServicio(exchange, params);
                return;
            }

            guardarServicio(exchange, params);
        }

        private void cambiarEstadoServicio(HttpExchange exchange, Map<String, Object> params) throws IOException {
            int idTour = 0;
            try {
                idTour = Integer.parseInt(String.valueOf(params.getOrDefault("idServicio", params.getOrDefault("idTour", "0"))));
            } catch (Exception e) {}

            int idAgencia = 0;
            try {
                idAgencia = Integer.parseInt(String.valueOf(params.getOrDefault("idAgencia", "1")));
            } catch (Exception e) { idAgencia = 1; }

            String nuevoEstado = String.valueOf(params.getOrDefault("nuevoEstado", "")).trim().toUpperCase();
            if (nuevoEstado.isEmpty()) {
                String act = String.valueOf(params.getOrDefault("action", "")).trim();
                if ("pausar".equalsIgnoreCase(act)) nuevoEstado = "PAUSADO";
                else if ("activar".equalsIgnoreCase(act)) nuevoEstado = "ACTIVO";
            }

            if (idTour <= 0) {
                JavaApiServer.sendJsonResponse(exchange, 400, "{\"status\":\"error\",\"message\":\"ID de servicio no válido\"}");
                return;
            }

            try (Connection con = ConexionDB.getConnection()) {
                // Verificar estado actual
                String estadoActual = "";
                try (PreparedStatement ps = con.prepareStatement("SELECT estado FROM Tour WHERE idTour = ? AND idAgencia = ?")) {
                    ps.setInt(1, idTour);
                    ps.setInt(2, idAgencia);
                    try (ResultSet rs = ps.executeQuery()) {
                        if (rs.next()) {
                            estadoActual = rs.getString("estado");
                        } else {
                            JavaApiServer.sendJsonResponse(exchange, 404, "{\"status\":\"error\",\"message\":\"Servicio no encontrado\"}");
                            return;
                        }
                    }
                }

                // Si fue desactivado por el administrador, la agencia NO puede modificarlo
                if ("DESACTIVADO_POR_ADMIN".equalsIgnoreCase(estadoActual)) {
                    JavaApiServer.sendJsonResponse(exchange, 403, "{\"status\":\"error\",\"message\":\"Este servicio fue desactivado por el administrador y no puede ser reactivado por la agencia.\"}");
                    return;
                }

                // Si intenta pausar, validar reservas activas futuras
                if ("PAUSADO".equalsIgnoreCase(nuevoEstado)) {
                    try (PreparedStatement psCheck = con.prepareStatement(
                            "SELECT COUNT(*) FROM DetalleReserva dr " +
                            "INNER JOIN TourFecha tf ON dr.idTourFecha = tf.idTourFecha " +
                            "INNER JOIN Reserva r ON dr.idReserva = r.idReserva " +
                            "WHERE tf.idTour = ? AND r.estado IN ('CONFIRMADA', 'PENDIENTE') AND tf.fecha >= CURRENT_DATE")) {
                        psCheck.setInt(1, idTour);
                        try (ResultSet rsC = psCheck.executeQuery()) {
                            if (rsC.next() && rsC.getInt(1) > 0) {
                                JavaApiServer.sendJsonResponse(exchange, 400, "{\"status\":\"error\",\"message\":\"No se puede pausar un servicio con reservas activas futuras (" + rsC.getInt(1) + " reserva(s) pendiente(s)).\"}");
                                return;
                            }
                        }
                    }
                }

                try (PreparedStatement psUpd = con.prepareStatement("UPDATE Tour SET estado = ? WHERE idTour = ? AND idAgencia = ?")) {
                    psUpd.setString(1, nuevoEstado);
                    psUpd.setInt(2, idTour);
                    psUpd.setInt(3, idAgencia);
                    psUpd.executeUpdate();
                }

                JavaApiServer.sendJsonResponse(exchange, 200, "{\"status\":\"success\",\"message\":\"Estado del servicio actualizado a " + nuevoEstado + "\"}");

            } catch (Exception e) {
                e.printStackTrace();
                JavaApiServer.sendJsonResponse(exchange, 500, "{\"status\":\"error\",\"message\":\"Error al cambiar el estado del servicio: " + e.getMessage() + "\"}");
            }
        }

        private void eliminarServicio(HttpExchange exchange, Map<String, Object> params) throws IOException {
            int idTour = 0;
            try {
                idTour = Integer.parseInt(String.valueOf(params.getOrDefault("idServicio", params.getOrDefault("idTour", "0"))));
            } catch (Exception e) {}

            int idAgencia = 0;
            try {
                idAgencia = Integer.parseInt(String.valueOf(params.getOrDefault("idAgencia", "1")));
            } catch (Exception e) { idAgencia = 1; }

            if (idTour <= 0) {
                JavaApiServer.sendJsonResponse(exchange, 400, "{\"status\":\"error\",\"message\":\"ID de servicio no válido\"}");
                return;
            }

            try (Connection con = ConexionDB.getConnection()) {
                // Verificar reservas históricas
                int reservasHistoricas = 0;
                try (PreparedStatement psHist = con.prepareStatement(
                        "SELECT COUNT(*) FROM DetalleReserva dr " +
                        "INNER JOIN TourFecha tf ON dr.idTourFecha = tf.idTourFecha " +
                        "WHERE tf.idTour = ?")) {
                    psHist.setInt(1, idTour);
                    try (ResultSet rsH = psHist.executeQuery()) {
                        if (rsH.next()) reservasHistoricas = rsH.getInt(1);
                    }
                }

                // Si tuvo reservas, NO se elimina: se pausa automáticamente
                if (reservasHistoricas > 0) {
                    try (PreparedStatement psPausar = con.prepareStatement("UPDATE Tour SET estado = 'PAUSADO' WHERE idTour = ? AND idAgencia = ?")) {
                        psPausar.setInt(1, idTour);
                        psPausar.setInt(2, idAgencia);
                        psPausar.executeUpdate();
                    }
                    JavaApiServer.sendJsonResponse(exchange, 400, "{\"status\":\"warning\",\"canPause\":true,\"message\":\"El servicio no se puede eliminar porque tiene historial de reservas registradas. Ha sido pausado automáticamente.\"}");
                    return;
                }

                // Si no tuvo reservas, se elimina completamente
                try (PreparedStatement psDelImg = con.prepareStatement("DELETE FROM TourImagen WHERE idTour = ?")) {
                    psDelImg.setInt(1, idTour);
                    psDelImg.executeUpdate();
                }
                try (PreparedStatement psDelFechas = con.prepareStatement("DELETE FROM TourFecha WHERE idTour = ?")) {
                    psDelFechas.setInt(1, idTour);
                    psDelFechas.executeUpdate();
                }
                try (PreparedStatement psDelTour = con.prepareStatement("DELETE FROM Tour WHERE idTour = ? AND idAgencia = ?")) {
                    psDelTour.setInt(1, idTour);
                    psDelTour.setInt(2, idAgencia);
                    psDelTour.executeUpdate();
                }

                JavaApiServer.sendJsonResponse(exchange, 200, "{\"status\":\"success\",\"message\":\"Servicio eliminado exitosamente.\"}");

            } catch (Exception e) {
                e.printStackTrace();
                JavaApiServer.sendJsonResponse(exchange, 500, "{\"status\":\"error\",\"message\":\"Error al eliminar el servicio: " + e.getMessage() + "\"}");
            }
        }

        private void guardarServicio(HttpExchange exchange, Map<String, Object> params) throws IOException {
            String nombre = String.valueOf(params.getOrDefault("nombre", "")).trim();
            String descripcion = String.valueOf(params.getOrDefault("descripcion", "")).trim();
            String tipoServicio = String.valueOf(params.getOrDefault("tipoServicio", params.getOrDefault("categoria", ""))).trim();
            String duracion = String.valueOf(params.getOrDefault("duracion", "")).trim();
            String estado = String.valueOf(params.getOrDefault("estado", "ACTIVO")).trim();

            int idAgencia = 1;
            int idDestino = 0;
            int idTour = 0;

            try {
                idAgencia = Integer.parseInt(String.valueOf(params.getOrDefault("idAgencia", "1")));
            } catch (Exception ignored) {}

            try {
                idDestino = Integer.parseInt(String.valueOf(params.getOrDefault("idDestino", "2")));
            } catch (Exception ignored) {}

            try {
                idTour = Integer.parseInt(String.valueOf(params.getOrDefault("idServicio", params.getOrDefault("idTour", "0"))));
            } catch (Exception ignored) {}

            double pAdulto = 0.0;
            double pNino = 0.0;
            double pBebe = 0.0;

            try {
                pAdulto = Double.parseDouble(String.valueOf(params.getOrDefault("precioAdulto", params.getOrDefault("precio", "0"))));
                pNino = Double.parseDouble(String.valueOf(params.getOrDefault("precioNino", "0")));
                pBebe = Double.parseDouble(String.valueOf(params.getOrDefault("precioBebe", "0")));
            } catch (Exception e) {
                JavaApiServer.sendJsonResponse(exchange, 400, "{\"status\":\"error\",\"message\":\"Los valores de precios no son números válidos\"}");
                return;
            }

            // Validaciones
            if (nombre.isEmpty() || tipoServicio.isEmpty() || duracion.isEmpty() || idDestino <= 0) {
                JavaApiServer.sendJsonResponse(exchange, 400, "{\"status\":\"error\",\"message\":\"Completa todos los campos obligatorios: Nombre, Tipo, Destino y Duración.\"}");
                return;
            }

            // Validación de precios: Adulto > 0 y Adulto >= Niño >= Bebé
            if (pAdulto <= 0) {
                JavaApiServer.sendJsonResponse(exchange, 400, "{\"status\":\"error\",\"message\":\"El precio de adulto debe ser mayor a 0.\"}");
                return;
            }

            if (pAdulto < pNino || pNino < pBebe || pBebe < 0) {
                JavaApiServer.sendJsonResponse(exchange, 400, "{\"status\":\"error\",\"message\":\"Los precios deben cumplir la regla de jerarquía: Adulto ≥ Niño ≥ Bebé.\"}");
                return;
            }

            try (Connection con = ConexionDB.getConnection()) {
                // Validación: Nombre único dentro de la misma agencia
                try (PreparedStatement psCheckName = con.prepareStatement(
                        "SELECT idTour FROM Tour WHERE idAgencia = ? AND LOWER(TRIM(nombre)) = LOWER(TRIM(?)) AND idTour != ?")) {
                    psCheckName.setInt(1, idAgencia);
                    psCheckName.setString(2, nombre);
                    psCheckName.setInt(3, idTour);
                    try (ResultSet rsN = psCheckName.executeQuery()) {
                        if (rsN.next()) {
                            JavaApiServer.sendJsonResponse(exchange, 400, "{\"status\":\"error\",\"message\":\"Ya existe un servicio con el nombre '" + nombre + "' registrado en tu agencia.\"}");
                            return;
                        }
                    }
                }

                if (idTour > 0) {
                    // Actualizar
                    // Verificar que no esté desactivado por admin
                    try (PreparedStatement psCheckAdmin = con.prepareStatement("SELECT estado FROM Tour WHERE idTour = ? AND idAgencia = ?")) {
                        psCheckAdmin.setInt(1, idTour);
                        psCheckAdmin.setInt(2, idAgencia);
                        try (ResultSet rsA = psCheckAdmin.executeQuery()) {
                            if (rsA.next() && "DESACTIVADO_POR_ADMIN".equalsIgnoreCase(rsA.getString("estado"))) {
                                JavaApiServer.sendJsonResponse(exchange, 403, "{\"status\":\"error\",\"message\":\"No se puede editar un servicio desactivado por el administrador.\"}");
                                return;
                            }
                        }
                    }
                    String ubicacion = String.valueOf(params.getOrDefault("ubicacion", "")).trim();
                    boolean aceptaBebes = Boolean.parseBoolean(String.valueOf(params.getOrDefault("aceptaBebes", "false")));
                    String queIncluye = String.valueOf(params.getOrDefault("queIncluye", "")).trim();
                    String queNoIncluye = String.valueOf(params.getOrDefault("queNoIncluye", "")).trim();
                    int dias = 1;
                    int horas = 6;
                    try { dias = Integer.parseInt(String.valueOf(params.getOrDefault("dias", "1"))); } catch (Exception ignored) {}
                    try { horas = Integer.parseInt(String.valueOf(params.getOrDefault("horas", "6"))); } catch (Exception ignored) {}

                    String sqlUpd = "UPDATE Tour SET nombre = ?, descripcion = ?, categoria = ?, precioAdulto = ?, precioNino = ?, precioBebe = ?, duracion = ?, estado = ?, idDestino = ?, ubicacion = ?, aceptaBebes = ?, queIncluye = ?, queNoIncluye = ?, dias = ?, horas = ? WHERE idTour = ? AND idAgencia = ?";
                    try (PreparedStatement ps = con.prepareStatement(sqlUpd)) {
                        ps.setString(1, nombre);
                        ps.setString(2, descripcion);
                        ps.setString(3, tipoServicio);
                        ps.setDouble(4, pAdulto);
                        ps.setDouble(5, pNino);
                        ps.setDouble(6, pBebe);
                        ps.setString(7, duracion);
                        ps.setString(8, estado);
                        ps.setInt(9, idDestino);
                        ps.setString(10, ubicacion);
                        ps.setBoolean(11, aceptaBebes);
                        ps.setString(12, queIncluye.isEmpty() ? null : queIncluye);
                        ps.setString(13, queNoIncluye.isEmpty() ? null : queNoIncluye);
                        ps.setInt(14, dias);
                        ps.setInt(15, horas);
                        ps.setInt(16, idTour);
                        ps.setInt(17, idAgencia);
                        ps.executeUpdate();
                    }
                } else {
                    // Crear nuevo
                    String ubicacion = String.valueOf(params.getOrDefault("ubicacion", "")).trim();
                    boolean aceptaBebes = Boolean.parseBoolean(String.valueOf(params.getOrDefault("aceptaBebes", "false")));
                    String queIncluye = String.valueOf(params.getOrDefault("queIncluye", "")).trim();
                    String queNoIncluye = String.valueOf(params.getOrDefault("queNoIncluye", "")).trim();
                    int dias = 1;
                    int horas = 6;
                    try { dias = Integer.parseInt(String.valueOf(params.getOrDefault("dias", "1"))); } catch (Exception ignored) {}
                    try { horas = Integer.parseInt(String.valueOf(params.getOrDefault("horas", "6"))); } catch (Exception ignored) {}

                    String slug = nombre.toLowerCase().replaceAll("[^a-z0-9]+", "-") + "-" + (System.currentTimeMillis() % 100000);
                    String sqlIns = "INSERT INTO Tour (idAgencia, idDestino, slug, nombre, descripcion, precioAdulto, precioNino, precioBebe, duracion, categoria, estado, ubicacion, aceptaBebes, queIncluye, queNoIncluye, dias, horas) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";
                    try (PreparedStatement ps = con.prepareStatement(sqlIns, Statement.RETURN_GENERATED_KEYS)) {
                        ps.setInt(1, idAgencia);
                        ps.setInt(2, idDestino);
                        ps.setString(3, slug);
                        ps.setString(4, nombre);
                        ps.setString(5, descripcion);
                        ps.setDouble(6, pAdulto);
                        ps.setDouble(7, pNino);
                        ps.setDouble(8, pBebe);
                        ps.setString(9, duracion);
                        ps.setString(10, tipoServicio);
                        ps.setString(11, estado);
                        ps.setString(12, ubicacion);
                        ps.setBoolean(13, aceptaBebes);
                        ps.setString(14, queIncluye.isEmpty() ? null : queIncluye);
                        ps.setString(15, queNoIncluye.isEmpty() ? null : queNoIncluye);
                        ps.setInt(16, dias);
                        ps.setInt(17, horas);
                        ps.executeUpdate();
                        try (ResultSet rk = ps.getGeneratedKeys()) {
                            if (rk.next()) idTour = rk.getInt(1);
                        }
                    }
                }

                // Procesar imágenes si se enviaron (hasta 5)
                String imagenesStr = String.valueOf(params.getOrDefault("imagenes", "")).trim();
                String imagenPrincipal = String.valueOf(params.getOrDefault("imagenPrincipal", "")).trim();

                if (!imagenesStr.isEmpty() || !imagenPrincipal.isEmpty()) {
                    List<String> urls = new ArrayList<>();
                    if (!imagenPrincipal.isEmpty()) urls.add(imagenPrincipal);

                    if (!imagenesStr.isEmpty()) {
                        String[] parts = imagenesStr.split(",");
                        for (String p : parts) {
                            String clean = p.trim().replaceAll("[\"\\[\\]]", "");
                            if (!clean.isEmpty() && !urls.contains(clean) && urls.size() < 5) {
                                urls.add(clean);
                            }
                        }
                    }

                    if (!urls.isEmpty()) {
                        try (PreparedStatement psDelImg = con.prepareStatement("DELETE FROM TourImagen WHERE idTour = ?")) {
                            psDelImg.setInt(1, idTour);
                            psDelImg.executeUpdate();
                        }
                        for (int i = 0; i < urls.size(); i++) {
                            try (PreparedStatement psInsImg = con.prepareStatement("INSERT INTO TourImagen (idTour, url, esPrincipal, orden) VALUES (?, ?, ?, ?)")) {
                                psInsImg.setInt(1, idTour);
                                psInsImg.setString(2, urls.get(i));
                                psInsImg.setBoolean(3, i == 0);
                                psInsImg.setInt(4, i + 1);
                                psInsImg.executeUpdate();
                            }
                        }
                    }
                }

                JavaApiServer.sendJsonResponse(exchange, 200, "{\"status\":\"success\",\"message\":\"Servicio guardado exitosamente\",\"idServicio\":" + idTour + "}");

            } catch (Exception e) {
                e.printStackTrace();
                JavaApiServer.sendJsonResponse(exchange, 500, "{\"status\":\"error\",\"message\":\"Error al procesar el servicio: " + e.getMessage() + "\"}");
            }
        }
    }

