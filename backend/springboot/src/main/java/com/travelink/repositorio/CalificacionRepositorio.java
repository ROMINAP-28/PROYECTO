package com.travelink.repositorio;

import com.travelink.entidades.Calificacion;
import java.util.List;
import java.util.Optional;

public interface CalificacionRepositorio {
    Calificacion guardar(Calificacion calificacion);
    Optional<Calificacion> buscarPorId(int idCalificacion);
    List<Calificacion> buscarPorAgencia(int idAgencia);
    List<Calificacion> buscarPorUsuario(int idUsuario);
    Optional<Calificacion> buscarPorReserva(int idReserva);
}
