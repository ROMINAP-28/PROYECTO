package com.travelink.repositorio;

import com.travelink.entidades.Calificacion;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * ROL: DAO Principal (Spring Data JPA)
 * Responsabilidad: Operaciones CRUD y consultas simples sobre la entidad Calificacion.
 */
@Repository
public interface CalificacionRepositorio extends JpaRepository<Calificacion, Integer> {
    List<Calificacion> findByIdAgencia(Integer idAgencia);
    List<Calificacion> findByIdUsuario(Integer idUsuario);
    List<Calificacion> findByIdReserva(Integer idReserva);
    List<Calificacion> findByFlageadaTrue();
    List<Calificacion> findByEliminadoPorAdminFalse();

    @Query("SELECT AVG(c.estrellas) FROM Calificacion c WHERE c.idAgencia = :idAgencia AND c.eliminadoPorAdmin = false")
    Double promedioEstrellasPorAgencia(@Param("idAgencia") Integer idAgencia);
}
