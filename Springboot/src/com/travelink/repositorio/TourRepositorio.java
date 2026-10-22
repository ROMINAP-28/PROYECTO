package com.travelink.repositorio;

import com.travelink.entidades.Tour;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface TourRepositorio extends JpaRepository<Tour, Integer> {
    Optional<Tour> findBySlug(String slug);
    List<Tour> findByIdAgencia(Integer idAgencia);
    List<Tour> findByIdDestino(Integer idDestino);
    List<Tour> findByCategoria(String categoria);
    List<Tour> findByEstado(String estado);

    @Query("SELECT t FROM Tour t WHERE t.idAgencia = :idAgencia AND t.estado = :estado")
    List<Tour> findByIdAgenciaAndEstado(@Param("idAgencia") Integer idAgencia, @Param("estado") String estado);
}
