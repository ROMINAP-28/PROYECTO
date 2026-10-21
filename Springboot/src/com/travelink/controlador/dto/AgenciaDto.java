package com.travelink.controlador.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.travelink.entidades.Agencia;

import java.math.BigDecimal;

@JsonInclude(JsonInclude.Include.NON_NULL)
public class AgenciaDto {

    private Integer idAgencia;
    private Integer idUsuario;
    private String razonSocial;
    private String nombreComercial;
    private String ruc;
    private String telefono;
    private String email;
    private String direccion;
    private String descripcion;
    private String logoUrl;
    private String estado;
    private BigDecimal porcentajeComision;
    private Double promedioCalificacion;
    private String representante;

    public AgenciaDto() {}

    public static AgenciaDto desdeEntidad(Agencia a) {
        if (a == null) return null;
        AgenciaDto dto = new AgenciaDto();
        dto.setIdAgencia(a.getIdAgencia());
        dto.setIdUsuario(a.getIdUsuario());
        dto.setRazonSocial(a.getRazonSocial());
        dto.setNombreComercial(a.getNombreComercial());
        dto.setRuc(a.getRuc());
        dto.setTelefono(a.getTelefono());
        dto.setEmail(a.getCorreo());
        dto.setDireccion(a.getDireccion());
        dto.setDescripcion(a.getDescripcion());
        dto.setLogoUrl(a.getLogoUrl());
        dto.setEstado(a.getEstado());
        dto.setPorcentajeComision(a.getPorcentajeComision());
        if (a.getPromedioCalificacion() != null) {
            dto.setPromedioCalificacion(a.getPromedioCalificacion().doubleValue());
        }
        if (a.getUsuario() != null && a.getUsuario().getPersona() != null) {
            dto.setRepresentante(a.getUsuario().getPersona().getNombreCompleto());
        }
        return dto;
    }

    public Integer getIdAgencia() { return idAgencia; }
    public void setIdAgencia(Integer idAgencia) { this.idAgencia = idAgencia; }

    public Integer getIdUsuario() { return idUsuario; }
    public void setIdUsuario(Integer idUsuario) { this.idUsuario = idUsuario; }

    public String getRazonSocial() { return razonSocial; }
    public void setRazonSocial(String razonSocial) { this.razonSocial = razonSocial; }

    public String getNombreComercial() { return nombreComercial; }
    public void setNombreComercial(String nombreComercial) { this.nombreComercial = nombreComercial; }

    public String getRuc() { return ruc; }
    public void setRuc(String ruc) { this.ruc = ruc; }

    public String getTelefono() { return telefono; }
    public void setTelefono(String telefono) { this.telefono = telefono; }

    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }

    public String getDireccion() { return direccion; }
    public void setDireccion(String direccion) { this.direccion = direccion; }

    public String getDescripcion() { return descripcion; }
    public void setDescripcion(String descripcion) { this.descripcion = descripcion; }

    public String getLogoUrl() { return logoUrl; }
    public void setLogoUrl(String logoUrl) { this.logoUrl = logoUrl; }

    public String getEstado() { return estado; }
    public void setEstado(String estado) { this.estado = estado; }

    public BigDecimal getPorcentajeComision() { return porcentajeComision; }
    public void setPorcentajeComision(BigDecimal porcentajeComision) { this.porcentajeComision = porcentajeComision; }

    public Double getPromedioCalificacion() { return promedioCalificacion; }
    public void setPromedioCalificacion(Double promedioCalificacion) { this.promedioCalificacion = promedioCalificacion; }

    public String getRepresentante() { return representante; }
    public void setRepresentante(String representante) { this.representante = representante; }
}
