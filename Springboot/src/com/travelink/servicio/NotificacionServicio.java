package com.travelink.servicio;

import com.travelink.entidades.Notificacion;

import java.util.List;

/**
 * Interface del Servicio de Notificaciones.
 * Aplica principios SOLID:
 * - SRP: Emisión, registro y lectura de notificaciones para los roles del sistema.
 * - DIP: Abstracción inyectable en controladores y servicios.
 */
public interface NotificacionServicio {

    List<Notificacion> listarPorUsuario(Integer idUsuario);

    List<Notificacion> listarPorRol(String rolTarget);

    Notificacion crear(Notificacion notificacion);

    boolean registrarNotificacion(Integer idUsuario, String titulo, String mensaje, String tipo, String icono, String color, String enlace);

    boolean marcarLeido(Integer id);
}
