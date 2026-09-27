package com.travelink.repositorio;

import com.travelink.entidades.Usuario;
import java.util.List;
import java.util.Optional;

public interface UsuarioRepositorio {
    Usuario guardar(Usuario usuario);
    Optional<Usuario> buscarPorId(int idUsuario);
    Optional<Usuario> buscarPorNombreUsuario(String nombreUsuario);
    Optional<Usuario> buscarPorCorreoOUsuario(String login);
    List<Usuario> listarTodos();
    boolean actualizar(Usuario usuario);
    boolean eliminar(int idUsuario);
}
