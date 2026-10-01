package com.travelink.entidades;

public class PaqueteTour {
    private int idPaqueteTour;
    private int idPaquete;
    private int idTour;

    public PaqueteTour() {}

    public int getIdPaqueteTour() { return idPaqueteTour; }
    public void setIdPaqueteTour(int idPaqueteTour) { this.idPaqueteTour = idPaqueteTour; }

    public int getIdPaquete() { return idPaquete; }
    public void setIdPaquete(int idPaquete) { this.idPaquete = idPaquete; }

    public int getIdTour() { return idTour; }
    public void setIdTour(int idTour) { this.idTour = idTour; }
}
