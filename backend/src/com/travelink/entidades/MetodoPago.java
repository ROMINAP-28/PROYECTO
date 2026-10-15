package com.travelink.entidades;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import java.math.BigDecimal;
import java.util.Date;

@Entity
@Table(name = "MetodoPago")
public class MetodoPago {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer idMetodoPago;

    @Column(name = "idReserva", insertable = false, updatable = false)
    private Integer idReserva;

    @Column(name = "tipoPago", nullable = false, length = 50)
    private String tipoPago; // Efectivo / Tarjeta / Yape / Plin

    @Column(name = "monto", precision = 10, scale = 2, nullable = false)
    private BigDecimal monto = BigDecimal.ZERO;

    @Column(name = "esAdelanto")
    private Boolean esAdelanto = false;

    @Column(name = "estadoPago", length = 20)
    private String estadoPago = "Pendiente"; // Pendiente / Completado / Rechazado

    @Column(name = "numeroOperacion", length = 100)
    private String numeroOperacion;

    @Column(name = "marcaTarjeta", length = 30)
    private String marcaTarjeta;

    @Column(name = "ultimosDigitos", length = 4)
    private String ultimosDigitos;

    @Column(name = "urlComprobante", length = 255)
    private String urlComprobante;

    @Temporal(TemporalType.TIMESTAMP)
    @Column(name = "fechaPago")
    private Date fechaPago = new Date();

    @JsonIgnore
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "idReserva", nullable = false)
    private Reserva reserva;

    public MetodoPago() {
        this.tipoPago = "Tarjeta";
        this.monto = BigDecimal.ZERO;
        this.esAdelanto = false;
        this.estadoPago = "Pendiente";
        this.fechaPago = new Date();
    }

    public MetodoPago(Integer idMetodoPago, Integer idReserva, String tipoPago, BigDecimal monto, Boolean esAdelanto, String estadoPago) {
        this.idMetodoPago = idMetodoPago;
        this.idReserva = idReserva;
        this.tipoPago = tipoPago;
        this.monto = monto;
        this.esAdelanto = esAdelanto;
        this.estadoPago = estadoPago;
        this.fechaPago = new Date();
    }

    public Integer getIdMetodoPago() { return idMetodoPago; }
    public void setIdMetodoPago(Integer idMetodoPago) { this.idMetodoPago = idMetodoPago; }

    public Integer getIdReserva() {
        return idReserva != null ? idReserva : (reserva != null ? reserva.getIdReserva() : null);
    }
    public void setIdReserva(Integer idReserva) { this.idReserva = idReserva; }

    public String getTipoPago() { return tipoPago; }
    public void setTipoPago(String tipoPago) { this.tipoPago = tipoPago; }

    public BigDecimal getMonto() { return monto; }
    public void setMonto(BigDecimal monto) { this.monto = monto; }
    public void setMonto(double monto) { this.monto = BigDecimal.valueOf(monto); }

    public Boolean getEsAdelanto() { return esAdelanto != null ? esAdelanto : false; }
    public void setEsAdelanto(Boolean esAdelanto) { this.esAdelanto = esAdelanto; }
    public boolean isEsAdelanto() { return getEsAdelanto(); }

    public String getEstadoPago() { return estadoPago; }
    public void setEstadoPago(String estadoPago) { this.estadoPago = estadoPago; }

    public String getNumeroOperacion() { return numeroOperacion; }
    public void setNumeroOperacion(String numeroOperacion) { this.numeroOperacion = numeroOperacion; }

    public String getMarcaTarjeta() { return marcaTarjeta; }
    public void setMarcaTarjeta(String marcaTarjeta) { this.marcaTarjeta = marcaTarjeta; }

    public String getUltimosDigitos() { return ultimosDigitos; }
    public void setUltimosDigitos(String ultimosDigitos) { this.ultimosDigitos = ultimosDigitos; }

    public String getUrlComprobante() { return urlComprobante; }
    public void setUrlComprobante(String urlComprobante) { this.urlComprobante = urlComprobante; }

    public Date getFechaPago() { return fechaPago; }
    public void setFechaPago(Date fechaPago) { this.fechaPago = fechaPago; }

    public Reserva getReserva() { return reserva; }
    public void setReserva(Reserva reserva) {
        this.reserva = reserva;
        if (reserva != null) this.idReserva = reserva.getIdReserva();
    }
}
