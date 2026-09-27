package com.travelink.entidades;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class Reserva {
    private int idReserva;
    private int idUsuario; // FK → Usuario (titular)
    private String codigoReserva;
    private Date fechaRegistro;
    private Date fechaInicio;
    private Date fechaFin;
    private String estado; // Pendiente / Confirmada / Cancelada
    private String motivoCancelacion;
    private BigDecimal total;

    // Relaciones
    private Usuario usuario;
    private List<DetalleReserva> detalles = new ArrayList<>();
    private List<Pasajero> pasajeros = new ArrayList<>();
    private MetodoPago metodoPago;

    public Reserva() {
        this.fechaRegistro = new Date();
        this.estado = "Pendiente";
        this.total = BigDecimal.ZERO;
    }

    public int getIdReserva() { return idReserva; }
    public void setIdReserva(int idReserva) { this.idReserva = idReserva; }

    public int getIdUsuario() { return idUsuario; }
    public void setIdUsuario(int idUsuario) { this.idUsuario = idUsuario; }

    public String getCodigoReserva() { return codigoReserva; }
    public void setCodigoReserva(String codigoReserva) { this.codigoReserva = codigoReserva; }

    public Date getFechaRegistro() { return fechaRegistro; }
    public void setFechaRegistro(Date fechaRegistro) { this.fechaRegistro = fechaRegistro; }

    public Date getFechaInicio() { return fechaInicio; }
    public void setFechaInicio(Date fechaInicio) { this.fechaInicio = fechaInicio; }

    public Date getFechaFin() { return fechaFin; }
    public void setFechaFin(Date fechaFin) { this.fechaFin = fechaFin; }

    public String getEstado() { return estado; }
    public void setEstado(String estado) { this.estado = estado; }

    public String getMotivoCancelacion() { return motivoCancelacion; }
    public void setMotivoCancelacion(String motivoCancelacion) { this.motivoCancelacion = motivoCancelacion; }

    public BigDecimal getTotal() { return total; }
    public void setTotal(BigDecimal total) { this.total = total; }
    public void setTotal(double total) { this.total = BigDecimal.valueOf(total); }

    public Usuario getUsuario() { return usuario; }
    public void setUsuario(Usuario usuario) { 
        this.usuario = usuario; 
        if (usuario != null) this.idUsuario = usuario.getIdUsuario();
    }

    public List<DetalleReserva> getDetalles() { return detalles; }
    public void setDetalles(List<DetalleReserva> detalles) { this.detalles = detalles; }

    public List<Pasajero> getPasajeros() { return pasajeros; }
    public void setPasajeros(List<Pasajero> pasajeros) { this.pasajeros = pasajeros; }

    public MetodoPago getMetodoPago() { return metodoPago; }
    public void setMetodoPago(MetodoPago metodoPago) { this.metodoPago = metodoPago; }

    // Compatibilidad
    public BigDecimal getPrecioTotal() { return total; }
    public void setPrecioTotal(BigDecimal precioTotal) { this.total = precioTotal; }
    public String getCodigo() { return codigoReserva; }
    public void setCodigo(String codigo) { this.codigoReserva = codigo; }
}
