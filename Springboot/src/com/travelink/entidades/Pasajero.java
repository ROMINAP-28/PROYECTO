package com.travelink.entidades;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;

@Entity
@Table(name = "Pasajero")
public class Pasajero {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer idPasajero;

    @Column(name = "idReserva", insertable = false, updatable = false)
    private Integer idReserva;

    @Column(name = "nroDocumento", nullable = false, length = 20)
    private String nroDocumento;

    @Column(name = "nombre", nullable = false, length = 100)
    private String nombre;

    @Column(name = "apellidoPaterno", length = 100)
    private String apellidoPaterno;

    @Column(name = "apellidoMaterno", length = 100)
    private String apellidoMaterno;

    @Column(name = "esTitular")
    private Boolean esTitular = false;

    @Column(name = "tipo", length = 20)
    private String tipo = "ADULTO";

    @JsonIgnore
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "idReserva", nullable = false)
    private Reserva reserva;

    public Pasajero() {
        this.esTitular = false;
        this.tipo = "ADULTO";
    }

    public Pasajero(Integer idPasajero, Integer idReserva, String nroDocumento, String nombre, String apellidoPaterno, String apellidoMaterno, Boolean esTitular) {
        this.idPasajero = idPasajero;
        this.idReserva = idReserva;
        this.nroDocumento = nroDocumento;
        this.nombre = nombre;
        this.apellidoPaterno = apellidoPaterno;
        this.apellidoMaterno = apellidoMaterno;
        this.esTitular = esTitular;
    }

    public Integer getIdPasajero() { return idPasajero; }
    public void setIdPasajero(Integer idPasajero) { this.idPasajero = idPasajero; }

    public Integer getIdReserva() {
        return idReserva != null ? idReserva : (reserva != null ? reserva.getIdReserva() : null);
    }
    public void setIdReserva(Integer idReserva) { this.idReserva = idReserva; }

    public String getNroDocumento() { return nroDocumento; }
    public void setNroDocumento(String nroDocumento) { this.nroDocumento = nroDocumento; }

    public String getNombre() { return nombre; }
    public void setNombre(String nombre) { this.nombre = nombre; }

    public String getApellidoPaterno() { return apellidoPaterno; }
    public void setApellidoPaterno(String apellidoPaterno) { this.apellidoPaterno = apellidoPaterno; }

    public String getApellidoMaterno() { return apellidoMaterno; }
    public void setApellidoMaterno(String apellidoMaterno) { this.apellidoMaterno = apellidoMaterno; }

    public String getApellidos() {
        return ((apellidoPaterno != null ? apellidoPaterno : "") + " " + (apellidoMaterno != null ? apellidoMaterno : "")).trim();
    }
    public void setApellidos(String apellidos) {
        if (apellidos != null && (apellidoPaterno == null || apellidoPaterno.isEmpty())) {
            String[] parts = apellidos.trim().split(" ");
            this.apellidoPaterno = parts[0];
            if (parts.length > 1) {
                this.apellidoMaterno = apellidos.trim().substring(parts[0].length()).trim();
            }
        }
    }

    public Boolean getEsTitular() { return esTitular != null ? esTitular : false; }
    public void setEsTitular(Boolean esTitular) { this.esTitular = esTitular; }
    public boolean isEsTitular() { return Boolean.TRUE.equals(this.esTitular); }

    public String getTipo() { return tipo != null ? tipo : "ADULTO"; }
    public void setTipo(String tipo) { this.tipo = tipo; }

    public Reserva getReserva() { return reserva; }
    public void setReserva(Reserva reserva) {
        this.reserva = reserva;
        if (reserva != null) this.idReserva = reserva.getIdReserva();
    }

    public String getNombreCompleto() {
        String ape = getApellidos();
        return ((nombre != null ? nombre : "") + " " + ape).trim();
    }
}
