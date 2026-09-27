package com.travelink.repositorio;

import com.travelink.entidades.Pasajero;
import java.util.List;
import java.util.Optional;

public interface PasajeroRepositorio {
    Pasajero guardar(Pasajero pasajero);
    List<Pasajero> guardarTodos(List<Pasajero> pasajeros);
    Optional<Pasajero> buscarPorId(int idPasajero);
    List<Pasajero> buscarPorReserva(int idReserva);
    boolean eliminarPorReserva(int idReserva);
}
