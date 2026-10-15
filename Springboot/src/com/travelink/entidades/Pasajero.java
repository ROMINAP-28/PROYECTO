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

    @Column(name = "telefono", length = 20)
    private String telefono;

    @Column(name = "nombre", nullable = false, length = 100)
    private String nombre;

    @Column(name = "apellidoPaterno", length = 100)
    private String apellidoPaterno;

    @Column(name = "apellidoMaterno", length = 100)
    private String apellidoMaterno;

    @Column(name = "apellidos", nullable = false, length = 200)
    private String apellidos;

    @Column(name = "esTitular")
    private Boolean esTitular = false;

    @Column(name = "tipo", length = 20)
    private String tipo = "ADULTO";

    @Column(name = "edad")
    private Integer edad;

    @JsonIgnore
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "idReserva", nullable = false)
    private Reserva reserva;

    public Pasajero() {
        this.esTitular = false;
        this.tipo = "ADULTO";
    }

    public Pasajero(Integer idPasajero, Integer idReserva, String nroDocumento, String nombre, String apellidos, Boolean esTitular) {
        this.idPasajero = idPasajero;
        this.idReserva = idReserva;
        this.nroDocumento = nroDocumento;
        this.nombre = nombre;
        this.apellidos = apellidos;
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

    public String getTelefono() { return telefono; }
    public void setTelefono(String telefono) { this.telefono = telefono; }

    public String getNombre() { return nombre; }
    public void setNombre(String nombre) { this.nombre = nombre; }

    public String getApellidoPaterno() { return apellidoPaterno; }
    public void setApellidoPaterno(String apellidoPaterno) { this.apellidoPaterno = apellidoPaterno; }

    public String getApellidoMaterno() { return apellidoMaterno; }
    public void setApellidoMaterno(String apellidoMaterno) { this.apellidoMaterno = apellidoMaterno; }

    public String getApellidos() { return apellidos; }
    public void setApellidos(String apellidos) { this.apellidos = apellidos; }

    public Boolean getEsTitular() { return esTitular != null ? esTitular : false; }
    public void setEsTitular(Boolean esTitular) { this.esTitular = esTitular; }
    public boolean isEsTitular() { return Boolean.TRUE.equals(this.esTitular); }

    public String getTipo() { return tipo != null ? tipo : "ADULTO"; }
    public void setTipo(String tipo) { this.tipo = tipo; }

    public Integer getEdad() { return edad; }
    public void setEdad(Integer edad) { this.edad = edad; }

    public Reserva getReserva() { return reserva; }
    public void setReserva(Reserva reserva) {
        this.reserva = reserva;
        if (reserva != null) this.idReserva = reserva.getIdReserva();
    }

    public String getNombreCompleto() {
        return (nombre != null ? nombre : "") + " " + (apellidos != null ? apellidos : "").trim();
    }
}
