package com.travelink.entidades;

import java.math.BigDecimal;
import java.util.List;

public class Paquete {
    private int idPaquete;
    private int idAgencia;
    private String nombre;
    private String descripcion;
    private BigDecimal descuento;
    private String imagenUrl;
    private String estado;

    // Relaciones adicionales para facilidad en la UI
    private List<Tour> toursIncluidos;

    public Paquete() {}

    public int getIdPaquete() { return idPaquete; }
    public void setIdPaquete(int idPaquete) { this.idPaquete = idPaquete; }

    public int getIdAgencia() { return idAgencia; }
    public void setIdAgencia(int idAgencia) { this.idAgencia = idAgencia; }

    public String getNombre() { return nombre; }
    public void setNombre(String nombre) { this.nombre = nombre; }

    public String getDescripcion() { return descripcion; }
    public void setDescripcion(String descripcion) { this.descripcion = descripcion; }

    public BigDecimal getDescuento() { return descuento; }
    public void setDescuento(BigDecimal descuento) { this.descuento = descuento; }

    public String getImagenUrl() { return imagenUrl; }
    public void setImagenUrl(String imagenUrl) { this.imagenUrl = imagenUrl; }

    public String getEstado() { return estado; }
    public void setEstado(String estado) { this.estado = estado; }

    public List<Tour> getToursIncluidos() { return toursIncluidos; }
    public void setToursIncluidos(List<Tour> toursIncluidos) { this.toursIncluidos = toursIncluidos; }
}
