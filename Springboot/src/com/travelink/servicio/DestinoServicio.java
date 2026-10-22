package com.travelink.servicio;

import com.travelink.entidades.Destino;
import com.travelink.repositorio.DestinoRepositorio;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Service
public class DestinoServicio {

    private final DestinoRepositorio destinoRepositorio;

    public DestinoServicio(DestinoRepositorio destinoRepositorio) {
        this.destinoRepositorio = destinoRepositorio;
    }

    public List<Destino> listarTodos() {
        return destinoRepositorio.findAll();
    }

    public List<Destino> listarActivos() {
        return destinoRepositorio.findByEstado("ACTIVO");
    }

    public Optional<Destino> buscarPorId(Integer id) {
        return destinoRepositorio.findById(id);
    }

    @Transactional
    public Destino guardar(Destino destino) {
        return destinoRepositorio.save(destino);
    }
}
