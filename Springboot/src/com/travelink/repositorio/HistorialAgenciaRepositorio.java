package com.travelink.repositorio;

import com.travelink.entidades.HistorialAgencia;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * ROL: DAO Principal (Spring Data JPA)
 * Responsabilidad: Operaciones CRUD y consultas simples sobre la entidad HistorialAgencia.
 */
@Repository
public interface HistorialAgenciaRepositorio extends JpaRepository<HistorialAgencia, Integer> {
    List<HistorialAgencia> findByIdAgencia(Integer idAgencia);
}
