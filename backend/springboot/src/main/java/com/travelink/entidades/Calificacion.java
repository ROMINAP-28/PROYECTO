package com.travelink.entidades;

import java.util.Date;

public class Calificacion {
    private int idCalificacion;
    private int idUsuario; // FK → Usuario
    private int idAgencia; // FK → Agencia
    private int idReserva; // FK → Reserva (trazabilidad)
    private int estrellas;  // 1-5
    private String comentario; // VARCHAR(500)
    private Date fechaCalificacion;

    // Relaciones
    private Usuario usuario;
    private Agencia agencia;
    private Reserva reserva;

    public Calificacion() {
        this.estrellas = 5;
        this.fechaCalificacion = new Date();
    }

    public Calificacion(int idCalificacion, int idUsuario, int idAgencia, int idReserva, int estrellas, String comentario) {
        this.idCalificacion = idCalificacion;
        this.idUsuario = idUsuario;
        this.idAgencia = idAgencia;
        this.idReserva = idReserva;
        this.estrellas = estrellas;
        this.comentario = comentario;
        this.fechaCalificacion = new Date();
    }

    public int getIdCalificacion() { return idCalificacion; }
    public void setIdCalificacion(int idCalificacion) { this.idCalificacion = idCalificacion; }

    public int getIdUsuario() { return idUsuario; }
    public void setIdUsuario(int idUsuario) { this.idUsuario = idUsuario; }

    public int getIdAgencia() { return idAgencia; }
    public void setIdAgencia(int idAgencia) { this.idAgencia = idAgencia; }

    public int getIdReserva() { return idReserva; }
    public void setIdReserva(int idReserva) { this.idReserva = idReserva; }

    public int getEstrellas() { return estrellas; }
    public void setEstrellas(int estrellas) { 
        if (estrellas < 1) this.estrellas = 1;
        else if (estrellas > 5) this.estrellas = 5;
        else this.estrellas = estrellas;
    }

    public String getComentario() { return comentario; }
    public void setComentario(String comentario) { this.comentario = comentario; }

    public Date getFechaCalificacion() { return fechaCalificacion; }
    public void setFechaCalificacion(Date fechaCalificacion) { this.fechaCalificacion = fechaCalificacion; }

    public Usuario getUsuario() { return usuario; }
    public void setUsuario(Usuario usuario) { 
        this.usuario = usuario; 
        if (usuario != null) this.idUsuario = usuario.getIdUsuario();
    }

    public Agencia getAgencia() { return agencia; }
    public void setAgencia(Agencia agencia) { 
        this.agencia = agencia; 
        if (agencia != null) this.idAgencia = agencia.getIdAgencia();
    }

    public Reserva getReserva() { return reserva; }
    public void setReserva(Reserva reserva) { 
        this.reserva = reserva; 
        if (reserva != null) this.idReserva = reserva.getIdReserva();
    }
}
