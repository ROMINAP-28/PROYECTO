package com.travelink.entidades;

import jakarta.persistence.*;
import java.math.BigDecimal;

@Entity
@Table(name = "Tour")
public class Tour {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer idTour;

    @Column(name = "idAgencia", insertable = false, updatable = false)
    private Integer idAgencia;

    @Column(name = "idDestino", insertable = false, updatable = false)
    private Integer idDestino;

    @Column(name = "slug", nullable = false, unique = true, length = 100)
    private String slug;

    @Column(name = "nombre", nullable = false, length = 150)
    private String nombre;

    @Column(name = "descripcion", columnDefinition = "TEXT")
    private String descripcion;

    @Column(name = "descripcionCorta", length = 255)
    private String descripcionCorta;

    @Column(name = "itinerario", columnDefinition = "TEXT")
    private String itinerario;

    @Column(name = "aceptaNinos", nullable = false)
    private boolean aceptaNinos = true;

    @Column(name = "edadMaxNino", nullable = false)
    private int edadMaxNino = 11;

    @Column(name = "edadMaxBebe", nullable = false)
    private int edadMaxBebe = 2;

    @Column(name = "descuento", precision = 5, scale = 2, nullable = false)
    private BigDecimal descuento = BigDecimal.ZERO;

    @Column(name = "condiciones", columnDefinition = "TEXT")
    private String condiciones;

    @Column(name = "minAsientosOferta", nullable = false)
    private int minAsientosOferta = 5;

    @Column(name = "precioAdulto", precision = 10, scale = 2, nullable = false)
    private BigDecimal precioAdulto = BigDecimal.ZERO;

    @Column(name = "precioNino", precision = 10, scale = 2, nullable = false)
    private BigDecimal precioNino = BigDecimal.ZERO;

    @Column(name = "precioBebe", precision = 10, scale = 2, nullable = false)
    private BigDecimal precioBebe = BigDecimal.ZERO;

    @Column(name = "duracion", length = 50)
    private String duracion;

    @Column(name = "ubicacion", length = 100)
    private String ubicacion;

    @Column(name = "categoria", length = 50)
    private String categoria;

    @Column(name = "calificacionPromedio")
    private double calificacionPromedio = 5.0;

    @Column(name = "estado", length = 50)
    private String estado = "PUBLICADO";

    @Column(name = "aceptaBebes", nullable = false)
    private boolean aceptaBebes = true;

    @Column(name = "queIncluye", columnDefinition = "TEXT")
    private String queIncluye;

    @Column(name = "queNoIncluye", columnDefinition = "TEXT")
    private String queNoIncluye;

    @Column(name = "dias", nullable = false)
    private int dias = 1;

    @Column(name = "horas", nullable = false)
    private int horas = 6;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "idAgencia", nullable = false)
    private Agencia agencia;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "idDestino", nullable = false)
    private Destino destinoObj;

    public Tour() {}

    public Integer getIdTour() { return idTour; }
    public void setIdTour(Integer idTour) { this.idTour = idTour; }

    public Integer getIdAgencia() { return idAgencia != null ? idAgencia : (agencia != null ? agencia.getIdAgencia() : null); }
    public void setIdAgencia(Integer idAgencia) { this.idAgencia = idAgencia; }

    public Integer getIdDestino() { return idDestino != null ? idDestino : (destinoObj != null ? destinoObj.getIdDestino() : null); }
    public void setIdDestino(Integer idDestino) { this.idDestino = idDestino; }

    public String getSlug() { return slug; }
    public void setSlug(String slug) { this.slug = slug; }

    public String getNombre() { return nombre; }
    public void setNombre(String nombre) { this.nombre = nombre; }

    public String getDescripcion() { return descripcion; }
    public void setDescripcion(String descripcion) { this.descripcion = descripcion; }

    public String getDescripcionCorta() { return descripcionCorta; }
    public void setDescripcionCorta(String descripcionCorta) { this.descripcionCorta = descripcionCorta; }

    public String getItinerario() { return itinerario; }
    public void setItinerario(String itinerario) { this.itinerario = itinerario; }

    public boolean isAceptaNinos() { return aceptaNinos; }
    public boolean getAceptaNinos() { return aceptaNinos; }
    public void setAceptaNinos(boolean aceptaNinos) { this.aceptaNinos = aceptaNinos; }

    public int getEdadMaxNino() { return edadMaxNino; }
    public void setEdadMaxNino(int edadMaxNino) { this.edadMaxNino = edadMaxNino; }

    public int getEdadMaxBebe() { return edadMaxBebe; }
    public void setEdadMaxBebe(int edadMaxBebe) { this.edadMaxBebe = edadMaxBebe; }

    public BigDecimal getDescuento() { return descuento; }
    public void setDescuento(BigDecimal descuento) { this.descuento = descuento; }

    public String getCondiciones() { return condiciones; }
    public void setCondiciones(String condiciones) { this.condiciones = condiciones; }

    public int getMinAsientosOferta() { return minAsientosOferta; }
    public void setMinAsientosOferta(int minAsientosOferta) { this.minAsientosOferta = minAsientosOferta; }

    public BigDecimal getPrecioAdulto() { return precioAdulto; }
    public void setPrecioAdulto(BigDecimal precioAdulto) { this.precioAdulto = precioAdulto; }

    public BigDecimal getPrecioNino() { return precioNino; }
    public void setPrecioNino(BigDecimal precioNino) { this.precioNino = precioNino; }

    public BigDecimal getPrecioBebe() { return precioBebe; }
    public void setPrecioBebe(BigDecimal precioBebe) { this.precioBebe = precioBebe; }

    public String getDuracion() { return duracion; }
    public void setDuracion(String duracion) { this.duracion = duracion; }

    public String getUbicacion() { return ubicacion; }
    public void setUbicacion(String ubicacion) { this.ubicacion = ubicacion; }

    public String getCategoria() { return categoria; }
    public void setCategoria(String categoria) { this.categoria = categoria; }

    public double getCalificacionPromedio() { return calificacionPromedio; }
    public void setCalificacionPromedio(double calificacionPromedio) { this.calificacionPromedio = calificacionPromedio; }

    public String getEstado() { return estado; }
    public void setEstado(String estado) { this.estado = estado; }

    public boolean isAceptaBebes() { return aceptaBebes; }
    public boolean getAceptaBebes() { return aceptaBebes; }
    public void setAceptaBebes(boolean aceptaBebes) { this.aceptaBebes = aceptaBebes; }

    public String getQueIncluye() { return queIncluye; }
    public void setQueIncluye(String queIncluye) { this.queIncluye = queIncluye; }

    public String getQueNoIncluye() { return queNoIncluye; }
    public void setQueNoIncluye(String queNoIncluye) { this.queNoIncluye = queNoIncluye; }

    public int getDias() { return dias; }
    public void setDias(int dias) { this.dias = dias; }

    public int getHoras() { return horas; }
    public void setHoras(int horas) { this.horas = horas; }

    public Agencia getAgencia() { return agencia; }
    public void setAgencia(Agencia agencia) { 
        this.agencia = agencia; 
        if (agencia != null) this.idAgencia = agencia.getIdAgencia();
    }

    public Destino getDestinoObj() { return destinoObj; }
    public void setDestinoObj(Destino destinoObj) {
        this.destinoObj = destinoObj;
        if (destinoObj != null) this.idDestino = destinoObj.getIdDestino();
    }
}
