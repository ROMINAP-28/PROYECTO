package com.travelink.repositorio;

import com.travelink.entidades.Rol;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

/**
 * ROL: DAO Principal (Spring Data JPA)
 * Responsabilidad: Operaciones CRUD y consultas simples sobre la entidad Rol.
 */
@Repository
public interface RolRepositorio extends JpaRepository<Rol, Integer> {
    Optional<Rol> findByNombreRol(String nombreRol);
}
