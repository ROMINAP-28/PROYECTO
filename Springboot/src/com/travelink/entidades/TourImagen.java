package com.travelink.entidades;

import jakarta.persistence.*;

@Entity
@Table(name = "TourImagen")
public class TourImagen {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer idImagen;

    @Column(name = "idTour", insertable = false, updatable = false)
    private Integer idTour;

    @Column(name = "url", nullable = false, length = 255)
    private String url;

    @Column(name = "esPrincipal")
    private boolean esPrincipal = false;

    @Column(name = "orden")
    private int orden = 0;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "idTour", nullable = false)
    private Tour tour;

    public TourImagen() {}

    public Integer getIdImagen() { return idImagen; }
    public void setIdImagen(Integer idImagen) { this.idImagen = idImagen; }

    public Integer getIdTour() { return idTour != null ? idTour : (tour != null ? tour.getIdTour() : null); }
    public void setIdTour(Integer idTour) { this.idTour = idTour; }

    public String getUrl() { return url; }
    public void setUrl(String url) { this.url = url; }

    public boolean isEsPrincipal() { return esPrincipal; }
    public void setEsPrincipal(boolean esPrincipal) { this.esPrincipal = esPrincipal; }

    public int getOrden() { return orden; }
    public void setOrden(int orden) { this.orden = orden; }

    public Tour getTour() { return tour; }
    public void setTour(Tour tour) {
        this.tour = tour;
        if (tour != null) this.idTour = tour.getIdTour();
    }
}
