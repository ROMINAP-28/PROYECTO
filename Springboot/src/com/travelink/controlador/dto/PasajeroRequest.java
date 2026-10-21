package com.travelink.controlador.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

public class PasajeroRequest {

    @NotBlank(message = "El número de documento del pasajero es obligatorio")
    @Pattern(regexp = "^[0-9A-Za-z]{8,15}$", message = "Documento del pasajero inválido")
    private String nroDocumento;

    @NotBlank(message = "El nombre del pasajero es obligatorio")
    @Size(max = 100)
    private String nombre;

    @NotBlank(message = "El apellido paterno del pasajero es obligatorio")
    @Size(max = 100)
    private String apellidoPaterno;

    private String apellidoMaterno;
    private Boolean esTitular;
    private String tipo;

    public PasajeroRequest() {}

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
