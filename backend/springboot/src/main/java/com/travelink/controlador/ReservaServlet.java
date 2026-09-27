package com.travelink.controlador;

import com.travelink.repositorio.ReservaRepositorio;

import java.util.HashMap;
import java.util.Map;

public class ReservaServlet {
    private final ReservaRepositorio reservaRepositorio;

    public ReservaServlet() {
        this.reservaRepositorio = new ReservaRepositorio();
    }

    public Map<String, Object> procesarReserva(Map<String, Object> params) {
        Map<String, Object> respuesta = new HashMap<>();
        try {
            Map<String, Object> resultado = reservaRepositorio.guardarReservaCompleta(params);
            if ("success".equals(resultado.get("status"))) {
                respuesta.put("success", true);
                respuesta.put("message", "Reserva guardada exitosamente.");
                respuesta.put("data", resultado.get("data"));
            } else {
                respuesta.put("success", false);
                respuesta.put("message", resultado.getOrDefault("message", "Error al guardar la reserva en la base de datos."));
            }
        } catch (Exception e) {
            respuesta.put("success", false);
            respuesta.put("message", "Excepción al procesar reserva: " + e.getMessage());
        }
        return respuesta;
    }
}
