package com.travelink.controlador.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

public class RegistroAgenciaRequest {

    @NotBlank(message = "El RUC es obligatorio")
    @Pattern(regexp = "^(10|20)[0-9]{9}$", message = "El RUC debe tener 11 dígitos y empezar con 10 o 20")
    private String ruc;

    @NotBlank(message = "La razón social es obligatoria")
    private String razonSocial;

    private String nombreComercial;

    @NotBlank(message = "La dirección de la agencia es obligatoria")
    private String direccion;

    @NotBlank(message = "El teléfono de la empresa es obligatorio")
    private String telefonoEmpresa;

    @NotBlank(message = "El correo de contacto es obligatorio")
    @Email(message = "El correo de la empresa no es válido")
    private String correoEmpresa;

    @NotBlank(message = "El representante legal es obligatorio")
    private String representanteLegal;

    @NotBlank(message = "El DNI del representante es obligatorio")
    @Pattern(regexp = "^[0-9]{8}$", message = "El DNI del representante debe tener 8 dígitos")
    private String dniRepresentante;

    @NotBlank(message = "El teléfono del representante es obligatorio")
    private String telefonoRepresentante;

    @NotBlank(message = "El nombre de usuario es obligatorio")
    @Size(min = 3, max = 50, message = "El nombre de usuario debe tener al menos 3 caracteres")
    private String nombreUsuario;

    @NotBlank(message = "La contraseña es obligatoria")
    @Size(min = 6, message = "La contraseña debe tener al menos 6 caracteres")
    private String contrasena;

    private String descripcion;

    public RegistroAgenciaRequest() {}

    public String getRuc() { return ruc; }
    public void setRuc(String ruc) { this.ruc = ruc; }

    public String getRazonSocial() { return razonSocial; }
    public void setRazonSocial(String razonSocial) { this.razonSocial = razonSocial; }

    public String getNombreComercial() { return nombreComercial; }
    public void setNombreComercial(String nombreComercial) { this.nombreComercial = nombreComercial; }

    public String getDireccion() { return direccion; }
    public void setDireccion(String direccion) { this.direccion = direccion; }

    public String getTelefonoEmpresa() { return telefonoEmpresa; }
    public void setTelefonoEmpresa(String telefonoEmpresa) { this.telefonoEmpresa = telefonoEmpresa; }

    public String getCorreoEmpresa() { return correoEmpresa; }
    public void setCorreoEmpresa(String correoEmpresa) { this.correoEmpresa = correoEmpresa; }

    public String getRepresentanteLegal() { return representanteLegal; }
    public void setRepresentanteLegal(String representanteLegal) { this.representanteLegal = representanteLegal; }

    public String getDniRepresentante() { return dniRepresentante; }
    public void setDniRepresentante(String dniRepresentante) { this.dniRepresentante = dniRepresentante; }

    public String getTelefonoRepresentante() { return telefonoRepresentante; }
    public void setTelefonoRepresentante(String telefonoRepresentante) { this.telefonoRepresentante = telefonoRepresentante; }

    public String getNombreUsuario() { return nombreUsuario; }
    public void setNombreUsuario(String nombreUsuario) { this.nombreUsuario = nombreUsuario; }

    public String getContrasena() { return contrasena; }
    public void setContrasena(String contrasena) { this.contrasena = contrasena; }

    public String getDescripcion() { return descripcion; }
    public void setDescripcion(String descripcion) { this.descripcion = descripcion; }
}
