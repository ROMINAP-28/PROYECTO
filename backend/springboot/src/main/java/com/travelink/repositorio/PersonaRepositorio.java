package com.travelink.repositorio;

import com.travelink.entidades.Persona;
import java.util.List;
import java.util.Optional;

public interface PersonaRepositorio {
    Persona guardar(Persona persona);
    Optional<Persona> buscarPorId(int idPersona);
    Optional<Persona> buscarPorDocumento(String nroDocumento);
    Optional<Persona> buscarPorEmail(String email);
    List<Persona> listarTodos();
    boolean actualizar(Persona persona);
    boolean eliminar(int idPersona);
}
