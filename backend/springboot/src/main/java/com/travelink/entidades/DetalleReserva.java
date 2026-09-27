package com.travelink.entidades;

import java.math.BigDecimal;

public class DetalleReserva {
    private int idDetalle;
    private int idReserva;   // FK → Reserva
    private int idTourFecha; // FK → TourFecha
    private int cantAdultos;
    private int cantNinos;
    private int cantBebes;
    private BigDecimal subtotal;

    // Relaciones
    private Reserva reserva;
    private TourFecha tourFecha;

    public DetalleReserva() {
        this.cantAdultos = 1;
        this.cantNinos = 0;
        this.cantBebes = 0;
        this.subtotal = BigDecimal.ZERO;
    }

    public int getIdDetalle() { return idDetalle; }
    public void setIdDetalle(int idDetalle) { this.idDetalle = idDetalle; }

    public int getIdReserva() { return idReserva; }
    public void setIdReserva(int idReserva) { this.idReserva = idReserva; }

    public int getIdTourFecha() { return idTourFecha; }
    public void setIdTourFecha(int idTourFecha) { this.idTourFecha = idTourFecha; }

    public int getCantAdultos() { return cantAdultos; }
    public void setCantAdultos(int cantAdultos) { this.cantAdultos = cantAdultos; }

    public int getCantNinos() { return cantNinos; }
    public void setCantNinos(int cantNinos) { this.cantNinos = cantNinos; }

    public int getCantBebes() { return cantBebes; }
    public void setCantBebes(int cantBebes) { this.cantBebes = cantBebes; }

    public BigDecimal getSubtotal() { return subtotal; }
    public void setSubtotal(BigDecimal subtotal) { this.subtotal = subtotal; }

    public Reserva getReserva() { return reserva; }
    public void setReserva(Reserva reserva) { 
        this.reserva = reserva; 
        if (reserva != null) this.idReserva = reserva.getIdReserva();
    }

    public TourFecha getTourFecha() { return tourFecha; }
    public void setTourFecha(TourFecha tourFecha) { 
        this.tourFecha = tourFecha; 
        if (tourFecha != null) this.idTourFecha = tourFecha.getIdTourFecha();
    }

    public int getTotalPersonas() {
        return cantAdultos + cantNinos + cantBebes;
    }
}
