package com.travelink.controlador;

import com.travelink.entidades.Calificacion;
import com.travelink.persistencia.CalificacionPersistencia;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class CalificacionControlador {
    private CalificacionPersistencia calificacionPersistencia;

    public CalificacionControlador() {
        this.calificacionPersistencia = new CalificacionPersistencia();
    }

    public Map<String, Object> guardarCalificacion(Calificacion calificacion) {
        Map<String, Object> respuesta = new HashMap<>();
        if (calificacion == null || calificacion.getIdReserva() <= 0) {
            respuesta.put("status", "error");
            respuesta.put("message", "Datos de calificaciÃ³n invÃ¡lidos");
            return respuesta;
        }

        Calificacion guardada = calificacionPersistencia.guardar(calificacion);
        if (guardada != null) {
            respuesta.put("status", "success");
            respuesta.put("message", "CalificaciÃ³n registrada con Ã©xito");
            respuesta.put("calificacion", guardada);
        } else {
            respuesta.put("status", "error");
            respuesta.put("message", "Error al guardar calificaciÃ³n en base de datos");
        }
        return respuesta;
    }

    public Map<String, Object> listarPorAgencia(int idAgencia) {
        Map<String, Object> respuesta = new HashMap<>();
        List<Calificacion> lista = calificacionPersistencia.buscarPorAgencia(idAgencia);
        respuesta.put("status", "success");
        respuesta.put("calificaciones", lista);
        return respuesta;
    }
}

