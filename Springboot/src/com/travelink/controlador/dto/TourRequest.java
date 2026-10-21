package com.travelink.controlador.dto;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.math.BigDecimal;
import java.util.List;

public class TourRequest {

    private Integer idTour;
    private Integer idDestino;

    @NotBlank(message = "El nombre del tour es obligatorio")
    @Size(max = 200, message = "El nombre del tour no puede exceder 200 caracteres")
    private String nombre;

    @NotBlank(message = "La descripción es obligatoria")
    private String descripcion;

    private String descripcionCorta;
    private String itinerario;

    @NotNull(message = "El precio de adulto es obligatorio")
    @DecimalMin(value = "0.0", inclusive = false, message = "El precio de adulto debe ser mayor a 0")
    private BigDecimal precioAdulto;

    private BigDecimal precioNino = BigDecimal.ZERO;
    private BigDecimal precioBebe = BigDecimal.ZERO;
    private Boolean aceptaNinos = true;
    private Boolean aceptaBebes = false;
    private Integer edadMaxNino = 11;
    private Integer edadMaxBebe = 2;

    @NotBlank(message = "La ubicación / destino es obligatoria")
    private String ubicacion;

    private String categoria = "Cultura";
    private Integer dias = 1;
    private Integer horas = 6;
    private String duracion;
    private String estado = "PUBLICADO";
    private String queIncluye;
    private String queNoIncluye;
    private List<String> imagenes;

    public TourRequest() {}

    public Integer getIdTour() { return idTour; }
    public void setIdTour(Integer idTour) { this.idTour = idTour; }

    public Integer getIdDestino() { return idDestino; }
    public void setIdDestino(Integer idDestino) { this.idDestino = idDestino; }

    public String getNombre() { return nombre; }
    public void setNombre(String nombre) { this.nombre = nombre; }

    public String getDescripcion() { return descripcion; }
    public void setDescripcion(String descripcion) { this.descripcion = descripcion; }

    public String getDescripcionCorta() { return descripcionCorta; }
    public void setDescripcionCorta(String descripcionCorta) { this.descripcionCorta = descripcionCorta; }

    public String getItinerario() { return itinerario; }
    public void setItinerario(String itinerario) { this.itinerario = itinerario; }

    public BigDecimal getPrecioAdulto() { return precioAdulto; }
    public void setPrecioAdulto(BigDecimal precioAdulto) { this.precioAdulto = precioAdulto; }

    public BigDecimal getPrecioNino() { return precioNino; }
    public void setPrecioNino(BigDecimal precioNino) { this.precioNino = precioNino; }

    public BigDecimal getPrecioBebe() { return precioBebe; }
    public void setPrecioBebe(BigDecimal precioBebe) { this.precioBebe = precioBebe; }

    public Boolean getAceptaNinos() { return aceptaNinos; }
    public void setAceptaNinos(Boolean aceptaNinos) { this.aceptaNinos = aceptaNinos; }

    public Boolean getAceptaBebes() { return aceptaBebes; }
    public void setAceptaBebes(Boolean aceptaBebes) { this.aceptaBebes = aceptaBebes; }

    public Integer getEdadMaxNino() { return edadMaxNino; }
    public void setEdadMaxNino(Integer edadMaxNino) { this.edadMaxNino = edadMaxNino; }

    public Integer getEdadMaxBebe() { return edadMaxBebe; }
    public void setEdadMaxBebe(Integer edadMaxBebe) { this.edadMaxBebe = edadMaxBebe; }

    public String getUbicacion() { return ubicacion; }
    public void setUbicacion(String ubicacion) { this.ubicacion = ubicacion; }

    public String getCategoria() { return categoria; }
    public void setCategoria(String categoria) { this.categoria = categoria; }

    public Integer getDias() { return dias; }
    public void setDias(Integer dias) { this.dias = dias; }

    public Integer getHoras() { return horas; }
    public void setHoras(Integer horas) { this.horas = horas; }

    public String getDuracion() { return duracion; }
    public void setDuracion(String duracion) { this.duracion = duracion; }

    public String getEstado() { return estado; }
    public void setEstado(String estado) { this.estado = estado; }

    public String getQueIncluye() { return queIncluye; }
    public void setQueIncluye(String queIncluye) { this.queIncluye = queIncluye; }

    public String getQueNoIncluye() { return queNoIncluye; }
    public void setQueNoIncluye(String queNoIncluye) { this.queNoIncluye = queNoIncluye; }

    public List<String> getImagenes() { return imagenes; }
    public void setImagenes(List<String> imagenes) { this.imagenes = imagenes; }
}
