package com.travelink.servicio;

import com.travelink.entidades.Tour;
import com.travelink.entidades.TourFecha;

import java.util.List;
import java.util.Optional;

/**
 * Interface del Servicio de Tours y Paquetes Turísticos.
 * Aplica principios SOLID:
 * - SRP: Gestión del catálogo de tours, precios, imágenes y fechas disponibles.
 * - ISP: Interface con métodos específicos para turistas y agencias.
 * - DIP: Inyección de la abstracción en los controladores.
 */
public interface TourServicio {

    List<Tour> listarTodos();

    List<Tour> listarPorEstado(String estado);

    Optional<Tour> buscarPorId(Integer id);

    Optional<Tour> buscarPorSlug(String slug);

    List<Tour> listarPorAgencia(Integer idAgencia);

    Tour guardar(Tour tour);

    void eliminar(Integer id);

    List<TourFecha> listarFechasPorTour(Integer idTour);

    TourFecha guardarFecha(TourFecha fecha);
}
