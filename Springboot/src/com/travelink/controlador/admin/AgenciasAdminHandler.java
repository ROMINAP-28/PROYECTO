
package com.travelink.controlador.admin;

import com.travelink.server.JavaApiServer;

import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import com.travelink.config.ConexionDB;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class AgenciasAdminHandler implements HttpHandler {

    @Override
    public void handle(HttpExchange exchange) throws IOException {
        exchange.getResponseHeaders().set(
                "Access-Control-Allow-Origin", "*");
        exchange.getResponseHeaders().set(
                "Access-Control-Allow-Methods", "GET, POST, OPTIONS");
        exchange.getResponseHeaders().set(
                "Access-Control-Allow-Headers", "Content-Type");

        String metodo = exchange.getRequestMethod();

        if ("OPTIONS".equalsIgnoreCase(metodo)) {
            exchange.sendResponseHeaders(204, -1);
            return;
        }

        if ("GET".equalsIgnoreCase(metodo)) {
            consultarAgencias(exchange);
        } else if ("POST".equalsIgnoreCase(metodo)) {
            procesarSolicitud(exchange);
        } else {
            responder(exchange, 405,
                    "{\"status\":\"error\",\"message\":\"Método no permitido\"}");
        }
    }

    // =========================================================
    // GET: CONSULTAR SOLICITUDES DE AGENCIAS
    // =========================================================

    private void consultarAgencias(HttpExchange exchange)
            throws IOException {

        StringBuilder json = new StringBuilder();
        json.append("{\"status\":\"success\",\"data\":{\"agencias\":[");
        boolean primero = true;
        java.util.Set<String> rucsProcesados = new java.util.HashSet<>();

        boolean esAdmin = exchange.getRequestURI().getPath().contains("/admin/");
        String sqlAgencia = esAdmin ? """
            SELECT 
                a.idAgencia AS id,
                a.idUsuario,
                COALESCE(NULLIF(a.nombreComercial, ''), a.razonSocial) AS nombre,
                a.razonSocial,
                a.ruc,
                COALESCE(a.email, '') AS email,
                COALESCE(a.telefono, '') AS telefono,
                COALESCE(a.descripcion, 'Agencia turística autorizada.') AS descripcion,
                COALESCE(a.estado, 'ACTIVO') AS estado,
                COALESCE(a.comision, 15.00) AS comision,
                COALESCE(cAgg.rating, a.promedioCalificacion, 0.0) AS rating,
                COALESCE(cAgg.totalCalificaciones, 0) AS totalCalificaciones
            FROM Agencia a
            LEFT JOIN (
                SELECT idAgencia,
                       ROUND(AVG(estrellas), 1) AS rating,
                       COUNT(*) AS totalCalificaciones
                FROM Calificacion
                WHERE (eliminadoPorAdmin IS NULL OR eliminadoPorAdmin = 0)
                GROUP BY idAgencia
            ) cAgg ON a.idAgencia = cAgg.idAgencia
            ORDER BY a.idAgencia ASC
            """ : """
            SELECT 
                a.idAgencia AS id,
                a.idUsuario,
                COALESCE(NULLIF(a.nombreComercial, ''), a.razonSocial) AS nombre,
                a.razonSocial,
                a.ruc,
                COALESCE(a.email, '') AS email,
                COALESCE(a.telefono, '') AS telefono,
                COALESCE(a.descripcion, 'Agencia turística autorizada.') AS descripcion,
                COALESCE(a.estado, 'ACTIVO') AS estado,
                COALESCE(a.comision, 15.00) AS comision,
                COALESCE(cAgg.rating, a.promedioCalificacion, 0.0) AS rating,
                COALESCE(cAgg.totalCalificaciones, 0) AS totalCalificaciones
            FROM Agencia a
            LEFT JOIN (
                SELECT idAgencia,
                       ROUND(AVG(estrellas), 1) AS rating,
                       COUNT(*) AS totalCalificaciones
                FROM Calificacion
                WHERE (eliminadoPorAdmin IS NULL OR eliminadoPorAdmin = 0)
                GROUP BY idAgencia
            ) cAgg ON a.idAgencia = cAgg.idAgencia
            WHERE UPPER(COALESCE(a.estado, 'ACTIVO')) = 'ACTIVO'
            ORDER BY a.idAgencia ASC
            """;

        try (Connection con = ConexionDB.getConnection()) {
            if (con != null) {
                // 1. Fetch Agencias
                try (PreparedStatement ps = con.prepareStatement(sqlAgencia);
                     ResultSet rs = ps.executeQuery()) {
                    while (rs.next()) {
                        String ruc = rs.getString("ruc");
                        if (ruc != null && !ruc.isBlank()) {
                            rucsProcesados.add(ruc.trim());
                        }

                        if (!primero) json.append(",");
                        primero = false;

                        String nombreNom = rs.getString("nombre");
                        if (nombreNom != null) nombreNom = nombreNom.toUpperCase();
                        String razonSocialStr = rs.getString("razonSocial");
                        if (razonSocialStr != null) razonSocialStr = razonSocialStr.toUpperCase();

                        String lower = (nombreNom != null ? nombreNom : "").toLowerCase();
                        String ciudad = "Cusco";
                        if (lower.contains("lima")) ciudad = "Lima";
                        else if (lower.contains("arequipa")) ciudad = "Arequipa";
                        else if (lower.contains("selva") || lower.contains("tambopata")) ciudad = "Madre de Dios";
                        else if (lower.contains("puno") || lower.contains("titicaca")) ciudad = "Puno";
                        else if (lower.contains("ancash") || lower.contains("huascaran")) ciudad = "Áncash";

                        json.append("{")
                                .append("\"id\":").append(rs.getInt("id")).append(",")
                                .append("\"idUsuario\":").append(rs.getInt("idUsuario")).append(",")
                                .append("\"nombre\":\"").append(escapar(nombreNom)).append("\",")
                                .append("\"razonSocial\":\"").append(escapar(razonSocialStr)).append("\",")
                                .append("\"ruc\":\"").append(escapar(ruc)).append("\",")
                                .append("\"representante\":\"").append(escapar(nombreNom)).append("\",")
                                .append("\"telefono\":\"").append(escapar(rs.getString("telefono"))).append("\",")
                                .append("\"email\":\"").append(escapar(rs.getString("email"))).append("\",")
                                .append("\"descripcion\":\"").append(escapar(rs.getString("descripcion"))).append("\",")
                                .append("\"ciudad\":\"").append(ciudad).append("\",")
                                .append("\"comision\":").append(rs.getBigDecimal("comision")).append(",")
                                .append("\"rating\":").append(rs.getDouble("rating")).append(",")
                                .append("\"totalCalificaciones\":").append(rs.getInt("totalCalificaciones")).append(",")
                                .append("\"estado\":\"").append(escapar(rs.getString("estado"))).append("\"")
                                .append("}");
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }

                // 2. Fetch SolicitudAgencia (candidate applications) - only if esAdmin
                if (esAdmin) {
                    String sqlSol = """
                        SELECT idSolicitud, idUsuario, razonSocial, ruc, representanteLegal, telefonoContacto, correoContacto, estado, COALESCE(motivoRechazo, '') AS motivoRechazo
                        FROM SolicitudAgencia
                        ORDER BY idSolicitud DESC
                        """;
                    try (PreparedStatement ps = con.prepareStatement(sqlSol);
                         ResultSet rs = ps.executeQuery()) {
                    while (rs.next()) {
                        String rucSol = rs.getString("ruc");
                        if (rucSol != null && rucsProcesados.contains(rucSol.trim())) {
                            continue; // Skip duplicate agency request already listed in Agencia table
                        }
                        if (rucSol != null && !rucSol.isBlank()) {
                            rucsProcesados.add(rucSol.trim());
                        }

                        if (!primero) json.append(",");
                        primero = false;

                        String razonSocialStr = rs.getString("razonSocial");
                        if (razonSocialStr != null) razonSocialStr = razonSocialStr.toUpperCase();
                        String repStr = rs.getString("representanteLegal");
                        if (repStr != null) repStr = repStr.toUpperCase();

                        String lower = (razonSocialStr != null ? razonSocialStr : "").toLowerCase();
                        String ciudad = "Cusco";
                        if (lower.contains("pacific") || lower.contains("lima")) ciudad = "Lima";
                        else if (lower.contains("chachapoyas")) ciudad = "Amazonas";

                        json.append("{")
                                .append("\"id\":").append(1000 + rs.getInt("idSolicitud")).append(",")
                                .append("\"idSolicitud\":").append(rs.getInt("idSolicitud")).append(",")
                                .append("\"idUsuario\":").append(rs.getInt("idUsuario")).append(",")
                                .append("\"nombre\":\"").append(escapar(razonSocialStr)).append("\",")
                                .append("\"razonSocial\":\"").append(escapar(razonSocialStr)).append("\",")
                                .append("\"ruc\":\"").append(escapar(rucSol)).append("\",")
                                .append("\"representante\":\"").append(escapar(repStr)).append("\",")
                                .append("\"telefono\":\"").append(escapar(rs.getString("telefonoContacto"))).append("\",")
                                .append("\"email\":\"").append(escapar(rs.getString("correoContacto"))).append("\",")
                                .append("\"ciudad\":\"").append(ciudad).append("\",")
                                .append("\"comision\":15.00,")
                                .append("\"rating\":5.00,")
                                .append("\"motivoRechazo\":\"").append(escapar(rs.getString("motivoRechazo"))).append("\",")
                                .append("\"estado\":\"").append(escapar(rs.getString("estado"))).append("\"")
                                .append("}");
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }
    } catch (Exception e) {
        e.printStackTrace();
    }

        json.append("]}}");
        responder(exchange, 200, json.toString());
    }

    // =========================================================
    // POST: ACEPTAR O RECHAZAR UNA SOLICITUD
    // =========================================================

    private void procesarSolicitud(HttpExchange exchange)
            throws IOException {

        String cuerpo = new String(
                exchange.getRequestBody().readAllBytes(),
                StandardCharsets.UTF_8
        );

        Integer idSolicitud = extraerEntero(cuerpo, "idSolicitud");
        Integer idAdministrador = extraerEntero(cuerpo, "idAdministrador");
        if (idAdministrador == null || idAdministrador <= 0) {
            idAdministrador = 6; // Admin por defecto
        }
        String decision = extraerTexto(cuerpo, "decision");

        // Soporte directo para actualizar estado de Agencia existente (ej: Suspender/Activar)
        Integer idAgenciaDirecto = extraerEntero(cuerpo, "idAgencia");
        if (idAgenciaDirecto == null) idAgenciaDirecto = extraerEntero(cuerpo, "id");
        String estadoDirecto = extraerTexto(cuerpo, "estado");
        if (idAgenciaDirecto != null && idAgenciaDirecto > 0 && estadoDirecto != null && !estadoDirecto.isEmpty() && idSolicitud == null) {
            String nuevoEst = estadoDirecto.toUpperCase().contains("SUSPEND") || estadoDirecto.toUpperCase().contains("INACTIV") ? "INACTIVO" : "ACTIVO";
            try (Connection con = ConexionDB.getConnection();
                 PreparedStatement ps = con.prepareStatement("UPDATE Agencia SET estado = ? WHERE idAgencia = ?")) {
                ps.setString(1, nuevoEst);
                ps.setInt(2, idAgenciaDirecto);
                ps.executeUpdate();
                responder(exchange, 200, "{\"status\":\"success\",\"message\":\"Estado de agencia actualizado a " + nuevoEst + "\"}");
                return;
            } catch (Exception e) {
                e.printStackTrace();
                responder(exchange, 500, "{\"status\":\"error\",\"message\":\"Error al actualizar agencia en BD\"}");
                return;
            }
        }

        if (idSolicitud == null || idSolicitud <= 0
                || decision == null
                || (!decision.equalsIgnoreCase("Aceptar")
                && !decision.equalsIgnoreCase("Rechazar"))) {

            responder(exchange, 400,
                    "{\"status\":\"error\",\"message\":\"Datos de solicitud inválidos\"}");
            return;
        }

        boolean aceptar = decision.equalsIgnoreCase("Aceptar");

        try (Connection con = ConexionDB.getConnection()) {

            if (con == null) {
                throw new SQLException("No se pudo conectar a la base de datos");
            }

            con.setAutoCommit(false);

            try {
                // Soportar offset 1000 en idSolicitud si vino desde la interfaz
                int targetId = idSolicitud;
                if (targetId > 1000) {
                    String checkSql = "SELECT idSolicitud FROM SolicitudAgencia WHERE idSolicitud = ?";
                    try (PreparedStatement psCheck = con.prepareStatement(checkSql)) {
                        psCheck.setInt(1, targetId);
                        try (ResultSet rsCheck = psCheck.executeQuery()) {
                            if (!rsCheck.next()) {
                                targetId = targetId - 1000;
                            }
                        }
                    }
                }
                idSolicitud = targetId;

                // 1. Bloquear y consultar la solicitud.
                String sqlSolicitud = """
                    SELECT idUsuario, razonSocial, ruc, representanteLegal, telefonoContacto, correoContacto, estado
                    FROM SolicitudAgencia
                    WHERE idSolicitud = ?
                    FOR UPDATE
                    """;

                int idUsuario;
                String razonSocial;
                String rucSol;
                String repSol;
                String telSol;
                String correoSol;
                String estadoSolicitud;

                try (PreparedStatement ps = con.prepareStatement(sqlSolicitud)) {
                    ps.setInt(1, idSolicitud);

                    try (ResultSet rs = ps.executeQuery()) {
                        if (!rs.next()) {
                            throw new SolicitudException(
                                    404, "No se encontró la solicitud indicada (#" + idSolicitud + ")");
                        }

                        idUsuario = rs.getInt("idUsuario");
                        razonSocial = rs.getString("razonSocial");
                        rucSol = rs.getString("ruc");
                        repSol = rs.getString("representanteLegal");
                        telSol = rs.getString("telefonoContacto");
                        correoSol = rs.getString("correoContacto");
                        estadoSolicitud = rs.getString("estado");
                    }
                }

                // 2. Encontrar o crear la agencia asociada a la solicitud en la tabla Agencia.
                String sqlAgencia = """
                    SELECT idAgencia
                    FROM Agencia
                    WHERE idUsuario = ?
                       OR (ruc IS NOT NULL AND ruc != '' AND ruc = ?)
                       OR (razonSocial IS NOT NULL AND razonSocial = ?)
                    FOR UPDATE
                    """;

                int idAgencia = 0;

                try (PreparedStatement ps = con.prepareStatement(sqlAgencia)) {
                    ps.setInt(1, idUsuario);
                    ps.setString(2, rucSol != null ? rucSol.trim() : "");
                    ps.setString(3, razonSocial != null ? razonSocial.trim() : "");

                    try (ResultSet rs = ps.executeQuery()) {
                        if (rs.next()) {
                            idAgencia = rs.getInt("idAgencia");
                        }
                    }
                }

                String nuevoEstadoAgencia = aceptar ? "ACTIVO" : "INACTIVO";

                if (idAgencia > 0) {
                    // Actualizar agencia existente
                    String sqlActualizarAgencia = """
                        UPDATE Agencia
                        SET estado = ?,
                            razonSocial = COALESCE(NULLIF(razonSocial, ''), ?),
                            ruc = COALESCE(NULLIF(ruc, ''), ?),
                            email = COALESCE(NULLIF(email, ''), ?),
                            telefono = COALESCE(NULLIF(telefono, ''), ?)
                        WHERE idAgencia = ?
                        """;
                    try (PreparedStatement ps = con.prepareStatement(sqlActualizarAgencia)) {
                        ps.setString(1, nuevoEstadoAgencia);
                        ps.setString(2, razonSocial != null ? razonSocial : "");
                        ps.setString(3, rucSol != null ? rucSol : "");
                        ps.setString(4, correoSol != null ? correoSol : "");
                        ps.setString(5, telSol != null ? telSol : "");
                        ps.setInt(6, idAgencia);
                        ps.executeUpdate();
                    }
                } else if (aceptar) {
                    // Si no existía en tabla Agencia y fue aceptada, insertarla en tabla Agencia como ACTIVO
                    String sqlInsertarAgencia = """
                        INSERT INTO Agencia (idUsuario, razonSocial, nombreComercial, ruc, telefono, email, descripcion, estado, comision, promedioCalificacion)
                        VALUES (?, ?, ?, ?, ?, ?, 'Agencia turística autorizada.', 'ACTIVO', 15.00, 5.00)
                        """;
                    try (PreparedStatement ps = con.prepareStatement(sqlInsertarAgencia, Statement.RETURN_GENERATED_KEYS)) {
                        ps.setInt(1, idUsuario);
                        ps.setString(2, razonSocial != null ? razonSocial : "");
                        ps.setString(3, razonSocial != null ? razonSocial : "");
                        ps.setString(4, rucSol != null ? rucSol : "");
                        ps.setString(5, telSol != null ? telSol : "");
                        ps.setString(6, correoSol != null ? correoSol : "");
                        ps.executeUpdate();
                        try (ResultSet rsKey = ps.getGeneratedKeys()) {
                            if (rsKey.next()) {
                                idAgencia = rsKey.getInt(1);
                            }
                        }
                    }
                }

                // 3. Actualizar el estado de la solicitud.
                String nuevoEstado = aceptar ? "Aceptado" : "Rechazado";
                String motivo = aceptar
                        ? null
                        : "Solicitud rechazada por administración";

                String sqlActualizarSolicitud = """
                    UPDATE SolicitudAgencia
                    SET estado = ?,
                        idAdministrador = ?,
                        fechaRevision = CURRENT_TIMESTAMP,
                        motivoRechazo = ?
                    WHERE idSolicitud = ?
                    """;

                try (PreparedStatement ps = con.prepareStatement(sqlActualizarSolicitud)) {
                    ps.setString(1, nuevoEstado);
                    ps.setInt(2, idAdministrador);
                    ps.setString(3, motivo);
                    ps.setInt(4, idSolicitud);
                    ps.executeUpdate();
                }

                // 3.5. Si se aprueba la solicitud, activar también la cuenta de Usuario correspondiente
                if (idUsuario > 0) {
                    String sqlActualizarUsuario = "UPDATE Usuario SET estado = ? WHERE idUsuario = ?";
                    try (PreparedStatement psU = con.prepareStatement(sqlActualizarUsuario)) {
                        psU.setString(1, aceptar ? "ACTIVO" : "INACTIVO");
                        psU.setInt(2, idUsuario);
                        psU.executeUpdate();
                    }

                    // 3.6. Notificar a la agencia sobre el resultado de su solicitud
                    try (PreparedStatement psNotif = con.prepareStatement(
                            "INSERT INTO Notificacion (idUsuario, titulo, mensaje, tipo, icono, color, leida, fechaEnvio) VALUES (?, ?, ?, ?, ?, ?, 0, NOW())")) {
                        psNotif.setInt(1, idUsuario);
                        psNotif.setString(2, aceptar ? "Solicitud de Agencia Aprobada" : "Solicitud de Agencia Rechazada");
                        psNotif.setString(3, aceptar
                            ? "¡Felicidades! Tu solicitud para registrar tu agencia fue aprobada. Ya puedes acceder y publicar tus paquetes."
                            : "Tu solicitud para registrar tu agencia fue rechazada por administración.");
                        psNotif.setString(4, aceptar ? "EXITO" : "ALERTA");
                        psNotif.setString(5, aceptar ? "ti ti-circle-check" : "ti ti-circle-x");
                        psNotif.setString(6, aceptar ? "green" : "red");
                        psNotif.executeUpdate();
                    } catch (Exception ignored) {}
                }

                // 4. Confirmar transacción
                con.commit();

                String mensaje = aceptar
                        ? "La solicitud fue aceptada y la agencia quedó activa."
                        : "La solicitud fue rechazada y la agencia quedó inactiva.";

                responder(exchange, 200,
                        "{\"status\":\"success\",\"message\":\""
                                + escapar(mensaje)
                                + "\",\"idSolicitud\":" + idSolicitud
                                + ",\"idAgencia\":" + idAgencia
                                + ",\"estadoSolicitud\":\"" + nuevoEstado
                                + "\",\"estadoAgencia\":\"" + nuevoEstadoAgencia
                                + "\"}");

            } catch (Exception e) {
                try {
                    con.rollback();
                } catch (SQLException rollbackError) {
                    e.addSuppressed(rollbackError);
                }
                throw e;
            } finally {
                try {
                    con.setAutoCommit(true);
                } catch (SQLException e) {
                    e.printStackTrace();
                }
            }

        } catch (SolicitudException e) {
            responder(exchange, e.codigo,
                    "{\"status\":\"error\",\"message\":\""
                            + escapar(e.getMessage()) + "\"}");
        } catch (Exception e) {
            e.printStackTrace();
            responder(exchange, 500,
                    "{\"status\":\"error\",\"message\":\"Error al procesar la solicitud en la base de datos\"}");
        }
    }

    // =========================================================
    // UTILIDADES PARA LEER LOS DATOS JSON
    // =========================================================

    private Integer extraerEntero(String json, String campo) {
        Pattern patron = Pattern.compile(
                "\"" + Pattern.quote(campo) + "\"\\s*:\\s*(\\d+)"
        );
        Matcher matcher = patron.matcher(json);

        if (matcher.find()) {
            try {
                return Integer.parseInt(matcher.group(1));
            } catch (NumberFormatException e) {
                return null;
            }
        }
        return null;
    }

    private String extraerTexto(String json, String campo) {
        Pattern patron = Pattern.compile(
                "\"" + Pattern.quote(campo)
                        + "\"\\s*:\\s*\"((?:\\\\.|[^\"\\\\])*)\""
        );
        Matcher matcher = patron.matcher(json);

        if (matcher.find()) {
            return matcher.group(1)
                    .replace("\\\"", "\"")
                    .replace("\\\\", "\\");
        }
        return null;
    }

    private String escapar(String valor) {
        if (valor == null) return "";

        return valor.replace("\\", "\\\\")
                .replace("\"", "\\\"")
                .replace("\n", "\\n")
                .replace("\r", "\\r")
                .replace("\t", "\\t");
    }

    private void responder(
            HttpExchange exchange,
            int codigo,
            String respuesta
    ) throws IOException {

        byte[] bytes = respuesta.getBytes(StandardCharsets.UTF_8);

        exchange.getResponseHeaders().set(
                "Content-Type", "application/json; charset=UTF-8");

        exchange.sendResponseHeaders(codigo, bytes.length);

        try (var salida = exchange.getResponseBody()) {
            salida.write(bytes);
        }
    }

    private static class SolicitudException extends Exception {
        private final int codigo;

        private SolicitudException(int codigo, String mensaje) {
            super(mensaje);
            this.codigo = codigo;
        }
    }
}
