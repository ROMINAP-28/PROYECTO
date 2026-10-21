package com.travelink.controlador.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.util.List;

public class ReservaRequest {

    private Integer idTour;
    private Integer idTourFecha;
    private String fechaTour;
    private String horaSalida;

    @Min(value = 1, message = "Debe haber al menos 1 adulto en la reserva")
    private int cantAdultos = 1;

    @Min(value = 0, message = "Cantidad de niños no puede ser negativa")
    private int cantNinos = 0;

    @Min(value = 0, message = "Cantidad de bebés no puede ser negativa")
    private int cantBebes = 0;

    @NotBlank(message = "El método de pago es obligatorio")
    private String metodoPago;

    private String numeroOperacion;

    @Valid
    private List<PasajeroRequest> pasajeros;

    public ReservaRequest() {}

    public Integer getIdTour() { return idTour; }
    public void setIdTour(Integer idTour) { this.idTour = idTour; }

    public Integer getIdTourFecha() { return idTourFecha; }
    public void setIdTourFecha(Integer idTourFecha) { this.idTourFecha = idTourFecha; }

    public String getFechaTour() { return fechaTour; }
    public void setFechaTour(String fechaTour) { this.fechaTour = fechaTour; }

    public String getHoraSalida() { return horaSalida; }
    public void setHoraSalida(String horaSalida) { this.horaSalida = horaSalida; }

    public int getCantAdultos() { return cantAdultos; }
    public void setCantAdultos(int cantAdultos) { this.cantAdultos = cantAdultos; }

    public int getCantNinos() { return cantNinos; }
    public void setCantNinos(int cantNinos) { this.cantNinos = cantNinos; }

    public int getCantBebes() { return cantBebes; }
    public void setCantBebes(int cantBebes) { this.cantBebes = cantBebes; }

    public String getMetodoPago() { return metodoPago; }
    public void setMetodoPago(String metodoPago) { this.metodoPago = metodoPago; }

    public String getNumeroOperacion() { return numeroOperacion; }
    public void setNumeroOperacion(String numeroOperacion) { this.numeroOperacion = numeroOperacion; }

    public List<PasajeroRequest> getPasajeros() { return pasajeros; }
    public void setPasajeros(List<PasajeroRequest> pasajeros) { this.pasajeros = pasajeros; }
}
