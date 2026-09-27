package com.travelink.controlador;

import com.travelink.persistencia.AdminPersistencia;
import java.util.*;

public class AdminControlador {
    private final AdminPersistencia adminPersistencia;

    public AdminControlador() {
        this.adminPersistencia = new AdminPersistencia();
    }

    public Map<String, Object> getDashboard() {
        Map<String, Object> res = new HashMap<>();
        res.put("status", "success");
        res.put("data", adminPersistencia.getDashboardData());
        return res;
    }

    public Map<String, Object> getUsuarios(String tipo, String estado, String busqueda) {
        Map<String, Object> res = new HashMap<>();
        res.put("status", "success");
        res.put("data", adminPersistencia.getUsuarios(tipo, estado, busqueda));
        return res;
    }

    public Map<String, Object> guardarUsuario(Map<String, Object> params) {
        Map<String, Object> res = new HashMap<>();
        boolean ok = adminPersistencia.guardarUsuario(params);
        res.put("status", ok ? "success" : "error");
        res.put("message", ok ? "Usuario registrado exitosamente." : "Error al guardar usuario.");
        return res;
    }

    public Map<String, Object> eliminarUsuario(int idUsuario) {
        Map<String, Object> res = new HashMap<>();
        boolean ok = adminPersistencia.eliminarUsuario(idUsuario);
        res.put("status", ok ? "success" : "error");
        res.put("message", ok ? "Usuario eliminado exitosamente." : "Error al eliminar usuario.");
        return res;
    }

    public Map<String, Object> getTours(String estado, String agencia, String busqueda) {
        Map<String, Object> res = new HashMap<>();
        res.put("status", "success");
        res.put("data", adminPersistencia.getTours(estado, agencia, busqueda));
        return res;
    }

    public Map<String, Object> guardarTour(Map<String, Object> params) {
        Map<String, Object> res = new HashMap<>();
        boolean ok = adminPersistencia.guardarTour(params);
        res.put("status", ok ? "success" : "error");
        res.put("message", ok ? "Tour registrado exitosamente." : "Error al guardar tour.");
        return res;
    }

    public Map<String, Object> eliminarTour(int idTour) {
        Map<String, Object> res = new HashMap<>();
        boolean ok = adminPersistencia.eliminarTour(idTour);
        res.put("status", ok ? "success" : "error");
        res.put("message", ok ? "Tour eliminado exitosamente." : "Error al eliminar tour.");
        return res;
    }

    public Map<String, Object> getAgenciasFull(String estado, String destino, String busqueda) {
        Map<String, Object> res = new HashMap<>();
        res.put("status", "success");
        res.put("data", adminPersistencia.getAgenciasFull(estado, destino, busqueda));
        return res;
    }

    public Map<String, Object> guardarAgencia(Map<String, Object> params) {
        Map<String, Object> res = new HashMap<>();
        boolean ok = adminPersistencia.guardarAgencia(params);
        res.put("status", ok ? "success" : "error");
        res.put("message", ok ? "Agencia registrada exitosamente." : "Error al guardar agencia.");
        return res;
    }

    public Map<String, Object> eliminarAgencia(int idAgencia) {
        Map<String, Object> res = new HashMap<>();
        boolean ok = adminPersistencia.eliminarAgencia(idAgencia);
        res.put("status", ok ? "success" : "error");
        res.put("message", ok ? "Agencia eliminada exitosamente." : "Error al eliminar agencia.");
        return res;
    }

    public Map<String, Object> getReservas(String estado, String busqueda) {
        Map<String, Object> res = new HashMap<>();
        res.put("status", "success");
        res.put("data", adminPersistencia.getReservas(estado, busqueda));
        return res;
    }

    public Map<String, Object> getCalidad() {
        Map<String, Object> res = new HashMap<>();
        res.put("status", "success");
        res.put("data", adminPersistencia.getCalidadData());
        return res;
    }

    public Map<String, Object> getComisiones() {
        Map<String, Object> res = new HashMap<>();
        res.put("status", "success");
        res.put("data", adminPersistencia.getComisionesData());
        return res;
    }

    public Map<String, Object> liquidarComision(int idComision) {
        Map<String, Object> res = new HashMap<>();
        boolean ok = adminPersistencia.marcarComisionLiquidada(idComision);
        res.put("status", ok ? "success" : "error");
        res.put("message", ok ? "Comisión liquidada exitosamente." : "Error al liquidar comisión.");
        return res;
    }

    public Map<String, Object> ejecutarJobComisiones(String inicio, String fin) {
        Map<String, Object> res = new HashMap<>();
        int filas = adminPersistencia.ejecutarJobComisiones(inicio, fin);
        res.put("status", "success");
        res.put("filasInsertadas", filas);
        res.put("message", "Job de comisiones ejecutado exitosamente. Filas insertadas: " + filas);
        return res;
    }

    public Map<String, Object> getDestinos() {
        Map<String, Object> res = new HashMap<>();
        res.put("status", "success");
        res.put("data", adminPersistencia.getDestinos());
        return res;
    }

    public Map<String, Object> getToursPorDestino(String ubicacion) {
        Map<String, Object> res = new HashMap<>();
        res.put("status", "success");
        res.put("data", adminPersistencia.getToursPorDestino(ubicacion));
        return res;
    }

    public Map<String, Object> getNotificaciones(Integer idUsuario, int limite) {
        Map<String, Object> res = new HashMap<>();
        res.put("status", "success");
        res.put("data", adminPersistencia.getNotificacionesData(idUsuario, limite));
        return res;
    }

    public Map<String, Object> marcarTodasNotificacionesLeidas(Integer idUsuario) {
        Map<String, Object> res = new HashMap<>();
        boolean ok = adminPersistencia.marcarTodasNotificacionesLeidas(idUsuario);
        res.put("status", ok ? "success" : "error");
        res.put("message", ok ? "Notificaciones marcadas como leídas." : "Error al actualizar notificaciones.");
        return res;
    }

    public Map<String, Object> marcarNotificacionLeida(int idNotificacion) {
        Map<String, Object> res = new HashMap<>();
        boolean ok = adminPersistencia.marcarNotificacionLeida(idNotificacion);
        res.put("status", ok ? "success" : "error");
        res.put("message", ok ? "Notificación marcada como leída." : "Error al actualizar notificación.");
        return res;
    }
}
