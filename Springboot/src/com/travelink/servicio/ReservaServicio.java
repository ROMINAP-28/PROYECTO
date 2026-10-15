package com.travelink.servicio;

import com.travelink.entidades.DetalleReserva;
import com.travelink.entidades.Reserva;
import com.travelink.repositorio.ReservaJpaRepositorio;
import com.travelink.repositorio.TourFechaRepositorio;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

@Service
public class ReservaServicio {

    private final ReservaJpaRepositorio reservaJpaRepositorio;
    private final TourFechaRepositorio tourFechaRepositorio;

    public ReservaServicio(ReservaJpaRepositorio reservaJpaRepositorio, TourFechaRepositorio tourFechaRepositorio) {
        this.reservaJpaRepositorio = reservaJpaRepositorio;
        this.tourFechaRepositorio = tourFechaRepositorio;
    }

    public List<Reserva> listarTodas() {
        return reservaJpaRepositorio.findAll();
    }

    public Optional<Reserva> buscarPorId(Integer id) {
        return reservaJpaRepositorio.findById(id);
    }

    public Optional<Reserva> buscarPorCodigo(String codigo) {
        return reservaJpaRepositorio.findByCodigoReserva(codigo);
    }

    public List<Reserva> listarPorUsuario(Integer idUsuario) {
        return reservaJpaRepositorio.findByIdUsuarioOrderByFechaRegistroDesc(idUsuario);
    }

    public List<Reserva> listarPorAgencia(Integer idAgencia) {
        return reservaJpaRepositorio.findByIdAgenciaOrderByFechaRegistroDesc(idAgencia);
    }

    @Transactional
    public Reserva guardar(Reserva reserva) {
        return reservaJpaRepositorio.save(reserva);
    }

    @Transactional
    public int descontarCupoAtomico(Integer idTourFecha, int cuposRequeridos) {
        return tourFechaRepositorio.descontarCupo(idTourFecha, cuposRequeridos);
    }

    @Transactional
    public int devolverCupoAtomico(Integer idTourFecha, int cuposADevolver) {
        return tourFechaRepositorio.devolverCupo(idTourFecha, cuposADevolver);
    }

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
            return true;
        }
        return false;
    }
}
