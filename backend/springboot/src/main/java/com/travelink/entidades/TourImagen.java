package com.travelink.entidades;

public class TourImagen {
    private int idImagen;
    private int idTour;
    private String url;
    private boolean esPrincipal;
    private int orden;

    public TourImagen() {}

    public int getIdImagen() { return idImagen; }
    public void setIdImagen(int idImagen) { this.idImagen = idImagen; }

    public int getIdTour() { return idTour; }
    public void setIdTour(int idTour) { this.idTour = idTour; }

    public String getUrl() { return url; }
    public void setUrl(String url) { this.url = url; }

    public boolean isEsPrincipal() { return esPrincipal; }
    public void setEsPrincipal(boolean esPrincipal) { this.esPrincipal = esPrincipal; }

    public int getOrden() { return orden; }
    public void setOrden(int orden) { this.orden = orden; }
}
