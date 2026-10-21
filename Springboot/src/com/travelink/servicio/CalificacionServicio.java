package com.travelink.servicio;

import com.travelink.controlador.dto.CalificacionRequest;
import com.travelink.entidades.Calificacion;

import java.util.List;

/**
 * Interface del Servicio de Calificaciones y Reseñas.
 * Aplica principios SOLID:
 * - SRP: Gestión de valoraciones, cálculo de promedios de satisfacción y moderación.
 * - DIP: Abstracción desacoplada de la capa de persistencia.
 */
public interface CalificacionServicio {

    List<Calificacion> listarPorAgencia(Integer idAgencia);

    List<Calificacion> listarPorUsuario(Integer idUsuario);

    Double obtenerPromedioAgencia(Integer idAgencia);

    Calificacion guardar(Calificacion calificacion);

    Calificacion registrarCalificacion(CalificacionRequest req, int idUsuario);

    boolean marcarEliminado(Integer id, String motivo);
}
