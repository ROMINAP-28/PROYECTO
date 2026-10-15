package com.travelink.repositorio;

import com.travelink.entidades.SolicitudAgencia;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface SolicitudAgenciaRepositorio extends JpaRepository<SolicitudAgencia, Integer> {
    List<SolicitudAgencia> findByIdUsuario(Integer idUsuario);
    List<SolicitudAgencia> findByEstado(String estado);
    Optional<SolicitudAgencia> findByRuc(String ruc);
}
