package com.travelink.repositorio;

import com.travelink.entidades.Comision;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * ROL: DAO Principal (Spring Data JPA)
 * Responsabilidad: Operaciones CRUD y consultas simples sobre la entidad Comision.
 */
@Repository
public interface ComisionRepositorio extends JpaRepository<Comision, Integer> {
    List<Comision> findByIdAgencia(Integer idAgencia);
    List<Comision> findByPeriodo(String periodo);
    List<Comision> findByEstado(String estado);
}
