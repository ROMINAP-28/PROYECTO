package com.travelink.repositorio;

import com.travelink.entidades.Destino;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface DestinoRepositorio extends JpaRepository<Destino, Integer> {
    Optional<Destino> findByNombre(String nombre);
    List<Destino> findByEstado(String estado);
}
