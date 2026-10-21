package com.travelink.controlador.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.travelink.entidades.Pasajero;

@JsonInclude(JsonInclude.Include.NON_NULL)
public class PasajeroDto {

    private Integer idPasajero;
    private Integer idReserva;
    private String nroDocumento;
    private String nombre;
    private String apellidoPaterno;
    private String apellidoMaterno;
    private Boolean esTitular;
    private String tipo;

    public PasajeroDto() {}

    public static PasajeroDto desdeEntidad(Pasajero p) {
        if (p == null) return null;
        PasajeroDto dto = new PasajeroDto();
        dto.setIdPasajero(p.getIdPasajero());
        dto.setIdReserva(p.getIdReserva());
        dto.setNroDocumento(p.getNroDocumento());
        dto.setNombre(p.getNombre());
        dto.setApellidoPaterno(p.getApellidoPaterno());
        dto.setApellidoMaterno(p.getApellidoMaterno());
        dto.setEsTitular(p.isEsTitular());
        dto.setTipo(p.getTipo());
        return dto;
    }

    public Integer getIdPasajero() { return idPasajero; }
    public void setIdPasajero(Integer idPasajero) { this.idPasajero = idPasajero; }

    public Integer getIdReserva() { return idReserva; }
    public void setIdReserva(Integer idReserva) { this.idReserva = idReserva; }

    public String getNroDocumento() { return nroDocumento; }
    public void setNroDocumento(String nroDocumento) { this.nroDocumento = nroDocumento; }

    public String getNombre() { return nombre; }
    public void setNombre(String nombre) { this.nombre = nombre; }

    public String getApellidoPaterno() { return apellidoPaterno; }
    public void setApellidoPaterno(String apellidoPaterno) { this.apellidoPaterno = apellidoPaterno; }

    public String getApellidoMaterno() { return apellidoMaterno; }
    public void setApellidoMaterno(String apellidoMaterno) { this.apellidoMaterno = apellidoMaterno; }

    public Boolean getEsTitular() { return esTitular; }
    public void setEsTitular(Boolean esTitular) { this.esTitular = esTitular; }

    public String getTipo() { return tipo; }
    public void setTipo(String tipo) { this.tipo = tipo; }
}
