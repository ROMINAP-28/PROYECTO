package com.travelink.entidades;

import jakarta.persistence.*;
import java.sql.Time;
import java.util.Date;

@Entity
@Table(name = "TourFecha")
public class TourFecha {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer idTourFecha;

    @Column(name = "idTour", insertable = false, updatable = false)
    private Integer idTour;

    @Temporal(TemporalType.DATE)
    @Column(name = "fecha", nullable = false)
    private Date fecha;

    @Column(name = "horaInicio", nullable = false)
    private Time horaInicio;

    @Transient
    private Time horaFin;

    @Column(name = "cupoTotal", nullable = false)
    private int cupoTotal = 30;

    @Column(name = "cupoDisponible", nullable = false)
    private int cupoDisponible = 30;

    @Column(name = "estado", length = 20)
    private String estado = "DISPONIBLE";

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "idTour", nullable = false)
    private Tour tour;

    public TourFecha() {}

    public Integer getIdTourFecha() { return idTourFecha; }
    public void setIdTourFecha(Integer idTourFecha) { this.idTourFecha = idTourFecha; }

    public Integer getIdTour() { return idTour != null ? idTour : (tour != null ? tour.getIdTour() : null); }
    public void setIdTour(Integer idTour) { this.idTour = idTour; }

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

    public Tour getTour() { return tour; }
    public void setTour(Tour tour) { 
        this.tour = tour; 
        if (tour != null) this.idTour = tour.getIdTour();
    }
}
