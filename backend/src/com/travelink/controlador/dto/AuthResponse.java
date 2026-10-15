package com.travelink.controlador.dto;

import com.fasterxml.jackson.annotation.JsonInclude;

import java.util.HashMap;
import java.util.Map;

@JsonInclude(JsonInclude.Include.NON_NULL)
public class AuthResponse {

    private Integer idUsuario;
    private String nombre;
    private String correo;
    private String rol;
    private Integer idAgencia;

    // Campos retrocompatibles para scripts existentes
    private String status = "success";
    private String message;
    private Map<String, Object> user;

    public AuthResponse() {}

    public AuthResponse(Integer idUsuario, String nombre, String correo, String rol, Integer idAgencia) {
        this.idUsuario = idUsuario;
        this.nombre = nombre;
        this.correo = correo;
        this.rol = rol;
        this.idAgencia = idAgencia;

        // Construir objeto anidado retrocompatible
        Map<String, Object> u = new HashMap<>();
        u.put("idUsuario", idUsuario);
        u.put("nombre", nombre);
        u.put("correo", correo);
        u.put("email", correo);
        u.put("rol", rol);
        u.put("idRol", "ROLE_ADMIN".equals(rol) ? 3 : ("ROLE_AGENCIA".equals(rol) ? 2 : 1));
        if (idAgencia != null) {
            u.put("idAgencia", idAgencia);
        }
        this.user = u;
    }

    public Integer getIdUsuario() { return idUsuario; }
    public void setIdUsuario(Integer idUsuario) { this.idUsuario = idUsuario; }

    public String getNombre() { return nombre; }
    public void setNombre(String nombre) { this.nombre = nombre; }

    public String getCorreo() { return correo; }
    public void setCorreo(String correo) { this.correo = correo; }

    public String getRol() { return rol; }
    public void setRol(String rol) { this.rol = rol; }

    public Integer getIdAgencia() { return idAgencia; }
    public void setIdAgencia(Integer idAgencia) { this.idAgencia = idAgencia; }

    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }

    public String getMessage() { return message; }
    public void setMessage(String message) { this.message = message; }

    public Map<String, Object> getUser() { return user; }
    public void setUser(Map<String, Object> user) { this.user = user; }
}
