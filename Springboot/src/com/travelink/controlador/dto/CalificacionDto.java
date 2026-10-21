package com.travelink.controlador.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.travelink.entidades.Calificacion;

import java.util.Date;

@JsonInclude(JsonInclude.Include.NON_NULL)
public class CalificacionDto {

    private Integer idCalificacion;
    private Integer idUsuario;
    private String usuarioNombre;
    private Integer idAgencia;
    private String agenciaNombre;
    private Integer idReserva;
    private String tourNombre;
    private Integer estrellas;
    private String comentario;
    private Date fechaCalificacion;

    public CalificacionDto() {}

    public static CalificacionDto desdeEntidad(Calificacion c) {
        if (c == null) return null;
        CalificacionDto dto = new CalificacionDto();
        dto.setIdCalificacion(c.getIdCalificacion());
        dto.setIdUsuario(c.getIdUsuario());
        dto.setIdAgencia(c.getIdAgencia());
        dto.setIdReserva(c.getIdReserva());
        dto.setEstrellas(c.getEstrellas());
        dto.setComentario(c.getComentario());
        dto.setFechaCalificacion(c.getFechaCalificacion());

        if (c.getUsuario() != null) {
            dto.setUsuarioNombre(c.getUsuario().getPersona() != null 
                    ? c.getUsuario().getPersona().getNombreCompleto() 
                    : c.getUsuario().getNombreUsuario());
        }
        if (c.getAgencia() != null) {
            dto.setAgenciaNombre(c.getAgencia().getNombreComercial() != null 
                    ? c.getAgencia().getNombreComercial() 
                    : c.getAgencia().getRazonSocial());
        }
        return dto;
    }

    public Integer getIdCalificacion() { return idCalificacion; }
    public void setIdCalificacion(Integer idCalificacion) { this.idCalificacion = idCalificacion; }

    public Integer getIdUsuario() { return idUsuario; }
    public void setIdUsuario(Integer idUsuario) { this.idUsuario = idUsuario; }

    public String getUsuarioNombre() { return usuarioNombre; }
    public void setUsuarioNombre(String usuarioNombre) { this.usuarioNombre = usuarioNombre; }

    public Integer getIdAgencia() { return idAgencia; }
    public void setIdAgencia(Integer idAgencia) { this.idAgencia = idAgencia; }

    public String getAgenciaNombre() { return agenciaNombre; }
    public void setAgenciaNombre(String agenciaNombre) { this.agenciaNombre = agenciaNombre; }

    public Integer getIdReserva() { return idReserva; }
    public void setIdReserva(Integer idReserva) { this.idReserva = idReserva; }

    public String getTourNombre() { return tourNombre; }
    public void setTourNombre(String tourNombre) { this.tourNombre = tourNombre; }

    public Integer getEstrellas() { return estrellas; }
    public void setEstrellas(Integer estrellas) { this.estrellas = estrellas; }

    public String getComentario() { return comentario; }
    public void setComentario(String comentario) { this.comentario = comentario; }

    public Date getFechaCalificacion() { return fechaCalificacion; }
    public void setFechaCalificacion(Date fechaCalificacion) { this.fechaCalificacion = fechaCalificacion; }
}
