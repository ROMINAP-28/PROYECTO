package com.travelink.repositorio;

import com.travelink.entidades.Pasajero;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * ROL: DAO Principal (Spring Data JPA)
 * Responsabilidad: Operaciones CRUD y consultas simples sobre la entidad Pasajero.
 */
@Repository
public interface PasajeroRepositorio extends JpaRepository<Pasajero, Integer> {
    List<Pasajero> findByIdReserva(Integer idReserva);
}
