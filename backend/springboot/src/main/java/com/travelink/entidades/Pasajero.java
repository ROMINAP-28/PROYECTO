package com.travelink.entidades;

public class Pasajero {
    private int idPasajero;
    private int idReserva; // FK → Reserva
    private String nroDocumento;
    private String nombre;
    private String apellidos;
    private int edad;
    private String tipoSeguro; // SIS / Particular / Ninguno
    private boolean esTitular;

    public Pasajero() {
        this.tipoSeguro = "Particular";
        this.esTitular = false;
    }

    public Pasajero(int idPasajero, int idReserva, String nroDocumento, String nombre, String apellidos, int edad, String tipoSeguro, boolean esTitular) {
        this.idPasajero = idPasajero;
        this.idReserva = idReserva;
        this.nroDocumento = nroDocumento;
        this.nombre = nombre;
        this.apellidos = apellidos;
        this.edad = edad;
        this.tipoSeguro = tipoSeguro;
        this.esTitular = esTitular;
    }

    public int getIdPasajero() { return idPasajero; }
    public void setIdPasajero(int idPasajero) { this.idPasajero = idPasajero; }

    public int getIdReserva() { return idReserva; }
    public void setIdReserva(int idReserva) { this.idReserva = idReserva; }

    public String getNroDocumento() { return nroDocumento; }
    public void setNroDocumento(String nroDocumento) { this.nroDocumento = nroDocumento; }

    public String getNombre() { return nombre; }
    public void setNombre(String nombre) { this.nombre = nombre; }

    public String getApellidos() { return apellidos; }
    public void setApellidos(String apellidos) { this.apellidos = apellidos; }

    public int getEdad() { return edad; }
    public void setEdad(int edad) { this.edad = edad; }

    public String getTipoSeguro() { return tipoSeguro; }
    public void setTipoSeguro(String tipoSeguro) { this.tipoSeguro = tipoSeguro; }

    public boolean isEsTitular() { return esTitular; }
    public void setEsTitular(boolean esTitular) { this.esTitular = esTitular; }

    // Helper
    public String getNombreCompleto() {
        return (nombre != null ? nombre : "") + " " + (apellidos != null ? apellidos : "").trim();
    }
}
