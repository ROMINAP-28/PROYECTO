package com.travelink.controlador;

import com.travelink.entidades.DetalleReserva;
import com.travelink.entidades.MetodoPago;
import com.travelink.entidades.Pasajero;
import com.travelink.entidades.Reserva;
import com.travelink.repositorio.ReservaRepositorio;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ReservaControlador {
    private final ReservaRepositorio reservaRepositorio;

    public ReservaControlador() {
        this.reservaRepositorio = new ReservaRepositorio();
    }

    public Map<String, Object> crearReserva(int idUsuario, int idTourFecha, int cantAdultos, int cantNinos, int cantBebes, double total, String tipoPago, List<Pasajero> pasajeros) {
        Map<String, Object> response = new HashMap<>();

        try {
            Reserva reserva = new Reserva();
            reserva.setIdUsuario(idUsuario);
            reserva.setTotal(total);

            DetalleReserva detalle = new DetalleReserva();
            detalle.setIdTourFecha(idTourFecha);
            detalle.setCantAdultos(cantAdultos);
            detalle.setCantNinos(cantNinos);
            detalle.setCantBebes(cantBebes);
            detalle.setSubtotal(BigDecimal.valueOf(total));
            reserva.getDetalles().add(detalle);

            if (pasajeros != null) {
                reserva.setPasajeros(pasajeros);
            }

            MetodoPago pago = new MetodoPago();
            pago.setTipoPago(tipoPago);
            pago.setMonto(BigDecimal.valueOf(total));
            reserva.setMetodoPago(pago);

            boolean guardado = reservaRepositorio.guardarReserva(reserva);

            if (guardado) {
                response.put("status", "success");
                response.put("message", "Reserva guardada exitosamente.");
            } else {
                response.put("status", "error");
                response.put("message", "Error al guardar en la base de datos.");
            }
        } catch (Exception e) {
            response.put("status", "error");
            response.put("message", "Datos inválidos enviados al servidor: " + e.getMessage());
        }

        return response;
    }

    public Map<String, Object> listarReservasPorUsuario(String correo, int idUsuario) {
        return reservaRepositorio.obtenerReservasPorUsuario(correo, idUsuario);
    }

    public boolean cancelarReserva(int idReserva, String motivo) {
        return reservaRepositorio.cancelarReserva(idReserva, motivo);
    }
}
