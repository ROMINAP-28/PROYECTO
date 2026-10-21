package com.travelink.servicio.impl;

import com.travelink.entidades.Destino;
import com.travelink.repositorio.DestinoRepositorio;
import com.travelink.servicio.DestinoServicio;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

/**
 * Implementación del Servicio de Destinos.
 * Aplica principios SOLID:
 * - SRP: Lógica del catálogo de destinos.
 * - DIP: Inyección por constructor.
 */
@Service
public class DestinoServicioImpl implements DestinoServicio {

    private final DestinoRepositorio destinoRepositorio;

    public DestinoServicioImpl(DestinoRepositorio destinoRepositorio) {
        this.destinoRepositorio = destinoRepositorio;
    }

    @Override
    public List<Destino> listarTodos() {
        return destinoRepositorio.findAll();
    }

    @Override
    public List<Destino> listarActivos() {
        return destinoRepositorio.findByEstado("ACTIVO");
    }

    @Override
    public Optional<Destino> buscarPorId(Integer id) {
        return destinoRepositorio.findById(id);
    }

    @Override
    @Transactional
    public Destino guardar(Destino destino) {
        return destinoRepositorio.save(destino);
    }
}
