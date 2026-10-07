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
        String sql = "INSERT INTO Tour (idAgencia, idDestino, slug, nombre, descripcion, precioAdulto, precioNino, precioBebe, duracion, categoria, calificacionPromedio, estado, aceptaBebes, queIncluye, queNoIncluye, dias, horas) " +
                     "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";
        try (Connection con = ConexionDB.getConnection();
             PreparedStatement ps = con.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {

            ps.setInt(1, tour.getIdAgencia());
            ps.setInt(2, tour.getIdDestino());
            ps.setString(3, tour.getSlug());
            ps.setString(4, tour.getNombre());
            ps.setString(5, tour.getDescripcion());
            ps.setBigDecimal(6, tour.getPrecioAdulto());
            ps.setBigDecimal(7, tour.getPrecioNino());
            ps.setBigDecimal(8, tour.getPrecioBebe());
            ps.setString(9, tour.getDuracion());
            ps.setString(10, tour.getCategoria());
            ps.setDouble(11, tour.getCalificacionPromedio());
            ps.setString(12, tour.getEstado() != null ? tour.getEstado() : "ACTIVO");
            ps.setBoolean(13, tour.isAceptaBebes());
            ps.setString(14, tour.getQueIncluye());
            ps.setString(15, tour.getQueNoIncluye());
            ps.setInt(16, tour.getDias() > 0 ? tour.getDias() : 1);
            ps.setInt(17, tour.getHoras() > 0 ? tour.getHoras() : 6);

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
        String sql = "UPDATE Tour SET idAgencia = ?, idDestino = ?, slug = ?, nombre = ?, descripcion = ?, precioAdulto = ?, precioNino = ?, precioBebe = ?, duracion = ?, categoria = ?, estado = ?, aceptaBebes = ?, queIncluye = ?, queNoIncluye = ?, dias = ?, horas = ? WHERE idTour = ?";
        try (Connection con = ConexionDB.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setInt(1, tour.getIdAgencia());
            ps.setInt(2, tour.getIdDestino());
            ps.setString(3, tour.getSlug());
            ps.setString(4, tour.getNombre());
            ps.setString(5, tour.getDescripcion());
            ps.setBigDecimal(6, tour.getPrecioAdulto());
            ps.setBigDecimal(7, tour.getPrecioNino());
            ps.setBigDecimal(8, tour.getPrecioBebe());
            ps.setString(9, tour.getDuracion());
            ps.setString(10, tour.getCategoria());
            ps.setString(11, tour.getEstado());
            ps.setBoolean(12, tour.isAceptaBebes());
            ps.setString(13, tour.getQueIncluye());
            ps.setString(14, tour.getQueNoIncluye());
            ps.setInt(15, tour.getDias() > 0 ? tour.getDias() : 1);
            ps.setInt(16, tour.getHoras() > 0 ? tour.getHoras() : 6);
            ps.setInt(17, tour.getIdTour());
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
        t.setIdDestino(rs.getInt("idDestino"));
        t.setCategoria(rs.getString("categoria"));
        t.setCalificacionPromedio(rs.getDouble("calificacionPromedio"));
        t.setEstado(rs.getString("estado"));
        t.setAceptaBebes(rs.getBoolean("aceptaBebes"));
        t.setQueIncluye(rs.getString("queIncluye"));
        t.setQueNoIncluye(rs.getString("queNoIncluye"));
        t.setDias(rs.getInt("dias"));
        t.setHoras(rs.getInt("horas"));
        return t;
    }
}
