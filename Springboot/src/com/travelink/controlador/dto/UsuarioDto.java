package com.travelink.controlador.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.travelink.entidades.Usuario;

import java.util.Date;

@JsonInclude(JsonInclude.Include.NON_NULL)
public class UsuarioDto {

    private Integer idUsuario;
    private String nombreUsuario;
    private Integer idRol;
    private String rol;
    private String estado;
    private Date fechaRegistro;

    // Datos de la Persona asociada
    private Integer idPersona;
    private String nombre;
    private String apellidoPaterno;
    private String apellidoMaterno;
    private String nombreCompleto;
    private String nroDocumento;
    private String telefono;
    private String email;

    public UsuarioDto() {}

    public static UsuarioDto desdeEntidad(Usuario u) {
        if (u == null) return null;
        UsuarioDto dto = new UsuarioDto();
        dto.setIdUsuario(u.getIdUsuario());
        dto.setNombreUsuario(u.getNombreUsuario());
        dto.setIdRol(u.getIdRol());
        if (u.getIdRol() != null) {
            if (u.getIdRol() == 3) dto.setRol("ADMIN");
            else if (u.getIdRol() == 2) dto.setRol("AGENCIA");
            else dto.setRol("TURISTA");
        }
        dto.setEstado(u.getEstado());
        dto.setFechaRegistro(u.getFechaRegistro());

        if (u.getPersona() != null) {
            dto.setIdPersona(u.getPersona().getIdPersona());
            dto.setNombre(u.getPersona().getNombre());
            dto.setApellidoPaterno(u.getPersona().getApellidoPaterno());
            dto.setApellidoMaterno(u.getPersona().getApellidoMaterno());
            dto.setNombreCompleto(u.getPersona().getNombreCompleto());
            dto.setNroDocumento(u.getPersona().getNroDocumento());
            dto.setTelefono(u.getPersona().getTelefono());
            dto.setEmail(u.getPersona().getEmail());
        } else {
            dto.setNombre(u.getNombre());
            dto.setApellidoPaterno(u.getApellidoPaterno());
            dto.setApellidoMaterno(u.getApellidoMaterno());
            dto.setNombreCompleto(u.getNombreUsuario());
            dto.setNroDocumento(u.getNroDocumento());
            dto.setTelefono(u.getTelefono());
            dto.setEmail(u.getCorreo());
        }
        return dto;
    }

    public Integer getIdUsuario() { return idUsuario; }
    public void setIdUsuario(Integer idUsuario) { this.idUsuario = idUsuario; }

    public String getNombreUsuario() { return nombreUsuario; }
    public void setNombreUsuario(String nombreUsuario) { this.nombreUsuario = nombreUsuario; }

    public Integer getIdRol() { return idRol; }
    public void setIdRol(Integer idRol) { this.idRol = idRol; }

    public String getRol() { return rol; }
    public void setRol(String rol) { this.rol = rol; }

    public String getEstado() { return estado; }
    public void setEstado(String estado) { this.estado = estado; }

    public Date getFechaRegistro() { return fechaRegistro; }
    public void setFechaRegistro(Date fechaRegistro) { this.fechaRegistro = fechaRegistro; }

    public Integer getIdPersona() { return idPersona; }
    public void setIdPersona(Integer idPersona) { this.idPersona = idPersona; }

    public String getNombre() { return nombre; }
    public void setNombre(String nombre) { this.nombre = nombre; }

    public String getApellidoPaterno() { return apellidoPaterno; }
    public void setApellidoPaterno(String apellidoPaterno) { this.apellidoPaterno = apellidoPaterno; }

    public String getApellidoMaterno() { return apellidoMaterno; }
    public void setApellidoMaterno(String apellidoMaterno) { this.apellidoMaterno = apellidoMaterno; }

    public String getNombreCompleto() { return nombreCompleto; }
    public void setNombreCompleto(String nombreCompleto) { this.nombreCompleto = nombreCompleto; }

    public String getNroDocumento() { return nroDocumento; }
    public void setNroDocumento(String nroDocumento) { this.nroDocumento = nroDocumento; }

    public String getTelefono() { return telefono; }
    public void setTelefono(String telefono) { this.telefono = telefono; }

    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }
}
