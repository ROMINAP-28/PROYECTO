package com.travelink.persistencia;

import com.travelink.config.ConexionDB;
import com.travelink.entidades.Tour;
import com.travelink.repositorio.TourRepositorio;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class TourPersistencia implements TourRepositorio {

    @Override
    public Tour guardar(Tour tour) {
        String sql = "INSERT INTO Tour (idAgencia, slug, nombre, descripcion, precioAdulto, precioNino, precioBebe, duracion, ubicacion, categoria, calificacionPromedio, estado) " +
                     "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";
        try (Connection con = ConexionDB.getConnection();
             PreparedStatement ps = con.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {

            ps.setInt(1, tour.getIdAgencia());
            ps.setString(2, tour.getSlug());
            ps.setString(3, tour.getNombre());
            ps.setString(4, tour.getDescripcion());
            ps.setBigDecimal(5, tour.getPrecioAdulto());
            ps.setBigDecimal(6, tour.getPrecioNino());
            ps.setBigDecimal(7, tour.getPrecioBebe());
            ps.setString(8, tour.getDuracion());
            ps.setString(9, tour.getUbicacion());
            ps.setString(10, tour.getCategoria());
            ps.setDouble(11, tour.getCalificacionPromedio());
            ps.setString(12, tour.getEstado() != null ? tour.getEstado() : "ACTIVO");

            ps.executeUpdate();
            try (ResultSet rs = ps.getGeneratedKeys()) {
                if (rs.next()) {
                    tour.setIdTour(rs.getInt(1));
                }
            }
            return tour;
        } catch (SQLException e) {
            e.printStackTrace();
            return null;
        }
    }

    @Override
    public Optional<Tour> buscarPorId(int idTour) {
        String sql = "SELECT * FROM Tour WHERE idTour = ?";
        try (Connection con = ConexionDB.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setInt(1, idTour);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return Optional.of(mapResultSetToTour(rs));
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return Optional.empty();
    }

    @Override
    public Optional<Tour> buscarPorSlug(String slug) {
        String sql = "SELECT * FROM Tour WHERE slug = ?";
        try (Connection con = ConexionDB.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setString(1, slug);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return Optional.of(mapResultSetToTour(rs));
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return Optional.empty();
    }

    @Override
    public List<Tour> listarTodos() {
        List<Tour> lista = new ArrayList<>();
        String sql = "SELECT * FROM Tour WHERE estado = 'ACTIVO' ORDER BY idTour ASC";
        try (Connection con = ConexionDB.getConnection();
             PreparedStatement ps = con.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                lista.add(mapResultSetToTour(rs));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return lista;
    }

    @Override
    public List<Tour> buscarPorAgencia(int idAgencia) {
        List<Tour> lista = new ArrayList<>();
        String sql = "SELECT * FROM Tour WHERE idAgencia = ? AND estado = 'ACTIVO'";
        try (Connection con = ConexionDB.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setInt(1, idAgencia);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    lista.add(mapResultSetToTour(rs));
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return lista;
    }

    @Override
    public List<Tour> buscarPorCategoria(String categoria) {
        List<Tour> lista = new ArrayList<>();
        String sql = "SELECT * FROM Tour WHERE categoria LIKE ? AND estado = 'ACTIVO'";
        try (Connection con = ConexionDB.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setString(1, "%" + categoria + "%");
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    lista.add(mapResultSetToTour(rs));
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return lista;
    }

    @Override
    public boolean actualizar(Tour tour) {
        String sql = "UPDATE Tour SET idAgencia = ?, slug = ?, nombre = ?, descripcion = ?, precioAdulto = ?, precioNino = ?, precioBebe = ?, duracion = ?, ubicacion = ?, categoria = ?, estado = ? WHERE idTour = ?";
        try (Connection con = ConexionDB.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setInt(1, tour.getIdAgencia());
            ps.setString(2, tour.getSlug());
            ps.setString(3, tour.getNombre());
            ps.setString(4, tour.getDescripcion());
            ps.setBigDecimal(5, tour.getPrecioAdulto());
            ps.setBigDecimal(6, tour.getPrecioNino());
            ps.setBigDecimal(7, tour.getPrecioBebe());
            ps.setString(8, tour.getDuracion());
            ps.setString(9, tour.getUbicacion());
            ps.setString(10, tour.getCategoria());
            ps.setString(11, tour.getEstado());
            ps.setInt(12, tour.getIdTour());
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    @Override
    public boolean eliminar(int idTour) {
        String sql = "DELETE FROM Tour WHERE idTour = ?";
        try (Connection con = ConexionDB.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setInt(1, idTour);
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    private Tour mapResultSetToTour(ResultSet rs) throws SQLException {
        Tour t = new Tour();
        t.setIdTour(rs.getInt("idTour"));
        t.setIdAgencia(rs.getInt("idAgencia"));
        t.setSlug(rs.getString("slug"));
        t.setNombre(rs.getString("nombre"));
        t.setDescripcion(rs.getString("descripcion"));
        t.setPrecioAdulto(rs.getBigDecimal("precioAdulto"));
        t.setPrecioNino(rs.getBigDecimal("precioNino"));
        t.setPrecioBebe(rs.getBigDecimal("precioBebe"));
        t.setDuracion(rs.getString("duracion"));
        t.setUbicacion(rs.getString("ubicacion"));
        t.setCategoria(rs.getString("categoria"));
        t.setCalificacionPromedio(rs.getDouble("calificacionPromedio"));
        t.setEstado(rs.getString("estado"));
        return t;
    }
}
