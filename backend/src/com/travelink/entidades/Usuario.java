package com.travelink.entidades;

import jakarta.persistence.*;
import java.util.Date;

@Entity
@Table(name = "Usuario")
public class Usuario {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer idUsuario;

    @Column(name = "idPersona", insertable = false, updatable = false)
    private Integer idPersona;

    @Column(name = "idRol", insertable = false, updatable = false)
    private Integer idRol;

    @Column(name = "nombreUsuario", nullable = false, unique = true, length = 30)
    private String nombreUsuario;

    @com.fasterxml.jackson.annotation.JsonProperty(access = com.fasterxml.jackson.annotation.JsonProperty.Access.WRITE_ONLY)
    @Column(name = "contrasena", nullable = false, length = 255)
    private String contrasena;

    @Column(name = "estado", length = 20)
    private String estado = "ACTIVO";

    @Temporal(TemporalType.TIMESTAMP)
    @Column(name = "fechaRegistro")
    private Date fechaRegistro = new Date();

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "idPersona", nullable = false)
    private Persona persona;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "idRol", nullable = false)
    private Rol rol;

    public Usuario() {}

    public Usuario(Integer idUsuario, Integer idPersona, Integer idRol, String nombreUsuario, String contrasena) {
        this.idUsuario = idUsuario;
        this.idPersona = idPersona;
        this.idRol = idRol;
        this.nombreUsuario = nombreUsuario;
        this.contrasena = contrasena;
        this.estado = "ACTIVO";
        this.fechaRegistro = new Date();
    }

    public Integer getIdUsuario() { return idUsuario; }
    public void setIdUsuario(Integer idUsuario) { this.idUsuario = idUsuario; }

    public Integer getIdPersona() { return idPersona != null ? idPersona : (persona != null ? persona.getIdPersona() : null); }
    public void setIdPersona(Integer idPersona) { this.idPersona = idPersona; }

    public Integer getIdRol() { return idRol != null ? idRol : (rol != null ? rol.getIdRol() : null); }
    public void setIdRol(Integer idRol) { this.idRol = idRol; }

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
