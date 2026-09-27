package com.travelink.entidades;

import java.math.BigDecimal;

public class Tour {
    private int idTour;
    private int idAgencia; // FK → Agencia
    private String slug;
    private String nombre;
    private String descripcion;
    private BigDecimal precioAdulto;
    private BigDecimal precioNino;
    private BigDecimal precioBebe;
    private String duracion;
    private String ubicacion;
    private String categoria;
    private double calificacionPromedio;
    private String estado;

    // Relación
    private Agencia agencia;

    public Tour() {}

    public int getIdTour() { return idTour; }
    public void setIdTour(int idTour) { this.idTour = idTour; }

    public int getIdAgencia() { return idAgencia; }
    public void setIdAgencia(int idAgencia) { this.idAgencia = idAgencia; }

    public String getSlug() { return slug; }
    public void setSlug(String slug) { this.slug = slug; }

    public String getNombre() { return nombre; }
    public void setNombre(String nombre) { this.nombre = nombre; }

    public String getDescripcion() { return descripcion; }
    public void setDescripcion(String descripcion) { this.descripcion = descripcion; }

    public BigDecimal getPrecioAdulto() { return precioAdulto; }
    public void setPrecioAdulto(BigDecimal precioAdulto) { this.precioAdulto = precioAdulto; }

    public BigDecimal getPrecioNino() { return precioNino; }
    public void setPrecioNino(BigDecimal precioNino) { this.precioNino = precioNino; }

    public BigDecimal getPrecioBebe() { return precioBebe; }
    public void setPrecioBebe(BigDecimal precioBebe) { this.precioBebe = precioBebe; }

    public String getDuracion() { return duracion; }
    public void setDuracion(String duracion) { this.duracion = duracion; }

    public String getUbicacion() { return ubicacion; }
    public void setUbicacion(String ubicacion) { this.ubicacion = ubicacion; }

    public String getCategoria() { return categoria; }
    public void setCategoria(String categoria) { this.categoria = categoria; }

    public double getCalificacionPromedio() { return calificacionPromedio; }
    public void setCalificacionPromedio(double calificacionPromedio) { this.calificacionPromedio = calificacionPromedio; }

    public String getEstado() { return estado; }
    public void setEstado(String estado) { this.estado = estado; }

    public Agencia getAgencia() { return agencia; }
    public void setAgencia(Agencia agencia) { 
        this.agencia = agencia; 
        if (agencia != null) this.idAgencia = agencia.getIdAgencia();
    }
}
