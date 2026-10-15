package com.travelink.servicio;

import com.travelink.entidades.Tour;
import com.travelink.repositorio.TourRepositorio;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Service
public class TourServicio {

    private final TourRepositorio tourRepositorio;

    public TourServicio(TourRepositorio tourRepositorio) {
        this.tourRepositorio = tourRepositorio;
    }

    public List<Tour> listarTodos() {
        return tourRepositorio.findAll();
    }

    public List<Tour> listarPorEstado(String estado) {
        return tourRepositorio.findByEstado(estado);
    }

    public Optional<Tour> buscarPorId(Integer id) {
        return tourRepositorio.findById(id);
    }

    public Optional<Tour> buscarPorSlug(String slug) {
        return tourRepositorio.findBySlug(slug);
    }

    public List<Tour> listarPorAgencia(Integer idAgencia) {
        return tourRepositorio.findByIdAgencia(idAgencia);
    }

    @Transactional
    public Tour guardar(Tour tour) {
        return tourRepositorio.save(tour);
    }

    @Transactional
    public void eliminar(Integer id) {
        tourRepositorio.deleteById(id);
    }
}
