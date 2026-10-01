package com.travelink.entidades;

import java.math.BigDecimal;
import java.util.Date;

public class Comision {
    private int idComision;
    private int idAgencia;
    private String periodo;
    private int toursVendidos;
    private BigDecimal montoComision;
    private BigDecimal montoNeto;
    private String estado;
    private Date fechaPago;

    public Comision() {}

    public int getIdComision() { return idComision; }
    public void setIdComision(int idComision) { this.idComision = idComision; }

    public int getIdAgencia() { return idAgencia; }
    public void setIdAgencia(int idAgencia) { this.idAgencia = idAgencia; }

    public String getPeriodo() { return periodo; }
    public void setPeriodo(String periodo) { this.periodo = periodo; }

    public int getToursVendidos() { return toursVendidos; }
    public void setToursVendidos(int toursVendidos) { this.toursVendidos = toursVendidos; }

    public BigDecimal getMontoComision() { return montoComision; }
    public void setMontoComision(BigDecimal montoComision) { this.montoComision = montoComision; }

    public BigDecimal getMontoNeto() { return montoNeto; }
    public void setMontoNeto(BigDecimal montoNeto) { this.montoNeto = montoNeto; }

    public String getEstado() { return estado; }
    public void setEstado(String estado) { this.estado = estado; }

    public Date getFechaPago() { return fechaPago; }
    public void setFechaPago(Date fechaPago) { this.fechaPago = fechaPago; }
}
