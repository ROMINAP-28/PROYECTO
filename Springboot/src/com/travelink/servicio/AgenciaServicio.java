package com.travelink.servicio;

import com.travelink.entidades.Agencia;
import com.travelink.repositorio.AgenciaRepositorio;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Service
public class AgenciaServicio {

    private final AgenciaRepositorio agenciaRepositorio;

    public AgenciaServicio(AgenciaRepositorio agenciaRepositorio) {
        this.agenciaRepositorio = agenciaRepositorio;
    }

    public List<Agencia> listarTodas() {
        return agenciaRepositorio.findAll();
    }

    public List<Agencia> listarPorEstado(String estado) {
        return agenciaRepositorio.findByEstado(estado);
    }

    public Optional<Agencia> buscarPorId(Integer id) {
        return agenciaRepositorio.findById(id);
    }

    public Optional<Agencia> buscarPorRuc(String ruc) {
        return agenciaRepositorio.findByRuc(ruc);
    }

    public Optional<Agencia> buscarPorIdUsuario(Integer idUsuario) {
        return agenciaRepositorio.findByIdUsuario(idUsuario);
    }

    @Transactional
    public Agencia guardar(Agencia agencia) {
        return agenciaRepositorio.save(agencia);
    }
}
