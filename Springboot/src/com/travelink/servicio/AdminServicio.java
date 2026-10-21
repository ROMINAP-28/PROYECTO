package com.travelink.servicio;

import java.util.List;
import java.util.Map;

/**
 * Interface del Servicio Administrativo.
 * Aplica principios SOLID:
 * - ISP: Interfaz específica para operaciones administrativas.
 * - DIP: Abstracción inyectable en controladores sin acoplamiento a JDBC/JPA.
 */
public interface AdminServicio {

    Map<String, Object> obtenerDashboardData();

    List<Map<String, Object>> listarAgencias(String filtroEstado, String filtroDestino, String busqueda);

    boolean actualizarEstadoAgencia(Map<String, Object> params);

    boolean eliminarAgencia(int idAgencia);

    List<Map<String, Object>> listarReservas(String filtroEstado, String busqueda);

    Map<String, Object> obtenerDatosCalidad();

    boolean eliminarResena(int idCalificacion, String motivo);

    Map<String, Object> listarComisiones();

    boolean liquidarComision(int idComision);

    int ejecutarJobComisiones(String inicioPeriodo, String finPeriodo);

    Map<String, Object> obtenerCatalogoDestinos();

    Map<String, Object> obtenerToursPorDestino(String ubicacion);

    List<Map<String, Object>> listarTours(String filtroEstado, String filtroAgencia, String busqueda);

    List<Map<String, Object>> listarUsuarios(String filtroTipo, String filtroEstado, String busqueda);

    boolean guardarUsuarioAdmin(Map<String, Object> params);

    boolean eliminarUsuarioAdmin(int idUsuario);

    Map<String, Object> obtenerNotificaciones(Integer idUsuario, int limite);

    boolean marcarNotificacionesLeidas(Integer idUsuario);
}
