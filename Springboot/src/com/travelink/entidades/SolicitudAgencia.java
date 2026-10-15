package com.travelink.entidades;

import jakarta.persistence.*;
import java.util.Date;

@Entity
@Table(name = "SolicitudAgencia")
public class SolicitudAgencia {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer idSolicitud;

    @Column(name = "idUsuario", insertable = false, updatable = false)
    private Integer idUsuario;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "idUsuario", nullable = false)
    private Usuario usuario;

    @Column(name = "razonSocial", nullable = false, length = 150)
    private String razonSocial;

    @Column(name = "ruc", nullable = false, length = 20)
    private String ruc;

    @Column(name = "representanteLegal", nullable = false, length = 150)
    private String representanteLegal;

    @Column(name = "telefonoContacto", length = 15)
    private String telefonoContacto;

    @Column(name = "correoContacto", length = 100)
    private String correoContacto;

    @Column(name = "documentoRuc", length = 255)
    private String documentoRuc;

    @Column(name = "estado", length = 20)
    private String estado = "Pendiente";

    @Temporal(TemporalType.TIMESTAMP)
    @Column(name = "fechaSolicitud")
    private Date fechaSolicitud = new Date();

    public SolicitudAgencia() {}

    public Integer getIdSolicitud() { return idSolicitud; }
    public void setIdSolicitud(Integer idSolicitud) { this.idSolicitud = idSolicitud; }

    public Integer getIdUsuario() { return idUsuario != null ? idUsuario : (usuario != null ? usuario.getIdUsuario() : null); }
    public void setIdUsuario(Integer idUsuario) { this.idUsuario = idUsuario; }

    public Usuario getUsuario() { return usuario; }
    public void setUsuario(Usuario usuario) {
        this.usuario = usuario;
        if (usuario != null) this.idUsuario = usuario.getIdUsuario();
    }

    public String getRazonSocial() { return razonSocial; }
    public void setRazonSocial(String razonSocial) { this.razonSocial = razonSocial; }

    public String getRuc() { return ruc; }
    public void setRuc(String ruc) { this.ruc = ruc; }

    public String getRepresentanteLegal() { return representanteLegal; }
    public void setRepresentanteLegal(String representanteLegal) { this.representanteLegal = representanteLegal; }

    public String getTelefonoContacto() { return telefonoContacto; }
    public void setTelefonoContacto(String telefonoContacto) { this.telefonoContacto = telefonoContacto; }

    public String getCorreoContacto() { return correoContacto; }
    public void setCorreoContacto(String correoContacto) { this.correoContacto = correoContacto; }

    public String getDocumentoRuc() { return documentoRuc; }
    public void setDocumentoRuc(String documentoRuc) { this.documentoRuc = documentoRuc; }

    public String getEstado() { return estado; }
    public void setEstado(String estado) { this.estado = estado; }

    public Date getFechaSolicitud() { return fechaSolicitud; }
    public void setFechaSolicitud(Date fechaSolicitud) { this.fechaSolicitud = fechaSolicitud; }
}
