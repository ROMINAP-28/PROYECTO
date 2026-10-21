package com.travelink.servicio;

import com.travelink.controlador.dto.RegistroTuristaRequest;
import com.travelink.entidades.Usuario;

import java.util.List;
import java.util.Optional;

/**
 * Interface del Servicio de Usuarios y Autenticación.
 * Aplica principios SOLID:
 * - SRP: Gestión de cuentas de usuario, perfiles y seguridad de contraseñas.
 * - DIP: Abstracción inyectable en controladores.
 */
public interface UsuarioServicio {

    List<Usuario> listarTodos();

    Optional<Usuario> buscarPorId(Integer id);

    Optional<Usuario> buscarPorLogin(String login);

    Optional<Usuario> buscarPorCorreoOUsuario(String login);

    Usuario registrar(Usuario usuario);

    Usuario guardar(Usuario usuario);

    Usuario registrarTurista(RegistroTuristaRequest request);

    boolean verificarContrasena(String rawPassword, String encodedPassword);
}
