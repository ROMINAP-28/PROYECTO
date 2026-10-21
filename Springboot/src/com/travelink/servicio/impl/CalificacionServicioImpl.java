package com.travelink.servicio.impl;

import com.travelink.controlador.dto.CalificacionRequest;
import com.travelink.entidades.Calificacion;
import com.travelink.entidades.Reserva;
import com.travelink.repositorio.CalificacionRepositorio;
import com.travelink.repositorio.ReservaJpaRepositorio;
import com.travelink.servicio.CalificacionServicio;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Date;
import java.util.List;
import java.util.Optional;

/**
 * Implementación del Servicio de Calificaciones.
 * Aplica principios SOLID:
 * - SRP: Encapsula el registro y moderación de calificaciones.
 * - DIP: Inyección de dependencias por constructor.
 */
@Service
public class CalificacionServicioImpl implements CalificacionServicio {

    private static final Logger log = LoggerFactory.getLogger(CalificacionServicioImpl.class);

    private final CalificacionRepositorio calificacionRepositorio;
    private final ReservaJpaRepositorio reservaJpaRepositorio;

    public CalificacionServicioImpl(CalificacionRepositorio calificacionRepositorio,
                                    ReservaJpaRepositorio reservaJpaRepositorio) {
        this.calificacionRepositorio = calificacionRepositorio;
        this.reservaJpaRepositorio = reservaJpaRepositorio;
    }

    @Override
    public List<Calificacion> listarPorAgencia(Integer idAgencia) {
        return calificacionRepositorio.findByIdAgencia(idAgencia);
    }

    @Override
    public List<Calificacion> listarPorUsuario(Integer idUsuario) {
        return calificacionRepositorio.findByIdUsuario(idUsuario);
    }

    @Override
    public Double obtenerPromedioAgencia(Integer idAgencia) {
        Double avg = calificacionRepositorio.promedioEstrellasPorAgencia(idAgencia);
        return avg != null ? Math.round(avg * 10.0) / 10.0 : 5.0;
    }

    @Override
    @Transactional
    public Calificacion guardar(Calificacion calificacion) {
        return calificacionRepositorio.save(calificacion);
    }

    @Override
    @Transactional
    public Calificacion registrarCalificacion(CalificacionRequest req, int idUsuario) {
        Calificacion c = new Calificacion();
        c.setIdUsuario(idUsuario);
        c.setIdReserva(req.getIdReserva());
        c.setEstrellas(req.getEstrellas());
        c.setComentario(req.getComentario());
        c.setFechaCalificacion(new Date());

        // Completar idAgencia desde la reserva
        Optional<Reserva> r = reservaJpaRepositorio.findById(req.getIdReserva());
        if (r.isPresent()) {
            c.setIdAgencia(r.get().getIdAgencia());
        } else if (req.getIdAgencia() != null) {
            c.setIdAgencia(req.getIdAgencia());
        } else {
            c.setIdAgencia(1);
        }

        Calificacion guardada = calificacionRepositorio.save(c);
        log.info("Calificación #{} registrada exitosamente para reserva #{} con {} estrellas",
                guardada.getIdCalificacion(), req.getIdReserva(), req.getEstrellas());
        return guardada;
    }

    @Override
    @Transactional
    public boolean marcarEliminado(Integer id, String motivo) {
        Optional<Calificacion> c = calificacionRepositorio.findById(id);
        if (c.isPresent()) {
            Calificacion cal = c.get();
            cal.setEliminadoPorAdmin(true);
            cal.setMotivoEliminacion(motivo);
            calificacionRepositorio.save(cal);
            log.info("Calificación #{} moderada/eliminada por admin con motivo: {}", id, motivo);
            return true;
        }
        return false;
    }
}
