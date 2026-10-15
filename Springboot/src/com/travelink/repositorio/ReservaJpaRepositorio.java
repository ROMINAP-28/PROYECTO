package com.travelink.repositorio;

import com.travelink.entidades.Reserva;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ReservaJpaRepositorio extends JpaRepository<Reserva, Integer> {
    Optional<Reserva> findByCodigoReserva(String codigoReserva);
    List<Reserva> findByIdUsuarioOrderByFechaRegistroDesc(Integer idUsuario);
    List<Reserva> findByIdAgenciaOrderByFechaRegistroDesc(Integer idAgencia);
    List<Reserva> findByEstado(String estado);
}
