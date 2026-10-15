package com.travelink.repositorio;

import com.travelink.entidades.HistorialAgencia;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface HistorialAgenciaRepositorio extends JpaRepository<HistorialAgencia, Integer> {
    List<HistorialAgencia> findByIdAgencia(Integer idAgencia);
}
