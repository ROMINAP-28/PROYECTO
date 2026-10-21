package com.travelink.repositorio;

import com.travelink.entidades.Persona;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

/**
 * ROL: DAO Principal (Spring Data JPA)
 * Responsabilidad: Operaciones CRUD y consultas simples sobre la entidad Persona.
 */
@Repository
public interface PersonaRepositorio extends JpaRepository<Persona, Integer> {
    Optional<Persona> findByNroDocumento(String nroDocumento);
}
