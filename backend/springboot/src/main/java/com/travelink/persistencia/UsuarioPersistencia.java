package com.travelink.persistencia;

import com.travelink.config.ConexionDB;
import com.travelink.entidades.Persona;
import com.travelink.entidades.Rol;
import com.travelink.entidades.Usuario;
import com.travelink.repositorio.UsuarioRepositorio;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class UsuarioPersistencia implements UsuarioRepositorio {

    @Override
    public Usuario guardar(Usuario usuario) {
        Connection con = null;
        try {
            con = ConexionDB.getConnection();
            con.setAutoCommit(false);

            // 1. Si no tiene idPersona, insertar primero en Persona
            if (usuario.getIdPersona() <= 0) {
                Persona p = usuario.getPersona();
                if (p == null) {
                    p = new Persona();
                    p.setNombre(usuario.getNombre());
                    p.setApellidoPaterno(usuario.getApellidoPaterno());
                    p.setApellidoMaterno(usuario.getApellidoMaterno());
                    p.setEmail(usuario.getCorreo());
                    p.setTelefono(usuario.getTelefono());
                    p.setNroDocumento(usuario.getNroDocumento() != null && !usuario.getNroDocumento().isEmpty() ? 
                                      usuario.getNroDocumento() : "DNI" + System.currentTimeMillis() % 100000000);
                }
                String sqlPersona = "INSERT INTO Persona (nombre, apellidoPaterno, apellidoMaterno, nroDocumento, telefono, email) VALUES (?, ?, ?, ?, ?, ?)";
                try (PreparedStatement psP = con.prepareStatement(sqlPersona, Statement.RETURN_GENERATED_KEYS)) {
                    psP.setString(1, p.getNombre() != null ? p.getNombre() : "");
                    psP.setString(2, p.getApellidoPaterno() != null ? p.getApellidoPaterno() : "");
                    psP.setString(3, p.getApellidoMaterno() != null ? p.getApellidoMaterno() : "");
                    psP.setString(4, p.getNroDocumento() != null ? p.getNroDocumento() : "");
                    psP.setString(5, p.getTelefono() != null ? p.getTelefono() : "");
                    psP.setString(6, p.getEmail() != null ? p.getEmail() : "");
                    psP.executeUpdate();

                    try (ResultSet rsP = psP.getGeneratedKeys()) {
                        if (rsP.next()) {
                            p.setIdPersona(rsP.getInt(1));
                            usuario.setIdPersona(p.getIdPersona());
                            usuario.setPersona(p);
                        }
                    }
                }
            }

            // 2. Insertar en Usuario
            String sqlUsuario = "INSERT INTO Usuario (idPersona, idRol, nombreUsuario, contrasena, estado) VALUES (?, ?, ?, ?, ?)";
            try (PreparedStatement psU = con.prepareStatement(sqlUsuario, Statement.RETURN_GENERATED_KEYS)) {
                psU.setInt(1, usuario.getIdPersona());
                psU.setInt(2, usuario.getIdRol() > 0 ? usuario.getIdRol() : 1); // 1 = Turista por defecto
                psU.setString(3, usuario.getNombreUsuario());
                psU.setString(4, usuario.getContrasena());
                psU.setString(5, usuario.getEstado() != null ? usuario.getEstado() : "ACTIVO");
                psU.executeUpdate();

                try (ResultSet rsU = psU.getGeneratedKeys()) {
                    if (rsU.next()) {
                        usuario.setIdUsuario(rsU.getInt(1));
                    }
                }
            }

            con.commit();
            return usuario;
        } catch (SQLException e) {
            if (con != null) {
                try { con.rollback(); } catch (SQLException ex) { ex.printStackTrace(); }
            }
            e.printStackTrace();
            return null;
        } finally {
            if (con != null) {
                try { con.setAutoCommit(true); con.close(); } catch (SQLException e) { e.printStackTrace(); }
            }
        }
    }

    @Override
    public Optional<Usuario> buscarPorId(int idUsuario) {
        String sql = "SELECT u.*, p.nombre, p.apellidoPaterno, p.apellidoMaterno, p.nroDocumento, p.telefono, p.email, r.nombreRol " +
                     "FROM Usuario u " +
                     "INNER JOIN Persona p ON u.idPersona = p.idPersona " +
                     "INNER JOIN Rol r ON u.idRol = r.idRol " +
                     "WHERE u.idUsuario = ?";
        try (Connection con = ConexionDB.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setInt(1, idUsuario);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return Optional.of(mapResultSetToUsuario(rs));
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return Optional.empty();
    }

    @Override
    public Optional<Usuario> buscarPorNombreUsuario(String nombreUsuario) {
        String sql = "SELECT u.*, p.nombre, p.apellidoPaterno, p.apellidoMaterno, p.nroDocumento, p.telefono, p.email, r.nombreRol " +
                     "FROM Usuario u " +
                     "INNER JOIN Persona p ON u.idPersona = p.idPersona " +
                     "INNER JOIN Rol r ON u.idRol = r.idRol " +
                     "WHERE u.nombreUsuario = ?";
        try (Connection con = ConexionDB.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setString(1, nombreUsuario);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return Optional.of(mapResultSetToUsuario(rs));
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return Optional.empty();
    }

    @Override
    public Optional<Usuario> buscarPorCorreoOUsuario(String login) {
        String sql = "SELECT u.*, p.nombre, p.apellidoPaterno, p.apellidoMaterno, p.nroDocumento, p.telefono, p.email, r.nombreRol " +
                     "FROM Usuario u " +
                     "INNER JOIN Persona p ON u.idPersona = p.idPersona " +
                     "INNER JOIN Rol r ON u.idRol = r.idRol " +
                     "WHERE u.nombreUsuario = ? OR p.email = ?";
        try (Connection con = ConexionDB.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setString(1, login);
            ps.setString(2, login);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return Optional.of(mapResultSetToUsuario(rs));
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return Optional.empty();
    }

    @Override
    public List<Usuario> listarTodos() {
        List<Usuario> lista = new ArrayList<>();
        String sql = "SELECT u.*, p.nombre, p.apellidoPaterno, p.apellidoMaterno, p.nroDocumento, p.telefono, p.email, r.nombreRol " +
                     "FROM Usuario u " +
                     "INNER JOIN Persona p ON u.idPersona = p.idPersona " +
                     "INNER JOIN Rol r ON u.idRol = r.idRol";
        try (Connection con = ConexionDB.getConnection();
             PreparedStatement ps = con.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                lista.add(mapResultSetToUsuario(rs));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return lista;
    }

    @Override
    public boolean actualizar(Usuario usuario) {
        String sql = "UPDATE Usuario SET idRol = ?, nombreUsuario = ?, contrasena = ?, estado = ? WHERE idUsuario = ?";
        try (Connection con = ConexionDB.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setInt(1, usuario.getIdRol());
            ps.setString(2, usuario.getNombreUsuario());
            ps.setString(3, usuario.getContrasena());
            ps.setString(4, usuario.getEstado());
            ps.setInt(5, usuario.getIdUsuario());
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    @Override
    public boolean eliminar(int idUsuario) {
        String sql = "DELETE FROM Usuario WHERE idUsuario = ?";
        try (Connection con = ConexionDB.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setInt(1, idUsuario);
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    private Usuario mapResultSetToUsuario(ResultSet rs) throws SQLException {
        Usuario u = new Usuario();
        u.setIdUsuario(rs.getInt("idUsuario"));
        u.setIdPersona(rs.getInt("idPersona"));
        u.setIdRol(rs.getInt("idRol"));
        u.setNombreUsuario(rs.getString("nombreUsuario"));
        u.setContrasena(rs.getString("contrasena"));
        u.setEstado(rs.getString("estado"));
        u.setFechaRegistro(rs.getTimestamp("fechaRegistro"));

        // Persona anidada
        Persona p = new Persona();
        p.setIdPersona(rs.getInt("idPersona"));
        p.setNombre(rs.getString("nombre"));
        p.setApellidoPaterno(rs.getString("apellidoPaterno"));
        p.setApellidoMaterno(rs.getString("apellidoMaterno"));
        p.setNroDocumento(rs.getString("nroDocumento"));
        p.setTelefono(rs.getString("telefono"));
        p.setEmail(rs.getString("email"));
        u.setPersona(p);

        // Rol anidado
        Rol r = new Rol();
        r.setIdRol(rs.getInt("idRol"));
        r.setNombreRol(rs.getString("nombreRol"));
        u.setRol(r);

        return u;
    }
}
