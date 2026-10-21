package com.travelink.controlador.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.travelink.entidades.Reserva;

import java.math.BigDecimal;
import java.util.Date;
import java.util.List;

@JsonInclude(JsonInclude.Include.NON_NULL)
public class ReservaDto {

    private Integer idReserva;
    private String codigoReserva;
    private Integer idUsuario;
    private Integer idAgencia;
    private Integer idTour;
    private Integer idTourFecha;
    private String tourNombre;
    private String tourUbicacion;
    private String agenciaNombre;
    private String cliente;
    private String emailCliente;
    private String telefonoCliente;
    private BigDecimal total;
    private String estado;
    private String motivoCancelacion;
    private Date fechaInicio;
    private Date fechaFin;
    private Date fechaRegistro;
    private String metodoPago;
    private String desglose;
    private Integer cupos;
    private Boolean calificada;
    private List<PasajeroDto> pasajeros;

    public ReservaDto() {}

    public static ReservaDto desdeEntidad(Reserva r) {
        if (r == null) return null;
        ReservaDto dto = new ReservaDto();
        dto.setIdReserva(r.getIdReserva());
        dto.setCodigoReserva(r.getCodigoReserva());
        dto.setIdUsuario(r.getIdUsuario());
        dto.setIdAgencia(r.getIdAgencia());
        dto.setIdTour(r.getIdTour());
        dto.setIdTourFecha(r.getIdTourFecha());
        dto.setTotal(r.getTotal());
        dto.setEstado(r.getEstado());
        dto.setMotivoCancelacion(r.getMotivoCancelacion());
        dto.setFechaInicio(r.getFechaInicio());
        dto.setFechaFin(r.getFechaFin());
        dto.setFechaRegistro(r.getFechaRegistro());

        if (r.getTour() != null) {
            dto.setTourNombre(r.getTour().getNombre());
            dto.setTourUbicacion(r.getTour().getUbicacion());
            if (r.getTour().getAgencia() != null) {
                dto.setAgenciaNombre(r.getTour().getAgencia().getNombreComercial() != null 
                        ? r.getTour().getAgencia().getNombreComercial() 
                        : r.getTour().getAgencia().getRazonSocial());
            }
        }
        if (r.getUsuario() != null) {
            if (r.getUsuario().getPersona() != null) {
                dto.setCliente(r.getUsuario().getPersona().getNombreCompleto());
                dto.setEmailCliente(r.getUsuario().getPersona().getEmail());
                dto.setTelefonoCliente(r.getUsuario().getPersona().getTelefono());
            } else {
                dto.setCliente(r.getUsuario().getNombreUsuario());
            }
        }
        if (r.getMetodoPago() != null) {
            dto.setMetodoPago(r.getMetodoPago().getTipoPago());
        }
        return dto;
    }

    public Integer getIdReserva() { return idReserva; }
    public void setIdReserva(Integer idReserva) { this.idReserva = idReserva; }

    public String getCodigoReserva() { return codigoReserva; }
    public void setCodigoReserva(String codigoReserva) { this.codigoReserva = codigoReserva; }

    public Integer getIdUsuario() { return idUsuario; }
    public void setIdUsuario(Integer idUsuario) { this.idUsuario = idUsuario; }

    public Integer getIdAgencia() { return idAgencia; }
    public void setIdAgencia(Integer idAgencia) { this.idAgencia = idAgencia; }

    public Integer getIdTour() { return idTour; }
    public void setIdTour(Integer idTour) { this.idTour = idTour; }

    public Integer getIdTourFecha() { return idTourFecha; }
    public void setIdTourFecha(Integer idTourFecha) { this.idTourFecha = idTourFecha; }

    public String getTourNombre() { return tourNombre; }
    public void setTourNombre(String tourNombre) { this.tourNombre = tourNombre; }

    public String getTourUbicacion() { return tourUbicacion; }
    public void setTourUbicacion(String tourUbicacion) { this.tourUbicacion = tourUbicacion; }

    public String getAgenciaNombre() { return agenciaNombre; }
    public void setAgenciaNombre(String agenciaNombre) { this.agenciaNombre = agenciaNombre; }

    public String getCliente() { return cliente; }
    public void setCliente(String cliente) { this.cliente = cliente; }

    public String getEmailCliente() { return emailCliente; }
    public void setEmailCliente(String emailCliente) { this.emailCliente = emailCliente; }

    public String getTelefonoCliente() { return telefonoCliente; }
    public void setTelefonoCliente(String telefonoCliente) { this.telefonoCliente = telefonoCliente; }

    public BigDecimal getTotal() { return total; }
    public void setTotal(BigDecimal total) { this.total = total; }

    public String getEstado() { return estado; }
    public void setEstado(String estado) { this.estado = estado; }

    public String getMotivoCancelacion() { return motivoCancelacion; }
    public void setMotivoCancelacion(String motivoCancelacion) { this.motivoCancelacion = motivoCancelacion; }

    public Date getFechaInicio() { return fechaInicio; }
    public void setFechaInicio(Date fechaInicio) { this.fechaInicio = fechaInicio; }

    public Date getFechaFin() { return fechaFin; }
    public void setFechaFin(Date fechaFin) { this.fechaFin = fechaFin; }

    public Date getFechaRegistro() { return fechaRegistro; }
    public void setFechaRegistro(Date fechaRegistro) { this.fechaRegistro = fechaRegistro; }

    public String getMetodoPago() { return metodoPago; }
    public void setMetodoPago(String metodoPago) { this.metodoPago = metodoPago; }

    public String getDesglose() { return desglose; }
    public void setDesglose(String desglose) { this.desglose = desglose; }

    public Integer getCupos() { return cupos; }
    public void setCupos(Integer cupos) { this.cupos = cupos; }

    public Boolean getCalificada() { return calificada; }
    public void setCalificada(Boolean calificada) { this.calificada = calificada; }

    public List<PasajeroDto> getPasajeros() { return pasajeros; }
    public void setPasajeros(List<PasajeroDto> pasajeros) { this.pasajeros = pasajeros; }
}
