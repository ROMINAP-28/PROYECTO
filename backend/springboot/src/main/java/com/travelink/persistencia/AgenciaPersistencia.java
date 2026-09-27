package com.travelink.persistencia;

import com.travelink.config.ConexionDB;
import com.travelink.entidades.Agencia;
import com.travelink.repositorio.AgenciaRepositorio;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class AgenciaPersistencia implements AgenciaRepositorio {

    @Override
    public Agencia guardar(Agencia agencia) {
        String sql = "INSERT INTO Agencia (idUsuario, razonSocial, nombreComercial, ruc, telefono, email, direccion, descripcion, logoUrl, estado) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";
        try (Connection con = ConexionDB.getConnection();
             PreparedStatement ps = con.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {

            ps.setInt(1, agencia.getIdUsuario());
            ps.setString(2, agencia.getRazonSocial());
            ps.setString(3, agencia.getNombreComercial());
            ps.setString(4, agencia.getRuc());
            ps.setString(5, agencia.getTelefono());
            ps.setString(6, agencia.getCorreo());
            ps.setString(7, agencia.getDireccion());
            ps.setString(8, agencia.getDescripcion());
            ps.setString(9, "");
            ps.setString(10, agencia.getEstado() != null ? agencia.getEstado() : "ACTIVO");

            ps.executeUpdate();
            try (ResultSet rs = ps.getGeneratedKeys()) {
                if (rs.next()) {
                    agencia.setIdAgencia(rs.getInt(1));
                }
            }
            return agencia;
        } catch (SQLException e) {
            e.printStackTrace();
            return null;
        }
    }

    @Override
    public Optional<Agencia> buscarPorId(int idAgencia) {
        String sql = "SELECT * FROM Agencia WHERE idAgencia = ?";
        try (Connection con = ConexionDB.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setInt(1, idAgencia);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return Optional.of(mapResultSetToAgencia(rs));
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return Optional.empty();
    }

    @Override
    public Optional<Agencia> buscarPorRuc(String ruc) {
        String sql = "SELECT * FROM Agencia WHERE ruc = ?";
        try (Connection con = ConexionDB.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setString(1, ruc);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return Optional.of(mapResultSetToAgencia(rs));
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return Optional.empty();
    }

    @Override
    public List<Agencia> listarTodas() {
        List<Agencia> lista = new ArrayList<>();
        String sql = "SELECT * FROM Agencia WHERE estado = 'ACTIVO'";
        try (Connection con = ConexionDB.getConnection();
             PreparedStatement ps = con.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                lista.add(mapResultSetToAgencia(rs));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return lista;
    }

    @Override
    public boolean actualizar(Agencia agencia) {
        String sql = "UPDATE Agencia SET razonSocial = ?, nombreComercial = ?, ruc = ?, telefono = ?, email = ?, direccion = ?, descripcion = ?, estado = ? WHERE idAgencia = ?";
        try (Connection con = ConexionDB.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setString(1, agencia.getRazonSocial());
            ps.setString(2, agencia.getNombreComercial());
            ps.setString(3, agencia.getRuc());
            ps.setString(4, agencia.getTelefono());
            ps.setString(5, agencia.getCorreo());
            ps.setString(6, agencia.getDireccion());
            ps.setString(7, agencia.getDescripcion());
            ps.setString(8, agencia.getEstado());
            ps.setInt(9, agencia.getIdAgencia());
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    @Override
    public boolean eliminar(int idAgencia) {
        String sql = "DELETE FROM Agencia WHERE idAgencia = ?";
        try (Connection con = ConexionDB.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setInt(1, idAgencia);
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    private Agencia mapResultSetToAgencia(ResultSet rs) throws SQLException {
        Agencia a = new Agencia();
        a.setIdAgencia(rs.getInt("idAgencia"));
        a.setIdUsuario(rs.getInt("idUsuario"));
        a.setRazonSocial(rs.getString("razonSocial"));
        a.setNombreComercial(rs.getString("nombreComercial"));
        a.setRuc(rs.getString("ruc"));
        a.setTelefono(rs.getString("telefono"));
        a.setCorreo(rs.getString("email"));
        a.setDireccion(rs.getString("direccion"));
        a.setDescripcion(rs.getString("descripcion"));
        a.setEstado(rs.getString("estado"));
        return a;
    }
}
