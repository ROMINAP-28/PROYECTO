package com.travelink.servicio;

import com.travelink.entidades.Reserva;

import java.util.List;
import java.util.Map;
import java.util.Optional;

/**
 * Interface del Servicio de Reservas.
 * Aplica principios SOLID:
 * - SRP: Responsabilidad exclusiva sobre reservas, pagos asociados y cupos.
 * - ISP: Define únicamente operaciones relacionadas al ciclo de vida de la reserva.
 * - DIP: Desacopla controladores de implementaciones concretas JPA y JDBC.
 */
public interface ReservaServicio {

    List<Reserva> listarTodas();

    Optional<Reserva> buscarPorId(Integer id);

    Optional<Reserva> buscarPorCodigo(String codigo);

    List<Reserva> listarPorUsuario(Integer idUsuario);

    List<Reserva> listarPorAgencia(Integer idAgencia);

    Reserva guardar(Reserva reserva);

    int descontarCupoAtomico(Integer idTourFecha, int cuposRequeridos);

    int devolverCupoAtomico(Integer idTourFecha, int cuposADevolver);

    boolean cancelar(Integer idReserva, String motivo);

    Map<String, Object> guardarReservaCompleta(Map<String, Object> reqData);

    Map<String, Object> obtenerReservasPorUsuario(String correo, int idUsuario);

    List<Map<String, Object>> obtenerReservasPorAgencia(int idAgencia);

    boolean perteneceAUsuario(int idReserva, int idUsuario);
}
