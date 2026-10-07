package com.travelink.controlador;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.io.File;
import java.io.OutputStream;
import java.nio.charset.StandardCharsets;
import com.travelink.entidades.Usuario;

import com.travelink.entidades.Usuario;
import com.travelink.persistencia.UsuarioPersistencia;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

public class UsuarioControlador {
    private final UsuarioPersistencia usuarioPersistencia;

    public UsuarioControlador() {
        this.usuarioPersistencia = new UsuarioPersistencia();
    }

    public Map<String, Object> login(String usuarioCorreo, String contrasena) {
        Map<String, Object> respuesta = new HashMap<>();
        Optional<Usuario> usuarioOpt = usuarioPersistencia.buscarPorCorreoOUsuario(usuarioCorreo);

        if (usuarioOpt.isPresent()) {
            Usuario u = usuarioOpt.get();
            if (u.getContrasena().equals(contrasena)) {
                respuesta.put("status", "success");
                respuesta.put("message", "Login exitoso");
                respuesta.put("user", u);
                return respuesta;
            }
        }

        respuesta.put("status", "error");
        respuesta.put("message", "Credenciales incorrectas");
        return respuesta;
    }

    public Map<String, Object> registrar(Usuario nuevoUsuario) {
        Map<String, Object> respuesta = new HashMap<>();

        if (usuarioPersistencia.buscarPorCorreoOUsuario(nuevoUsuario.getCorreo()).isPresent()) {
            respuesta.put("status", "error");
            respuesta.put("message", "El correo ya se encuentra registrado");
            return respuesta;
        }

        Usuario guardado = usuarioPersistencia.guardar(nuevoUsuario);
        respuesta.put("status", "success");
        respuesta.put("message", "Usuario registrado exitosamente");
        respuesta.put("user", guardado);
        return respuesta;
    }
}

