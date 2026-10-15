package com.travelink.entidades;

import jakarta.persistence.*;
import java.util.Date;

@Entity
@Table(name = "Notificacion")
public class Notificacion {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer idNotificacion;

    @Column(name = "idUsuario")
    private Integer idUsuario;

    @Column(name = "rolTarget", nullable = false, length = 20)
    private String rolTarget = "AGENCIA";

    @Column(name = "titulo", nullable = false, length = 150)
    private String titulo;

    @Column(name = "mensaje", columnDefinition = "TEXT", nullable = false)
    private String mensaje;

    @Column(name = "leido")
    private Boolean leido = false;

    @Temporal(TemporalType.TIMESTAMP)
    @Column(name = "fechaRegistro")
    private Date fechaRegistro = new Date();

    public Notificacion() {
        this.leido = false;
        this.fechaRegistro = new Date();
    }

    public Notificacion(Integer idNotificacion, Integer idUsuario, String rolTarget, String titulo, String mensaje) {
        this.idNotificacion = idNotificacion;
        this.idUsuario = idUsuario;
        this.rolTarget = rolTarget != null ? rolTarget : "AGENCIA";
        this.titulo = titulo;
        this.mensaje = mensaje;
        this.leido = false;
        this.fechaRegistro = new Date();
    }

    public Integer getIdNotificacion() { return idNotificacion; }
    public void setIdNotificacion(Integer idNotificacion) { this.idNotificacion = idNotificacion; }

    public Integer getIdUsuario() { return idUsuario; }
    public void setIdUsuario(Integer idUsuario) { this.idUsuario = idUsuario; }

    public String getRolTarget() { return rolTarget; }
    public void setRolTarget(String rolTarget) { this.rolTarget = rolTarget; }

    public String getTitulo() { return titulo; }
    public void setTitulo(String titulo) { this.titulo = titulo; }

    public String getMensaje() { return mensaje; }
    public void setMensaje(String mensaje) { this.mensaje = mensaje; }

    public Boolean getLeido() { return leido != null ? leido : false; }
    public void setLeido(Boolean leido) { this.leido = leido; }
    public boolean isLeido() { return getLeido(); }

    public Date getFechaRegistro() { return fechaRegistro; }
    public void setFechaRegistro(Date fechaRegistro) { this.fechaRegistro = fechaRegistro; }

    // Compatibilidad
    public boolean isLeida() { return getLeido(); }
    public void setLeida(boolean leida) { setLeido(leida); }
    public Date getFechaEnvio() { return fechaRegistro; }
    public void setFechaEnvio(Date fechaEnvio) { this.fechaRegistro = fechaEnvio; }
    public String getTipo() { return rolTarget; }
    public void setTipo(String tipo) { this.rolTarget = tipo; }
    public String getEstado() { return getLeido() ? "LEIDO" : "PENDIENTE"; }
    public void setEstado(String estado) { this.leido = "LEIDO".equalsIgnoreCase(estado); }
}
