package com.travelink.persistencia;

import com.travelink.config.ConexionDB;
import com.travelink.entidades.Calificacion;
import com.travelink.repositorio.CalificacionRepositorio;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/**
 * ROL: Consulta Especializada (JDBC)
 * Responsabilidad: Inserción directa y consultas de reseñas por agencia/turista.
 */
public class CalificacionPersistencia {
    public Calificacion guardar(Calificacion calificacion) {
        if (calificacion == null) return null;

        // 1. Verificar si ya existe una calificación para esta reserva
        if (calificacion.getIdReserva() > 0) {
            Optional<Calificacion> existente = buscarPorReserva(calificacion.getIdReserva());
            if (existente.isPresent()) {
                Calificacion c = existente.get();
                String updateSql = "UPDATE Calificacion SET estrellas = ?, comentario = ?, fechaCalificacion = NOW() WHERE idCalificacion = ?";
                try (Connection con = ConexionDB.getConnection();
                     PreparedStatement ps = con.prepareStatement(updateSql)) {
                    ps.setInt(1, calificacion.getEstrellas());
                    ps.setString(2, calificacion.getComentario());
                    ps.setInt(3, c.getIdCalificacion());
                    ps.executeUpdate();
                    c.setEstrellas(calificacion.getEstrellas());
                    c.setComentario(calificacion.getComentario());
                    return c;
                } catch (SQLException e) {
                    e.printStackTrace();
                    return null;
                }
            }
        }

        // 2. Si no existe, insertar nueva calificación
        String sql = "INSERT INTO Calificacion (idUsuario, idAgencia, idReserva, estrellas, comentario, fechaCalificacion) VALUES (?, ?, ?, ?, ?, NOW())";
        try (Connection con = ConexionDB.getConnection()) {
            try (PreparedStatement psFk = con.prepareStatement("SET FOREIGN_KEY_CHECKS = 0;")) {
                psFk.execute();
            }
            try (PreparedStatement ps = con.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
                ps.setInt(1, calificacion.getIdUsuario());
                ps.setInt(2, calificacion.getIdAgencia());
                ps.setInt(3, calificacion.getIdReserva());
                ps.setInt(4, calificacion.getEstrellas());
                ps.setString(5, calificacion.getComentario());

                ps.executeUpdate();
                try (ResultSet rs = ps.getGeneratedKeys()) {
                    if (rs.next()) {
                        calificacion.setIdCalificacion(rs.getInt(1));
                    }
                }
            }
            try (PreparedStatement psFk = con.prepareStatement("SET FOREIGN_KEY_CHECKS = 1;")) {
                psFk.execute();
            }
            return calificacion;
        } catch (SQLException e) {
            e.printStackTrace();
            return null;
        }
    }
    public Optional<Calificacion> buscarPorId(int idCalificacion) {
        String sql = "SELECT * FROM Calificacion WHERE idCalificacion = ?";
        try (Connection con = ConexionDB.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setInt(1, idCalificacion);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return Optional.of(mapResultSetToCalificacion(rs));
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return Optional.empty();
    }
    public List<Calificacion> buscarPorAgencia(int idAgencia) {
        List<Calificacion> lista = new ArrayList<>();
        String sql = "SELECT * FROM Calificacion WHERE idAgencia = ? ORDER BY fechaCalificacion DESC";
        try (Connection con = ConexionDB.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setInt(1, idAgencia);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    lista.add(mapResultSetToCalificacion(rs));
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return lista;
    }
    public List<Calificacion> buscarPorUsuario(int idUsuario) {
        List<Calificacion> lista = new ArrayList<>();
        String sql = "SELECT * FROM Calificacion WHERE idUsuario = ? ORDER BY fechaCalificacion DESC";
        try (Connection con = ConexionDB.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setInt(1, idUsuario);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    lista.add(mapResultSetToCalificacion(rs));
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return lista;
    }
    public Optional<Calificacion> buscarPorReserva(int idReserva) {
        String sql = "SELECT * FROM Calificacion WHERE idReserva = ? LIMIT 1";
        try (Connection con = ConexionDB.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setInt(1, idReserva);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return Optional.of(mapResultSetToCalificacion(rs));
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return Optional.empty();
    }

    private Calificacion mapResultSetToCalificacion(ResultSet rs) throws SQLException {
        Calificacion c = new Calificacion();
        c.setIdCalificacion(rs.getInt("idCalificacion"));
        c.setIdUsuario(rs.getInt("idUsuario"));
        c.setIdAgencia(rs.getInt("idAgencia"));
        c.setIdReserva(rs.getInt("idReserva"));
        c.setEstrellas(rs.getInt("estrellas"));
        c.setComentario(rs.getString("comentario"));
        c.setFechaCalificacion(rs.getTimestamp("fechaCalificacion"));
        return c;
    }
}
