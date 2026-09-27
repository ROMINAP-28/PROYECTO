package com.travelink.repositorio;

import com.travelink.entidades.Reserva;
import com.travelink.persistencia.ReservaPersistencia;

import java.util.HashMap;
import java.util.Map;

public class ReservaRepositorio {
    private final ReservaPersistencia persistencia = new ReservaPersistencia();

    public boolean guardarReserva(Reserva reserva) {
        if (reserva == null) return false;
        Map<String, Object> reqData = new HashMap<>();
        reqData.put("idUsuario", reserva.getIdUsuario());
        reqData.put("codigo", reserva.getCodigoReserva());
        reqData.put("total", reserva.getTotal());
        if (reserva.getMetodoPago() != null) {
            reqData.put("metodoPago", reserva.getMetodoPago().getTipoPago());
        }
        Map<String, Object> res = persistencia.guardarReservaCompleta(reqData);
        return "success".equals(res.get("status"));
    }

    public Map<String, Object> guardarReservaCompleta(Map<String, Object> reqData) {
        return persistencia.guardarReservaCompleta(reqData);
    }

    public Map<String, Object> obtenerReservasPorUsuario(String correo, int idUsuario) {
        return persistencia.obtenerReservasPorUsuario(correo, idUsuario);
    }

    public boolean cancelarReserva(int idReserva, String motivo) {
        return persistencia.cancelarReserva(idReserva, motivo);
    }
}
