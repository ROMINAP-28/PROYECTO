package com.travelink.servicio.impl;

import com.travelink.entidades.Tour;
import com.travelink.entidades.TourFecha;
import com.travelink.repositorio.TourFechaRepositorio;
import com.travelink.repositorio.TourRepositorio;
import com.travelink.servicio.TourServicio;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

/**
 * Implementación del Servicio de Tours.
 * Aplica principios SOLID:
 * - SRP: Lógica de negocio de tours y disponibilidad.
 * - DIP: Inyección de repositorios por constructor.
 */
@Service
public class TourServicioImpl implements TourServicio {

    private final TourRepositorio tourRepositorio;
    private final TourFechaRepositorio tourFechaRepositorio;

    public TourServicioImpl(TourRepositorio tourRepositorio,
                            TourFechaRepositorio tourFechaRepositorio) {
        this.tourRepositorio = tourRepositorio;
        this.tourFechaRepositorio = tourFechaRepositorio;
    }

    @Override
    public List<Tour> listarTodos() {
        return tourRepositorio.findAll();
    }

    @Override
    public List<Tour> listarPorEstado(String estado) {
        return tourRepositorio.findByEstado(estado);
    }

    @Override
    public Optional<Tour> buscarPorId(Integer id) {
        return tourRepositorio.findById(id);
    }

    @Override
    public Optional<Tour> buscarPorSlug(String slug) {
        return tourRepositorio.findBySlug(slug);
    }

    @Override
    public List<Tour> listarPorAgencia(Integer idAgencia) {
        return tourRepositorio.findByIdAgencia(idAgencia);
    }

    @Override
    @Transactional
    public Tour guardar(Tour tour) {
        return tourRepositorio.save(tour);
    }

    @Override
    @Transactional
    public void eliminar(Integer id) {
        tourRepositorio.deleteById(id);
    }

    @Override
    public List<TourFecha> listarFechasPorTour(Integer idTour) {
        return tourFechaRepositorio.findByIdTourOrderByFechaAsc(idTour);
    }

    @Override
    @Transactional
    public TourFecha guardarFecha(TourFecha fecha) {
        return tourFechaRepositorio.save(fecha);
    }
}
