package com.travelink.entidades;
import java.util.Date;
public class Liquidacion {
    private int idLiquidacion;
    private double montoBruto;
    private double montoComision;
    private double montoNeto;
    private Date fechaGeneracion;
    private Date fechaLiquidacion;
    private String estado;
    private int idAgencia;
    public Liquidacion() {}
    public int getIdLiquidacion() { return idLiquidacion; }
    public void setIdLiquidacion(int idLiquidacion) { this.idLiquidacion = idLiquidacion; }
    public double getMontoBruto() { return montoBruto; }
    public void setMontoBruto(double montoBruto) { this.montoBruto = montoBruto; }
    public double getMontoComision() { return montoComision; }
    public void setMontoComision(double montoComision) { this.montoComision = montoComision; }
    public double getMontoNeto() { return montoNeto; }
    public void setMontoNeto(double montoNeto) { this.montoNeto = montoNeto; }
    public Date getFechaGeneracion() { return fechaGeneracion; }
    public void setFechaGeneracion(Date fechaGeneracion) { this.fechaGeneracion = fechaGeneracion; }
    public Date getFechaLiquidacion() { return fechaLiquidacion; }
    public void setFechaLiquidacion(Date fechaLiquidacion) { this.fechaLiquidacion = fechaLiquidacion; }
    public String getEstado() { return estado; }
    public void setEstado(String estado) { this.estado = estado; }
    public int getIdAgencia() { return idAgencia; }
    public void setIdAgencia(int idAgencia) { this.idAgencia = idAgencia; }
}
