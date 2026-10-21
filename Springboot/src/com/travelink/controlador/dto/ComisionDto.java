package com.travelink.controlador.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.travelink.entidades.Comision;

import java.math.BigDecimal;
import java.util.Date;

@JsonInclude(JsonInclude.Include.NON_NULL)
public class ComisionDto {

    private Integer idComision;
    private Integer idAgencia;
    private String agenciaNombre;
    private String ruc;
    private BigDecimal subtotal;
    private BigDecimal porcentajeComision;
    private BigDecimal montoComision;
    private BigDecimal montoAgencia;
    private String estadoLiquidacion;
    private Date fechaCalculo;
    private Date fechaLiquidacion;
    private String periodo;

    public ComisionDto() {}

    public static ComisionDto desdeEntidad(Comision c) {
        if (c == null) return null;
        ComisionDto dto = new ComisionDto();
        dto.setIdComision(c.getIdComision());
        dto.setIdAgencia(c.getIdAgencia());
        dto.setSubtotal(c.getSubtotal());
        dto.setPorcentajeComision(c.getPorcentajeComision());
        dto.setMontoComision(c.getMontoComision());
        dto.setMontoAgencia(c.getMontoAgencia());
        dto.setEstadoLiquidacion(c.getEstadoLiquidacion());
        dto.setFechaCalculo(c.getFechaCalculo());
        dto.setFechaLiquidacion(c.getFechaLiquidacion());

        if (c.getAgencia() != null) {
            dto.setAgenciaNombre(c.getAgencia().getNombreComercial() != null 
                    ? c.getAgencia().getNombreComercial() 
                    : c.getAgencia().getRazonSocial());
            dto.setRuc(c.getAgencia().getRuc());
        }
        return dto;
    }

    public Integer getIdComision() { return idComision; }
    public void setIdComision(Integer idComision) { this.idComision = idComision; }

    public Integer getIdAgencia() { return idAgencia; }
    public void setIdAgencia(Integer idAgencia) { this.idAgencia = idAgencia; }

    public String getAgenciaNombre() { return agenciaNombre; }
    public void setAgenciaNombre(String agenciaNombre) { this.agenciaNombre = agenciaNombre; }

    public String getRuc() { return ruc; }
    public void setRuc(String ruc) { this.ruc = ruc; }

    public BigDecimal getSubtotal() { return subtotal; }
    public void setSubtotal(BigDecimal subtotal) { this.subtotal = subtotal; }

    public BigDecimal getPorcentajeComision() { return porcentajeComision; }
    public void setPorcentajeComision(BigDecimal porcentajeComision) { this.porcentajeComision = porcentajeComision; }

    public BigDecimal getMontoComision() { return montoComision; }
    public void setMontoComision(BigDecimal montoComision) { this.montoComision = montoComision; }

    public BigDecimal getMontoAgencia() { return montoAgencia; }
    public void setMontoAgencia(BigDecimal montoAgencia) { this.montoAgencia = montoAgencia; }

    public String getEstadoLiquidacion() { return estadoLiquidacion; }
    public void setEstadoLiquidacion(String estadoLiquidacion) { this.estadoLiquidacion = estadoLiquidacion; }

    public Date getFechaCalculo() { return fechaCalculo; }
    public void setFechaCalculo(Date fechaCalculo) { this.fechaCalculo = fechaCalculo; }

    public Date getFechaLiquidacion() { return fechaLiquidacion; }
    public void setFechaLiquidacion(Date fechaLiquidacion) { this.fechaLiquidacion = fechaLiquidacion; }

    public String getPeriodo() { return periodo; }
    public void setPeriodo(String periodo) { this.periodo = periodo; }
}
