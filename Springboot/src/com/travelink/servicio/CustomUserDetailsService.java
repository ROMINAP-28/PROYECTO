package com.travelink.servicio;

import com.travelink.entidades.Usuario;
import com.travelink.repositorio.UsuarioRepositorio;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import java.util.Collections;

@Service
public class CustomUserDetailsService implements UserDetailsService {

    private final UsuarioRepositorio usuarioRepositorio;

    public CustomUserDetailsService(UsuarioRepositorio usuarioRepositorio) {
        this.usuarioRepositorio = usuarioRepositorio;
    }

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        Usuario usuario = usuarioRepositorio.buscarPorCorreoOUsuario(username)
                .orElseThrow(() -> new UsernameNotFoundException("Usuario no encontrado con correo o username: " + username));

        String rolName = "ROLE_TURISTA";
        if (usuario.getIdRol() != null) {
            if (usuario.getIdRol() == 3) {
                rolName = "ROLE_ADMIN";
            } else if (usuario.getIdRol() == 2) {
                rolName = "ROLE_AGENCIA";
            }
        }

        boolean enabled = "ACTIVO".equalsIgnoreCase(usuario.getEstado());

        return new User(
                usuario.getNombreUsuario(),
                usuario.getContrasena() != null ? usuario.getContrasena() : "",
                enabled,
                true,
                true,
                true,
                Collections.singletonList(new SimpleGrantedAuthority(rolName))
        );
    }
}
