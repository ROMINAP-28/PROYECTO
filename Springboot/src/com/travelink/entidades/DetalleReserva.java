package com.travelink.entidades;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import java.math.BigDecimal;

@Entity
@Table(name = "DetalleReserva")
public class DetalleReserva {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer idDetalle;

    @Column(name = "idReserva", insertable = false, updatable = false)
    private Integer idReserva;

    @Column(name = "idTourFecha", insertable = false, updatable = false)
    private Integer idTourFecha;

    @Column(name = "cantAdultos")
    private Integer cantAdultos = 1;

    @Column(name = "cantNinos")
    private Integer cantNinos = 0;

    @Column(name = "cantBebes")
    private Integer cantBebes = 0;

    @Column(name = "tipo", length = 20)
    private String tipo;

    @Column(name = "cantidad")
    private Integer cantidad;

    @Column(name = "precioUnitario", precision = 10, scale = 2)
    private BigDecimal precioUnitario;

    @Column(name = "subtotal", precision = 10, scale = 2, nullable = false)
    private BigDecimal subtotal = BigDecimal.ZERO;

    @Column(name = "idPaquete")
    private Integer idPaquete;

    @JsonIgnore
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "idReserva", nullable = false)
    private Reserva reserva;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "idTourFecha", nullable = false)
    private TourFecha tourFecha;

    public DetalleReserva() {
        this.cantAdultos = 1;
        this.cantNinos = 0;
        this.cantBebes = 0;
        this.subtotal = BigDecimal.ZERO;
    }

    public Integer getIdDetalle() { return idDetalle; }
    public void setIdDetalle(Integer idDetalle) { this.idDetalle = idDetalle; }

    public Integer getIdReserva() {
        return idReserva != null ? idReserva : (reserva != null ? reserva.getIdReserva() : null);
    }
    public void setIdReserva(Integer idReserva) { this.idReserva = idReserva; }

    public Integer getIdTourFecha() {
        return idTourFecha != null ? idTourFecha : (tourFecha != null ? tourFecha.getIdTourFecha() : null);
    }
    public void setIdTourFecha(Integer idTourFecha) { this.idTourFecha = idTourFecha; }

    public Integer getCantAdultos() { return cantAdultos; }
    public void setCantAdultos(Integer cantAdultos) { this.cantAdultos = cantAdultos; }

    public Integer getCantNinos() { return cantNinos; }
    public void setCantNinos(Integer cantNinos) { this.cantNinos = cantNinos; }

    public Integer getCantBebes() { return cantBebes; }
    public void setCantBebes(Integer cantBebes) { this.cantBebes = cantBebes; }

    public String getTipo() { return tipo; }
    public void setTipo(String tipo) { this.tipo = tipo; }

    public Integer getCantidad() { return cantidad; }
    public void setCantidad(Integer cantidad) { this.cantidad = cantidad; }

    public BigDecimal getPrecioUnitario() { return precioUnitario; }
    public void setPrecioUnitario(BigDecimal precioUnitario) { this.precioUnitario = precioUnitario; }

    public BigDecimal getSubtotal() { return subtotal; }
    public void setSubtotal(BigDecimal subtotal) { this.subtotal = subtotal; }

    public Integer getIdPaquete() { return idPaquete; }
    public void setIdPaquete(Integer idPaquete) { this.idPaquete = idPaquete; }

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
        return (cantAdultos != null ? cantAdultos : 0) +
               (cantNinos != null ? cantNinos : 0) +
               (cantBebes != null ? cantBebes : 0);
    }
}
