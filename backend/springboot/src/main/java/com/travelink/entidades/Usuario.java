package com.travelink.entidades;

import java.util.Date;

public class Usuario {
    private int idUsuario;
    private int idPersona; // FK → Persona
    private int idRol;     // FK → Rol
    private String nombreUsuario; // único
    private String contrasena;    // hasheada
    private String estado;
    private Date fechaRegistro;

    // Relaciones (Objetos Anidados)
    private Persona persona;
    private Rol rol;

    public Usuario() {}

    public Usuario(int idUsuario, int idPersona, int idRol, String nombreUsuario, String contrasena) {
        this.idUsuario = idUsuario;
        this.idPersona = idPersona;
        this.idRol = idRol;
        this.nombreUsuario = nombreUsuario;
        this.contrasena = contrasena;
        this.estado = "ACTIVO";
        this.fechaRegistro = new Date();
    }

    public int getIdUsuario() { return idUsuario; }
    public void setIdUsuario(int idUsuario) { this.idUsuario = idUsuario; }

    public int getIdPersona() { return idPersona; }
    public void setIdPersona(int idPersona) { this.idPersona = idPersona; }

    public int getIdRol() { return idRol; }
    public void setIdRol(int idRol) { this.idRol = idRol; }

    public String getNombreUsuario() { return nombreUsuario; }
    public void setNombreUsuario(String nombreUsuario) { this.nombreUsuario = nombreUsuario; }

    public String getContrasena() { return contrasena; }
    public void setContrasena(String contrasena) { this.contrasena = contrasena; }

    public String getEstado() { return estado; }
    public void setEstado(String estado) { this.estado = estado; }

    public Date getFechaRegistro() { return fechaRegistro; }
    public void setFechaRegistro(Date fechaRegistro) { this.fechaRegistro = fechaRegistro; }

    public Persona getPersona() { return persona; }
    public void setPersona(Persona persona) { 
        this.persona = persona; 
        if (persona != null) {
            this.idPersona = persona.getIdPersona();
        }
    }

    public Rol getRol() { return rol; }
    public void setRol(Rol rol) { 
        this.rol = rol; 
        if (rol != null) {
            this.idRol = rol.getIdRol();
        }
    }

    // Métodos helper de compatibilidad delegados a Persona
    public String getNombre() { return persona != null ? persona.getNombre() : ""; }
    public void setNombre(String nombre) {
        if (this.persona == null) this.persona = new Persona();
        this.persona.setNombre(nombre);
    }

    public String getApellidoPaterno() { return persona != null ? persona.getApellidoPaterno() : ""; }
    public void setApellidoPaterno(String apellidoPaterno) {
        if (this.persona == null) this.persona = new Persona();
        this.persona.setApellidoPaterno(apellidoPaterno);
    }

    public String getApellidoMaterno() { return persona != null ? persona.getApellidoMaterno() : ""; }
    public void setApellidoMaterno(String apellidoMaterno) {
        if (this.persona == null) this.persona = new Persona();
        this.persona.setApellidoMaterno(apellidoMaterno);
    }

    public String getCorreo() { return persona != null ? persona.getEmail() : ""; }
    public void setCorreo(String correo) {
        if (this.persona == null) this.persona = new Persona();
        this.persona.setEmail(correo);
    }

    public String getEmail() { return getCorreo(); }
    public void setEmail(String email) { setCorreo(email); }

    public String getTelefono() { return persona != null ? persona.getTelefono() : ""; }
    public void setTelefono(String telefono) {
        if (this.persona == null) this.persona = new Persona();
        this.persona.setTelefono(telefono);
    }

    public String getNroDocumento() { return persona != null ? persona.getNroDocumento() : ""; }
    public void setNroDocumento(String nroDocumento) {
        if (this.persona == null) this.persona = new Persona();
        this.persona.setNroDocumento(nroDocumento);
    }
}
