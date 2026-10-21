package com.travelink.servicio.impl;

import com.travelink.entidades.DetalleReserva;
import com.travelink.entidades.Reserva;
import com.travelink.persistencia.ReservaPersistencia;
import com.travelink.repositorio.ReservaJpaRepositorio;
import com.travelink.repositorio.TourFechaRepositorio;
import com.travelink.servicio.ReservaServicio;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;
import java.util.Optional;

/**
 * Implementación del Servicio de Reservas.
 * Aplica principios SOLID:
 * - SRP: Encapsula la lógica de negocio de reservas y cupos.
 * - DIP: Implementa la interface ReservaServicio e inyecta dependencias por constructor.
 */
@Service
public class ReservaServicioImpl implements ReservaServicio {

    private static final Logger log = LoggerFactory.getLogger(ReservaServicioImpl.class);

    private final ReservaJpaRepositorio reservaJpaRepositorio;
    private final TourFechaRepositorio tourFechaRepositorio;
    private final ReservaPersistencia reservaPersistencia;

    public ReservaServicioImpl(ReservaJpaRepositorio reservaJpaRepositorio,
                               TourFechaRepositorio tourFechaRepositorio) {
        this.reservaJpaRepositorio = reservaJpaRepositorio;
        this.tourFechaRepositorio = tourFechaRepositorio;
        this.reservaPersistencia = new ReservaPersistencia();
    }

    @Override
    public List<Reserva> listarTodas() {
        return reservaJpaRepositorio.findAll();
    }

    @Override
    public Optional<Reserva> buscarPorId(Integer id) {
        return reservaJpaRepositorio.findById(id);
    }

    @Override
    public Optional<Reserva> buscarPorCodigo(String codigo) {
        return reservaJpaRepositorio.findByCodigoReserva(codigo);
    }

    @Override
    public List<Reserva> listarPorUsuario(Integer idUsuario) {
        return reservaJpaRepositorio.findByIdUsuarioOrderByFechaRegistroDesc(idUsuario);
    }

    @Override
    public List<Reserva> listarPorAgencia(Integer idAgencia) {
        return reservaJpaRepositorio.findByIdAgenciaOrderByFechaRegistroDesc(idAgencia);
    }

    @Override
    @Transactional
    public Reserva guardar(Reserva reserva) {
        return reservaJpaRepositorio.save(reserva);
    }

    @Override
    @Transactional
    public int descontarCupoAtomico(Integer idTourFecha, int cuposRequeridos) {
        return tourFechaRepositorio.descontarCupo(idTourFecha, cuposRequeridos);
    }

    @Override
    @Transactional
    public int devolverCupoAtomico(Integer idTourFecha, int cuposADevolver) {
        return tourFechaRepositorio.devolverCupo(idTourFecha, cuposADevolver);
    }

    @Override
    @Transactional
    public boolean cancelar(Integer idReserva, String motivo) {
        Optional<Reserva> r = reservaJpaRepositorio.findById(idReserva);
        if (r.isPresent()) {
            Reserva res = r.get();
            if ("Cancelada".equalsIgnoreCase(res.getEstado())) {
                return true;
            }
            res.setEstado("Cancelada");
            res.setMotivoCancelacion(motivo);
            reservaJpaRepositorio.save(res);

            // Devolver cupo atómicamente
            if (res.getIdTourFecha() != null) {
                int adultos = 1;
                int ninos = 0;
                int bebes = 0;
                boolean aceptaBebes = false;
                BigDecimal precioBebe = BigDecimal.ZERO;

                if (res.getTour() != null) {
                    aceptaBebes = Boolean.TRUE.equals(res.getTour().getAceptaBebes());
                    precioBebe = res.getTour().getPrecioBebe() != null ? res.getTour().getPrecioBebe() : BigDecimal.ZERO;
                }
                if (res.getDetalles() != null && !res.getDetalles().isEmpty()) {
                    for (DetalleReserva d : res.getDetalles()) {
                        if (d.getCantAdultos() != null && d.getCantAdultos() > 0) adultos = d.getCantAdultos();
                        if (d.getCantNinos() != null) ninos = d.getCantNinos();
                        if (d.getCantBebes() != null) bebes = d.getCantBebes();
                    }
                }
                boolean bebeOcupa = aceptaBebes && precioBebe.compareTo(BigDecimal.ZERO) > 0;
                int cuposADevolver = adultos + ninos + (bebeOcupa ? bebes : 0);
                tourFechaRepositorio.devolverCupo(res.getIdTourFecha(), cuposADevolver);
            }
            log.info("Reserva #{} cancelada exitosamente con motivo: {}", idReserva, motivo);
            return true;
        }
        return false;
    }

    @Override
    public Map<String, Object> guardarReservaCompleta(Map<String, Object> reqData) {
        return reservaPersistencia.guardarReservaCompleta(reqData);
    }

    @Override
    public Map<String, Object> obtenerReservasPorUsuario(String correo, int idUsuario) {
        return reservaPersistencia.obtenerReservasPorUsuario(correo, idUsuario);
    }

    @Override
    public List<Map<String, Object>> obtenerReservasPorAgencia(int idAgencia) {
        return reservaPersistencia.obtenerReservasPorAgencia(idAgencia);
    }

    @Override
    public boolean perteneceAUsuario(int idReserva, int idUsuario) {
        return reservaPersistencia.perteneceAUsuario(idReserva, idUsuario);
    }
}
