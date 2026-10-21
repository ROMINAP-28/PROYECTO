package com.travelink.controlador.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.travelink.entidades.Tour;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

@JsonInclude(JsonInclude.Include.NON_NULL)
public class TourDto {

    private Integer idTour;
    private Integer idAgencia;
    private String agenciaNombre;
    private Integer idDestino;
    private String slug;
    private String nombre;
    private String descripcion;
    private String descripcionCorta;
    private String itinerario;
    private Boolean aceptaNinos;
    private Integer edadMaxNino;
    private Integer edadMaxBebe;
    private BigDecimal descuento;
    private String condiciones;
    private Integer minAsientosOferta;
    private BigDecimal precioAdulto;
    private BigDecimal precioNino;
    private BigDecimal precioBebe;
    private String duracion;
    private String ubicacion;
    private String categoria;
    private Double calificacionPromedio;
    private String estado;
    private Boolean aceptaBebes;
    private String queIncluye;
    private String queNoIncluye;
    private Integer dias;
    private Integer horas;
    private String imagenPrincipal;
    private List<String> imagenes;
    private List<TourFechaDto> fechas;

    public TourDto() {}

    public static TourDto desdeEntidad(Tour t) {
        if (t == null) return null;
        TourDto dto = new TourDto();
        dto.setIdTour(t.getIdTour());
        dto.setIdAgencia(t.getIdAgencia());
        if (t.getAgencia() != null) {
            dto.setAgenciaNombre(t.getAgencia().getNombreComercial() != null 
                    ? t.getAgencia().getNombreComercial() 
                    : t.getAgencia().getRazonSocial());
        }
        dto.setIdDestino(t.getIdDestino());
        dto.setSlug(t.getSlug());
        dto.setNombre(t.getNombre());
        dto.setDescripcion(t.getDescripcion());
        dto.setDescripcionCorta(t.getDescripcionCorta());
        dto.setItinerario(t.getItinerario());
        dto.setAceptaNinos(t.isAceptaNinos());
        dto.setEdadMaxNino(t.getEdadMaxNino());
        dto.setEdadMaxBebe(t.getEdadMaxBebe());
        dto.setDescuento(t.getDescuento());
        dto.setCondiciones(t.getCondiciones());
        dto.setMinAsientosOferta(t.getMinAsientosOferta());
        dto.setPrecioAdulto(t.getPrecioAdulto());
        dto.setPrecioNino(t.getPrecioNino());
        dto.setPrecioBebe(t.getPrecioBebe());
        dto.setDuracion(t.getDuracion());
        dto.setUbicacion(t.getUbicacion());
        dto.setCategoria(t.getCategoria());
        dto.setCalificacionPromedio(t.getCalificacionPromedio());
        dto.setEstado(t.getEstado());
        dto.setAceptaBebes(t.isAceptaBebes());
        dto.setQueIncluye(t.getQueIncluye());
        dto.setQueNoIncluye(t.getQueNoIncluye());
        dto.setDias(t.getDias());
        dto.setHoras(t.getHoras());
        return dto;
    }

    public Integer getIdTour() { return idTour; }
    public void setIdTour(Integer idTour) { this.idTour = idTour; }

    public Integer getIdAgencia() { return idAgencia; }
    public void setIdAgencia(Integer idAgencia) { this.idAgencia = idAgencia; }

    public String getAgenciaNombre() { return agenciaNombre; }
    public void setAgenciaNombre(String agenciaNombre) { this.agenciaNombre = agenciaNombre; }

    public Integer getIdDestino() { return idDestino; }
    public void setIdDestino(Integer idDestino) { this.idDestino = idDestino; }

    public String getSlug() { return slug; }
    public void setSlug(String slug) { this.slug = slug; }

    public String getNombre() { return nombre; }
    public void setNombre(String nombre) { this.nombre = nombre; }

    public String getDescripcion() { return descripcion; }
    public void setDescripcion(String descripcion) { this.descripcion = descripcion; }

    public String getDescripcionCorta() { return descripcionCorta; }
    public void setDescripcionCorta(String descripcionCorta) { this.descripcionCorta = descripcionCorta; }

    public String getItinerario() { return itinerario; }
    public void setItinerario(String itinerario) { this.itinerario = itinerario; }

    public Boolean getAceptaNinos() { return aceptaNinos; }
    public void setAceptaNinos(Boolean aceptaNinos) { this.aceptaNinos = aceptaNinos; }

    public Integer getEdadMaxNino() { return edadMaxNino; }
    public void setEdadMaxNino(Integer edadMaxNino) { this.edadMaxNino = edadMaxNino; }

    public Integer getEdadMaxBebe() { return edadMaxBebe; }
    public void setEdadMaxBebe(Integer edadMaxBebe) { this.edadMaxBebe = edadMaxBebe; }

    public BigDecimal getDescuento() { return descuento; }
    public void setDescuento(BigDecimal descuento) { this.descuento = descuento; }

    public String getCondiciones() { return condiciones; }
    public void setCondiciones(String condiciones) { this.condiciones = condiciones; }

    public Integer getMinAsientosOferta() { return minAsientosOferta; }
    public void setMinAsientosOferta(Integer minAsientosOferta) { this.minAsientosOferta = minAsientosOferta; }

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

    public Double getCalificacionPromedio() { return calificacionPromedio; }
    public void setCalificacionPromedio(Double calificacionPromedio) { this.calificacionPromedio = calificacionPromedio; }

    public String getEstado() { return estado; }
    public void setEstado(String estado) { this.estado = estado; }

    public Boolean getAceptaBebes() { return aceptaBebes; }
    public void setAceptaBebes(Boolean aceptaBebes) { this.aceptaBebes = aceptaBebes; }

    public String getQueIncluye() { return queIncluye; }
    public void setQueIncluye(String queIncluye) { this.queIncluye = queIncluye; }

    public String getQueNoIncluye() { return queNoIncluye; }
    public void setQueNoIncluye(String queNoIncluye) { this.queNoIncluye = queNoIncluye; }

    public Integer getDias() { return dias; }
    public void setDias(Integer dias) { this.dias = dias; }

    public Integer getHoras() { return horas; }
    public void setHoras(Integer horas) { this.horas = horas; }

    public String getImagenPrincipal() { return imagenPrincipal; }
    public void setImagenPrincipal(String imagenPrincipal) { this.imagenPrincipal = imagenPrincipal; }

    public List<String> getImagenes() { return imagenes; }
    public void setImagenes(List<String> imagenes) { this.imagenes = imagenes; }

    public List<TourFechaDto> getFechas() { return fechas; }
    public void setFechas(List<TourFechaDto> fechas) { this.fechas = fechas; }
}
