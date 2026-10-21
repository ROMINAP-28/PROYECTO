package com.travelink.servicio.impl;

import com.travelink.config.ConexionDB;
import com.travelink.entidades.Agencia;
import com.travelink.entidades.Persona;
import com.travelink.entidades.SolicitudAgencia;
import com.travelink.entidades.Usuario;
import com.travelink.repositorio.AgenciaRepositorio;
import com.travelink.repositorio.PersonaRepositorio;
import com.travelink.repositorio.SolicitudAgenciaRepositorio;
import com.travelink.repositorio.UsuarioRepositorio;
import com.travelink.servicio.AgenciaServicio;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.*;

/**
 * Implementación del Servicio de Agencias.
 * Aplica principios SOLID:
 * - SRP: Lógica de agencias, aprobaciones y finanzas de la agencia.
 * - DIP: Inyección de dependencias por constructor.
 */
@Service
public class AgenciaServicioImpl implements AgenciaServicio {

    private static final Logger log = LoggerFactory.getLogger(AgenciaServicioImpl.class);

    private final AgenciaRepositorio agenciaRepositorio;
    private final SolicitudAgenciaRepositorio solicitudAgenciaRepositorio;
    private final UsuarioRepositorio usuarioRepositorio;
    private final PersonaRepositorio personaRepositorio;
    private final PasswordEncoder passwordEncoder;

    public AgenciaServicioImpl(AgenciaRepositorio agenciaRepositorio,
                              SolicitudAgenciaRepositorio solicitudAgenciaRepositorio,
                              UsuarioRepositorio usuarioRepositorio,
                              PersonaRepositorio personaRepositorio,
                              PasswordEncoder passwordEncoder) {
        this.agenciaRepositorio = agenciaRepositorio;
        this.solicitudAgenciaRepositorio = solicitudAgenciaRepositorio;
        this.usuarioRepositorio = usuarioRepositorio;
        this.personaRepositorio = personaRepositorio;
        this.passwordEncoder = passwordEncoder;
    }

    @Override
    public List<Agencia> listarTodas() {
        return agenciaRepositorio.findAll();
    }

    @Override
    public List<Agencia> listarPorEstado(String estado) {
        return agenciaRepositorio.findByEstado(estado);
    }

    @Override
    public Optional<Agencia> buscarPorId(Integer id) {
        return agenciaRepositorio.findById(id);
    }

    @Override
    public Optional<Agencia> buscarPorRuc(String ruc) {
        return agenciaRepositorio.findByRuc(ruc);
    }

    @Override
    public Optional<Agencia> buscarPorIdUsuario(Integer idUsuario) {
        return agenciaRepositorio.findByIdUsuario(idUsuario);
    }

    @Override
    public List<SolicitudAgencia> listarSolicitudesPorUsuario(Integer idUsuario) {
        return solicitudAgenciaRepositorio.findByIdUsuario(idUsuario);
    }

    @Override
    @Transactional
    public Agencia guardar(Agencia agencia) {
        return agenciaRepositorio.save(agencia);
    }

    @Override
    @Transactional
    public Map<String, Object> registrarAgencia(Map<String, Object> params) {
        Map<String, Object> res = new HashMap<>();

        String ruc = String.valueOf(params.getOrDefault("ruc", "")).trim();
        String razonSocial = String.valueOf(params.getOrDefault("razonSocial", "")).trim();
        String nombreComercial = String.valueOf(params.getOrDefault("nombreComercial", razonSocial)).trim();
        String direccion = String.valueOf(params.getOrDefault("direccion", "")).trim();
        String telefonoEmpresa = String.valueOf(params.getOrDefault("telefonoEmpresa", params.getOrDefault("telefono", ""))).trim();
        String correoEmpresa = String.valueOf(params.getOrDefault("correoEmpresa", params.getOrDefault("email", ""))).trim();
        String representanteLegal = String.valueOf(params.getOrDefault("representanteLegal", "")).trim();
        String dniRepresentante = String.valueOf(params.getOrDefault("dniRepresentante", params.getOrDefault("nroDocumento", ""))).trim();
        String telefonoRepresentante = String.valueOf(params.getOrDefault("telefonoRepresentante", telefonoEmpresa)).trim();
        String nombreUsuario = String.valueOf(params.getOrDefault("nombreUsuario", "")).trim();
        String clave = String.valueOf(params.getOrDefault("clave", params.getOrDefault("contrasena", ""))).trim();

        if (ruc.isEmpty() || razonSocial.isEmpty() || nombreUsuario.isEmpty() || clave.isEmpty()) {
            res.put("status", "error");
            res.put("message", "Faltan campos obligatorios para el registro de agencia");
            return res;
        }

        // Validar duplicados en Repositorios
        if (agenciaRepositorio.findByRuc(ruc).isPresent()) {
            res.put("status", "error");
            res.put("message", "El RUC '" + ruc + "' ya se encuentra registrado en el sistema");
            return res;
        }
        if (usuarioRepositorio.findByNombreUsuario(nombreUsuario).isPresent()) {
            res.put("status", "error");
            res.put("message", "El nombre de usuario '" + nombreUsuario + "' ya se encuentra en uso");
            return res;
        }

        // Crear Persona
        Persona p = new Persona();
        p.setNombre(representanteLegal);
        p.setApellidoPaterno("");
        p.setApellidoMaterno("");
        p.setNroDocumento(dniRepresentante);
        p.setTelefono(telefonoRepresentante);
        p.setEmail(correoEmpresa);
        p = personaRepositorio.save(p);

        // Crear Usuario (Rol 2 = Agencia, Estado = PENDIENTE)
        Usuario u = new Usuario();
        u.setIdPersona(p.getIdPersona());
        u.setIdRol(2); // ROLE_AGENCIA
        u.setNombreUsuario(nombreUsuario);
        u.setContrasena(passwordEncoder.encode(clave));
        u.setEstado("PENDIENTE");
        u = usuarioRepositorio.save(u);

        // Crear SolicitudAgencia
        SolicitudAgencia sa = new SolicitudAgencia();
        sa.setIdUsuario(u.getIdUsuario());
        sa.setRazonSocial(razonSocial);
        sa.setRuc(ruc);
        sa.setRepresentanteLegal(representanteLegal);
        sa.setTelefonoContacto(telefonoEmpresa);
        sa.setCorreoContacto(correoEmpresa);
        sa.setEstado("Pendiente");
        solicitudAgenciaRepositorio.save(sa);

        // Crear Agencia provisional en estado PENDIENTE
        Agencia ag = new Agencia();
        ag.setIdUsuario(u.getIdUsuario());
        ag.setRazonSocial(razonSocial);
        ag.setNombreComercial(nombreComercial);
        ag.setRuc(ruc);
        ag.setTelefono(telefonoEmpresa);
        ag.setCorreo(correoEmpresa);
        ag.setDireccion(direccion);
        ag.setEstado("PENDIENTE");
        ag.setPorcentajeComision(new BigDecimal("15.00"));
        ag = agenciaRepositorio.save(ag);

        log.info("Solicitud de agencia registrada con éxito: {} (RUC: {})", razonSocial, ruc);

        res.put("status", "success");
        res.put("message", "Solicitud de agencia registrada exitosamente. Pendiente de aprobación por el administrador.");
        res.put("idAgencia", ag.getIdAgencia());
        return res;
    }

    @Override
    public Map<String, Object> obtenerDashboardAgencia(int idAgencia) {
        Map<String, Object> data = new HashMap<>();
        try (Connection con = ConexionDB.getConnection()) {
            if (con != null) {
                // Nombre Agencia
                String sqlNom = "SELECT COALESCE(NULLIF(nombreComercial, ''), razonSocial, 'Agencia') FROM Agencia WHERE idAgencia = ?";
                try (PreparedStatement ps = con.prepareStatement(sqlNom)) {
                    ps.setInt(1, idAgencia);
                    try (ResultSet rs = ps.executeQuery()) {
                        if (rs.next()) data.put("nombreAgencia", rs.getString(1));
                    }
                }

                // Servicios activos
                String sqlServ = "SELECT COUNT(*) FROM Tour WHERE idAgencia = ? AND UPPER(estado) IN ('ACTIVO', 'PUBLICADO')";
                try (PreparedStatement ps = con.prepareStatement(sqlServ)) {
                    ps.setInt(1, idAgencia);
                    try (ResultSet rs = ps.executeQuery()) {
                        data.put("serviciosActivos", rs.next() ? rs.getInt(1) : 0);
                    }
                }

                // Reservas totales e ingresos
                String sqlRes = "SELECT COUNT(*), COALESCE(SUM(total), 0) FROM Reserva WHERE idAgencia = ? AND estado != 'Cancelada'";
                try (PreparedStatement ps = con.prepareStatement(sqlRes)) {
                    ps.setInt(1, idAgencia);
                    try (ResultSet rs = ps.executeQuery()) {
                        if (rs.next()) {
                            data.put("totalReservas", rs.getInt(1));
                            data.put("ingresosTotales", rs.getDouble(2));
                        }
                    }
                }
            }
        } catch (Exception e) {
            log.error("Error al obtener dashboard de agencia #{}: {}", idAgencia, e.getMessage());
        }
        return data;
    }

    @Override
    public List<Map<String, Object>> obtenerPagosAgencia(int idAgencia) {
        List<Map<String, Object>> pagos = new ArrayList<>();
        String sql = """
            SELECT mp.idMetodoPago, mp.idReserva, mp.tipoPago, mp.monto, mp.estadoPago, mp.numeroOperacion, mp.fechaPago,
                   r.codigoReserva, CONCAT(p.nombre, ' ', p.apellidoPaterno) as cliente,
                   COALESCE(t.nombre, r.nombreTour, 'Tour') as tour
            FROM MetodoPago mp
            JOIN Reserva r ON mp.idReserva = r.idReserva
            JOIN Usuario u ON r.idUsuario = u.idUsuario
            JOIN Persona p ON u.idPersona = p.idPersona
            LEFT JOIN Tour t ON r.idTour = t.idTour
            WHERE r.idAgencia = ?
            ORDER BY mp.idMetodoPago DESC
            """;
        try (Connection con = ConexionDB.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setInt(1, idAgencia);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    Map<String, Object> p = new HashMap<>();
                    p.put("idPago", rs.getInt("idMetodoPago"));
                    p.put("idReserva", rs.getInt("idReserva"));
                    p.put("codigoReserva", rs.getString("codigoReserva"));
                    p.put("cliente", rs.getString("cliente"));
                    p.put("tour", rs.getString("tour"));
                    p.put("tipoPago", rs.getString("tipoPago"));
                    p.put("monto", rs.getDouble("monto"));
                    p.put("estadoPago", rs.getString("estadoPago"));
                    p.put("numeroOperacion", rs.getString("numeroOperacion"));
                    p.put("fechaPago", rs.getString("fechaPago"));
                    pagos.add(p);
                }
            }
        } catch (Exception e) {
            log.error("Error al obtener pagos de agencia #{}: {}", idAgencia, e.getMessage());
        }
        return pagos;
    }
}
