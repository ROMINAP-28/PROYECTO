package com.travelink.entidades;

import java.sql.Time;
import java.util.Date;

public class TourFecha {
    private int idTourFecha;
    private int idTour; // FK → Tour
    private Date fecha;
    private Time horaInicio;
    private int cupoTotal;
    private int cupoDisponible;
    private String estado;

    // Relación
    private Tour tour;

    public TourFecha() {}

    public int getIdTourFecha() { return idTourFecha; }
    public void setIdTourFecha(int idTourFecha) { this.idTourFecha = idTourFecha; }

    public int getIdTour() { return idTour; }
    public void setIdTour(int idTour) { this.idTour = idTour; }

    public Date getFecha() { return fecha; }
    public void setFecha(Date fecha) { this.fecha = fecha; }

    public Time getHoraInicio() { return horaInicio; }
    public void setHoraInicio(Time horaInicio) { this.horaInicio = horaInicio; }

    public int getCupoTotal() { return cupoTotal; }
    public void setCupoTotal(int cupoTotal) { this.cupoTotal = cupoTotal; }

    public int getCupoDisponible() { return cupoDisponible; }
    public void setCupoDisponible(int cupoDisponible) { this.cupoDisponible = cupoDisponible; }

    public String getEstado() { return estado; }
    public void setEstado(String estado) { this.estado = estado; }

    public Tour getTour() { return tour; }
    public void setTour(Tour tour) { 
        this.tour = tour; 
        if (tour != null) this.idTour = tour.getIdTour();
    }
}
