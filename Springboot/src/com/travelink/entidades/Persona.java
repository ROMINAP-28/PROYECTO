package com.travelink.entidades;

import jakarta.persistence.*;

@Entity
@Table(name = "Persona")
public class Persona {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer idPersona;

    @Column(name = "nombre", nullable = false, length = 50)
    private String nombre;

    @Column(name = "apellidoPaterno", nullable = false, length = 50)
    private String apellidoPaterno;

    @Column(name = "apellidoMaterno", nullable = false, length = 50)
    private String apellidoMaterno;

    @Column(name = "nroDocumento", nullable = false, unique = true, length = 15)
    private String nroDocumento;

    @Column(name = "telefono", length = 15)
    private String telefono;

    @Column(name = "email", nullable = false, unique = true, length = 100)
    private String email;

    public Persona() {}

    public Persona(Integer idPersona, String nombre, String apellidoPaterno, String apellidoMaterno, String nroDocumento, String telefono, String email) {
        this.idPersona = idPersona;
        this.nombre = nombre;
        this.apellidoPaterno = apellidoPaterno;
        this.apellidoMaterno = apellidoMaterno;
        this.nroDocumento = nroDocumento;
        this.telefono = telefono;
        this.email = email;
    }

    public Integer getIdPersona() { return idPersona; }
    public void setIdPersona(Integer idPersona) { this.idPersona = idPersona; }

    public String getNombre() { return nombre; }
    public void setNombre(String nombre) { this.nombre = nombre; }

    public String getApellidoPaterno() { return apellidoPaterno; }
    public void setApellidoPaterno(String apellidoPaterno) { this.apellidoPaterno = apellidoPaterno; }

    public String getApellidoMaterno() { return apellidoMaterno; }
    public void setApellidoMaterno(String apellidoMaterno) { this.apellidoMaterno = apellidoMaterno; }

    public String getNroDocumento() { return nroDocumento; }
    public void setNroDocumento(String nroDocumento) { this.nroDocumento = nroDocumento; }

    public String getTelefono() { return telefono; }
    public void setTelefono(String telefono) { this.telefono = telefono; }

    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }

    public String getNombreCompleto() {
        return (nombre != null ? nombre : "") + " " + 
               (apellidoPaterno != null ? apellidoPaterno : "") + " " + 
               (apellidoMaterno != null ? apellidoMaterno : "").trim();
    }
}
