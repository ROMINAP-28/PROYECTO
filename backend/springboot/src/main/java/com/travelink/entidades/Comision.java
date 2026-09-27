package com.travelink.entidades;
import java.util.Date;
public class Comision {
    private int idComision;
    private double porcentaje;
    private double montoComision;
    private double montoNeto;
    private Date fechaCalculo;
    private String estado;
    private int idReserva;
    public Comision() {}
    public int getIdComision() { return idComision; }
    public void setIdComision(int idComision) { this.idComision = idComision; }
    public double getPorcentaje() { return porcentaje; }
    public void setPorcentaje(double porcentaje) { this.porcentaje = porcentaje; }
    public double getMontoComision() { return montoComision; }
    public void setMontoComision(double montoComision) { this.montoComision = montoComision; }
    public double getMontoNeto() { return montoNeto; }
    public void setMontoNeto(double montoNeto) { this.montoNeto = montoNeto; }
    public Date getFechaCalculo() { return fechaCalculo; }
    public void setFechaCalculo(Date fechaCalculo) { this.fechaCalculo = fechaCalculo; }
    public String getEstado() { return estado; }
    public void setEstado(String estado) { this.estado = estado; }
    public int getIdReserva() { return idReserva; }
    public void setIdReserva(int idReserva) { this.idReserva = idReserva; }
}
