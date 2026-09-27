package com.travelink.entidades;
import java.util.Date;
import java.sql.Time;
public class Disponibilidad {
    private int idDisponibilidad;
    private Date fecha;
    private Time horaInicio;
    private Time horaFin;
    private int cupoTotal;
    private int cupoDisponible;
    private String estado;
    private int idServicio;
    public Disponibilidad() {}
    public int getIdDisponibilidad() { return idDisponibilidad; }
    public void setIdDisponibilidad(int idDisponibilidad) { this.idDisponibilidad = idDisponibilidad; }
    public Date getFecha() { return fecha; }
    public void setFecha(Date fecha) { this.fecha = fecha; }
    public Time getHoraInicio() { return horaInicio; }
    public void setHoraInicio(Time horaInicio) { this.horaInicio = horaInicio; }
    public Time getHoraFin() { return horaFin; }
    public void setHoraFin(Time horaFin) { this.horaFin = horaFin; }
    public int getCupoTotal() { return cupoTotal; }
    public void setCupoTotal(int cupoTotal) { this.cupoTotal = cupoTotal; }
    public int getCupoDisponible() { return cupoDisponible; }
    public void setCupoDisponible(int cupoDisponible) { this.cupoDisponible = cupoDisponible; }
    public String getEstado() { return estado; }
    public void setEstado(String estado) { this.estado = estado; }
    public int getIdServicio() { return idServicio; }
    public void setIdServicio(int idServicio) { this.idServicio = idServicio; }
}
