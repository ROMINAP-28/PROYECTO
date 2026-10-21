package com.travelink.controlador.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.travelink.entidades.Destino;

@JsonInclude(JsonInclude.Include.NON_NULL)
public class DestinoDto {

    private Integer idDestino;
    private String nombre;
    private String descripcion;
    private String estado;
    private String imagen;
    private Integer toursActivos;

    public DestinoDto() {}

    public static DestinoDto desdeEntidad(Destino d) {
        if (d == null) return null;
        DestinoDto dto = new DestinoDto();
        dto.setIdDestino(d.getIdDestino());
        dto.setNombre(d.getNombre());
        dto.setEstado(d.getEstado());
        return dto;
    }

    public Integer getIdDestino() { return idDestino; }
    public void setIdDestino(Integer idDestino) { this.idDestino = idDestino; }

    public String getNombre() { return nombre; }
    public void setNombre(String nombre) { this.nombre = nombre; }

    public String getDescripcion() { return descripcion; }
    public void setDescripcion(String descripcion) { this.descripcion = descripcion; }

    public String getEstado() { return estado; }
    public void setEstado(String estado) { this.estado = estado; }

    public String getImagen() { return imagen; }
    public void setImagen(String imagen) { this.imagen = imagen; }

    public Integer getToursActivos() { return toursActivos; }
    public void setToursActivos(Integer toursActivos) { this.toursActivos = toursActivos; }
}
