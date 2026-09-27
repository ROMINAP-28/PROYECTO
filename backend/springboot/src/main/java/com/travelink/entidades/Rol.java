package com.travelink.entidades;

public class Rol {
    private int idRol;
    private String nombreRol; // Turista / Agencia / Administrador

    public Rol() {}

    public Rol(int idRol, String nombreRol) {
        this.idRol = idRol;
        this.nombreRol = nombreRol;
    }

    public int getIdRol() { return idRol; }
    public void setIdRol(int idRol) { this.idRol = idRol; }

    public String getNombreRol() { return nombreRol; }
    public void setNombreRol(String nombreRol) { this.nombreRol = nombreRol; }

    // Compatibilidad
    public String getNombre() { return nombreRol; }
    public void setNombre(String nombre) { this.nombreRol = nombre; }
}
