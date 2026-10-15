package com.travelink.entidades;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import java.util.Date;

@Entity
@Table(name = "Calificacion")
public class Calificacion {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer idCalificacion;

    @Column(name = "idUsuario", insertable = false, updatable = false)
    private Integer idUsuario;

    @Column(name = "idAgencia", insertable = false, updatable = false)
    private Integer idAgencia;

    @Column(name = "idReserva", insertable = false, updatable = false)
    private Integer idReserva;

    @Column(name = "estrellas", nullable = false)
    private Integer estrellas = 5;

    @Column(name = "comentario", length = 500)
    private String comentario;

    @Temporal(TemporalType.TIMESTAMP)
    @Column(name = "fechaCalificacion")
    private Date fechaCalificacion = new Date();

    @Column(name = "eliminadoPorAdmin")
    private Boolean eliminadoPorAdmin = false;

    @Column(name = "motivoEliminacion", length = 255)
    private String motivoEliminacion;

    @Column(name = "flageada")
    private Boolean flageada = false;

    @Column(name = "motivoFlag", length = 255)
    private String motivoFlag;

    @Temporal(TemporalType.TIMESTAMP)
    @Column(name = "fechaFlag")
    private Date fechaFlag;

    @Column(name = "estadoRevision", length = 20)
    private String estadoRevision = "APROBADO";

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "idUsuario", nullable = false)
    private Usuario usuario;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "idAgencia", nullable = false)
    private Agencia agencia;

    @JsonIgnore
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "idReserva", nullable = false)
    private Reserva reserva;

    public Calificacion() {
        this.estrellas = 5;
        this.fechaCalificacion = new Date();
        this.eliminadoPorAdmin = false;
        this.flageada = false;
        this.estadoRevision = "APROBADO";
    }

    public Calificacion(Integer idCalificacion, Integer idUsuario, Integer idAgencia, Integer idReserva, Integer estrellas, String comentario) {
        this.idCalificacion = idCalificacion;
        this.idUsuario = idUsuario;
        this.idAgencia = idAgencia;
        this.idReserva = idReserva;
        this.estrellas = estrellas;
        this.comentario = comentario;
        this.fechaCalificacion = new Date();
        this.eliminadoPorAdmin = false;
        this.flageada = false;
        this.estadoRevision = "APROBADO";
    }

    public Integer getIdCalificacion() { return idCalificacion; }
    public void setIdCalificacion(Integer idCalificacion) { this.idCalificacion = idCalificacion; }

    public Integer getIdUsuario() {
        return idUsuario != null ? idUsuario : (usuario != null ? usuario.getIdUsuario() : null);
    }
    public void setIdUsuario(Integer idUsuario) { this.idUsuario = idUsuario; }

    public Integer getIdAgencia() {
        return idAgencia != null ? idAgencia : (agencia != null ? agencia.getIdAgencia() : null);
    }
    public void setIdAgencia(Integer idAgencia) { this.idAgencia = idAgencia; }

    public Integer getIdReserva() {
        return idReserva != null ? idReserva : (reserva != null ? reserva.getIdReserva() : null);
    }
    public void setIdReserva(Integer idReserva) { this.idReserva = idReserva; }

    public Integer getEstrellas() { return estrellas; }
    public void setEstrellas(Integer estrellas) {
        if (estrellas == null) this.estrellas = 5;
        else if (estrellas < 1) this.estrellas = 1;
        else if (estrellas > 5) this.estrellas = 5;
        else this.estrellas = estrellas;
    }

    public String getComentario() { return comentario; }
    public void setComentario(String comentario) { this.comentario = comentario; }

    public Date getFechaCalificacion() { return fechaCalificacion; }
    public void setFechaCalificacion(Date fechaCalificacion) { this.fechaCalificacion = fechaCalificacion; }

    public Boolean getEliminadoPorAdmin() { return eliminadoPorAdmin != null ? eliminadoPorAdmin : false; }
    public void setEliminadoPorAdmin(Boolean eliminadoPorAdmin) { this.eliminadoPorAdmin = eliminadoPorAdmin; }

    public String getMotivoEliminacion() { return motivoEliminacion; }
    public void setMotivoEliminacion(String motivoEliminacion) { this.motivoEliminacion = motivoEliminacion; }

    public Boolean getFlageada() { return flageada != null ? flageada : false; }
    public void setFlageada(Boolean flageada) { this.flageada = flageada; }

    public String getMotivoFlag() { return motivoFlag; }
    public void setMotivoFlag(String motivoFlag) { this.motivoFlag = motivoFlag; }

    public Date getFechaFlag() { return fechaFlag; }
    public void setFechaFlag(Date fechaFlag) { this.fechaFlag = fechaFlag; }

    public String getEstadoRevision() { return estadoRevision; }
    public void setEstadoRevision(String estadoRevision) { this.estadoRevision = estadoRevision; }

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
