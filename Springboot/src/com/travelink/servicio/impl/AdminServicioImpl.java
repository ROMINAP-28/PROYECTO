package com.travelink.servicio.impl;

import com.travelink.persistencia.AdminPersistencia;
import com.travelink.repositorio.*;
import com.travelink.servicio.AdminServicio;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Date;
import java.util.List;
import java.util.Map;

/**
 * Implementación del Servicio de Administración.
 * Aplica principios SOLID:
 * - SRP: Métricas globales de la plataforma, liquidación de comisiones y supervisión de agencias.
 * - DIP: Inyección de repositorios JPA (DAO principal) y persistencia JDBC (consultas especializadas).
 */
@Service
public class AdminServicioImpl implements AdminServicio {

    private static final Logger log = LoggerFactory.getLogger(AdminServicioImpl.class);

    private final AdminPersistencia adminPersistencia;
    private final CalificacionRepositorio calificacionRepositorio;
    private final ComisionRepositorio comisionRepositorio;
    private final UsuarioRepositorio usuarioRepositorio;
    private final AgenciaRepositorio agenciaRepositorio;

    public AdminServicioImpl(CalificacionRepositorio calificacionRepositorio,
                            ComisionRepositorio comisionRepositorio,
                            UsuarioRepositorio usuarioRepositorio,
                            AgenciaRepositorio agenciaRepositorio) {
        this.calificacionRepositorio = calificacionRepositorio;
        this.comisionRepositorio = comisionRepositorio;
        this.usuarioRepositorio = usuarioRepositorio;
        this.agenciaRepositorio = agenciaRepositorio;
        this.adminPersistencia = new AdminPersistencia();
    }

    @Override
    public Map<String, Object> obtenerDashboardData() {
        return adminPersistencia.getDashboardData();
    }

    @Override
    public List<Map<String, Object>> listarAgencias(String filtroEstado, String filtroDestino, String busqueda) {
        return adminPersistencia.getAgenciasFull(filtroEstado, filtroDestino, busqueda);
    }

    @Override
    @Transactional
    public boolean actualizarEstadoAgencia(Map<String, Object> params) {
        boolean ok = adminPersistencia.guardarAgencia(params);
        if (ok) {
            log.info("Estado de agencia actualizado por administrador: {}", params);
        }
        return ok;
    }

    @Override
    @Transactional
    public boolean eliminarAgencia(int idAgencia) {
        boolean ok = adminPersistencia.eliminarAgencia(idAgencia);
        if (ok) {
            log.info("Agencia #{} eliminada por administrador", idAgencia);
        }
        return ok;
    }

    @Override
    public List<Map<String, Object>> listarReservas(String filtroEstado, String busqueda) {
        return adminPersistencia.getReservas(filtroEstado, busqueda);
    }

    @Override
    public Map<String, Object> obtenerDatosCalidad() {
        return adminPersistencia.getCalidadData();
    }

    @Override
    @Transactional
    public boolean eliminarResena(int idCalificacion, String motivo) {
        return calificacionRepositorio.findById(idCalificacion).map(c -> {
            c.setEliminadoPorAdmin(true);
            c.setMotivoEliminacion(motivo != null ? motivo : "Moderada por administrador");
            calificacionRepositorio.save(c);
            log.info("Reseña #{} eliminada por administrador con motivo: {}", idCalificacion, motivo);
            return true;
        }).orElse(false);
    }

    @Override
    public Map<String, Object> listarComisiones() {
        return adminPersistencia.getComisionesData();
    }

    @Override
    @Transactional
    public boolean liquidarComision(int idComision) {
        return comisionRepositorio.findById(idComision).map(c -> {
            c.setEstadoLiquidacion("Liquidada");
            c.setFechaLiquidacion(new Date());
            comisionRepositorio.save(c);
            log.info("Comisión #{} liquidada exitosamente por administrador", idComision);
            return true;
        }).orElse(false);
    }

    @Override
    public int ejecutarJobComisiones(String inicioPeriodo, String finPeriodo) {
        int filas = adminPersistencia.ejecutarJobComisiones(inicioPeriodo, finPeriodo);
        log.info("Job de comisiones ejecutado para periodo {} a {}: {} comisiones calculadas",
                inicioPeriodo, finPeriodo, filas);
        return filas;
    }

    @Override
    public Map<String, Object> obtenerCatalogoDestinos() {
        return adminPersistencia.getDestinos();
    }

    @Override
    public Map<String, Object> obtenerToursPorDestino(String ubicacion) {
        return adminPersistencia.getToursPorDestino(ubicacion);
    }

    @Override
    public List<Map<String, Object>> listarTours(String filtroEstado, String filtroAgencia, String busqueda) {
        return adminPersistencia.getTours(filtroEstado, filtroAgencia, busqueda);
    }

    @Override
    public List<Map<String, Object>> listarUsuarios(String filtroTipo, String filtroEstado, String busqueda) {
        return adminPersistencia.getUsuarios(filtroTipo, filtroEstado, busqueda);
    }

    @Override
    @Transactional
    public boolean guardarUsuarioAdmin(Map<String, Object> params) {
        return adminPersistencia.guardarUsuario(params);
    }

    @Override
    @Transactional
    public boolean eliminarUsuarioAdmin(int idUsuario) {
        return adminPersistencia.eliminarUsuario(idUsuario);
    }

    @Override
    public Map<String, Object> obtenerNotificaciones(Integer idUsuario, int limite) {
        return adminPersistencia.getNotificacionesData(idUsuario, limite);
    }

    @Override
    @Transactional
    public boolean marcarNotificacionesLeidas(Integer idUsuario) {
        return adminPersistencia.marcarTodasNotificacionesLeidas(idUsuario);
    }
}
