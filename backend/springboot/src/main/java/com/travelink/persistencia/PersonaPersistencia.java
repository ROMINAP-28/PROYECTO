package com.travelink.persistencia;

import com.travelink.config.ConexionDB;
import com.travelink.entidades.Persona;
import com.travelink.repositorio.PersonaRepositorio;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class PersonaPersistencia implements PersonaRepositorio {

    @Override
    public Persona guardar(Persona persona) {
        String sql = "INSERT INTO Persona (nombre, apellidoPaterno, apellidoMaterno, nroDocumento, telefono, email) VALUES (?, ?, ?, ?, ?, ?)";
        try (Connection con = ConexionDB.getConnection();
             PreparedStatement ps = con.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {

            ps.setString(1, persona.getNombre());
            ps.setString(2, persona.getApellidoPaterno());
            ps.setString(3, persona.getApellidoMaterno());
            ps.setString(4, persona.getNroDocumento());
            ps.setString(5, persona.getTelefono());
            ps.setString(6, persona.getEmail());

            ps.executeUpdate();
            try (ResultSet rs = ps.getGeneratedKeys()) {
                if (rs.next()) {
                    persona.setIdPersona(rs.getInt(1));
                }
            }
            return persona;
        } catch (SQLException e) {
            e.printStackTrace();
            return null;
        }
    }

    @Override
    public Optional<Persona> buscarPorId(int idPersona) {
        String sql = "SELECT * FROM Persona WHERE idPersona = ?";
        try (Connection con = ConexionDB.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setInt(1, idPersona);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return Optional.of(mapResultSetToPersona(rs));
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return Optional.empty();
    }

    @Override
    public Optional<Persona> buscarPorDocumento(String nroDocumento) {
        String sql = "SELECT * FROM Persona WHERE nroDocumento = ?";
        try (Connection con = ConexionDB.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setString(1, nroDocumento);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return Optional.of(mapResultSetToPersona(rs));
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return Optional.empty();
    }

    @Override
    public Optional<Persona> buscarPorEmail(String email) {
        String sql = "SELECT * FROM Persona WHERE email = ?";
        try (Connection con = ConexionDB.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setString(1, email);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return Optional.of(mapResultSetToPersona(rs));
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return Optional.empty();
    }

    @Override
    public List<Persona> listarTodos() {
        List<Persona> lista = new ArrayList<>();
        String sql = "SELECT * FROM Persona";
        try (Connection con = ConexionDB.getConnection();
             PreparedStatement ps = con.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                lista.add(mapResultSetToPersona(rs));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return lista;
    }

    @Override
    public boolean actualizar(Persona persona) {
        String sql = "UPDATE Persona SET nombre = ?, apellidoPaterno = ?, apellidoMaterno = ?, nroDocumento = ?, telefono = ?, email = ? WHERE idPersona = ?";
        try (Connection con = ConexionDB.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setString(1, persona.getNombre());
            ps.setString(2, persona.getApellidoPaterno());
            ps.setString(3, persona.getApellidoMaterno());
            ps.setString(4, persona.getNroDocumento());
            ps.setString(5, persona.getTelefono());
            ps.setString(6, persona.getEmail());
            ps.setInt(7, persona.getIdPersona());
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    @Override
    public boolean eliminar(int idPersona) {
        String sql = "DELETE FROM Persona WHERE idPersona = ?";
        try (Connection con = ConexionDB.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setInt(1, idPersona);
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    public static Persona mapResultSetToPersona(ResultSet rs) throws SQLException {
        Persona p = new Persona();
        p.setIdPersona(rs.getInt("idPersona"));
        p.setNombre(rs.getString("nombre"));
        p.setApellidoPaterno(rs.getString("apellidoPaterno"));
        p.setApellidoMaterno(rs.getString("apellidoMaterno"));
        p.setNroDocumento(rs.getString("nroDocumento"));
        p.setTelefono(rs.getString("telefono"));
        p.setEmail(rs.getString("email"));
        return p;
    }
}
