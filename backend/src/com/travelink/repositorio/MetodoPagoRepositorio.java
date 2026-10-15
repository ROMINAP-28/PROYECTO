package com.travelink.repositorio;

import com.travelink.entidades.MetodoPago;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface MetodoPagoRepositorio extends JpaRepository<MetodoPago, Integer> {
    List<MetodoPago> findByIdReserva(Integer idReserva);
    Optional<MetodoPago> findFirstByIdReserva(Integer idReserva);
}
