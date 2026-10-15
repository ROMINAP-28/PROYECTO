package com.travelink.repositorio;

import com.travelink.entidades.TourImagen;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface TourImagenRepositorio extends JpaRepository<TourImagen, Integer> {
    List<TourImagen> findByIdTourOrderByOrdenAsc(Integer idTour);
}
