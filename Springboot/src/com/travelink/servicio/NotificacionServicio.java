package com.travelink.servicio;

import com.travelink.entidades.Notificacion;
import com.travelink.repositorio.NotificacionRepositorio;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Service
public class NotificacionServicio {

    private final NotificacionRepositorio notificacionRepositorio;

    public NotificacionServicio(NotificacionRepositorio notificacionRepositorio) {
        this.notificacionRepositorio = notificacionRepositorio;
    }

    public List<Notificacion> listarPorUsuario(Integer idUsuario) {
        return notificacionRepositorio.findByIdUsuarioOrderByFechaRegistroDesc(idUsuario);
    }

    public List<Notificacion> listarPorRol(String rolTarget) {
        return notificacionRepositorio.findByRolTargetOrderByFechaRegistroDesc(rolTarget);
    }

    @Transactional
    public Notificacion crear(Notificacion notificacion) {
        return notificacionRepositorio.save(notificacion);
    }

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
