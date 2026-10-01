package com.travelink.persistencia;

import com.travelink.config.ConexionDB;
import com.travelink.entidades.Pasajero;
import com.travelink.repositorio.PasajeroRepositorio;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class PasajeroPersistencia implements PasajeroRepositorio {

    @Override
    public Pasajero guardar(Pasajero pasajero) {
        String sql = "INSERT INTO Pasajero (idReserva, nroDocumento, nombre, apellidos, esTitular) VALUES (?, ?, ?, ?, ?)";
        try (Connection con = ConexionDB.getConnection();
             PreparedStatement ps = con.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {

            ps.setInt(1, pasajero.getIdReserva());
            ps.setString(2, pasajero.getNroDocumento());
            ps.setString(3, pasajero.getNombre());
            ps.setString(4, pasajero.getApellidos());
            ps.setBoolean(5, pasajero.isEsTitular());

            ps.executeUpdate();
            try (ResultSet rs = ps.getGeneratedKeys()) {
                if (rs.next()) {
                    pasajero.setIdPasajero(rs.getInt(1));
                }
            }
            return pasajero;
        } catch (SQLException e) {
            e.printStackTrace();
            return null;
        }
    }

    @Override
    public List<Pasajero> guardarTodos(List<Pasajero> pasajeros) {
        List<Pasajero> guardados = new ArrayList<>();
        if (pasajeros == null || pasajeros.isEmpty()) return guardados;

        String sql = "INSERT INTO Pasajero (idReserva, nroDocumento, nombre, apellidos, esTitular) VALUES (?, ?, ?, ?, ?)";
        try (Connection con = ConexionDB.getConnection();
             PreparedStatement ps = con.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {

            for (Pasajero p : pasajeros) {
                ps.setInt(1, p.getIdReserva());
                ps.setString(2, p.getNroDocumento());
                ps.setString(3, p.getNombre());
                ps.setString(4, p.getApellidos());
                ps.setBoolean(5, p.isEsTitular());
                ps.addBatch();
            }

            ps.executeBatch();
            try (ResultSet rs = ps.getGeneratedKeys()) {
                int index = 0;
                while (rs.next() && index < pasajeros.size()) {
                    Pasajero p = pasajeros.get(index++);
                    p.setIdPasajero(rs.getInt(1));
                    guardados.add(p);
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return guardados;
    }

    @Override
    public Optional<Pasajero> buscarPorId(int idPasajero) {
        String sql = "SELECT * FROM Pasajero WHERE idPasajero = ?";
        try (Connection con = ConexionDB.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setInt(1, idPasajero);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return Optional.of(mapResultSetToPasajero(rs));
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return Optional.empty();
    }

    @Override
    public List<Pasajero> buscarPorReserva(int idReserva) {
        List<Pasajero> lista = new ArrayList<>();
        String sql = "SELECT * FROM Pasajero WHERE idReserva = ? ORDER BY esTitular DESC, idPasajero ASC";
        try (Connection con = ConexionDB.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setInt(1, idReserva);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    lista.add(mapResultSetToPasajero(rs));
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return lista;
    }

    @Override
    public boolean eliminarPorReserva(int idReserva) {
        String sql = "DELETE FROM Pasajero WHERE idReserva = ?";
        try (Connection con = ConexionDB.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setInt(1, idReserva);
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    private Pasajero mapResultSetToPasajero(ResultSet rs) throws SQLException {
        Pasajero p = new Pasajero();
        p.setIdPasajero(rs.getInt("idPasajero"));
        p.setIdReserva(rs.getInt("idReserva"));
        p.setNroDocumento(rs.getString("nroDocumento"));
        p.setNombre(rs.getString("nombre"));
        p.setApellidos(rs.getString("apellidos"));
        p.setEsTitular(rs.getBoolean("esTitular"));
        return p;
    }
}
