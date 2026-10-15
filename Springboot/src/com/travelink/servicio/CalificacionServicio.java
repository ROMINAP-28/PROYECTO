package com.travelink.servicio;

import com.travelink.entidades.Calificacion;
import com.travelink.repositorio.CalificacionRepositorio;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Service
public class CalificacionServicio {

    private final CalificacionRepositorio calificacionRepositorio;

    public CalificacionServicio(CalificacionRepositorio calificacionRepositorio) {
        this.calificacionRepositorio = calificacionRepositorio;
    }

    public List<Calificacion> listarPorAgencia(Integer idAgencia) {
        return calificacionRepositorio.findByIdAgencia(idAgencia);
    }

    public List<Calificacion> listarPorUsuario(Integer idUsuario) {
        return calificacionRepositorio.findByIdUsuario(idUsuario);
    }

    public Double obtenerPromedioAgencia(Integer idAgencia) {
        Double avg = calificacionRepositorio.promedioEstrellasPorAgencia(idAgencia);
        return avg != null ? Math.round(avg * 10.0) / 10.0 : 5.0;
    }

    @Transactional
    public Calificacion guardar(Calificacion calificacion) {
        return calificacionRepositorio.save(calificacion);
    }

    @Transactional
    public boolean marcarEliminado(Integer id, String motivo) {
        Optional<Calificacion> c = calificacionRepositorio.findById(id);
        if (c.isPresent()) {
            Calificacion cal = c.get();
            cal.setEliminadoPorAdmin(true);
            cal.setMotivoEliminacion(motivo);
            calificacionRepositorio.save(cal);
            return true;
        }
        return false;
    }
}
