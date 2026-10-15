package com.travelink.entidades;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import java.math.BigDecimal;
import java.util.Date;

@Entity
@Table(name = "Comision")
public class Comision {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer idComision;

    @Column(name = "idAgencia", insertable = false, updatable = false)
    private Integer idAgencia;

    @Column(name = "idReserva")
    private Integer idReserva;

    @Column(name = "periodo", nullable = false, length = 10)
    private String periodo;

    @Column(name = "subtotal", precision = 10, scale = 2)
    private BigDecimal subtotal = BigDecimal.ZERO;

    @Column(name = "porcentaje", precision = 5, scale = 2)
    private BigDecimal porcentaje = new BigDecimal("15.00");

    @Column(name = "porcentajeComision", precision = 5, scale = 2)
    private BigDecimal porcentajeComision = new BigDecimal("15.00");

    @Column(name = "montoComision", precision = 10, scale = 2, nullable = false)
    private BigDecimal montoComision = BigDecimal.ZERO;

    @Column(name = "montoNeto", precision = 10, scale = 2, nullable = false)
    private BigDecimal montoNeto = BigDecimal.ZERO;

    @Column(name = "montoAgencia", precision = 10, scale = 2)
    private BigDecimal montoAgencia = BigDecimal.ZERO;

    @Column(name = "montoTravelink", precision = 10, scale = 2)
    private BigDecimal montoTravelink = BigDecimal.ZERO;

    @Column(name = "ventasTotales", precision = 10, scale = 2)
    private BigDecimal ventasTotales = BigDecimal.ZERO;

    @Column(name = "estado", length = 20)
    private String estado = "PENDIENTE";

    @Column(name = "estadoLiquidacion", length = 20)
    private String estadoLiquidacion = "PENDIENTE";

    @Temporal(TemporalType.TIMESTAMP)
    @Column(name = "fechaCalculo")
    private Date fechaCalculo = new Date();

    @Temporal(TemporalType.TIMESTAMP)
    @Column(name = "fechaLiquidacion")
    private Date fechaLiquidacion;

    @JsonIgnore
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "idAgencia", nullable = false)
    private Agencia agencia;

    public Comision() {}

    public Integer getIdComision() { return idComision; }
    public void setIdComision(Integer idComision) { this.idComision = idComision; }

    public Integer getIdAgencia() {
        return idAgencia != null ? idAgencia : (agencia != null ? agencia.getIdAgencia() : null);
    }
    public void setIdAgencia(Integer idAgencia) { this.idAgencia = idAgencia; }

    public Integer getIdReserva() { return idReserva; }
    public void setIdReserva(Integer idReserva) { this.idReserva = idReserva; }

    public String getPeriodo() { return periodo; }
    public void setPeriodo(String periodo) { this.periodo = periodo; }

    public BigDecimal getSubtotal() { return subtotal; }
    public void setSubtotal(BigDecimal subtotal) { this.subtotal = subtotal; }

    public BigDecimal getPorcentaje() { return porcentaje; }
    public void setPorcentaje(BigDecimal porcentaje) { this.porcentaje = porcentaje; }

    public BigDecimal getPorcentajeComision() { return porcentajeComision; }
    public void setPorcentajeComision(BigDecimal porcentajeComision) { this.porcentajeComision = porcentajeComision; }

    public BigDecimal getMontoComision() { return montoComision; }
    public void setMontoComision(BigDecimal montoComision) { this.montoComision = montoComision; }

    public BigDecimal getMontoNeto() { return montoNeto; }
    public void setMontoNeto(BigDecimal montoNeto) { this.montoNeto = montoNeto; }

    public BigDecimal getMontoAgencia() { return montoAgencia; }
    public void setMontoAgencia(BigDecimal montoAgencia) { this.montoAgencia = montoAgencia; }

    public BigDecimal getMontoTravelink() { return montoTravelink; }
    public void setMontoTravelink(BigDecimal montoTravelink) { this.montoTravelink = montoTravelink; }

    public BigDecimal getVentasTotales() { return ventasTotales; }
    public void setVentasTotales(BigDecimal ventasTotales) { this.ventasTotales = ventasTotales; }

    public String getEstado() { return estado; }
    public void setEstado(String estado) { this.estado = estado; }

    public String getEstadoLiquidacion() { return estadoLiquidacion; }
    public void setEstadoLiquidacion(String estadoLiquidacion) { this.estadoLiquidacion = estadoLiquidacion; }

    public Date getFechaCalculo() { return fechaCalculo; }
    public void setFechaCalculo(Date fechaCalculo) { this.fechaCalculo = fechaCalculo; }

    public Date getFechaLiquidacion() { return fechaLiquidacion; }
    public void setFechaLiquidacion(Date fechaLiquidacion) { this.fechaLiquidacion = fechaLiquidacion; }

    public Agencia getAgencia() { return agencia; }
    public void setAgencia(Agencia agencia) {
        this.agencia = agencia;
        if (agencia != null) this.idAgencia = agencia.getIdAgencia();
    }

    // Compatibilidad
    public Date getFechaPago() { return fechaLiquidacion; }
    public void setFechaPago(Date fechaPago) { this.fechaLiquidacion = fechaPago; }
}
