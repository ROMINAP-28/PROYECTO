package com.travelink.persistencia;

import com.travelink.config.ConexionDB;
import java.sql.*;
import java.util.*;

public class NotificacionPersistencia {

    /**
     * Registra una notificación en la base de datos de manera persistente.
     */
    public static boolean registrar(Integer idUsuario, String titulo, String mensaje, String tipo, String icono, String color, String enlace) {
        String sql = "INSERT INTO Notificacion (idUsuario, titulo, mensaje, tipo, icono, color, enlace, leida, fechaEnvio) VALUES (?, ?, ?, ?, ?, ?, ?, FALSE, NOW())";
        try (Connection con = ConexionDB.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {

            if (idUsuario != null && idUsuario > 0) {
                ps.setInt(1, idUsuario);
            } else {
                ps.setNull(1, Types.INTEGER);
            }
            ps.setString(2, titulo);
            ps.setString(3, mensaje);
            ps.setString(4, (tipo != null && !tipo.isEmpty()) ? tipo : "INFO");
            ps.setString(5, (icono != null && !icono.isEmpty()) ? icono : "ti ti-bell");
            ps.setString(6, (color != null && !color.isEmpty()) ? color : "blue");
            ps.setString(7, enlace);

            int filas = ps.executeUpdate();
            return filas > 0;
        } catch (SQLException e) {
            System.err.println("Error al registrar notificación en BD: " + e.getMessage());
            e.printStackTrace();
            return false;
        }
    }

    /**
     * Obtiene la lista de notificaciones recientes para el panel.
     */
    public List<Map<String, Object>> obtenerNotificaciones(Integer idUsuario, int limite) {
        List<Map<String, Object>> lista = new ArrayList<>();
        StringBuilder sql = new StringBuilder("SELECT idNotificacion, idUsuario, titulo, mensaje, tipo, icono, color, enlace, leida, fechaEnvio FROM Notificacion ");
        if (idUsuario != null && idUsuario > 0) {
            sql.append("WHERE idUsuario = ? OR idUsuario IS NULL ");
        }
        sql.append("ORDER BY idNotificacion DESC LIMIT ?");

        try (Connection con = ConexionDB.getConnection();
             PreparedStatement ps = con.prepareStatement(sql.toString())) {

            int pIndex = 1;
            if (idUsuario != null && idUsuario > 0) {
                ps.setInt(pIndex++, idUsuario);
            }
            ps.setInt(pIndex, (limite > 0) ? limite : 20);

            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    Map<String, Object> item = new HashMap<>();
                    item.put("id", rs.getInt("idNotificacion"));
                    item.put("idUsuario", rs.getObject("idUsuario"));
                    item.put("titulo", rs.getString("titulo"));
                    item.put("mensaje", rs.getString("mensaje"));
                    item.put("tipo", rs.getString("tipo"));
                    item.put("icono", rs.getString("icono") != null ? rs.getString("icono") : "ti ti-bell");
                    item.put("color", rs.getString("color") != null ? rs.getString("color") : "blue");
                    item.put("enlace", rs.getString("enlace") != null ? rs.getString("enlace") : "");
                    item.put("leida", rs.getBoolean("leida"));

                    Timestamp ts = rs.getTimestamp("fechaEnvio");
                    item.put("fecha", ts != null ? ts.toString() : "");
                    item.put("tiempoRelativo", calcularTiempoRelativo(ts));
                    lista.add(item);
                }
            }
        } catch (SQLException e) {
            System.err.println("Error al obtener notificaciones: " + e.getMessage());
            e.printStackTrace();
        }
        return lista;
    }

    /**
     * Cuenta cuántas notificaciones no leídas existen.
     */
    public int contarNoLeidas(Integer idUsuario) {
        StringBuilder sql = new StringBuilder("SELECT COUNT(*) FROM Notificacion WHERE leida = FALSE ");
        if (idUsuario != null && idUsuario > 0) {
            sql.append("AND (idUsuario = ? OR idUsuario IS NULL)");
        }
        try (Connection con = ConexionDB.getConnection();
             PreparedStatement ps = con.prepareStatement(sql.toString())) {

            if (idUsuario != null && idUsuario > 0) {
                ps.setInt(1, idUsuario);
            }
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) return rs.getInt(1);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return 0;
    }

    /**
     * Marca todas las notificaciones como leídas.
     */
    public boolean marcarTodasComoLeidas(Integer idUsuario) {
        StringBuilder sql = new StringBuilder("UPDATE Notificacion SET leida = TRUE WHERE leida = FALSE ");
        if (idUsuario != null && idUsuario > 0) {
            sql.append("AND (idUsuario = ? OR idUsuario IS NULL)");
        }
        try (Connection con = ConexionDB.getConnection();
             PreparedStatement ps = con.prepareStatement(sql.toString())) {

            if (idUsuario != null && idUsuario > 0) {
                ps.setInt(1, idUsuario);
            }
            ps.executeUpdate();
            return true;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    /**
     * Marca una notificación específica como leída.
     */
    public boolean marcarComoLeida(int idNotificacion) {
        String sql = "UPDATE Notificacion SET leida = TRUE WHERE idNotificacion = ?";
        try (Connection con = ConexionDB.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setInt(1, idNotificacion);
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    private String calcularTiempoRelativo(Timestamp ts) {
        if (ts == null) return "Reciente";
        long diff = System.currentTimeMillis() - ts.getTime();
        long seg = diff / 1000;
        if (seg < 60) return "Hace un momento";
        long min = seg / 60;
        if (min < 60) return "Hace " + min + (min == 1 ? " minuto" : " minutos");
        long horas = min / 60;
        if (horas < 24) return "Hace " + horas + (horas == 1 ? " hora" : " horas");
        long dias = horas / 24;
        return "Hace " + dias + (dias == 1 ? " día" : " días");
    }
}
