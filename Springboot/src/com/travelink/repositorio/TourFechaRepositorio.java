package com.travelink.repositorio;

import com.travelink.entidades.TourFecha;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Date;
import java.util.List;

@Repository
public interface TourFechaRepositorio extends JpaRepository<TourFecha, Integer> {
    List<TourFecha> findByIdTour(Integer idTour);
    List<TourFecha> findByIdTourAndFechaGreaterThanEqual(Integer idTour, Date fecha);

    @Modifying
    @Query("UPDATE TourFecha SET cupoDisponible = cupoDisponible - :n, estado = CASE WHEN (cupoDisponible - :n) <= 0 THEN 'AGOTADO' ELSE 'DISPONIBLE' END WHERE idTourFecha = :id AND cupoDisponible >= :n")
    int descontarCupo(@Param("id") Integer id, @Param("n") Integer n);

    @Modifying
    @Query("UPDATE TourFecha SET cupoDisponible = cupoDisponible + :n, estado = 'DISPONIBLE' WHERE idTourFecha = :id")
    int devolverCupo(@Param("id") Integer id, @Param("n") Integer n);
}
