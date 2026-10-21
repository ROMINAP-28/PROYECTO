package com.travelink.servicio.impl;

import com.travelink.entidades.Notificacion;
import com.travelink.persistencia.NotificacionPersistencia;
import com.travelink.repositorio.NotificacionRepositorio;
import com.travelink.servicio.NotificacionServicio;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

/**
 * Implementación del Servicio de Notificaciones.
 * Aplica principios SOLID:
 * - SRP: Gestión del ciclo de vida de notificaciones del sistema.
 * - DIP: Inyección de dependencias por constructor.
 */
@Service
public class NotificacionServicioImpl implements NotificacionServicio {

    private final NotificacionRepositorio notificacionRepositorio;

    public NotificacionServicioImpl(NotificacionRepositorio notificacionRepositorio) {
        this.notificacionRepositorio = notificacionRepositorio;
    }

    @Override
    public List<Notificacion> listarPorUsuario(Integer idUsuario) {
        return notificacionRepositorio.findByIdUsuarioOrderByFechaRegistroDesc(idUsuario);
    }

    @Override
    public List<Notificacion> listarPorRol(String rolTarget) {
        return notificacionRepositorio.findByRolTargetOrderByFechaRegistroDesc(rolTarget);
    }

    @Override
    @Transactional
    public Notificacion crear(Notificacion notificacion) {
        return notificacionRepositorio.save(notificacion);
    }

    @Override
    public boolean registrarNotificacion(Integer idUsuario, String titulo, String mensaje, String tipo, String icono, String color, String enlace) {
        return NotificacionPersistencia.registrar(idUsuario, titulo, mensaje, tipo, icono, color, enlace);
    }

    @Override
    @Transactional
    public boolean marcarLeido(Integer id) {
        Optional<Notificacion> n = notificacionRepositorio.findById(id);
        if (n.isPresent()) {
            Notificacion notif = n.get();
            notif.setLeido(true);
            notificacionRepositorio.save(notif);
            return true;
        }
        return false;
    }
}
