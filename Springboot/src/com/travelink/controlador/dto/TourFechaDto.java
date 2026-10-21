package com.travelink.controlador.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.travelink.entidades.TourFecha;

import java.sql.Time;
import java.util.Date;

@JsonInclude(JsonInclude.Include.NON_NULL)
public class TourFechaDto {

    private Integer idTourFecha;
    private Integer idTour;
    private Date fecha;
    private Time horaInicio;
    private Integer cupoTotal;
    private Integer cupoDisponible;
    private String estado;

    public TourFechaDto() {}

    public static TourFechaDto desdeEntidad(TourFecha tf) {
        if (tf == null) return null;
        TourFechaDto dto = new TourFechaDto();
        dto.setIdTourFecha(tf.getIdTourFecha());
        dto.setIdTour(tf.getIdTour());
        dto.setFecha(tf.getFecha());
        dto.setHoraInicio(tf.getHoraInicio());
        dto.setCupoTotal(tf.getCupoTotal());
        dto.setCupoDisponible(tf.getCupoDisponible());
        dto.setEstado(tf.getEstado());
        return dto;
    }

    public Integer getIdTourFecha() { return idTourFecha; }
    public void setIdTourFecha(Integer idTourFecha) { this.idTourFecha = idTourFecha; }

    public Integer getIdTour() { return idTour; }
    public void setIdTour(Integer idTour) { this.idTour = idTour; }

    public Date getFecha() { return fecha; }
    public void setFecha(Date fecha) { this.fecha = fecha; }

    public Time getHoraInicio() { return horaInicio; }
    public void setHoraInicio(Time horaInicio) { this.horaInicio = horaInicio; }

    public Integer getCupoTotal() { return cupoTotal; }
    public void setCupoTotal(Integer cupoTotal) { this.cupoTotal = cupoTotal; }

    public Integer getCupoDisponible() { return cupoDisponible; }
    public void setCupoDisponible(Integer cupoDisponible) { this.cupoDisponible = cupoDisponible; }

    public String getEstado() { return estado; }
    public void setEstado(String estado) { this.estado = estado; }
}
