package com.travelink.controlador.dto;

import jakarta.validation.constraints.NotBlank;

public class LoginRequest {

    @NotBlank(message = "El usuario o correo es obligatorio")
    private String login;

    @NotBlank(message = "La contraseña es obligatoria")
    private String clave;

    public LoginRequest() {}

    public LoginRequest(String login, String clave) {
        this.login = login;
        this.clave = clave;
    }

    public String getLogin() { return login; }
    public void setLogin(String login) { this.login = login; }

    public String getClave() { return clave; }
    public void setClave(String clave) { this.clave = clave; }
}
