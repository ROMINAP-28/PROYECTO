package com.travelink.servicio;

import com.travelink.entidades.Agencia;
import com.travelink.entidades.SolicitudAgencia;

import java.util.List;
import java.util.Map;
import java.util.Optional;

/**
 * Interface del Servicio de Agencias.
 * Aplica principios SOLID:
 * - SRP: Gestión del ciclo de vida de agencias, registro, dashboard y pagos.
 * - ISP: Métodos orientados a la operativa de agencias de turismo.
 * - DIP: Abstracción inyectable en controladores.
 */
public interface AgenciaServicio {

    List<Agencia> listarTodas();

    List<Agencia> listarPorEstado(String estado);

    Optional<Agencia> buscarPorId(Integer id);

    Optional<Agencia> buscarPorRuc(String ruc);

    Optional<Agencia> buscarPorIdUsuario(Integer idUsuario);

    List<SolicitudAgencia> listarSolicitudesPorUsuario(Integer idUsuario);

    Agencia guardar(Agencia agencia);

    Map<String, Object> registrarAgencia(Map<String, Object> params);

    Map<String, Object> obtenerDashboardAgencia(int idAgencia);

    List<Map<String, Object>> obtenerPagosAgencia(int idAgencia);
}
