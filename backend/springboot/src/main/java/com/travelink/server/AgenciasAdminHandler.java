
package com.travelink.server;

import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import com.travelink.config.ConexionDB;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
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

        String sqlAgencia = """
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
                COALESCE(a.promedioCalificacion, 5.00) AS rating
            FROM Agencia a
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
                                .append("\"rating\":").append(rs.getBigDecimal("rating")).append(",")
                                .append("\"estado\":\"").append(escapar(rs.getString("estado"))).append("\"")
                                .append("}");
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }

                // 2. Fetch SolicitudAgencia (candidate applications) - omit duplicates by RUC
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
        String decision = extraerTexto(cuerpo, "decision");

        if (idSolicitud == null || idSolicitud <= 0
                || idAdministrador == null || idAdministrador <= 0
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
                // 1. Bloquear y consultar la solicitud.
                String sqlSolicitud = """
                    SELECT idUsuario, razonSocial, estado
                    FROM SolicitudAgencia
                    WHERE idSolicitud = ?
                    FOR UPDATE
                    """;

                int idUsuario;
                String razonSocial;
                String estadoSolicitud;

                try (PreparedStatement ps = con.prepareStatement(sqlSolicitud)) {
                    ps.setInt(1, idSolicitud);

                    try (ResultSet rs = ps.executeQuery()) {
                        if (!rs.next()) {
                            throw new SolicitudException(
                                    404, "No se encontró la solicitud indicada");
                        }

                        idUsuario = rs.getInt("idUsuario");
                        razonSocial = rs.getString("razonSocial");
                        estadoSolicitud = rs.getString("estado");
                    }
                }

                if (estadoSolicitud == null
                        || !estadoSolicitud.equalsIgnoreCase("Pendiente")) {
                    throw new SolicitudException(
                            409, "La solicitud ya fue procesada o no está pendiente");
                }

                // 2. Encontrar la agencia exacta asociada a la solicitud.
                // Se valida usuario y razón social para no activar
                // otra agencia cuando existan usuarios duplicados.
                String sqlAgencia = """
                    SELECT idAgencia
                    FROM Agencia
                    WHERE idUsuario = ?
                      AND razonSocial = ?
                    FOR UPDATE
                    """;

                int idAgencia;

                try (PreparedStatement ps = con.prepareStatement(sqlAgencia)) {
                    ps.setInt(1, idUsuario);
                    ps.setString(2, razonSocial);

                    try (ResultSet rs = ps.executeQuery()) {
                        if (!rs.next()) {
                            throw new SolicitudException(
                                    409,
                                    "No existe una agencia vinculada con el mismo usuario y razón social. Verifica la asociación antes de procesar esta solicitud.");
                        }

                        idAgencia = rs.getInt("idAgencia");

                        if (rs.next()) {
                            throw new SolicitudException(
                                    409,
                                    "Se encontraron varias agencias vinculadas. Corrige la asociación antes de procesar la solicitud.");
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

                    if (ps.executeUpdate() != 1) {
                        throw new SQLException(
                                "No se pudo actualizar la solicitud");
                    }
                }

                // 4. Actualizar el estado de la agencia vinculada.
                String nuevoEstadoAgencia = aceptar ? "ACTIVO" : "INACTIVO";

                String sqlActualizarAgencia = """
                    UPDATE Agencia
                    SET estado = ?
                    WHERE idAgencia = ?
                      AND idUsuario = ?
                    """;

                try (PreparedStatement ps = con.prepareStatement(sqlActualizarAgencia)) {
                    ps.setString(1, nuevoEstadoAgencia);
                    ps.setInt(2, idAgencia);
                    ps.setInt(3, idUsuario);

                    if (ps.executeUpdate() != 1) {
                        throw new SQLException(
                                "No se pudo actualizar el estado de la agencia");
                    }
                }

                // 5. Confirmar ambas modificaciones.
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