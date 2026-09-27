package com.travelink.persistencia;

import com.travelink.config.ConexionDB;
import com.travelink.entidades.MetodoPago;
import com.travelink.repositorio.MetodoPagoRepositorio;

import java.sql.*;
import java.util.Optional;

public class MetodoPagoPersistencia implements MetodoPagoRepositorio {

    @Override
    public MetodoPago guardar(MetodoPago pago) {
        String sql = "INSERT INTO MetodoPago (idReserva, tipoPago, monto, esAdelanto, estadoPago, numeroOperacion) VALUES (?, ?, ?, ?, ?, ?)";
        try (Connection con = ConexionDB.getConnection();
             PreparedStatement ps = con.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {

            ps.setInt(1, pago.getIdReserva());
            ps.setString(2, pago.getTipoPago());
            ps.setBigDecimal(3, pago.getMonto());
            ps.setBoolean(4, pago.isEsAdelanto());
            ps.setString(5, pago.getEstadoPago() != null ? pago.getEstadoPago() : "Pendiente");
            ps.setString(6, pago.getNumeroOperacion());

            ps.executeUpdate();
            try (ResultSet rs = ps.getGeneratedKeys()) {
                if (rs.next()) {
                    pago.setIdMetodoPago(rs.getInt(1));
                }
            }
            return pago;
        } catch (SQLException e) {
            e.printStackTrace();
            return null;
        }
    }

    @Override
    public Optional<MetodoPago> buscarPorId(int idPago) {
        String sql = "SELECT * FROM MetodoPago WHERE idMetodoPago = ?";
        try (Connection con = ConexionDB.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setInt(1, idPago);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return Optional.of(mapResultSetToMetodoPago(rs));
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return Optional.empty();
    }

    @Override
    public Optional<MetodoPago> buscarPorReserva(int idReserva) {
        String sql = "SELECT * FROM MetodoPago WHERE idReserva = ? ORDER BY idMetodoPago DESC LIMIT 1";
        try (Connection con = ConexionDB.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setInt(1, idReserva);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return Optional.of(mapResultSetToMetodoPago(rs));
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return Optional.empty();
    }

    @Override
    public boolean actualizarEstado(int idPago, String estado) {
        String sql = "UPDATE MetodoPago SET estadoPago = ? WHERE idMetodoPago = ?";
        try (Connection con = ConexionDB.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setString(1, estado);
            ps.setInt(2, idPago);
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    private MetodoPago mapResultSetToMetodoPago(ResultSet rs) throws SQLException {
        MetodoPago m = new MetodoPago();
        m.setIdMetodoPago(rs.getInt("idMetodoPago"));
        m.setIdReserva(rs.getInt("idReserva"));
        m.setTipoPago(rs.getString("tipoPago"));
        m.setMonto(rs.getBigDecimal("monto"));
        m.setEsAdelanto(rs.getBoolean("esAdelanto"));
        m.setEstadoPago(rs.getString("estadoPago"));
        m.setNumeroOperacion(rs.getString("numeroOperacion"));
        m.setFechaPago(rs.getTimestamp("fechaPago"));
        return m;
    }
}
