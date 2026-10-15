package com.travelink.repositorio;

import com.travelink.entidades.DetalleReserva;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface DetalleReservaRepositorio extends JpaRepository<DetalleReserva, Integer> {
    List<DetalleReserva> findByIdReserva(Integer idReserva);
}
