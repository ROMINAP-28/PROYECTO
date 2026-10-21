package com.travelink.controlador.dto;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public class CalificacionRequest {

    @NotNull(message = "El id de reserva es obligatorio")
    private Integer idReserva;

    private Integer idTour;
    private Integer idAgencia;

    @NotNull(message = "La puntuación en estrellas es obligatoria")
    @Min(value = 1, message = "La calificación mínima es 1 estrella")
    @Max(value = 5, message = "La calificación máxima es 5 estrellas")
    private Integer estrellas;

    @NotBlank(message = "El comentario es obligatorio")
    @Size(max = 1000, message = "El comentario no puede exceder 1000 caracteres")
    private String comentario;

    public CalificacionRequest() {}

    public Integer getIdReserva() { return idReserva; }
    public void setIdReserva(Integer idReserva) { this.idReserva = idReserva; }

    public Integer getIdTour() { return idTour; }
    public void setIdTour(Integer idTour) { this.idTour = idTour; }

    public Integer getIdAgencia() { return idAgencia; }
    public void setIdAgencia(Integer idAgencia) { this.idAgencia = idAgencia; }

    public Integer getEstrellas() { return estrellas; }
    public void setEstrellas(Integer estrellas) { this.estrellas = estrellas; }

    public String getComentario() { return comentario; }
    public void setComentario(String comentario) { this.comentario = comentario; }
}
