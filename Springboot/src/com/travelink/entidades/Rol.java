package com.travelink.entidades;

import jakarta.persistence.*;

@Entity
@Table(name = "Rol")
public class Rol {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer idRol;

    @Column(name = "nombreRol", nullable = false, unique = true, length = 20)
    private String nombreRol; // TURISTA / AGENCIA / ADMINISTRADOR

    public Rol() {}

    public Rol(Integer idRol, String nombreRol) {
        this.idRol = idRol;
        this.nombreRol = nombreRol;
    }

    public Integer getIdRol() { return idRol; }
    public void setIdRol(Integer idRol) { this.idRol = idRol; }

    public String getNombreRol() { return nombreRol; }
    public void setNombreRol(String nombreRol) { this.nombreRol = nombreRol; }

    public String getNombre() { return nombreRol; }
    public void setNombre(String nombre) { this.nombreRol = nombre; }
}
