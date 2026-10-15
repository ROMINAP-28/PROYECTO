package com.travelink.entidades;

import jakarta.persistence.*;
import java.util.Date;

@Entity
@Table(name = "HistorialAgencia")
public class HistorialAgencia {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer idHistorial;

    @Column(name = "idAgencia", insertable = false, updatable = false)
    private Integer idAgencia;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "idAgencia", nullable = false)
    private Agencia agencia;

    @Column(name = "idAdministrador")
    private Integer idAdministrador;

    @Column(name = "estadoAnterior", length = 20)
    private String estadoAnterior;

    @Column(name = "estadoNuevo", length = 20)
    private String estadoNuevo;

    @Column(name = "motivo", length = 255)
    private String motivo;

    @Temporal(TemporalType.TIMESTAMP)
    @Column(name = "fecha")
    private Date fecha = new Date();

    public HistorialAgencia() {}

    public Integer getIdHistorial() { return idHistorial; }
    public void setIdHistorial(Integer idHistorial) { this.idHistorial = idHistorial; }

    public Integer getIdAgencia() { return idAgencia != null ? idAgencia : (agencia != null ? agencia.getIdAgencia() : null); }
    public void setIdAgencia(Integer idAgencia) { this.idAgencia = idAgencia; }

    public Agencia getAgencia() { return agencia; }
    public void setAgencia(Agencia agencia) {
        this.agencia = agencia;
        if (agencia != null) this.idAgencia = agencia.getIdAgencia();
    }

    public Integer getIdAdministrador() { return idAdministrador; }
    public void setIdAdministrador(Integer idAdministrador) { this.idAdministrador = idAdministrador; }

    public String getEstadoAnterior() { return estadoAnterior; }
    public void setEstadoAnterior(String estadoAnterior) { this.estadoAnterior = estadoAnterior; }

    public String getEstadoNuevo() { return estadoNuevo; }
    public void setEstadoNuevo(String estadoNuevo) { this.estadoNuevo = estadoNuevo; }

    public String getMotivo() { return motivo; }
    public void setMotivo(String motivo) { this.motivo = motivo; }

    public Date getFecha() { return fecha; }
    public void setFecha(Date fecha) { this.fecha = fecha; }
}
