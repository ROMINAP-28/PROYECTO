package com.travelink.entidades;

import jakarta.persistence.*;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

@Entity
@Table(name = "Reserva")
public class Reserva {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer idReserva;

    @Column(name = "idUsuario", insertable = false, updatable = false)
    private Integer idUsuario;

    @Column(name = "idAgencia", insertable = false, updatable = false)
    private Integer idAgencia;

    @Column(name = "idTour", insertable = false, updatable = false)
    private Integer idTour;

    @Column(name = "idTourFecha", insertable = false, updatable = false)
    private Integer idTourFecha;

    @Column(name = "nombreTour", length = 150)
    private String nombreTour;

    @Column(name = "codigoReserva", nullable = false, unique = true, length = 20)
    private String codigoReserva;

    @Temporal(TemporalType.TIMESTAMP)
    @Column(name = "fechaRegistro")
    private Date fechaRegistro = new Date();

    @Temporal(TemporalType.DATE)
    @Column(name = "fechaInicio", nullable = false)
    private Date fechaInicio;

    @Temporal(TemporalType.DATE)
    @Column(name = "fechaFin", nullable = false)
    private Date fechaFin;

    @Column(name = "estado", nullable = false, length = 20)
    private String estado = "PENDIENTE";

    @Column(name = "motivoCancelacion", length = 255)
    private String motivoCancelacion;

    @Column(name = "total", precision = 10, scale = 2, nullable = false)
    private BigDecimal total = BigDecimal.ZERO;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "idUsuario", nullable = false)
    private Usuario usuario;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "idAgencia", nullable = false)
    private Agencia agencia;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "idTour")
    private Tour tour;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "idTourFecha")
    private TourFecha tourFecha;

    @OneToMany(mappedBy = "reserva", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
    private List<DetalleReserva> detalles = new ArrayList<>();

    @OneToMany(mappedBy = "reserva", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
    private List<Pasajero> pasajeros = new ArrayList<>();

    @OneToMany(mappedBy = "reserva", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
    private List<MetodoPago> metodosPago = new ArrayList<>();

    public Reserva() {}

    public Integer getIdReserva() { return idReserva; }
    public void setIdReserva(Integer idReserva) { this.idReserva = idReserva; }

    public Integer getIdUsuario() { return idUsuario != null ? idUsuario : (usuario != null ? usuario.getIdUsuario() : null); }
    public void setIdUsuario(Integer idUsuario) { this.idUsuario = idUsuario; }

    public Integer getIdAgencia() { return idAgencia != null ? idAgencia : (agencia != null ? agencia.getIdAgencia() : null); }
    public void setIdAgencia(Integer idAgencia) { this.idAgencia = idAgencia; }

    public Integer getIdTour() { return idTour != null ? idTour : (tour != null ? tour.getIdTour() : null); }
    public void setIdTour(Integer idTour) { this.idTour = idTour; }

    public Integer getIdTourFecha() { return idTourFecha != null ? idTourFecha : (tourFecha != null ? tourFecha.getIdTourFecha() : null); }
    public void setIdTourFecha(Integer idTourFecha) { this.idTourFecha = idTourFecha; }

    public String getNombreTour() { return nombreTour; }
    public void setNombreTour(String nombreTour) { this.nombreTour = nombreTour; }

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

    public Usuario getUsuario() { return usuario; }
    public void setUsuario(Usuario usuario) {
        this.usuario = usuario;
        if (usuario != null) this.idUsuario = usuario.getIdUsuario();
    }

    public Agencia getAgencia() { return agencia; }
    public void setAgencia(Agencia agencia) {
        this.agencia = agencia;
        if (agencia != null) this.idAgencia = agencia.getIdAgencia();
    }

    public Tour getTour() { return tour; }
    public void setTour(Tour tour) {
        this.tour = tour;
        if (tour != null) this.idTour = tour.getIdTour();
    }

    public TourFecha getTourFecha() { return tourFecha; }
    public void setTourFecha(TourFecha tourFecha) {
        this.tourFecha = tourFecha;
        if (tourFecha != null) this.idTourFecha = tourFecha.getIdTourFecha();
    }

    public List<DetalleReserva> getDetalles() { return detalles; }
    public void setDetalles(List<DetalleReserva> detalles) { this.detalles = detalles; }

    public List<Pasajero> getPasajeros() { return pasajeros; }
    public void setPasajeros(List<Pasajero> pasajeros) { this.pasajeros = pasajeros; }

    public List<MetodoPago> getMetodosPago() { return metodosPago; }
    public void setMetodosPago(List<MetodoPago> metodosPago) { this.metodosPago = metodosPago; }

    public MetodoPago getMetodoPago() {
        return (metodosPago != null && !metodosPago.isEmpty()) ? metodosPago.get(0) : null;
    }
    public void setMetodoPago(MetodoPago mp) {
        if (this.metodosPago == null) this.metodosPago = new ArrayList<>();
        this.metodosPago.clear();
        if (mp != null) {
            mp.setReserva(this);
            this.metodosPago.add(mp);
        }
    }

    // Compatibilidad
    public BigDecimal getPrecioTotal() { return total; }
    public void setPrecioTotal(BigDecimal precioTotal) { this.total = precioTotal; }
    public String getCodigo() { return codigoReserva; }
    public void setCodigo(String codigo) { this.codigoReserva = codigo; }
}
