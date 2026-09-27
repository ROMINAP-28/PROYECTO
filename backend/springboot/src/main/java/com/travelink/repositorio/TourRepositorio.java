package com.travelink.repositorio;

import com.travelink.entidades.Tour;
import java.util.List;
import java.util.Optional;

public interface TourRepositorio {
    Tour guardar(Tour tour);
    Optional<Tour> buscarPorId(int idTour);
    Optional<Tour> buscarPorSlug(String slug);
    List<Tour> listarTodos();
    List<Tour> buscarPorAgencia(int idAgencia);
    List<Tour> buscarPorCategoria(String categoria);
    boolean actualizar(Tour tour);
    boolean eliminar(int idTour);
}
