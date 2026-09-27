package com.travelink.entidades;

public class Persona {
    private int idPersona;
    private String nombre;
    private String apellidoPaterno;
    private String apellidoMaterno;
    private String nroDocumento;
    private String telefono;
    private String email;

    public Persona() {}

    public Persona(int idPersona, String nombre, String apellidoPaterno, String apellidoMaterno, String nroDocumento, String telefono, String email) {
        this.idPersona = idPersona;
        this.nombre = nombre;
        this.apellidoPaterno = apellidoPaterno;
        this.apellidoMaterno = apellidoMaterno;
        this.nroDocumento = nroDocumento;
        this.telefono = telefono;
        this.email = email;
    }

    public int getIdPersona() { return idPersona; }
    public void setIdPersona(int idPersona) { this.idPersona = idPersona; }

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
