package com.travelink.repositorio;

import com.travelink.entidades.Agencia;
import java.util.List;
import java.util.Optional;

public interface AgenciaRepositorio {
    Agencia guardar(Agencia agencia);
    Optional<Agencia> buscarPorId(int idAgencia);
    Optional<Agencia> buscarPorRuc(String ruc);
    List<Agencia> listarTodas();
    boolean actualizar(Agencia agencia);
    boolean eliminar(int idAgencia);
}
