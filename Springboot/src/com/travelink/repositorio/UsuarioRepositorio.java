package com.travelink.repositorio;

import com.travelink.entidades.Usuario;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface UsuarioRepositorio extends JpaRepository<Usuario, Integer> {
    Optional<Usuario> findByPersonaEmail(String email);
    Optional<Usuario> findByNombreUsuario(String nombreUsuario);

    @Query("SELECT u FROM Usuario u WHERE LOWER(TRIM(u.persona.email)) = LOWER(TRIM(:login)) OR LOWER(TRIM(u.nombreUsuario)) = LOWER(TRIM(:login))")
    Optional<Usuario> buscarPorCorreoOUsuario(@Param("login") String login);

    List<Usuario> findByIdRol(Integer idRol);
}
