package com.travelink.entidades;

import jakarta.persistence.*;
import java.math.BigDecimal;
import java.util.Date;

@Entity
@Table(name = "Agencia")
public class Agencia {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer idAgencia;

    @Column(name = "idUsuario", insertable = false, updatable = false)
    private Integer idUsuario;

    @OneToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "idUsuario", nullable = false, unique = true)
    private Usuario usuario;

    @Column(name = "razonSocial", nullable = false, length = 150)
    private String razonSocial;

    @Column(name = "nombreComercial", length = 150)
    private String nombreComercial;

    @Column(name = "ruc", nullable = false, unique = true, length = 20)
    private String ruc;

    @Column(name = "telefono", length = 15)
    private String telefono;

    @Column(name = "email", length = 100)
    private String email;

    @Column(name = "direccion", length = 255)
    private String direccion;

    @Column(name = "descripcion", columnDefinition = "TEXT")
    private String descripcion;

    @Column(name = "logoUrl", length = 255)
    private String logoUrl;

    @Column(name = "estado", length = 20)
    private String estado = "PENDIENTE";

    @Column(name = "porcentajeComision", precision = 5, scale = 2)
    private BigDecimal porcentajeComision = new BigDecimal("15.00");

    @Column(name = "promedioCalificacion", precision = 3, scale = 2)
    private BigDecimal promedioCalificacion = BigDecimal.ZERO;

    @Temporal(TemporalType.TIMESTAMP)
    @Column(name = "fechaRegistro")
    private Date fechaRegistro = new Date();

    @Column(name = "comision", precision = 10, scale = 2)
    private BigDecimal comision = BigDecimal.ZERO;

    public Agencia() {}

    public Integer getIdAgencia() { return idAgencia; }
    public void setIdAgencia(Integer idAgencia) { this.idAgencia = idAgencia; }

    public Integer getIdUsuario() { return idUsuario != null ? idUsuario : (usuario != null ? usuario.getIdUsuario() : null); }
    public void setIdUsuario(Integer idUsuario) { this.idUsuario = idUsuario; }

    public Usuario getUsuario() { return usuario; }
    public void setUsuario(Usuario usuario) {
        this.usuario = usuario;
        if (usuario != null) this.idUsuario = usuario.getIdUsuario();
    }

    public String getRazonSocial() { return razonSocial; }
    public void setRazonSocial(String razonSocial) { this.razonSocial = razonSocial; }

    public String getNombreComercial() { return nombreComercial; }
    public void setNombreComercial(String nombreComercial) { this.nombreComercial = nombreComercial; }

    public String getRuc() { return ruc; }
    public void setRuc(String ruc) { this.ruc = ruc; }

    public String getTelefono() { return telefono; }
    public void setTelefono(String telefono) { this.telefono = telefono; }

    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }

    public String getCorreo() { return email; }
    public void setCorreo(String correo) { this.email = correo; }

    public String getDireccion() { return direccion; }
    public void setDireccion(String direccion) { this.direccion = direccion; }

    public String getDescripcion() { return descripcion; }
    public void setDescripcion(String descripcion) { this.descripcion = descripcion; }

    public String getLogoUrl() { return logoUrl; }
    public void setLogoUrl(String logoUrl) { this.logoUrl = logoUrl; }

    public String getEstado() { return estado; }
    public void setEstado(String estado) { this.estado = estado; }

    public BigDecimal getPorcentajeComision() { return porcentajeComision; }
    public void setPorcentajeComision(BigDecimal porcentajeComision) { this.porcentajeComision = porcentajeComision; }

    public BigDecimal getPromedioCalificacion() { return promedioCalificacion; }
    public void setPromedioCalificacion(BigDecimal promedioCalificacion) { this.promedioCalificacion = promedioCalificacion; }

    public Date getFechaRegistro() { return fechaRegistro; }
    public void setFechaRegistro(Date fechaRegistro) { this.fechaRegistro = fechaRegistro; }

    public BigDecimal getComision() { return comision; }
    public void setComision(BigDecimal comision) { this.comision = comision; }
}
