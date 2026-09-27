package com.travelink.repositorio;

import com.travelink.entidades.MetodoPago;
import java.util.Optional;

public interface MetodoPagoRepositorio {
    MetodoPago guardar(MetodoPago pago);
    Optional<MetodoPago> buscarPorId(int idPago);
    Optional<MetodoPago> buscarPorReserva(int idReserva);
    boolean actualizarEstado(int idPago, String estado);
}
