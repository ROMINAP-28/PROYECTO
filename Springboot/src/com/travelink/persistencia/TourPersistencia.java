package com.travelink.persistencia;

import com.travelink.config.ConexionDB;
import com.travelink.entidades.Tour;
import com.travelink.repositorio.TourRepositorio;

import java.math.BigDecimal;
import java.sql.*;
import java.text.Normalizer;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/**
 * ROL: Consulta Especializada (JDBC)
 * Responsabilidad: Manejo de slugs normalizados, formateo de duraciones, y consultas complejas de tours.
 */
public class TourPersistencia {

    public static String generarSlug(String nombre) {
        if (nombre == null) return "tour-" + System.currentTimeMillis();
        String normalized = Normalizer.normalize(nombre, Normalizer.Form.NFD)
                .replaceAll("[\\p{InCombiningDiacriticalMarks}]", "")
                .toLowerCase()
                .replaceAll("[^a-z0-9\\s-]", "")
                .trim()
                .replaceAll("\\s+", "-");
        return normalized.isEmpty() ? "tour-" + System.currentTimeMillis() : normalized;
    }

    public static String generarDuracion(int dias, int horas) {
        if (dias > 0 && horas > 0) {
            return dias + (dias == 1 ? " día" : " días") + " y " + horas + (horas == 1 ? " hora" : " horas");
        } else if (dias > 0) {
            return dias + (dias == 1 ? " día" : " días");
        } else if (horas > 0) {
            return horas + (horas == 1 ? " hora" : " horas");
        }
        return "1 día";
    }

    public static String obtenerNombreDestino(Connection con, int idDestino) {
        String sql = "SELECT nombre FROM Destino WHERE idDestino = ?";
        try (PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setInt(1, idDestino);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) return rs.getString("nombre");
            }
        } catch (Exception ignored) {}
        return "Perú";
    }
    public Tour guardar(Tour tour) {
        String sql = "INSERT INTO Tour (idAgencia, idDestino, slug, nombre, descripcion, descripcionCorta, itinerario, " +
                     "aceptaNinos, edadMaxNino, edadMaxBebe, descuento, condiciones, minAsientosOferta, " +
                     "precioAdulto, precioNino, precioBebe, duracion, ubicacion, categoria, calificacionPromedio, " +
                     "estado, aceptaBebes, queIncluye, queNoIncluye, dias, horas) " +
                     "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";
        try (Connection con = ConexionDB.getConnection()) {
            if (tour.getSlug() == null || tour.getSlug().trim().isEmpty()) {
                String baseSlug = generarSlug(tour.getNombre());
                tour.setSlug(baseSlug + "-" + (System.currentTimeMillis() % 10000));
            }
            if (tour.getUbicacion() == null || tour.getUbicacion().trim().isEmpty()) {
                tour.setUbicacion(obtenerNombreDestino(con, tour.getIdDestino()));
            }
            tour.setDuracion(generarDuracion(tour.getDias(), tour.getHoras()));

            if (!tour.isAceptaNinos()) {
                tour.setPrecioNino(BigDecimal.ZERO);
            }
            if (!tour.isAceptaBebes()) {
                tour.setPrecioBebe(BigDecimal.ZERO);
            }

            try (PreparedStatement ps = con.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
                ps.setInt(1, tour.getIdAgencia());
                ps.setInt(2, tour.getIdDestino());
                ps.setString(3, tour.getSlug());
                ps.setString(4, tour.getNombre());
                ps.setString(5, tour.getDescripcion());
                ps.setString(6, tour.getDescripcionCorta());
                ps.setString(7, tour.getItinerario());
                ps.setBoolean(8, tour.isAceptaNinos());
                ps.setInt(9, tour.getEdadMaxNino());
                ps.setInt(10, tour.getEdadMaxBebe());
                ps.setBigDecimal(11, tour.getDescuento() != null ? tour.getDescuento() : BigDecimal.ZERO);
                ps.setString(12, tour.getCondiciones());
                ps.setInt(13, tour.getMinAsientosOferta() > 0 ? tour.getMinAsientosOferta() : 5);
                ps.setBigDecimal(14, tour.getPrecioAdulto() != null ? tour.getPrecioAdulto() : BigDecimal.ZERO);
                ps.setBigDecimal(15, tour.getPrecioNino() != null ? tour.getPrecioNino() : BigDecimal.ZERO);
                ps.setBigDecimal(16, tour.getPrecioBebe() != null ? tour.getPrecioBebe() : BigDecimal.ZERO);
                ps.setString(17, tour.getDuracion());
                ps.setString(18, tour.getUbicacion());
                ps.setString(19, tour.getCategoria() != null ? tour.getCategoria() : "Cultura");
                ps.setDouble(20, tour.getCalificacionPromedio() > 0 ? tour.getCalificacionPromedio() : 5.0);
                ps.setString(21, tour.getEstado() != null ? tour.getEstado() : "PUBLICADO");
                ps.setBoolean(22, tour.isAceptaBebes());
                ps.setString(23, tour.getQueIncluye());
                ps.setString(24, tour.getQueNoIncluye());
                ps.setInt(25, tour.getDias() > 0 ? tour.getDias() : 1);
                ps.setInt(26, tour.getHoras() > 0 ? tour.getHoras() : 6);

                ps.executeUpdate();
                try (ResultSet rs = ps.getGeneratedKeys()) {
                    if (rs.next()) {
                        tour.setIdTour(rs.getInt(1));
                    }
                }
                return tour;
            }
        } catch (SQLException e) {
            e.printStackTrace();
            return null;
        }
    }
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
    public List<Tour> listarTodos() {
        List<Tour> lista = new ArrayList<>();
        String sql = "SELECT * FROM Tour WHERE estado = 'PUBLICADO' ORDER BY idTour ASC";
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
    public List<Tour> buscarPorAgencia(int idAgencia) {
        List<Tour> lista = new ArrayList<>();
        String sql = "SELECT * FROM Tour WHERE idAgencia = ? ORDER BY idTour DESC";
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
    public List<Tour> buscarPorCategoria(String categoria) {
        List<Tour> lista = new ArrayList<>();
        String sql = "SELECT * FROM Tour WHERE categoria LIKE ? AND estado = 'PUBLICADO'";
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
    public boolean actualizar(Tour tour) {
        String sql = "UPDATE Tour SET idAgencia = ?, idDestino = ?, slug = ?, nombre = ?, descripcion = ?, " +
                     "descripcionCorta = ?, itinerario = ?, aceptaNinos = ?, edadMaxNino = ?, edadMaxBebe = ?, " +
                     "descuento = ?, condiciones = ?, minAsientosOferta = ?, precioAdulto = ?, precioNino = ?, " +
                     "precioBebe = ?, duracion = ?, ubicacion = ?, categoria = ?, estado = ?, aceptaBebes = ?, " +
                     "queIncluye = ?, queNoIncluye = ?, dias = ?, horas = ? WHERE idTour = ?";
        try (Connection con = ConexionDB.getConnection()) {
            if (tour.getUbicacion() == null || tour.getUbicacion().trim().isEmpty()) {
                tour.setUbicacion(obtenerNombreDestino(con, tour.getIdDestino()));
            }
            tour.setDuracion(generarDuracion(tour.getDias(), tour.getHoras()));

            if (!tour.isAceptaNinos()) {
                tour.setPrecioNino(BigDecimal.ZERO);
            }
            if (!tour.isAceptaBebes()) {
                tour.setPrecioBebe(BigDecimal.ZERO);
            }

            try (PreparedStatement ps = con.prepareStatement(sql)) {
                ps.setInt(1, tour.getIdAgencia());
                ps.setInt(2, tour.getIdDestino());
                ps.setString(3, tour.getSlug());
                ps.setString(4, tour.getNombre());
                ps.setString(5, tour.getDescripcion());
                ps.setString(6, tour.getDescripcionCorta());
                ps.setString(7, tour.getItinerario());
                ps.setBoolean(8, tour.isAceptaNinos());
                ps.setInt(9, tour.getEdadMaxNino());
                ps.setInt(10, tour.getEdadMaxBebe());
                ps.setBigDecimal(11, tour.getDescuento() != null ? tour.getDescuento() : BigDecimal.ZERO);
                ps.setString(12, tour.getCondiciones());
                ps.setInt(13, tour.getMinAsientosOferta() > 0 ? tour.getMinAsientosOferta() : 5);
                ps.setBigDecimal(14, tour.getPrecioAdulto() != null ? tour.getPrecioAdulto() : BigDecimal.ZERO);
                ps.setBigDecimal(15, tour.getPrecioNino() != null ? tour.getPrecioNino() : BigDecimal.ZERO);
                ps.setBigDecimal(16, tour.getPrecioBebe() != null ? tour.getPrecioBebe() : BigDecimal.ZERO);
                ps.setString(17, tour.getDuracion());
                ps.setString(18, tour.getUbicacion());
                ps.setString(19, tour.getCategoria());
                ps.setString(20, tour.getEstado());
                ps.setBoolean(21, tour.isAceptaBebes());
                ps.setString(22, tour.getQueIncluye());
                ps.setString(23, tour.getQueNoIncluye());
                ps.setInt(24, tour.getDias() > 0 ? tour.getDias() : 1);
                ps.setInt(25, tour.getHoras() > 0 ? tour.getHoras() : 6);
                ps.setInt(26, tour.getIdTour());
                return ps.executeUpdate() > 0;
            }
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }
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
        t.setDescripcionCorta(rs.getString("descripcionCorta"));
        t.setItinerario(rs.getString("itinerario"));
        t.setAceptaNinos(rs.getBoolean("aceptaNinos"));
        t.setEdadMaxNino(rs.getInt("edadMaxNino"));
        t.setEdadMaxBebe(rs.getInt("edadMaxBebe"));
        t.setDescuento(rs.getBigDecimal("descuento"));
        t.setCondiciones(rs.getString("condiciones"));
        t.setMinAsientosOferta(rs.getInt("minAsientosOferta"));
        t.setPrecioAdulto(rs.getBigDecimal("precioAdulto"));
        t.setPrecioNino(rs.getBigDecimal("precioNino"));
        t.setPrecioBebe(rs.getBigDecimal("precioBebe"));
        t.setDuracion(rs.getString("duracion"));
        t.setUbicacion(rs.getString("ubicacion"));
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
