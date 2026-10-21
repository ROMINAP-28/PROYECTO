package com.travelink.repositorio;

import com.travelink.entidades.Agencia;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * ROL: DAO Principal (Spring Data JPA)
 * Responsabilidad: Operaciones CRUD y consultas simples sobre la entidad Agencia.
 */
@Repository
public interface AgenciaRepositorio extends JpaRepository<Agencia, Integer> {
    Optional<Agencia> findByRuc(String ruc);
    Optional<Agencia> findByIdUsuario(Integer idUsuario);
    List<Agencia> findByEstado(String estado);
}
