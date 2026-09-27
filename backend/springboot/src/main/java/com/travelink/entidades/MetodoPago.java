package com.travelink.entidades;

import java.math.BigDecimal;
import java.util.Date;

public class MetodoPago {
    private int idMetodoPago;
    private int idReserva; // FK → Reserva
    private String tipoPago; // Efectivo / Tarjeta / Yape / Plin
    private BigDecimal monto;
    private boolean esAdelanto; // true solo si Efectivo
    private String estadoPago; // Pendiente / Completado / Rechazado
    private String numeroOperacion;
    private Date fechaPago;

    public MetodoPago() {
        this.tipoPago = "Tarjeta";
        this.monto = BigDecimal.ZERO;
        this.esAdelanto = false;
        this.estadoPago = "Pendiente";
        this.fechaPago = new Date();
    }

    public MetodoPago(int idMetodoPago, int idReserva, String tipoPago, BigDecimal monto, boolean esAdelanto, String estadoPago) {
        this.idMetodoPago = idMetodoPago;
        this.idReserva = idReserva;
        this.tipoPago = tipoPago;
        this.monto = monto;
        this.esAdelanto = esAdelanto;
        this.estadoPago = estadoPago;
        this.fechaPago = new Date();
    }

    public int getIdMetodoPago() { return idMetodoPago; }
    public void setIdMetodoPago(int idMetodoPago) { this.idMetodoPago = idMetodoPago; }

    public int getIdReserva() { return idReserva; }
    public void setIdReserva(int idReserva) { this.idReserva = idReserva; }

    public String getTipoPago() { return tipoPago; }
    public void setTipoPago(String tipoPago) { this.tipoPago = tipoPago; }

    public BigDecimal getMonto() { return monto; }
    public void setMonto(BigDecimal monto) { this.monto = monto; }
    public void setMonto(double monto) { this.monto = BigDecimal.valueOf(monto); }

    public boolean isEsAdelanto() { return esAdelanto; }
    public void setEsAdelanto(boolean esAdelanto) { this.esAdelanto = esAdelanto; }

    public String getEstadoPago() { return estadoPago; }
    public void setEstadoPago(String estadoPago) { this.estadoPago = estadoPago; }

    public String getNumeroOperacion() { return numeroOperacion; }
    public void setNumeroOperacion(String numeroOperacion) { this.numeroOperacion = numeroOperacion; }

    public Date getFechaPago() { return fechaPago; }
    public void setFechaPago(Date fechaPago) { this.fechaPago = fechaPago; }
}
